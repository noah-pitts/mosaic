package com.databricks.mosaic.expressions.geometry

import com.databricks.mosaic.core.geometry.api.GeometryAPI
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.catalyst.expressions.{Expression, ExpressionDescription, NullIntolerant, UnaryExpression}
import org.apache.spark.sql.types.{DataType, StringType}
import org.apache.spark.unsafe.types.UTF8String

import java.util.Locale

@ExpressionDescription(
  usage =
    "_FUNC_(expr1) - Returns the OGC Geometry class name for a given geometry.",
  examples =
    """
    Examples:
      > SELECT _FUNC_(a);
       {"MULTIPOLYGON"}
  """,
  since = "1.0"
)
case class ST_GeometryType(inputGeom: Expression, geometryAPIName: String)
  extends UnaryExpression
    with NullIntolerant
    with CodegenFallback {

  /** ST_GeometryType expression returns the OGC Geometry class name for a given
   * geometry, allowing basic type checking of geometries in more complex
   * functions.
   */

  override def child: Expression = inputGeom

  override def dataType: DataType = StringType

  override def nullSafeEval(input1: Any): Any = {
    val geometryAPI = GeometryAPI(geometryAPIName)

    val geom = geometryAPI.geometry(input1, inputGeom.dataType)
    UTF8String.fromString(geom.getGeometryType.toUpperCase(Locale.ROOT))
  }

  override def makeCopy(newArgs: Array[AnyRef]): Expression = {
    val asArray = newArgs.take(1).map(_.asInstanceOf[Expression])
    val res = ST_GeometryType(asArray(0), geometryAPIName)
    res.copyTagsFrom(this)
    res
  }

}