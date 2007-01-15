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

import java.io.File;
import org.netbeans.installer.Installer;

/**
 *
 * @author Danila_Dugurov
 */
public class DownloadConfig {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Constants
  public static final int DISPATCHER_QUANTUM;
  public static final int DISPATCHER_POOL;
  
  public static final File DEFAULT_OUTPUT_DIR;
  
  public static final int ATTEMPT_COUNT;
  public static final int REATTEMPT_DELAY;
  
  static {
    DISPATCHER_QUANTUM = 100;
    DISPATCHER_POOL = 10;
    
    DEFAULT_OUTPUT_DIR = new File(Installer.DEFAULT_LOCAL_DIRECTORY_PATH, "downloads");
    ATTEMPT_COUNT = 10;
    REATTEMPT_DELAY = 5 * 1000;
  }
}
