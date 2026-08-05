package me.xv.holymoderation.service;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import me.xv.holymoderation.core.BaseService;

public class StateService extends BaseService {
   private boolean enabled = true;
   private boolean blocked = false;
   private boolean connected = false;
   private boolean isOnHW = false;
   private boolean gameInitCompleted = false;
   private boolean inHub = false;
   private boolean vanishEnabled = false;
   private boolean flyEnabled = false;
   private boolean gm3Enabled = false;
   private boolean hacAlertsEnabled = false;
   private boolean godEnabled = false;
   private String player = "";
   private String spyPlayer = "";
   private String spyPlayerStatus = "";
   private String spyPlayerActivity = "";
   private String moderNickname = "";
   private String moderLocation = "";
   private boolean moderLocationTrusted = false;
   private String lastAnarchyLocation = "";
   private String vkUrl = "";
   private boolean checkingTwinks = false;
   private int rank = 0;
   private Map<String, Object> journalProfile = new HashMap<>();
   private Map<String, Object> journalStats = new HashMap<>();

   public void resetState() {
      this.connected = false;
      this.isOnHW = false;
      this.gameInitCompleted = false;
      this.inHub = false;
      this.vanishEnabled = false;
      this.flyEnabled = false;
      this.gm3Enabled = false;
      this.hacAlertsEnabled = false;
      this.godEnabled = false;
      this.player = "";
      this.spyPlayer = "";
      this.spyPlayerStatus = "";
      this.spyPlayerActivity = "";
      this.moderNickname = "";
      this.moderLocation = "";
      this.moderLocationTrusted = false;
      this.lastAnarchyLocation = "";
      this.vkUrl = "";
      this.rank = 0;
      this.journalProfile = new HashMap<>();
      this.journalStats = new HashMap<>();
   }

   @Generated
   public boolean getEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean getBlocked() {
      return this.blocked;
   }

   @Generated
   public boolean getConnected() {
      return this.connected;
   }

   @Generated
   public boolean isOnHW() {
      return this.isOnHW;
   }

   @Generated
   public boolean getGameInitCompleted() {
      return this.gameInitCompleted;
   }

   @Generated
   public boolean getInHub() {
      return this.inHub;
   }

   @Generated
   public boolean getVanishEnabled() {
      return this.vanishEnabled;
   }

   @Generated
   public boolean getFlyEnabled() {
      return this.flyEnabled;
   }

   @Generated
   public boolean getGm3Enabled() {
      return this.gm3Enabled;
   }

   @Generated
   public boolean getHacAlertsEnabled() {
      return this.hacAlertsEnabled;
   }

   @Generated
   public boolean getGodEnabled() {
      return this.godEnabled;
   }

   @Generated
   public String getPlayer() {
      return this.player;
   }

   @Generated
   public String getSpyPlayer() {
      return this.spyPlayer;
   }

   @Generated
   public String getSpyPlayerStatus() {
      return this.spyPlayerStatus;
   }

   @Generated
   public String getSpyPlayerActivity() {
      return this.spyPlayerActivity;
   }

   @Generated
   public String getModerNickname() {
      return this.moderNickname;
   }

   @Generated
   public String getModerLocation() {
      return this.moderLocation;
   }

   @Generated
   public boolean getModerLocationTrusted() {
      return this.moderLocationTrusted;
   }

   @Generated
   public String getLastAnarchyLocation() {
      return this.lastAnarchyLocation;
   }

   @Generated
   public String getVkUrl() {
      return this.vkUrl;
   }

   @Generated
   public boolean getCheckingTwinks() {
      return this.checkingTwinks;
   }

   @Generated
   public int getRank() {
      return this.rank;
   }

   @Generated
   public Map getJournalProfile() {
      return this.journalProfile;
   }

   @Generated
   public Map getJournalStats() {
      return this.journalStats;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public void setBlocked(boolean enabled) {
      this.blocked = enabled;
   }

   @Generated
   public void setConnected(boolean enabled) {
      this.connected = enabled;
   }

   @Generated
   public void setIsOnHW(boolean enabled) {
      this.isOnHW = enabled;
   }

   @Generated
   public void setGameInitCompleted(boolean enabled) {
      this.gameInitCompleted = enabled;
   }

   @Generated
   public void setInHub(boolean enabled) {
      this.inHub = enabled;
   }

   @Generated
   public void setVanishEnabled(boolean enabled) {
      this.vanishEnabled = enabled;
   }

   @Generated
   public void setFlyEnabled(boolean enabled) {
      this.flyEnabled = enabled;
   }

   @Generated
   public void setGm3Enabled(boolean enabled) {
      this.gm3Enabled = enabled;
   }

   @Generated
   public void setHacAlertsEnabled(boolean enabled) {
      this.hacAlertsEnabled = enabled;
   }

   @Generated
   public void setGodEnabled(boolean enabled) {
      this.godEnabled = enabled;
   }

   @Generated
   public void setPlayer(String text) {
      this.player = text;
   }

   @Generated
   public void setSpyPlayer(String text) {
      this.spyPlayer = text;
   }

   @Generated
   public void setSpyPlayerStatus(String text) {
      this.spyPlayerStatus = text;
   }

   @Generated
   public void setSpyPlayerActivity(String text) {
      this.spyPlayerActivity = text;
   }

   @Generated
   public void setModerNickname(String text) {
      this.moderNickname = text;
   }

   @Generated
   public void setModerLocation(String text) {
      this.moderLocation = text;
   }

   @Generated
   public void setModerLocationTrusted(boolean trusted) {
      this.moderLocationTrusted = trusted;
   }

   @Generated
   public void setLastAnarchyLocation(String text) {
      this.lastAnarchyLocation = text;
   }

   @Generated
   public void setVkUrl(String text) {
      this.vkUrl = text;
   }

   @Generated
   public void setCheckingTwinks(boolean enabled) {
      this.checkingTwinks = enabled;
   }

   @Generated
   public void setRank(int value) {
      this.rank = value;
   }

   @Generated
   public void setJournalProfile(Map map) {
      this.journalProfile = map;
   }

   @Generated
   public void setJournalStats(Map map) {
      this.journalStats = map;
   }

   static {
   }
}
