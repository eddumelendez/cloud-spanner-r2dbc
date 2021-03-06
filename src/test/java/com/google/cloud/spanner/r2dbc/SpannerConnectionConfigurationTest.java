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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link SpannerConnectionConfiguration}.
 */
public class SpannerConnectionConfigurationTest {

  @Test
  public void missingInstanceNameTriggersException() {
    assertThatThrownBy(
        () -> {
          new SpannerConnectionConfiguration.Builder()
              .setProjectId("project1")
              .setDatabaseName("db")
              .build();
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("instanceName must not be null");
  }

  @Test
  public void missingDatabaseNameTriggersException() {
    assertThatThrownBy(
        () -> {
          new SpannerConnectionConfiguration.Builder()
              .setProjectId("project1")
              .setInstanceName("an-instance")
              .build();
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("databaseName must not be null");
  }

  @Test
  public void missingProjectIdTriggersException() {
    assertThatThrownBy(
        () -> {
          new SpannerConnectionConfiguration.Builder()
              .setInstanceName("an-instance")
              .setDatabaseName("db")
              .build();
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("projectId must not be null");
  }

  @Test
  public void passingCustomGoogleCredentials() throws IOException {
    GoogleCredentials fakeCredentials = Mockito.mock(GoogleCredentials.class);

    SpannerConnectionConfiguration configuration =
        new SpannerConnectionConfiguration.Builder()
            .setProjectId("project")
            .setInstanceName("an-instance")
            .setDatabaseName("db")
            .setCredentials(fakeCredentials)
            .build();

    assertThat(configuration.getCredentials()).isSameAs(fakeCredentials);
  }

  @Test
  public void nonNullConstructorParametersPassPreconditions() throws IOException {
    SpannerConnectionConfiguration config
        = new SpannerConnectionConfiguration.Builder()
        .setProjectId("project1")
        .setInstanceName("an-instance")
        .setDatabaseName("db")
        .build();
    assertThat(config.getFullyQualifiedDatabaseName())
        .isEqualTo("projects/project1/instances/an-instance/databases/db");
  }

}
