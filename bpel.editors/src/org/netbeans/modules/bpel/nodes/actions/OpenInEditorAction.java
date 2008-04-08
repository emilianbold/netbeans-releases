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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.nodes.ImportNode;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 19 April 2006
 */
public class OpenInEditorAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    
    protected String getBundleName() {
        return NbBundle.getMessage(OpenInEditorAction.class,
                "CTL_OpenInEditorAction"); // NOI18N
    }
    
    @Override
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        Import imprt = ((ImportNode)nodes[0]).getReference();
        if (imprt == null) {
            return;
        }
        
        
        ModelSource modelSource = ResolverUtility.getImportedModelSource(imprt);
        if (modelSource == null) {
            return;
        }
        FileObject fo = modelSource.getLookup().lookup(FileObject.class);
        if (fo == null || !fo.isValid()) {
            return;
        }
        
        try {
            DataObject d = DataObject.find(fo);
            LineCookie lc = d.getCookie(LineCookie.class);
            if (lc == null) {
                return;
            }
            final Line l = lc.getLineSet().getOriginal(1);
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.show(Line.SHOW_GOTO);
                }
            });
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().notify(ex);
        }
        
    }
    
    @Override
    public boolean enable(Node[] nodes) {
        if (
                nodes == null ||
                nodes.length != 1 ||
                !(nodes[0] instanceof ImportNode))
        {
            return false;
        }

        Import imprt = ((ImportNode)nodes[0]).getReference();
        if (imprt == null) {
            return false;
        }
        ModelSource modelSource = ResolverUtility.getImportedModelSource(imprt);
        if (modelSource == null) {
            return false;
        }
        FileObject fo = modelSource.getLookup().lookup(FileObject.class);
        if (fo == null || !fo.isValid()) {
            return false;
        }
        //
        return true;
    }
    
    public ActionType getType() {
        return ActionType.OPEN_IN_EDITOR;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
}
