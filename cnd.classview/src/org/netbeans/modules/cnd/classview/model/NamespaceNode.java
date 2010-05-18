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

package org.netbeans.modules.cnd.classview.model;

import java.util.Collection;
import javax.swing.Action;
import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;
import org.netbeans.modules.cnd.classview.actions.MoreDeclarations;
import org.openide.nodes.Children;

/**
 * @author Vladimir Kvasihn
 */
public class NamespaceNode extends NPNode {
    private String id;
    private CsmProject project;
    
    public NamespaceNode(CsmNamespace ns, Children.Array key) {
        super(key);
        init(ns);
    }

    private void init(CsmNamespace ns){
        id = ns.getQualifiedName().toString();
        project = ns.getProject();
        String name = ns.getQualifiedName().toString();
        String displayName = CVUtil.getNamespaceDisplayName(ns).toString();
        setName(name);
        setDisplayName(displayName);
        setShortDescription(ns.getQualifiedName().toString());
    }

    public CsmNamespace getNamespace() {
        return project.findNamespace(id);
    }
    
    @Override
    public String getHtmlDisplayName() {
        String retValue = getDisplayName();
        // make unnamed namespace bold and italic
        if (retValue.startsWith(" ")) { // NOI18N
            retValue = "<i>" + retValue; // NOI18N
        }
        return retValue;
    }

    @Override
    public Action getPreferredAction() {
        return createOpenAction();
    }
    
    private Action createOpenAction() {
        CsmNamespace ns = getNamespace();
        if (ns != null){
            Collection<? extends CsmOffsetableDeclaration> arr = ns.getDefinitions();
            if (arr.size() > 0) {
                return new GoToDeclarationAction(arr.iterator().next());
            }
        }
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action action = createOpenAction();
        if (action != null){
            CsmNamespace ns = getNamespace();
            Collection<? extends CsmOffsetableDeclaration> arr = ns.getDefinitions();
            if (arr.size() > 1){
                Action more = new MoreDeclarations(arr);
                return new Action[] { action, more };
            }
            return new Action[] { action };
        }
        return new Action[0];
    }
}
