package com.sinnerschrader.smaller.internal;

import java.io.InputStream;
import java.net.InetSocketAddress;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * @author marwol
 */
public class Server {

  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  private String version;

  private final org.eclipse.jetty.server.Server server;

  /**
   * @param args
   */
  public static void main(final String[] args) {
    new Server(args).start();
  }

  /**
   * @param args
   */
  public Server(final String... args) {
    final ListenAddress la = new ListenAddress(args);
    LOGGER.info("\nVersion: {}\nListen On: {}", this.getVersion(), la);
    server = new org.eclipse.jetty.server.Server(InetSocketAddress.createUnresolved(la.getHost(), la.getPort()));
    server.setHandler(new RequestHandler());
  }

  /**
   * 
   */
  public void start() {
    try {
      server.start();
      server.join();
    } catch (final Exception e) {
      LoggerFactory.getLogger(Server.class).error("Failed to start jetty server", e);
    }
  }

  /**
   * 
   */
  public void stop() {
    try {
      server.stop();
    } catch (final Exception e) {
      LoggerFactory.getLogger(Server.class).error("Failed to stop jetty server", e);
    }
  }

  private String getVersion() {
    if (version != null) {
      return version;
    }
    synchronized (this) {
      if (version != null) {
        return version;
      }
      String v = "Smaller(development)";
      final InputStream is = Server.class.getClassLoader().getResourceAsStream("META-INF/maven/com.sinnerschrader.smaller/server/pom.xml");
      if (is != null) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
          final DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.parse(is);
          v = "Smaller(" + doc.getElementsByTagName("version").item(0).getTextContent() + ")";
        } catch (final Exception e) {
          LOGGER.warn("Failed to get version info from pom", e);
        }
      }
      version = v;
    }
    return version;
  }

}