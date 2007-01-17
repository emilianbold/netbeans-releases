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
 */
package org.netbeans.installer.downloader;

/**
 *
 * @author Danila_Dugurov
 */

/**
 * be aware of: If listener contract will be changed - analize carefully all fired events
 * Some pices of code based on reflection and invocation, so
 * NoSuchMethodException may occures.(It's implementation pitfalls)
 */
public interface DownloadListener {
  
  /**
   * notification that pumping was update means that some pice of bytes downloaded
   * or some meta data such as real url, output file name obtain from server.
   */
  void pumpingUpdate(String id);
  
  /**
   * This property was separate from pumping update because it's describe pumping as process
   * and in pumping update - notify that pumping was changed as entity.
   */
  void pumpingStateChange(String id);
  
  void pumpingAdd(String id);
  void pumpingDelete(String id);
  
  void queueReset();
  
  /**
   * notification that downloader invoked work.
   */
  void pumpsInvoke();
  
  
  /**
   * notification that downloader stoped work.
   */
  void pumpsTerminate();
}
