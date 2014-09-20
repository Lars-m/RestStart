package server;

import model.Player;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author plaul1
 */
public class RestServer {

  static int port = 8080;
  static String ip = "127.0.0.1";
  static String publicFolder = "src/htmlFiles/";
  static String startFile = "index.html";
  static String filesUri = "/pages";

  public void run() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(ip, port), 0);
    //REST Routes
    server.createContext("/Date", new HandlerDate());
    server.createContext("/AllPlayerNames", new HandlerQuestion1());
    //HTTP Server Routes
    server.createContext(filesUri, new HandlerFileServer());
    server.createContext("/", new HttpHandler() {
      @Override
      public void handle(HttpExchange he) throws IOException {
        String redirect = "http://localhost:8080/pages/index.html";
        System.out.println(redirect);
        he.getResponseHeaders().add("Location", redirect);
        he.sendResponseHeaders(301, 0);
      }
    });
    server.start();
    System.out.println("Server started, listening on port: " + port);
  }

  public static void main(String[] args) throws Exception {
    if (args.length >= 3) {
      port = Integer.parseInt(args[0]);
      ip = args[1];
      publicFolder = args[2];
    }
    new RestServer().run();
  }

  class HandlerDate implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
      String response = new Date().toString();

      he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      he.getResponseHeaders().add("Content-Type", "text/plain");
      he.sendResponseHeaders(200, response.length());
      try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
        pw.print(response); //What happens if we use a println instead of print --> Explain
      }
    }
  }

  class HandlerQuestion1 implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {

      String response = new Gson().toJson(getPlayers());
      System.out.println(response);
      //he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      he.getResponseHeaders().add("Content-Type", "application/json");
      he.sendResponseHeaders(200, 0);
      try (OutputStream os = he.getResponseBody()) {
        os.write(response.getBytes());
      }
    }
  }

  class HandlerQuestion2 implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {

    }
  }

  private List<Player> getPlayers() {
    List<Player> players = new ArrayList();
    players.add(new Player(1, "James Rodríguez", "Columbia"));
    players.add(new Player(2, "Thomas Mueller", "Germany"));
    players.add(new Player(3, "Messi", "Argentina"));
    players.add(new Player(4, "Neymar", "Brasil"));
    players.add(new Player(5, "van Persie", "Holland"));
    return players;
  }

  class HandlerFileServer implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
      int responseCode = 500;
      //Set initial error values if an un expected problem occurs
      String errorMsg = null;
      byte[] bytesToSend = "<h1>Internal Error </h1><p>We are sorry. The server encountered an unexpected problem</p>".getBytes();
      String mime = null;
      
      String requestedFile = he.getRequestURI().toString();
      String f = requestedFile.substring(requestedFile.lastIndexOf("/") + 1);
      try {
        String extension = f.substring(f.lastIndexOf("."));
        mime = getMime(extension);
        File file = new File(publicFolder + f);
        System.out.println(publicFolder + f);
        bytesToSend = new byte[(int) file.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        bis.read(bytesToSend, 0, bytesToSend.length);
        responseCode = 200;
      } catch (Exception e) {
        responseCode = 404;
        errorMsg = "<h1>404 Not Found</h1>No context found for request";
      }
      if (responseCode == 200) {
        Headers h = he.getResponseHeaders();
        h.set("Content-Type", mime);
      } else {
        bytesToSend = errorMsg.getBytes();
      }
      he.sendResponseHeaders(responseCode, bytesToSend.length);
      try (OutputStream os = he.getResponseBody()) {
        os.write(bytesToSend, 0, bytesToSend.length);
      }
    }

    private String getMime(String extension) {
      String mime = "";
      switch (extension) {
        case ".pdf":
          mime = "application/pdf";
          break;
        case ".png":
          mime = "image/png";
          break;
        case ".js":
          mime = "text/javascript";
          break;
        case ".html":
          mime = "text/html";
          break;
        case ".jar":
          mime = "application/java-archive";
          break;
      }
      return mime;
    }
  }
}
