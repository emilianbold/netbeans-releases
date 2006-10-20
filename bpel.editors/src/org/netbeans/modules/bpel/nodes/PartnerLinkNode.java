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
package org.netbeans.modules.bpel.nodes;

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.design.nodes.DiagramExtInfo;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.PartnerLinkMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.DeletePLinkAction;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nk160297
 */
public class PartnerLinkNode extends DiagramBpelNode<PartnerLink, DiagramExtInfo> {
    
    private String wsdlFile;
    
    public PartnerLinkNode(PartnerLink reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public PartnerLinkNode(PartnerLink reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.PARTNER_LINK;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        DiagramExtInfo diagramReference = getDiagramReference();
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Node.Property prop;
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        prop = PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                WSDL_FILE, "getWsdlFile", null); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        prop = PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.PARTNER_LINK_TYPE, PARTNER_LINK_TYPE,
                "getPartnerLinkType", "setPartnerLinkType", null); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        prop = PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.MY_ROLE, MY_ROLE,
                "getMyRole", "setMyRole", "removeMyRole"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        prop = PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.PARTNER_ROLE, PARTNER_ROLE,
                "getPartnerRole", "setPartnerRole", "removePartnerRole"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        if (diagramReference != null) {
//            PropertyUtils.registerProperty(mainPropertySet,
//                    DOCUMENTATION, diagramReference,
//                    "getDocumentation", "setDocumentation");
//            PropertyUtils.registerProperty(mainPropertySet,
//                    CATEGORY, diagramReference,
//                    "getCategory", "setCategory");
//            PropertyUtils.registerProperty(mainPropertySet,
//                    BACKGROUND, diagramReference,
//                    "getBackground", "setBackground");
        }
        return sheet;
    }
    
    public String getWsdlFile() {
        if (wsdlFile == null) {
            wsdlFile = calculateWsdlUri();
        }
        return wsdlFile;
    }
    
    private String calculateWsdlUri() {
        try {
            PartnerLink pLink = getReference();
            if (pLink != null) {
                WSDLReference<PartnerLinkType> pltRef = pLink.getPartnerLinkType();
                if(pltRef != null){
                    PartnerLinkType plt = pltRef.get();
                    if (plt != null) {
                        Lookup modellookup = 
                                plt.getModel().getModelSource().getLookup();
                        FileObject modelFo = 
                                (FileObject) modellookup.lookup(FileObject.class);
                        String result = ResolverUtility.calculateRelativePathName(
                                modelFo, pLink.getBpelModel());
                        return result;
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return "";
    }
    
    public Component getCustomizer() {
        return new SimpleCustomEditor<PartnerLink>(
                this, PartnerLinkMainPanel.class, EditingMode.EDIT_INSTANCE);
    }
    
//    protected String getImplShortDescription() {
//        StringBuffer result = new StringBuffer();
//        result.append(getName());
//        PartnerLink pl = getReference();
//        if (pl == null) {
//            return super.getImplShortDescription();
//        }
//        
//        WSDLReference myRoleRef = pl.getMyRole();
//        result.append(myRoleRef == null ? "" : " myRole="+myRoleRef.getRefString()); // NOI18N
//        
//        WSDLReference partnerRoleRef = pl.getPartnerRole();
//        result.append(partnerRoleRef == null ? "" : " partnerRole="+partnerRoleRef.getRefString()); // NOI18N
//        
//        return NbBundle.getMessage(PartnerLinkNode.class,
//                "LBL_PARTNER_LINK_NODE_TOOLTIP", // NOI18N
//                result.toString()
//                );
//    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.OPEN_PL_IN_EDITOR,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }

    public Action createAction(ActionType actionType) {
        switch (actionType) {
            case REMOVE: 
                return SystemAction.get(DeletePLinkAction.class);
            default: 
                return super.createAction(actionType);
        }
    }
    
}
