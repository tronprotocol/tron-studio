package org.tron.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.tron.core.config.args.Args;
import org.tron.core.db.RevokingDatabase;
import org.tron.core.db.RevokingStore;
import org.tron.core.db.api.IndexHelper;
import org.tron.core.db2.core.SnapshotManager;

@Configuration
@Import(CommonConfig.class)
@Slf4j(topic = "DefaultConfig")
public class DefaultConfig {

  @Autowired
  ApplicationContext appCtx;

  @Autowired
  CommonConfig commonConfig;

  public DefaultConfig() {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      logger.error("Uncaught exception " + t + "," + e);
      e.printStackTrace();
    });
  }

  @Bean
  public IndexHelper indexHelper() {
    if (!Args.getInstance().isSolidityNode()) {
      return null;
    }
    return new IndexHelper();
  }

  @Bean
  public RevokingDatabase revokingDatabase() {
    int dbVersion = Args.getInstance().getStorage().getDbVersion();
    if (dbVersion == 1) {
      return RevokingStore.getInstance();
    } else if (dbVersion == 2) {
      return new SnapshotManager();
    } else {
      throw new RuntimeException("db version is error.");
    }
  }

}
