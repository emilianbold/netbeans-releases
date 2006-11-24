package org.netbeans.installer.downloader.connector;

import java.net.Proxy;
import java.net.Proxy.Type;

/**
 *
 * @author Danila_Dugurov
 */

public enum MyProxyType {
   DIRECT(Type.DIRECT),
   HTTP(Type.HTTP),
   SOCKS(Type.SOCKS),
   FTP(Type.SOCKS);
   
   private Proxy.Type type;
   
   private MyProxyType(Proxy.Type type) {
      this.type = type;
   }
   
   public Proxy.Type getType() {
      return type;
   }
}
