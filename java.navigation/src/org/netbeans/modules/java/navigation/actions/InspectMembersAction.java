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

package org.netbeans.modules.java.navigation.actions;

import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.navigation.JavaMembers;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import static javax.lang.model.util.ElementFilter.*;

/**
 * This action shows the members of the types in the selected file in a pop up window.
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class InspectMembersAction extends AbstractNavigationAction {
    
    public InspectMembersAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = getDataObject(activatedNodes);

        if (dataObject != null) {
            final FileObject fileObject = dataObject.getPrimaryFile();

            if (fileObject != null && 
                    "java".equalsIgnoreCase(fileObject.getExt()) && // NOI18N
                    "text/x-java".equals(fileObject.getMIMEType())) { // NOI18N
                JavaMembers.show(fileObject);
                return;
            }
        }

        beep();
    }

    public String getName() {
        return NbBundle.getMessage(InspectMembersAction.class,
            "CTL_InspectMembersAction");
    }

    protected Class[] cookieClasses() {
        return new Class[] { DataObject.class };
    }
    
    @Override
    public boolean enable( Node[] nodes) {
        if ( OpenProjects.getDefault().getOpenProjects().length == 0) {
            return false;
        }
        return getDataObject(nodes) != null;
    }
    
    private DataObject getDataObject(Node nodes[]) {
        
        if ( nodes == null || nodes.length == 0) {
            return null;
        }
        
        return nodes[0].getLookup().lookup(DataObject.class);
    }
    
}
