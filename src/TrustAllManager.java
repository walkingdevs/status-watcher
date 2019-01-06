import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class TrustAllManager implements X509TrustManager {
   public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
   }

   public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
   }

   public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
   }

   static void trust() {
      try {
         TrustManager[] certs = new TrustManager[]{new TrustAllManager()};
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(null, certs, new SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
         HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> {
            return true;
         });
      } catch (Exception ignored) {
      }
   }
}