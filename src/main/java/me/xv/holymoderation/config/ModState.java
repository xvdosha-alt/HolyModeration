package me.xv.holymoderation.config;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Generated;

public class ModState {
   private final String currentVersion = "2.10alpha";
   @Expose
   private String apiToken = "";
   @Expose
   private boolean soundsEnabled = true;
   @Expose
   private int soundsVolume = 70;
   @Expose
   private int spyDelay = 2;
   @Expose
   private boolean copyButtonEnabled = false;
   @Expose
   private String copyButtonText = "§f§l[§a§lcopy§f§l]";
   @Expose
   private String playerMarker = "§d§l[CHECK]";
   @Expose
   private Float spyHudCenterX;
   @Expose
   private Float spyHudTopY;
   @Expose
   private Float checkoutHudCenterX;
   @Expose
   private Float checkoutHudTopY;
   @Expose
   private final List<String> textsList = new ArrayList<>();
   @Expose
   private boolean autoVanishEnabled = true;
   @Expose
   private boolean autoFlyEnabled = false;
   @Expose
   private boolean autoGm3Enabled = false;
   @Expose
   private boolean autoHacAlertsEnabled = false;
   @Expose
   private boolean autoGodEnabled = false;
   @Expose
   private boolean dupeIpEnabled = false;
   @Expose
   private boolean autoAnyDeskEnabled = true;
   @Expose
   private boolean autoTpEnabled = true;
   @Expose
   private boolean autoBanEnabled = true;
   @Expose
   private Map<String, ModState.KeyBindEntry> keyBinds = new HashMap<>();

   public ModState.KeyBindEntry getKeyBind(String id) {
      return this.keyBinds.get(id);
   }

   public void getKeyBinds(String id, ModState.KeyBindEntry entry) {
      this.keyBinds.put(id, entry);
   }

   public void getKeyBinds(String id) {
      this.keyBinds.remove(id);
   }

   public void getKeyBinds(String id, ModState.KeyModifier modifier, int keyCode, int... modifierKeys) {
      ModState.KeyBindEntry entry = this.keyBinds.get(id);
      if (entry != null) {
         entry.setDefaultKeyModifier(modifier);
         entry.setKeyCode(keyCode);
         entry.getMutedPlayers().clear();
         for (int modifierKey : modifierKeys) {
            entry.getMutedPlayers().add(modifierKey);
         }
      }
   }

   @Generated
   public String getConfigPath() {
      return this.currentVersion;
   }

   @Generated
   public String getApiToken() {
      return this.apiToken;
   }

   @Generated
   public boolean getSoundsEnabled() {
      return this.soundsEnabled;
   }

   @Generated
   public int getSoundsVolume() {
      return this.soundsVolume;
   }

   @Generated
   public int getSpyDelay() {
      return this.spyDelay;
   }

   @Generated
   public boolean getCopyButtonEnabled() {
      return this.copyButtonEnabled;
   }

   @Generated
   public String getCopyButtonText() {
      return this.copyButtonText;
   }

   @Generated
   public String getPlayerMarker() {
      return this.playerMarker;
   }

   @Generated
   public Float getSpyHudCenterX() {
      return this.spyHudCenterX;
   }

   @Generated
   public Float getSpyHudTopY() {
      return this.spyHudTopY;
   }

   @Generated
   public Float getCheckoutHudCenterX() {
      return this.checkoutHudCenterX;
   }

   @Generated
   public Float getCheckoutHudTopY() {
      return this.checkoutHudTopY;
   }

   @Generated
   public List<String> getTextsList() {
      return this.textsList;
   }

   @Generated
   public boolean getAutoVanishEnabled() {
      return this.autoVanishEnabled;
   }

   @Generated
   public boolean getAutoFlyEnabled() {
      return this.autoFlyEnabled;
   }

   @Generated
   public boolean getAutoGm3Enabled() {
      return this.autoGm3Enabled;
   }

   @Generated
   public boolean getAutoHacAlertsEnabled() {
      return this.autoHacAlertsEnabled;
   }

   @Generated
   public boolean getAutoGodEnabled() {
      return this.autoGodEnabled;
   }

   @Generated
   public boolean getDupeIpEnabled() {
      return this.dupeIpEnabled;
   }

   @Generated
   public boolean getAutoAnyDeskEnabled() {
      return this.autoAnyDeskEnabled;
   }

   @Generated
   public boolean getAutoTpEnabled() {
      return this.autoTpEnabled;
   }

   @Generated
   public boolean getAutoBanEnabled() {
      return this.autoBanEnabled;
   }

   @Generated
   public Map<String, ModState.KeyBindEntry> getKeyBinds() {
      return this.keyBinds;
   }

   @Generated
   public void setApiToken(String text) {
      this.apiToken = text;
   }

   @Generated
   public void setSoundsEnabled(boolean enabled) {
      this.soundsEnabled = enabled;
   }

   @Generated
   public void setSoundsVolume(int value) {
      this.soundsVolume = value;
   }

   @Generated
   public void setSpyDelay(int value) {
      this.spyDelay = value;
   }

   @Generated
   public void setCopyButtonEnabled(boolean enabled) {
      this.copyButtonEnabled = enabled;
   }

   @Generated
   public void setCopyButtonText(String text) {
      this.copyButtonText = text;
   }

   @Generated
   public void setPlayerMarker(String text) {
      this.playerMarker = text;
   }

   @Generated
   public void setSpyHudCenterX(Float value) {
      this.spyHudCenterX = value;
   }

   @Generated
   public void setSpyHudTopY(Float value) {
      this.spyHudTopY = value;
   }

   @Generated
   public void setCheckoutHudCenterX(Float value) {
      this.checkoutHudCenterX = value;
   }

   @Generated
   public void setCheckoutHudTopY(Float value) {
      this.checkoutHudTopY = value;
   }

   @Generated
   public void setAutoVanishEnabled(boolean enabled) {
      this.autoVanishEnabled = enabled;
   }

   @Generated
   public void setAutoFlyEnabled(boolean enabled) {
      this.autoFlyEnabled = enabled;
   }

   @Generated
   public void setAutoGm3Enabled(boolean enabled) {
      this.autoGm3Enabled = enabled;
   }

   @Generated
   public void setAutoHacAlertsEnabled(boolean enabled) {
      this.autoHacAlertsEnabled = enabled;
   }

   @Generated
   public void setAutoGodEnabled(boolean enabled) {
      this.autoGodEnabled = enabled;
   }

   @Generated
   public void setDupeIpEnabled(boolean enabled) {
      this.dupeIpEnabled = enabled;
   }

   @Generated
   public void setAutoAnyDeskEnabled(boolean enabled) {
      this.autoAnyDeskEnabled = enabled;
   }

   @Generated
   public void setAutoTpEnabled(boolean enabled) {
      this.autoTpEnabled = enabled;
   }

   @Generated
   public void setAutoBanEnabled(boolean enabled) {
      this.autoBanEnabled = enabled;
   }

   @Generated
   public void setKeyBinds(Map<String, ModState.KeyBindEntry> map) {
      this.keyBinds = map;
   }

   static {
   }

   public static class KeyBindEntry {
      @Expose
      private int mainKey;
      @Expose
      private Set<Integer> modifierKeys = new HashSet<>();
      @Expose
      private ModState.KeyModifier type;

      public KeyBindEntry(ModState.KeyModifier modifier, int mainKey, int... modifiers) {
         this.type = modifier;
         this.mainKey = mainKey;

         for (int key : modifiers) {
            this.modifierKeys.add(key);
         }
      }

      @Generated
      public int getKeyCode() {
         return this.mainKey;
      }

      @Generated
      public Set<Integer> getMutedPlayers() {
         return this.modifierKeys;
      }

      @Generated
      public ModState.KeyModifier getDefaultKeyModifier() {
         return this.type;
      }

      @Generated
      public void setKeyCode(int value) {
         this.mainKey = value;
      }

      @Generated
      public void setMutedPlayers(Set<Integer> keys) {
         this.modifierKeys = keys;
      }

      @Generated
      public void setDefaultKeyModifier(ModState.KeyModifier modifier) {
         this.type = modifier;
      }

      static {
      }
   }

   public static enum KeyModifier {
      SINGLE_PRESS,
      HOLD;
   }
}
