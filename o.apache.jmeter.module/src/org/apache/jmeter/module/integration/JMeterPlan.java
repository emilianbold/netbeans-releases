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

package org.apache.jmeter.module.integration;

import java.io.File;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JMeterPlan {
  final private TestPlan rootElement;
  final private HashTree planTree;
  private String pathToPlan;
  
  /** Creates a new instance of JMeterPlan */
  public JMeterPlan(String path, HashTree plan, TestPlan root) {
    rootElement = root;
    planTree = plan;
    pathToPlan = path;
    
    FileObject planFile = FileUtil.toFileObject(new File(pathToPlan));
    if (planFile != null) {
      planFile.addFileChangeListener(WeakListeners.create(FileChangeListener.class, new FileChangeAdapter() {
        @Override
        public void fileRenamed(FileRenameEvent fe) {
          pathToPlan = FileUtil.toFile(fe.getFile()).getAbsolutePath();
        }
      }, null));
    }
  }
  
  public TestPlan getRoot() {
    return rootElement;
  }
  
  public HashTree getTree() {
    return planTree;
  }
  
  public String getPath() {
    return pathToPlan;
  }
}
