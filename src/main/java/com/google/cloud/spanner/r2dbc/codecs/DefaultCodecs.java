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

package com.google.cloud.spanner.r2dbc.codecs;

import com.google.cloud.spanner.r2dbc.util.Assert;
import com.google.protobuf.NullValue;
import com.google.protobuf.Value;
import com.google.spanner.v1.Type;
import com.google.spanner.v1.TypeCode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import reactor.util.annotation.Nullable;

/**
 * The default {@link Codecs} implementation. Delegates to type-specific codec implementations.
 */
public final class DefaultCodecs implements Codecs {

  private static final com.google.protobuf.Value NULL_VALUE =
      com.google.protobuf.Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build();

  private final List<Codec<?>> codecs;

  /**
   * Constructs the {@link DefaultCodecs} used for type conversions.
   */
  public DefaultCodecs() {
    this.codecs = Arrays.asList(
        new ArrayCodec(this, Boolean[].class),
        new ArrayCodec(this, byte[][].class),
        new ArrayCodec(this, LocalDate[].class),
        new ArrayCodec(this, Double[].class),
        new ArrayCodec(this, Long[].class),
        new ArrayCodec(this, String[].class),
        new ArrayCodec(this, Timestamp[].class),
        new SpannerCodec<>(Boolean.class, TypeCode.BOOL,
            v -> Value.newBuilder().setBoolValue(v).build()),
        new SpannerCodec<>(byte[].class, TypeCode.BYTES,
            v -> Value.newBuilder().setStringValue(new String(v)).build()),
        new SpannerCodec<>(LocalDate.class, TypeCode.DATE, v -> Value.newBuilder().setStringValue(
            DateTimeFormatter.ISO_LOCAL_DATE.format(v))
            .build()),
        new SpannerCodec<>(Double.class, TypeCode.FLOAT64, v -> {
          Value result;
          if (v.isNaN()) {
            result = Value.newBuilder().setStringValue("NaN").build();
          } else if (v == Double.NEGATIVE_INFINITY) {
            result = Value.newBuilder().setStringValue("-Infinity").build();
          } else if (v == Double.POSITIVE_INFINITY) {
            result = Value.newBuilder().setStringValue("Infinity").build();
          } else {
            result = Value.newBuilder().setNumberValue(v).build();
          }
          return result;
        }),
        new SpannerCodec<>(Long.class, TypeCode.INT64,
            v -> Value.newBuilder().setStringValue(Long.toString(v)).build()),
        new SpannerCodec<>(String.class, TypeCode.STRING,
            v -> Value.newBuilder().setStringValue(v).build()),
        new SpannerCodec<>(Timestamp.class, TypeCode.TIMESTAMP,
            v -> Value.newBuilder()
                .setStringValue(ValueUtils.TIMESTAMP_FORMATTER.format(v.toInstant())).build())
    );
  }

  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T decode(Value value, Type spannerType, Class<? extends T> type) {
    Assert.requireNonNull(value, "value must not be null");
    Assert.requireNonNull(spannerType, "spannerType must not be null");
    Assert.requireNonNull(type, "type must not be null");

    for (Codec<?> codec : this.codecs) {
      if (codec.canDecode(spannerType, type)) {
        return ((Codec<T>) codec).decode(value, spannerType, type);
      }
    }

    throw new IllegalArgumentException(
        String.format("Cannot decode value of type %s to %s", spannerType, type.getName()));
  }

  @Override
  public Value encode(Object value) {
    if (value == null) {
      return NULL_VALUE;
    }
    for (Codec<?> codec : this.codecs) {
      if (codec.canEncode(value)) {
        return codec.encode(value);
      }
    }

    throw new IllegalArgumentException(
        String.format("Cannot encode parameter of type %s", value.getClass().getName()));
  }
}
