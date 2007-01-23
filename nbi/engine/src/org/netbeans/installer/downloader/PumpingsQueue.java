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
package org.netbeans.installer.downloader;

import java.io.File;
import java.net.URL;

/**
 * @author Danila_Dugurov
 */

/**
 * This interface - entry point for clients.
 * It's allow to client create new pumping and monitoring hole pumping process
 */
public interface PumpingsQueue {
  
  /**
   * In synchronious mode listener will be notified
   * about any updates in pumping process.
   * So the implementation of listeners must be worktime short.
   */
  void addListener(DownloadListener listener);
  
  /**
   * Terminate downloading process. Delete all pumpings.
   * If downloading process was runnig start it again.
   */
  void reset();
  
  Pumping getById(String id);
  
  /**
   * return all pumpings in queue.
   */
  Pumping[] toArray();
  
  /**
   * add new pumping. Output file in default folder
   */
  Pumping add(URL url);
  
  /**
   * add new pumping. Output file in specified folder
   */
  Pumping add(URL url, File folder);
  
  Pumping delete(String id);
  
  void delete(URL url);
}