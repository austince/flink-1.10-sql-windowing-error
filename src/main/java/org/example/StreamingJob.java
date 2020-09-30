/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableConfig;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.FileSystem;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.example.records.DescriptionRow;
import org.example.records.FieldDescriptor;
import org.example.records.NameRow;
import org.example.records.Result;

import java.nio.file.Paths;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingJob {

  static void registerCsvTempTable(StreamTableEnvironment tableEnv, String path, Schema schema, String tableName) {
    tableEnv.connect(
        new FileSystem()
            .path(path)
    )
        .withFormat(
            // the new format does not support ignoring the first line
            // so any table registered with this way must either not have headers or ignore the first row
            new Csv()
                .quoteCharacter('"')
                .ignoreParseErrors()
        )
        .withSchema(schema)
        .inAppendMode()
        .createTemporaryTable(tableName);
  }

  public static void main(String[] args) throws Exception {
    ParameterTool params = ParameterTool.fromArgs(args);

    String basePath = params.get("base-path", Paths.get(System.getProperty("user.dir"), "data").toString());

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    final EnvironmentSettings tableEnvSettings = EnvironmentSettings.newInstance()
        .useBlinkPlanner()
        .build();

    final StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, tableEnvSettings);

    TableConfig tConfig = tEnv.getConfig();
    tConfig.setIdleStateRetentionTime(Time.minutes(15), Time.hours(24));

    registerCsvTempTable(
        tEnv,
        Paths.get(basePath, NameRow.FILENAME).toString(),
        FieldDescriptor.toSchema(NameRow.FIELDS),
        "Names"
    );

    registerCsvTempTable(
        tEnv,
        Paths.get(basePath, DescriptionRow.FILENAME).toString(),
        FieldDescriptor.toSchema(DescriptionRow.FIELDS),
        "Descriptions"
    );

    Table resultTable = tEnv.sqlQuery(
        "SELECT " +
            "Names.uuid, " +
            "Names.name, " +
            "Descriptions.description " +
            "FROM Names " +
            "LEFT OUTER JOIN Descriptions ON Names.uuid = Descriptions.uuid"
    );

    DataStream<Result> resStream = tEnv
        .toRetractStream(
            resultTable,
            Row.class
        )
        .flatMap((FlatMapFunction<Tuple2<Boolean, Row>, Result>) (tup, out) -> {
          if (!tup.f0) { // only gather appends
            return;
          }
          Result result = new Result();
          Row row = tup.f1;
          result.uuid = (String) row.getField(0);
          result.name = (String) row.getField(1);
          result.description = (String) row.getField(2);
          // remove header rows
          if ("uuid".equals(result.uuid)) {
            return;
          }
          out.collect(result);
        })
        .returns(Result.class)
        .name("Parse Results")
        .uid("parse-results");

    DataStream<Result> dedupedResStream = resStream
        .keyBy(r -> r.uuid)
        .timeWindow(org.apache.flink.streaming.api.windowing.time.Time.seconds(30))
        .trigger(TimedCountTrigger.of(2)) // should only be max 2, since we're checking for a duplicate emission
        .process(new ProcessWindowFunction<Result, Result, String, TimeWindow>() {
          @Override
          public void process(String uuid, Context context, Iterable<Result> results, Collector<Result> out) throws Exception {
            Result res = null;
            int count = 0;
            for (Result r : results) {
              // always use the latest element
              res = r;
              count++;
            }

            System.out.println(String.format("De-duped window for uuid %s w/ count %d", uuid, count));
            out.collect(res);
          }
        });

    resStream.print("results");

    dedupedResStream.print("deduped-results");

    env.execute("Flink SQL w/ JOIN");
  }
}
