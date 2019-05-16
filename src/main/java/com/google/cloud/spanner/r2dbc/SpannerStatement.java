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

import com.google.cloud.spanner.r2dbc.client.Client;
import com.google.cloud.spanner.r2dbc.result.PartialResultFluxConverter;
import com.google.spanner.v1.PartialResultSet;
import com.google.spanner.v1.Session;
import com.google.spanner.v1.Transaction;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link Statement} implementation for Cloud Spanner.
 */
public class SpannerStatement implements Statement {

  private Client client;

  private Session session;

  private Mono<Transaction> transaction;

  private String sql;

  public SpannerStatement(Client client, Session session,  Mono<Transaction> transaction, String sql) {
    this.client = client;
    this.session = session;
    this.transaction = transaction;
    this.sql = sql;
  }

  @Override
  public Statement add() {
    return null;
  }

  @Override
  public Statement bind(Object o, Object o1) {
    return null;
  }

  @Override
  public Statement bind(int i, Object o) {
    return null;
  }

  @Override
  public Statement bindNull(Object o, Class<?> type) {
    return null;
  }

  @Override
  public Statement bindNull(int i, Class<?> type) {
    return null;
  }

  @Override
  public Publisher<? extends Result> execute() {
    Flux<PartialResultSet> result = client.executeStreamingSql(this.session, this.transaction, this.sql);


    PartialResultFluxConverter rsTracker = new PartialResultFluxConverter(result);
    //return Mono.just(new SpannerResult(Mono.fromSupplier(() -> 0)));
    return Mono.just(new SpannerResult(rsTracker.toRows()));


  }
}
