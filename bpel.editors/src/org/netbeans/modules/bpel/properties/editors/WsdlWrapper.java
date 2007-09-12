/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.properties.editors;

import java.io.IOException;
import java.util.StringTokenizer;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.11
 */
final class WsdlWrapper {

  /**
   * 
   * @param folder is projectSource Root
   * @param name is relative path from project source 
   * @param isCreate said is it required to create related wsdl with all path if needed
   * 
   */ 
  WsdlWrapper(FileObject folder, String name, boolean isCreate) {
    myFolder = prepareFolder(folder, name, isCreate);
    myName = prepareName(name);
    myIsCreate = isCreate;
  }

  WSDLModel getModel() {
    FileObject file = getFile();

    if (file == null) {
      return null;
    }
    ModelSource source = Utilities.getModelSource(file, file.canWrite());
    WSDLModel model = WSDLModelFactory.getDefault().getModel(source);

    if (myIsExisitingFile) {
      return model;
    }
    model.startTransaction();
    Definitions definitions = model.getDefinitions();
    definitions.setName(myName);
    definitions.setTargetNamespace(HOST + myName);
    model.endTransaction();

    return model;
  }

  FileObject getFile() {
    FileObject file = myFolder.getFileObject(myName, EXTENSION);

    if (file != null) {
      myIsExisitingFile = true;
      return file;
    }
    myIsExisitingFile = false;

    if (myIsCreate) {
      return copyFile(myFolder, "wsdl/", "wsdl.wsdl", myName); // NOI18N
    }
    return null;
  }

  private FileObject copyFile(
    FileObject destination,
    String path,
    String file,
    String name)
  {
    if (name == null || destination == null) {
      return null;
    }
    try {
      return FileUtil.copyFile(
        Repository.getDefault().getDefaultFileSystem().findResource(path + file),
        destination,
        name);
    }
    catch (IOException e) {
//    e.printStackTrace();
    }
    return null;
  }

  private FileObject prepareFolder(FileObject folder, String name, boolean isCreate) {
    if (folder == null) {
        return null;
    }
    
    int k1 = name.lastIndexOf("/"); // NOI18N
    int k2 = name.lastIndexOf("\\"); // NOI18N
    int k = Math.max(k1, k2);

    if (k == -1) {
      return folder;
    }
    String path = name.substring(0, k);
    StringTokenizer stk = new StringTokenizer(path, "/\\");

    while (stk.hasMoreTokens()) {
      String token = stk.nextToken();

      FileObject child = folder.getFileObject(token);

      if (child != null) {
        folder = child;
      } else if (isCreate) {
        try {
          folder = folder.createFolder(token);
        }
        catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      }
    }
    return folder;
  }

  private String prepareName(String name) {
    int k1 = name.lastIndexOf("/"); // NOI18N
    int k2 = name.lastIndexOf("\\"); // NOI18N
    int k = Math.max(k1, k2);

    if (k != -1) {
      name = name.substring(k + 1);
    }
    k = name.lastIndexOf(".wsdl"); // NOI18N

    if (k != -1) {
      return name.substring(0, k);
    }
    return name;
  }

  private void out() {
    System.out.println();
  }

  private void out(Object object) {
    System.out.println("*** " + object); // NOI18N
  }

  private String myName;
  private FileObject myFolder;
  private boolean myIsExisitingFile;
  private boolean myIsCreate;
  
  private static final String EXTENSION = "wsdl"; // NOI18N
  private static final String HOST = "http://enterprise.netbeans.org/bpel/"; // NOI18N
}
