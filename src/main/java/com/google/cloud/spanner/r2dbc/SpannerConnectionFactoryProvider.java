/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spanner.r2dbc;

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;

import com.google.cloud.spanner.r2dbc.client.GrpcClient;
import com.google.cloud.spanner.r2dbc.util.Assert;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ConnectionFactoryProvider;
import io.r2dbc.spi.Option;
import java.io.IOException;

/**
 * An implementation of {@link ConnectionFactoryProvider} for creating {@link
 * SpannerConnectionFactory}s.
 *
 * @author Mike Eltsufin
 */
public class SpannerConnectionFactoryProvider implements ConnectionFactoryProvider {

  /** R2DBC driver name for Google Cloud Spanner. */
  public static final String DRIVER_NAME = "spanner";

  /** Option name for GCP Project. */
  public static final Option<String> OPTION_PROJECT = Option.valueOf("project");

  /** Option name for GCP Spanner instance. */
  public static final Option<String> OPTION_INSTANCE = Option.valueOf("instance");

  @Override
  public ConnectionFactory create(ConnectionFactoryOptions connectionFactoryOptions) {
    SpannerConnectionConfiguration config = new SpannerConnectionConfiguration.Builder()
        .setProjectId(connectionFactoryOptions.getValue(OPTION_PROJECT))
        .setInstanceName(connectionFactoryOptions.getValue(OPTION_INSTANCE))
        .setDatabaseName(connectionFactoryOptions.getValue(DATABASE))
        .build();
    try {
      return new SpannerConnectionFactory(new GrpcClient(), config);
    } catch (IOException e) {
      // TODO: log and return null instead?
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean supports(ConnectionFactoryOptions connectionFactoryOptions) {
    Assert.requireNonNull(connectionFactoryOptions, "connectionFactoryOptions must not be null");
    String driver = connectionFactoryOptions.getValue(DRIVER);

    return DRIVER_NAME.equals(driver);
  }
}
