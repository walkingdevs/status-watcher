
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.grizzly.utils.Pair;
import walkingdevs.http.ReqBuilder;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
   public static void main(String[] args) {
      TrustAllManager.trust();

//      if (new File("/status-watcher-dir").exists()) {
//         serverListFile = "/status-watcher-dir/server.list";
//      } else {
//         serverListFile = "server.list";
//      }


      if (new File("server.list").exists()) {
         serverListFile = "server.list";
      } else {
         serverListFile = "server.list";
      }



      new Timer().scheduleAtFixedRate(
              new Task(), 0, 1000 * 60
      );
      bot.fire("Status monitoring is started!");
      Runtime.getRuntime().addShutdownHook(
              new Thread(() -> {
                 bot.fire("I am dead.");
              })
      );
   }

   private static class Task extends TimerTask {
      public void run() {
         ServerList serverList = readJson();
         List<Pair<String,Integer>> tcpServers = serverList.getTcpList();
         List<String> httpServers = serverList.getHttpList();
         try {
            ReqBuilder.GET("https://google.com").connectTimeout(1000).readTimeout(1000).build().send();
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
         for (Pair<String,Integer> server : tcpServers) {
            String s = server.getFirst()+":"+server.getSecond();
            if (!ping(server)) {
               tryCounts.put(s, getCount(s) + 1);
            }
            if (getCount(s) > 2) {
               bot.fire(server.getFirst() +":" +server.getSecond() + " is Down");
            }
         }
         if (requestCount.incrementAndGet() % 10080 == 0) {
            fireStats(httpServers, tcpServers);
         }
      }

      private void fireStats(List<String> httpServers, List<Pair<String,Integer>> tcpServers) {
         StringBuilder report = new StringBuilder("Stats\n");
         for (String server : httpServers) {
            report
                    .append(server)
                    .append(" | ")
                    .append(new DecimalFormat("##.####").format(stats.get(server) * 100.0 / requestCount.get()))
                    .append("\n");
         }
         for (Pair<String,Integer> server : tcpServers) {
            String s = server.getFirst()+":"+server.getSecond();
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
            ReqBuilder.GET(server).connectTimeout(3000).readTimeout(3000).build().send();
            updateStat(server);
            tryCounts.put(server, 0);
            return true;
         } catch (Exception e) {
            return false;
         }
      }

      private boolean ping(Pair<String,Integer> server){
         String s = server.getFirst() + ":" +server.getSecond();
         if (!stats.containsKey(s)) {
            stats.put(s, 0);
         }
         try {
            Socket socket = new Socket(server.getFirst(), server.getSecond());
            updateStat(s);
            tryCounts.put(s, 0);
            return true;
         }
         catch (IOException e){
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

   private static ServerList readJson(){
      try {
         return new ObjectMapper().readValue(
                 new String(Files.readAllBytes(new File(serverListFile).toPath())
                 ),
                 new TypeReference<ServerList>(){}
         );
      } catch (IOException e){
         System.out.println(e.getMessage());
         return new ServerList();
      }
   }

   private static String serverListFile;
   private static final Bot bot = new Bot();
   private static final Map<String, Integer> tryCounts = new ConcurrentHashMap<>();
   private static final Map<String, Integer> stats = new ConcurrentHashMap<>();
   private static final AtomicLong requestCount = new AtomicLong(0);
}