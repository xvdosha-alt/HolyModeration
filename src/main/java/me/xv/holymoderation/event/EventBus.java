package me.xv.holymoderation.event;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import me.xv.holymoderation.command.BaseCommandHandler;
import me.xv.holymoderation.service.LoggerService;

public class EventBus {
   private final Map<Class<?>, List<EventBus.Subscriber>> subscribers = new ConcurrentHashMap<>();
   private LoggerService loggerService;

   public void register(Object handler) {
      if (!(handler instanceof BaseCommandHandler commandHandler)) {
         return;
      }

      for (Method method : commandHandler.getClass().getDeclaredMethods()) {
         if (!method.isAnnotationPresent(Subscribe.class)) {
            continue;
         }

         Class<?>[] parameterTypes = method.getParameterTypes();
         if (parameterTypes.length != 1 || !BaseEvent.class.isAssignableFrom(parameterTypes[0])) {
            continue;
         }

         Class<?> eventType = parameterTypes[0];
         Subscribe subscribe = method.getAnnotation(Subscribe.class);
         Subscriber subscriber = new Subscriber(commandHandler, method, subscribe.priority());

         if (this.subscribers.values().stream().anyMatch(list -> isValidHandler(method, list))) {
            continue;
         }

         List<Subscriber> eventSubscribers = this.subscribers.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>());
         eventSubscribers.add(subscriber);
         eventSubscribers.sort(Comparator.comparingInt(Subscriber::priority));
         commandHandler.init(this.loggerService);

         if (this.loggerService != null) {
            this.loggerService.getLogger().debug("Eventbus: Registered new subscriber - {}", subscriber);
         }
      }
   }

   public void unregister(Object handler) {
      if (!(handler instanceof BaseCommandHandler commandHandler)) {
         return;
      }

      for (List<Subscriber> eventSubscribers : this.subscribers.values()) {
         eventSubscribers.removeIf(subscriber -> subscriber.target() == commandHandler);
      }

      if (this.loggerService != null) {
         this.loggerService.getLogger().debug("Eventbus: Unregistered module - {}", handler);
      }
   }

   public void unregisterAll() {
      this.subscribers.clear();

      if (this.loggerService != null) {
         this.loggerService.getLogger().debug("Eventbus: All subscribers cleared");
      }
   }

   public void post(BaseEvent event) {
      List<Subscriber> eventSubscribers = this.subscribers.get(event.getClass());
      if (eventSubscribers != null) {
         this.invokeSubscribers(eventSubscribers, event);
      }
   }

   private void invokeSubscribers(List<Subscriber> subscribers, BaseEvent event) {
      boolean stopOnCancel = !(event instanceof CommandEvent);

      for (Subscriber subscriber : subscribers) {
         if (stopOnCancel && event.isCancelled()) {
            break;
         }

         List<Subscriber> registered = this.subscribers.get(event.getClass());
         if (registered == null || !registered.contains(subscriber)) {
            break;
         }

         try {
            subscriber.invoke(event);
         } catch (Throwable throwable) {
            if (this.loggerService != null) {
               this.loggerService.log("Error in subscriber " + subscriber + ": " + throwable);
            }
         }
      }
   }

   public void setLogger(LoggerService loggerService) {
      this.loggerService = loggerService;
   }

   private static boolean isValidHandler(Method method, List<Subscriber> subscribers) {
      return subscribers.stream().anyMatch(subscriber -> isBridgeMethod(method, subscriber));
   }

   private static boolean isBridgeMethod(Method method, Subscriber subscriber) {
      return subscriber.method().equals(method);
   }

   private record Subscriber(BaseCommandHandler target, Method method, int priority) {
      void invoke(BaseEvent event) {
         try {
            this.method.invoke(this.target, event);
         } catch (Throwable throwable) {
            throw new RuntimeException("Error invoking subscriber method", throwable);
         }
      }
   }
}
