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
   
   String getId();
   
   URL declaredURL();
   URL realURL();
   File outputFile();
   File folder();
   
   long length();
   
   DownloadMode mode();
   
   State state();
   
   Section[] getSections();
   
   interface Section {
      Pair<Long, Long> getRange();
      long offset();
   }
   
   enum State {
      NOT_PROCESSED, CONNECTING, PUMPING, WAITING, INTERRUPTED, FAILED, FINISHED, DELETED
   }
}