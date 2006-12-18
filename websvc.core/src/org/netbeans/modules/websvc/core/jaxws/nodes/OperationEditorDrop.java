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

package org.netbeans.modules.websvc.core.jaxws.nodes;

import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;

/** Implementation of ActiveEditorDrop
 *
 * @author mkuchtiak
 */
public class OperationEditorDrop implements ActiveEditorDrop {
    
    OperationNode operationNode;
    
    public OperationEditorDrop(OperationNode operationNode) {
        this.operationNode=operationNode;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        Object mimeType = targetComponent.getDocument().getProperty("mimeType"); //NOI18N
        if (mimeType!=null && ("text/x-java".equals(mimeType) || "text/x-jsp".equals(mimeType) )) { //NOI18N
            
            try {
                boolean enableDnD=false;
                Node clientNode = operationNode.getParentNode().getParentNode().getParentNode();
                FileObject srcRoot = (FileObject)clientNode.getLookup().lookup(FileObject.class);
                Project clientProject = FileOwnerQuery.getOwner(srcRoot);
                Project targetProject = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(targetComponent.getDocument()));
                if (JaxWsUtils.addProjectReference(clientProject, targetProject)) {
                    JaxWsCodeGenerator.insertMethod(targetComponent.getDocument(), targetComponent.getCaret().getDot(), operationNode);
                    return true;
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        }
        return false;
    }
    
}