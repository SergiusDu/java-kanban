package com.tasktracker.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDayTimeAdapter extends TypeAdapter<LocalDateTime> {
  private static final DateTimeFormatter dft = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @Override
  public void write(JsonWriter out, LocalDateTime value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.value(dft.format(value));
  }

  @Override
  public LocalDateTime read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    return LocalDateTime.parse(in.nextString(), dft);
  }
}
