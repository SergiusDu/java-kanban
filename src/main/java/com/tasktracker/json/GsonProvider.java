package com.tasktracker.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tasktracker.json.adapters.DurationAdapter;
import com.tasktracker.json.adapters.LocalDayTimeAdapter;
import java.time.Duration;
import java.time.LocalDateTime;

public class GsonProvider {
  private static final Gson GSON_INSTANCE =
      new GsonBuilder()
          .setPrettyPrinting()
          .registerTypeAdapter(LocalDateTime.class, new LocalDayTimeAdapter())
          .registerTypeAdapter(Duration.class, new DurationAdapter())
          .create();

  private GsonProvider() {}

  public static Gson getGson() {
    return GSON_INSTANCE;
  }
}
