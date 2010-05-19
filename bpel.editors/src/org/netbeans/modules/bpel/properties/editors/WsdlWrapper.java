/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
public final class WsdlWrapper {

    public WsdlWrapper(FileObject folder, String name, boolean isCreate) {
        myFolder = folder;
        myName = prepareName(name);
        myIsCreate = isCreate;

    }

    public WSDLModel getModel() {
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
        definitions.setTargetNamespace("http://enterprise.netbeans.org/bpel/" + myName); // NOI18N
        model.endTransaction();

        return model;
    }

    FileObject getFile() {
        FileObject file = myFolder.getFileObject(myName, "wsdl"); // NOI18N

        if (file != null) {
            myIsExisitingFile = true;
            return file;
        }
        myIsExisitingFile = false;

        if (myIsCreate) {
            return copyFile(myFolder, "wsdl/wsdl.wsdl", myName); // NOI18N
        }
        return null;
    }

    private FileObject copyFile(FileObject folder, String file, String name) {
        if (name == null || folder == null) {
            return null;
        }
        try {
            return FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource(file), folder, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String prepareName(String name) {
        if (!name.toLowerCase().endsWith(".wsdl")) {
            return name;
        }
        int k = name.toLowerCase().lastIndexOf(".wsdl"); // NOI18N
        return name.substring(0, k);
    }
    private String myName;
    private FileObject myFolder;
    private boolean myIsExisitingFile;
    private boolean myIsCreate;
}
