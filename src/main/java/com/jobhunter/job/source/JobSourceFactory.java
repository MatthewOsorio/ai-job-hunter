package com.jobhunter.job.source;

import com.jobhunter.exception.ConfigurationException;
import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.List;

public class JobSourceFactory {

  public static List<JobSource> fromConfig(List<? extends Config> sourceConfigs) {
    List<JobSource> sources = new ArrayList<>();
    for (Config source : sourceConfigs) {
      if (!source.getBoolean("enabled"))
        continue;

      String type = source.getString("type");
      String name = source.getString("name");
      String url = source.getString("url");

      JobSource jobSource = switch (type) {
        case "simplify" -> new SimplifyJobSource(name, url);
        default -> throw new ConfigurationException("Unknown source type: " + type);
      };
      sources.add(jobSource);
    }
    return sources;
  }
}
