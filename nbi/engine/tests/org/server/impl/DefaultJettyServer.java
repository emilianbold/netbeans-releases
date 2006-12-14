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
package org.server.impl;

import org.*;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.server.*;

/**
 *
 * @author Danila_Dugurov
 */
public class DefaultJettyServer extends AbstractServer {
  
  private Server httpServer;
  
  public DefaultJettyServer(String testDataPath, int serverPort) {
    super(testDataPath, serverPort);
  }
  
  public void start() throws Exception {
    if (httpServer == null) httpServer = new Server(serverPort);
    if (httpServer.isRunning()) return;
    final ResourceHandler handler = new ResourceHandler();
    handler.setResourceBase(testDataPath);
    httpServer.setHandlers(new Handler[]{handler, new DefaultHandler()});
    httpServer.start();
  }
  
  public void stop() throws Exception {
    if (httpServer == null && httpServer.isStopped()) return;
    httpServer.stop();
  }
}
