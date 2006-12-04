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
 */

package org.apache.jmeter.module.cookies;

import org.apache.jmeter.module.integration.JMeterIntegrationEngine;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JMeterEditable implements Node.Cookie {
  
  public void edit(FileObject primaryFile) {
    final TopComponent currentComponent = TopComponent.getRegistry().getActivated();
    try {
      currentComponent.setCursor(org.openide.util.Utilities.createProgressCursor(currentComponent));
      Process prc = JMeterIntegrationEngine.getDefault().externalEdit(FileUtil.toFile(primaryFile).getCanonicalPath());
    } catch (Exception e) {
      ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
    } finally {
      RequestProcessor.getDefault().post(new Runnable() {
        public void run() {
          currentComponent.setCursor(null);
        }
      }, 2000);
      
    }
  }
  
}
