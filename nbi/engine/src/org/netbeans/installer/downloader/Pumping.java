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
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */

public interface Pumping {
  
  /**
   * It's runtime property of pumping. It means that it's not persistence property.
   * So if downloader client maintain it's state persistance - it mustn't base on pumpings ids.
   */
  String getId();
  
  /**
   * @return declared pumping url.
   */
  URL declaredURL();
  
  /**
   * @return real pumping url. It is url which was obtain at runtime.
   * It's may be the same as declared url if no redirect may occur.
   */
  URL realURL();
  
  /**
   * @return file corresponding to this pumping.
   */
  File outputFile();
  File folder();
  
  long length();
  
  /**
   * @return mode in which downloader process it. So if Single thread mode - it's means
   *  that only one thread process pumping(so one section invoked).
   * If multi thread mode - it's means that downloader allowed to process pumping in more
   * then one thread concurrently. But it's not means that downloader do it.
   * The issue process or not in multy thread deal with some external issues:
   * for example domain police and server side speed reducing for client who try to obtain
   * more then one connection at time. Base implementation in any case download in one thread.
   */
  DownloadMode mode();
  
  State state();
  
  /**
   * one section  - one thread.
   * Section - data structure for representation and manage downloading unit
   */
  Section[] getSections();
  
  /////////////////////////////////////////////////////////////////////////////////
  // Inner Classes
  
  interface Section {
    /**
     * range of bytes this section responsible for.
     */
    Pair<Long, Long> getRange();
    
    /**
     * absolute offset. Means if range: 12345 - 23456. initially offset equals 12345
     * when section downloaded it's equals 23456.
     */
    long offset();
  }
  
  enum State {
    NOT_PROCESSED, CONNECTING, PUMPING, WAITING, INTERRUPTED, FAILED, FINISHED, DELETED
  }
}