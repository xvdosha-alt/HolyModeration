package me.xv.holymoderation.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.xv.holymoderation.config.ModState;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.core.ServiceRegistry;

public class KeyBindingService extends BaseService {
   private final Set<Integer> pressedKeys = new HashSet<>();
   private final Map<String, Boolean> keyStates = new HashMap<>();

   public void registerBinding(int key, int action) {
      this.pressedKeys.add(key);
      if (action == 0) {
         this.pressedKeys.remove(key);
      }
      this.reloadBindings();
   }

   private void reloadBindings() {
      ModState modState = ServiceRegistry.getConfigManager().getState();
      for (Map.Entry<String, ModState.KeyBindEntry> entry : modState.getKeyBinds().entrySet()) {
         String bindingId = entry.getKey();
         ModState.KeyBindEntry bind = entry.getValue();
         boolean keyPressed = this.pressedKeys.contains(bind.getKeyCode());
         boolean modifiersOk = bind.getMutedPlayers() == null
            || this.pressedKeys.containsAll(bind.getMutedPlayers());
         boolean active = keyPressed && modifiersOk;

         if (bind.getDefaultKeyModifier() == ModState.KeyModifier.SINGLE_PRESS) {
            if (active && !this.keyStates.getOrDefault(bindingId, false)) {
               this.keyStates.put(bindingId, true);
            }
         } else {
            this.keyStates.put(bindingId, active);
         }
      }
   }

   public boolean isKeyDown(String bindingId) {
      return this.keyStates.getOrDefault(bindingId, false);
   }

   public boolean matchesBinding(String bindingId) {
      boolean matched = this.keyStates.getOrDefault(bindingId, false);
      if (matched) {
         this.keyStates.put(bindingId, false);
         return true;
      }
      return false;
   }

   static {
   }
}
