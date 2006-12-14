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
package org.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import org.MyTestCase;
import org.netbeans.installer.downloader.connector.MyProxy;
import org.netbeans.installer.downloader.connector.URLConnector;
import org.server.TestDataGenerator;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class ConnectorTest extends WithServerTestCase {
  
  public static URL smallest;
  public static URL small;
  public static URL noResource;
  
  static {
    try {
      smallest = new URL("http://localhost:8080/" + TestDataGenerator.testFiles[0]);
      small = new URL("http://127.0.0.1:8080/" + TestDataGenerator.testFiles[1]);
      noResource = new URL("http://localhost:8080/kadabra.data");
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    }
  }
  
  public void testDirect() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      assertEquals(TestDataGenerator.testFileSizes[0], connection.getContentLength());
      connection.getInputStream().close();
      connection = connector.establishConnection(small);
      assertEquals(TestDataGenerator.testFileSizes[1], connection.getContentLength());
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
    try {
      connection = connector.establishConnection(noResource);
      connection.getInputStream().close();
      fail();
    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    } finally {
      if (connection != null) {
        try {
          final InputStream in = connection.getInputStream();
          if (in != null) in.close();
        } catch (IOException ignored) {//skip
        }
      }
    }
  }
  
  public void testWithProxy() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
    connector.addProxy(new MyProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("www.fake.com", 1234))));
    connector.setUseProxy(true);
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
      fail();
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
  
  public void testWithProxyWithByPassList() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    connector.addProxy(new MyProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("www.fake.com", 1234))));
    connector.setUseProxy(true);
    connector.addByPassHost("127.0.0.1");
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
      fail();
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
    try {
      connection = connector.establishConnection(small);
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
  }
}
