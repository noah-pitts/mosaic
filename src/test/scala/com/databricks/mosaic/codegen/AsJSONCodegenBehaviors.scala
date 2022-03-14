package com.databricks.mosaic.codegen

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{be, noException}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.catalyst.expressions.codegen.CodeGenerator
import org.apache.spark.sql.execution.WholeStageCodegenExec
import org.apache.spark.sql.functions.col

import com.databricks.mosaic.functions.MosaicContext
import com.databricks.mosaic.test.mocks.getGeoJSONDf

trait AsJSONCodegenBehaviors { this: AnyFlatSpec =>

    def codeGeneration(mosaicContext: => MosaicContext): Unit = {
        val mc = mosaicContext
        import mc.functions._

        val geoJsonDf: DataFrame = getGeoJSONDf.select(as_json(col("geojson")).getItem("json").alias("geojson"))
        val queryExecution = geoJsonDf.queryExecution
        val plan = queryExecution.executedPlan

        plan.find(_.isInstanceOf[WholeStageCodegenExec]).isDefined shouldBe true

        val codeGenStage = plan.find(_.isInstanceOf[WholeStageCodegenExec]).get.asInstanceOf[WholeStageCodegenExec]
        val (_, code) = codeGenStage.doCodeGen()
        noException should be thrownBy CodeGenerator.compile(code)

        geoJsonDf.count() should be > 0L
    }
}