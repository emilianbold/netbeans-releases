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
import org.netbeans.modules.bpel.nodes.PartnerLinkNode;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import java.util.Collection;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.09.24
 */
public class UpdateWebService extends BpelNodeAction {

    protected String getBundleName() {
        return NbBundle.getMessage(UpdateWebService.class, "CTL_UpdateWebService"); // NOI18N
    }
    
    public void performAction(Node[] nodes) {
        FileObject wsdl = getWsdlFromEjbModule(nodes);
//System.out.println();
//System.out.println("ejb wsdl: "  + wsdl);
        ReferenceUtil.generateWsdlFromEjbModule(wsdl);
    }

    protected FileObject getWsdlFromEjbModule(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return null;
        }
        return getWsdlFromEjbModule(((PartnerLinkNode) nodes[0]).getReference());
    }

    private FileObject getWsdlFromEjbModule(PartnerLink pl) {
        if (pl == null) {
            return null;
        }
        WSDLReference<PartnerLinkType> pltRef = pl.getPartnerLinkType();
        PartnerLinkType plt = pltRef == null ? null : pltRef.get();

        if (plt == null) {
            return null;
        }
        WSDLModel wsdlModel = plt.getModel();
        FileObject bpel = SoaUtil.getFileObjectByModel(pl.getModel());
        FileObject wsdl = SoaUtil.getFileObjectByModel(wsdlModel);
//System.out.println("  bpel: "  + bpel);
//System.out.println("  wsdl: "  + wsdl);

        if ( !ReferenceUtil.isSameProject(wsdl, bpel)) {
            return null;
        }
        Definitions definitions = wsdlModel.getDefinitions();

        if (definitions == null) {
            return null;
        }
        Collection<Import> imports = definitions.getImports();

        if (imports == null) {
            return null;
        }
        if (imports.size() != 1) {
            return null;
        }
        try {
            return SoaUtil.getFileObjectByModel(imports.iterator().next().getImportedWSDLModel());
        }
        catch (org.netbeans.modules.xml.xam.locator.CatalogModelException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean isChangeAction() {
        return false;
    }
    
    public boolean enable(BpelEntity[] entities) {
        if (entities == null || entities.length != 1) {
            return false;
        }
        if ( !(entities[0] instanceof PartnerLink)) {
            return false;
        }
        return getWsdlFromEjbModule((PartnerLink) entities[0]) != null;
    }

    public ActionType getType() {
        return ActionType.UPDATE_WEB_SERVICE;
    }
    
    protected void performAction(BpelEntity[] entities) {}
}
