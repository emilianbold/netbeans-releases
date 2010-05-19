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
 * License.  When distributing the software, include this License Header
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
package org.netbeans.modules.xslt.project.anttasks;

import java.io.File;
import org.netbeans.modules.soa.ui.util.ModelUtil;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class IdeWsdlCatalogModel implements IDECatalogModel<WSDLModel> {

    static IdeWsdlCatalogModel singletonCatMod = null;

    public IdeWsdlCatalogModel () {}

    public boolean isAccepted(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String ext = FileUtil.getExtension(file.getName());
        return Util.WSDL_FILE_EXTENSION.equals(ext);
    }

    public static IdeWsdlCatalogModel getDefault() {
        if (singletonCatMod == null) {
            singletonCatMod = new IdeWsdlCatalogModel();
        }
        return singletonCatMod;
    }

    public WSDLModel getModel(File file) throws Exception {
        if ( !isAccepted(file)) {
            return null;
        }
        FileObject fileObject = FileUtil.toFileObject(file);
        ModelSource source = org.netbeans.modules.xml.retriever.catalog.Utilities.createModelSource(fileObject, true);
        WSDLModel model = WSDLModelFactory.getDefault().getModel(source);
        model.sync();
        return model;
    }
}
