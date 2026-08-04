package me.xv.holymoderation.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Generated;
import me.xv.holymoderation.core.ServiceRegistry;

public class ConfigManager {
   private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
   private ModState config = new ModState();

   public ConfigManager() {
      this.initDefaults();
      this.load();
   }

   private void initDefaults() {
      try {
         Files.createDirectories(PathsConfig.root());
      } catch (Exception exception) {
         ServiceRegistry.getLoggerService().log("Исключение в ConfigManager/ensureConfigDirectory: " + exception);
      }
   }

   public void load() {
      this.initDefaults();
      Path configPath = PathsConfig.configFile();

      if (Files.exists(configPath)) {
         try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configPath), StandardCharsets.UTF_8)) {
            ModState loaded = this.gson.fromJson(reader, ModState.class);
            if (loaded != null) {
               this.config = loaded;
            }
            this.save(this.config);
         } catch (Exception exception) {
            ServiceRegistry.getLoggerService().log("Исключение в ConfigManager/loadConfig: " + exception);
            this.save(this.config);
         }
      } else {
         this.save(this.config);
      }
   }

   public void save(ModState state) {
      this.config = state;
      this.initDefaults();

      try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(PathsConfig.configFile()), StandardCharsets.UTF_8)) {
         this.gson.toJson(this.config, writer);
      } catch (Exception exception) {
         ServiceRegistry.getLoggerService().log("Исключение в ConfigManager/saveCfg: " + exception);
      }
   }

   @Generated
   public ModState getState() {
      return this.config;
   }
}
