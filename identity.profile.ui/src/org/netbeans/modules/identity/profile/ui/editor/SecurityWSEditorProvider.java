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

/*
 * SecurityWSEditorProvider.java
 *
 * Created on April 10, 2006, 5:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.identity.profile.ui.editor;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Security editor provider for the web service attribuet editor.
 *
 * Created on April 10, 2006, 5:54 PM
 *
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider.class)
public class SecurityWSEditorProvider implements WSEditorProvider {

    /** Creates a new instance of SecurityWSEditorProvider */
    public SecurityWSEditorProvider() {
    }

    public WSEditor createWSEditor(Lookup nodeLookup) {
        FileObject srcRoot = nodeLookup.lookup(FileObject.class);
        if (srcRoot != null) {
            Project prj = FileOwnerQuery.getOwner(srcRoot);
            JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                return new SecurityWSEditor(jaxWsModel);
            }
        }
        return null;
    }
    
    public boolean enable(Node node) {
        try {
            return J2eeProjectHelper.newInstance(node, null).isAMInstalled();
        } catch (Exception ex) {
            return false;
        }
    }  
}
