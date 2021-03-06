/**
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

package org.apache.sqoop.mapreduce;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import com.cloudera.sqoop.lib.LargeObjectLoader;
import com.cloudera.sqoop.lib.SqoopRecord;
import com.cloudera.sqoop.mapreduce.AutoProgressMapper;

/**
 * Imports records by transforming them to strings for a plain-text flat file.
 */
public class TextImportMapper
    extends AutoProgressMapper<LongWritable, SqoopRecord, Text, NullWritable> {

  private Text outkey;
  private LargeObjectLoader lobLoader;

  public TextImportMapper() {
    outkey = new Text();
  }

  @Override
  protected void setup(Context context)
      throws IOException, InterruptedException {
    this.lobLoader = new LargeObjectLoader(context.getConfiguration());
  }

  @Override
  public void map(LongWritable key, SqoopRecord val, Context context)
      throws IOException, InterruptedException {

    try {
      // Loading of LOBs was delayed until we have a Context.
      val.loadLargeObjects(lobLoader);
    } catch (SQLException sqlE) {
      throw new IOException(sqlE);
    }

    outkey.set(val.toString());
    context.write(outkey, NullWritable.get());
  }

  @Override
  protected void cleanup(Context context) throws IOException {
    if (null != lobLoader) {
      lobLoader.close();
    }
  }
}

