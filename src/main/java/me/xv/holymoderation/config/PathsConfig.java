package me.xv.holymoderation.config;

import java.nio.file.Path;
import me.xv.holymoderation.core.ModBuild;
import net.fabricmc.loader.api.FabricLoader;

public final class PathsConfig {
   private static final Path ROOT = FabricLoader.getInstance().getConfigDir().resolve(
      ModBuild.BARE ? "hmclient" : "holymoderation"
   );

   private PathsConfig() {
   }

   public static Path root() {
      return ROOT;
   }

   public static Path configFile() {
      return ROOT.resolve("config.json");
   }

   public static Path tempDir() {
      return ROOT.resolve("temp");
   }

   public static Path checkTwinksFile() {
      return ROOT.resolve("checktwinks.txt");
   }

   public static Path resultsFile() {
      return ROOT.resolve("results.txt");
   }

   public static Path tempFile() {
      return tempDir().resolve("temp.txt");
   }

   public static Path debugLogFile() {
      return ROOT.resolve("hm-debug.log");
   }
}
