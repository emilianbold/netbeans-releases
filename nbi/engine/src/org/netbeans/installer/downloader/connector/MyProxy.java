/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * $Id$
 */
package org.netbeans.installer.downloader.connector;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.utils.xml.DomExternalizable;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Danila_Dugurov
 */
public class MyProxy implements DomExternalizable {
  
  private static final Map<Type, MyProxyType> type2Type = new HashMap<Type, MyProxyType>();
  
  static {
    type2Type.put(Type.DIRECT, MyProxyType.DIRECT);
    type2Type.put(Type.HTTP, MyProxyType.HTTP);
    type2Type.put(Type.SOCKS, MyProxyType.SOCKS);
  }
  
  int port = -1;
  String host = "";
  MyProxyType type = MyProxyType.DIRECT;
  
  public MyProxy(Proxy proxy) {
    type = type2Type.get(proxy.type());
    final InetSocketAddress address = (InetSocketAddress) proxy.address();
    if (address != null) {
      host = address.getHostName();
      port = address.getPort();
    }
  }
  
  public MyProxy(Proxy proxy, MyProxyType type) throws IllegalArgumentException {
    this(proxy);
    if (!proxy.type().equals(type.getType())) throw new IllegalArgumentException("types conflicts");
    this.type = type;
  }
  
  public MyProxy() {
  }
  
  public Proxy getProxy() {
    return type == MyProxyType.DIRECT ? Proxy.NO_PROXY:
      new Proxy(type.getType(), new InetSocketAddress(host, port));
  }
  
  public void readXML(Element element) {
    final DomVisitor visitor = new RecursiveDomVisitor() {
      public void visit(Element element) {
        if ("proxy-type".equals(element.getNodeName())) {
          type = MyProxyType.valueOf(element.getTextContent().trim().toUpperCase());
        } else if ("proxy-host".equals(element.getNodeName())) {
          host = element.getTextContent().trim();
        } else if ("proxy-port".equals(element.getNodeName())) {
          port = Integer.parseInt(element.getTextContent().trim());
        } else super.visit(element);
      }
    };
    visitor.visit(element);
  }
  
  public Element writeXML(Document document) {
    final Element root = document.createElement("proxy");
    DomUtil.addElemet(root, "proxy-type", type.toString());
    DomUtil.addElemet(root, "proxy-host", host);
    DomUtil.addElemet(root, "proxy-port", String.valueOf(port));
    return root;
  }
  
  public boolean equals(Object proxy) {
    if (this == proxy) return true;
    if (proxy == null) return false;
    if (proxy instanceof MyProxy) {
      final MyProxy prox = (MyProxy) proxy;
      if (port == prox.port && type == prox.type && host.equals(prox.host))
        return true;
    }
    return false;
  }
  
  public int hashCode() {
    int result;
    result = (host != null ? host.hashCode() : 0);
    result = 29 * result + (type != null ? type.hashCode() : 0);
    result = 29 * result + port;
    return result;
  }
}
