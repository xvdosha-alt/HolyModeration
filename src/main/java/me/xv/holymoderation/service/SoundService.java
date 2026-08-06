package me.xv.holymoderation.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import me.xv.holymoderation.core.BaseService;
import me.xv.holymoderation.core.ModBuild;
import me.xv.holymoderation.core.ServiceRegistry;

public class SoundService extends BaseService {
   public void playSound(String fileName) {
      if (ModBuild.BARE || !ServiceRegistry.getConfigManager().getState().getSoundsEnabled()) {
         return;
      }

      try (InputStream stream = SoundService.class.getResourceAsStream("/assets/holymoderation/sounds/" + fileName)) {
         if (stream == null) {
            return;
         }

         byte[] data = stream.readAllBytes();
         Clip clip = AudioSystem.getClip();
         clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(data)));

         if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gain.getMinimum();
            float max = gain.getMaximum();
            float volume = ServiceRegistry.getConfigManager().getState().getSoundsVolume() / 100.0F;
            gain.setValue(min + volume * (max - min));
         }

         clip.start();
         clip.addLineListener(event -> onLineFinished(clip, event));
      } catch (Exception exception) {
         if (this.loggerService != null) {
            this.loggerService.log("Исключение в SoundService/playSound: " + exception);
         }
      }
   }

   private static void onLineFinished(Clip clip, LineEvent event) {
      if (event.getType() == LineEvent.Type.STOP) {
         clip.close();
      }
   }
}
