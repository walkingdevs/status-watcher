import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import walkingdevs.http.ReqBuilder;

import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
   public static void main(String[] args) {
      TrustAllManager.trust();
      if (new File("/status-watcher-dir").exists()) {
         serverListFile = "/status-watcher-dir/server.list";
      } else {
         serverListFile = "server.list";
      }
      new Timer().scheduleAtFixedRate(
         new Task(), 0, 1000 * 60
      );
      bot.fire("Status monitoring is started...");
      Runtime.getRuntime().addShutdownHook(
         new Thread(() -> {
            bot.fire("I am dead.");
         })
      );
   }

   private static class Task extends TimerTask {
      public void run() {
         List<String> servers = readJson();
         try {
            ReqBuilder.GET("https://google.com").connectTimeout(1000).readTimeout(1000).build().send();
         } catch (Exception e) {
            bot.fire("no internet");
            return;
         }
         for (String server : servers) {
            if (!ping(server)) {
               tryCounts.put(server, getCount(server) + 1);
            }
            if (getCount(server) > 2) {
               bot.fire(server + " is Down");
            }
         }
         if (requestCount.incrementAndGet() % 10080 == 0) {
            fireStats(servers);
         }
      }

      private void fireStats(List<String> servers) {
         StringBuilder report = new StringBuilder("Stats\n");
         for (String server : servers) {
            report
               .append(server)
               .append(" | ")
               .append(new DecimalFormat("##.####").format(stats.get(server) * 100.0 / requestCount.get()))
               .append("\n");
         }
         bot.fire(report.toString());
      }

      private Integer getCount(String server) {
         if (!tryCounts.containsKey(server)) {
            tryCounts.put(server, 0);
         }
         return tryCounts.get(server);
      }

      private boolean ping(String server) {
         try {
            ReqBuilder.GET(server).connectTimeout(3000).readTimeout(3000).build().send();
            updateStat(server);
            tryCounts.put(server, 0);
            return true;
         } catch (Exception e) {
            return false;
         }
      }

      private void updateStat(String server) {
         if (!stats.containsKey(server)) {
            stats.put(server, 0);
         }
         stats.put(server, stats.get(server) + 1);
      }
   }

   private static List<String> readJson() {
      try {
         return new ObjectMapper().readValue(
            new String(
               Files.readAllBytes(new File(serverListFile).toPath())
            ),
            new TypeReference<List<String>>() {}
         );
      } catch (Exception e) {
         bot.fire(e.getMessage());
         return new ArrayList<>();
      }
   }

   private static String serverListFile;
   private static final Bot bot = new Bot();
   private static final Map<String, Integer> tryCounts = new ConcurrentHashMap<>();
   private static final Map<String, Integer> stats = new ConcurrentHashMap<>();
   private static final AtomicLong requestCount = new AtomicLong(0);
}