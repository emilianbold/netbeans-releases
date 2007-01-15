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
package org.netbeans.installer.downloader.dispatcher;

public interface ProcessDispatcher {
  /**
   * entry point to add process. This don't give any information when it will be processed
   * return false if process discarded. default impl - always true.
   */
  boolean schedule(Process process);
    
  /**
   * Force process termination.
   * deprecated since in any case of implementation it will deal with thread.stop();
   * which is deprecated
   */
  @Deprecated
  void terminate(Process process);
  
  void setLoadFactor(LoadFactor factor);
  
  /**
   * loadFactor allow managing system resources usages. By default Full - means no internal managment
   * In default impl loadFactor impact on frequency of blank quantums.
   */
  LoadFactor loadFactor();
  
  boolean isActive();
  
  int activeCount();
  
  int waitingCount();
  
  void start();
  
  /**
   * when dispatcher stops it terminate all running processes and also clear waiting queue.
   */
  void stop();
}
