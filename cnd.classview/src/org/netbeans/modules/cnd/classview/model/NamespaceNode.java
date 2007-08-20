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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        id = ns.getQualifiedName();
        project = ns.getProject();
        String name = ns.getQualifiedName();
        String displayName = CVUtil.getNamesapceDisplayName(ns);
        setName(name);
        setDisplayName(displayName);
        setShortDescription(ns.getQualifiedName());
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
