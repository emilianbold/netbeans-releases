/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.classview.model;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.SmartChangeEvent;
import org.netbeans.modules.cnd.classview.model.CVUtil.FillingDone;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * @author Vladimir Kvasihn
 */
public class NamespaceNode extends NPNode {
    private String id;
    private CsmProject project;
    
    public NamespaceNode(CsmNamespace ns) {
        super(ns, new FillingDone());
        //this.ns = ns;
        id = ns.getQualifiedName();
        project = ns.getProject();
        String name = ns.getQualifiedName();
        String displayName = ns.getName();
        if (displayName.length() == 0) {
            displayName = ns.getQualifiedName();
            int scope = displayName.lastIndexOf("::"); // NOI18N
            if (scope != -1) {
                displayName = displayName.substring(scope + 2);
            }
            displayName = displayName.replace('<', ' ').replace('>', ' '); // NOI18N
        }
        setName(name);
        setDisplayName(displayName);
        setShortDescription(ns.getQualifiedName());
    }

    
    public CsmNamespace getNamespace() {
        return project.findNamespace(id);
    }
    
    protected boolean isSubNamspace(CsmNamespace ns) {
        return ns!= null && ns.getParent() == getNamespace();
    }

    public boolean update(SmartChangeEvent e) {
	if( !isDismissed()){
            for (CsmNamespace ns : e.getRemovedNamespaces()){
                if (ns.getProject() == project && id.equals(ns.getQualifiedName())){
                    final Children children = getParentNode().getChildren();
                    children.MUTEX.writeAccess(new Runnable(){
                        public void run() {
                            children.remove(new Node[] { NamespaceNode.this });
                        }
                    });
                    return true;
                }
            }
            if (isInited()){
                return super.update(e);
            }
	}
        return false;
    }
   
    public void dismiss() {
        setDismissed();
        if (isInited()){
            super.dismiss();
        }
    }

    public String getHtmlDisplayName() {
        String retValue = getDisplayName();
        // make unnamed namespace bold and italic
        if (retValue.startsWith(" ")) { // NOI18N
            retValue = "<i>" + retValue; // NOI18N
        }
        return retValue;
    }

}
