import org.glassfish.grizzly.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class ServerList {

    private List<HttpAddress> http = new ArrayList<>();
    private List<TcpAddress> tcp = new ArrayList<>();

    public List<HttpAddress> getHttp() {
        return http;
    }

    public List<TcpAddress> getTcp() {
        return tcp;
    }

    public void setHttp(List<HttpAddress> http) {
        this.http = http;
    }

    public void setTcp(List<TcpAddress> tcp) {
        this.tcp = tcp;
    }

    List<Pair<String, Integer>> getTcpList (){
        List<Pair<String,Integer>> result = new ArrayList<>();
        for(TcpAddress tcpAddress: tcp){
            Pair<String,Integer> pair = new Pair<String, Integer>(tcpAddress.getHost(),tcpAddress.getPort());
            result.add(pair);
        }
        return result;
    }

    List<String> getHttpList (){
        List<String> result = new ArrayList<>();
        for(HttpAddress httpAddress: http){
            result.add(httpAddress.getUrl());
        }
        return result;
    }
}

class TcpAddress {
    private String host;
    private int port;

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

class HttpAddress {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
