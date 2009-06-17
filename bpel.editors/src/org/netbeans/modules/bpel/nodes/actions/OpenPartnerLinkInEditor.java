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
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.nodes.PartnerLinkNode;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.SoaUtil;

/**
 *
 * @author Vitaly Bychkov
 * @version 21 April 2006
 */
public class OpenPartnerLinkInEditor extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    
    protected String getBundleName() {
        return NbBundle.getMessage(OpenPartnerLinkInEditor.class, 
                "CTL_OpenPartnerLinkInEditor"); // NOI18N
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        PartnerLink pl = ((PartnerLinkNode)nodes[0]).getReference();
        if (pl == null) {
            return;
        }
        
        WSDLReference<PartnerLinkType> plt = pl.getPartnerLinkType();
        if (plt == null) {
            return;
        }
        
        WSDLModel wsdlModel = plt.get().getModel();
        FileObject fo = SoaUtil.getFileObjectByModel(wsdlModel);
        try {
            DataObject d = DataObject.find(fo);
            LineCookie lc = (LineCookie) d.getCookie(LineCookie.class);
            if (lc == null) {
                return;
            }
            final Line l = lc.getLineSet().getOriginal(1);
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                }
            });
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    
    public boolean isChangeAction() {
        return false;
    }
    
    
    public boolean enable(BpelEntity[] entities) {
        if (!super.enable(entities)) return false;
        return (entities[0] instanceof PartnerLink);
    }
//    
//    public boolean enable(Node[] nodes) {
//        return nodes != null
//            && nodes.length == 1
//            && nodes[0] instanceof PartnerLinkNode;
//    }
    
    public ActionType getType() {
        return ActionType.OPEN_IN_EDITOR;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
}
