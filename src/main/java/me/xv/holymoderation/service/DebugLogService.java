package me.xv.holymoderation.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import me.xv.holymoderation.config.PathsConfig;
import me.xv.holymoderation.core.BaseService;

public class DebugLogService extends BaseService {
   private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
   private boolean enabled = true;

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (enabled) {
         this.write("system", "debug logging enabled");
      }
   }

   public void clear() {
      try {
         Files.deleteIfExists(PathsConfig.debugLogFile());
         this.write("system", "log cleared");
      } catch (IOException exception) {
         this.write("system", "clear failed: " + exception.getMessage());
      }
   }

   public String getLogPath() {
      return PathsConfig.debugLogFile().toAbsolutePath().toString();
   }

   public void write(String category, String message) {
      if (!this.enabled) {
         return;
      }

      try {
         Files.createDirectories(PathsConfig.root());
         String line = LocalDateTime.now().format(TIME_FORMAT)
            + " [" + category + "] "
            + message
            + System.lineSeparator();
         Files.writeString(
            PathsConfig.debugLogFile(),
            line,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
         );
      } catch (IOException exception) {
         if (this.loggerService != null) {
            this.loggerService.log("DebugLogService/write: " + exception);
         }
      }
   }

   static {
   }
}
