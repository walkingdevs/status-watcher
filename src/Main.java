
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.grizzly.utils.Pair;
import walkingdevs.http.ReqBuilder;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
   public static void main(String[] args) {

      TrustAllManager.trust();
      if (new File("/status-watcher-dir").exists()) {
         serverListFile = "/status-watcher-dir/servers.json";
      } else {
         serverListFile = "servers.json";
      }
      String rt = System.getenv("readTimeout");
      String ct = System.getenv("connectTimeout");
      String ss = System.getenv("statsSchedule");
      readTimeout=3000;
      connectTimeout=3000;
      statsSchedule=10080;
      try {
         readTimeout=Integer.parseInt(rt);
      }
      catch (Exception e){}
      try {
         connectTimeout=Integer.parseInt(ct);
      }
      catch (Exception e){}
      try {
         statsSchedule=Integer.parseInt(ss);
      }
      catch (Exception e){}

      new Timer().scheduleAtFixedRate(
         new Task(), 0, 1000 * 30
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
         ServerList serverList = readJson();
         List<Pair<String, Integer>> tcpServers = serverList.getTcpList();
         List<String> httpServers = serverList.getHttpList();


         try {
            ReqBuilder.GET("https://google.com").connectTimeout(connectTimeout).readTimeout(readTimeout).build().send();
         } catch (Exception e) {
            bot.fire("no internet");
            return;
         }
         for (String server : httpServers) {
            if (!ping(server)) {
               tryCounts.put(server, getCount(server) + 1);
            }
            if (getCount(server) > 2) {
               bot.fire(server + " is Down");
            }
         }
         for (Pair<String, Integer> server : tcpServers) {
            String s = server.getFirst() + ":" + server.getSecond();
            if (!ping(server)) {
               tryCounts.put(s, getCount(s) + 1);
            }
            if (getCount(s) > 2) {
               bot.fire(server.getFirst() + ":" + server.getSecond() + " is Down");
            }
         }
         if (requestCount.incrementAndGet() % statsSchedule == 0) {
            fireStats(httpServers, tcpServers);
         }
      }

      private void fireStats(List<String> httpServers, List<Pair<String, Integer>> tcpServers) {
         StringBuilder report = new StringBuilder("Stats\n");
         for (String server : httpServers) {
            report
               .append(server)
               .append(" | ")
               .append(new DecimalFormat("##.####").format(stats.get(server) * 100.0 / requestCount.get()))
               .append("\n");
         }
         for (Pair<String, Integer> server : tcpServers) {
            String s = server.getFirst() + ":" + server.getSecond();
            report
               .append(s)
               .append(" | ")
               .append(new DecimalFormat("##.####").format(stats.get(s) * 100.0 / requestCount.get()))
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
         if (!stats.containsKey(server)) {
            stats.put(server, 0);
         }
         try {
            ReqBuilder.GET(server).connectTimeout(connectTimeout).readTimeout(readTimeout).build().send();
            updateStat(server);
            tryCounts.put(server, 0);
            return true;
         } catch (Exception e) {
            return false;
         }
      }

      private boolean ping(Pair<String, Integer> server) {
         String s = server.getFirst() + ":" + server.getSecond();
         if (!stats.containsKey(s)) {
            stats.put(s, 0);
         }
         try {
            new Socket(server.getFirst(), server.getSecond()).setSoTimeout(connectTimeout);
            updateStat(s);
            tryCounts.put(s, 0);
            return true;
         } catch (IOException e) {
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

   private static ServerList readJson() {
      try {
         return new ObjectMapper().readValue(
            new String(Files.readAllBytes(new File(serverListFile).toPath())), new TypeReference<ServerList>() {}
         );
      } catch (IOException e) {
         bot.fire(e.getMessage());
         return new ServerList();
      }
   }

   private static String serverListFile;
   private static int readTimeout;
   private static int connectTimeout;
   private static int statsSchedule;
   private static final Bot bot = new Bot();
   private static final Map<String, Integer> tryCounts = new ConcurrentHashMap<>();
   private static final Map<String, Integer> stats = new ConcurrentHashMap<>();
   private static final AtomicLong requestCount = new AtomicLong(0);
}