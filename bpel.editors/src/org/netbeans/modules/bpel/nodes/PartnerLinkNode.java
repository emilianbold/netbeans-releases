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
package org.netbeans.modules.bpel.nodes;

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
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
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.nodes.actions.DeletePLinkAction;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;

/**
 * @author nk160297
 */
public class PartnerLinkNode extends BpelNode<PartnerLink> {
    
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
    
    @Override
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Node.Property prop;
        PropertyUtils propUtil = PropertyUtils.getInstance();
        //
        propUtil.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        prop = propUtil.registerCalculatedProperty(this, mainPropertySet,
                WSDL_FILE, "getWsdlFile", null); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        prop = propUtil.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.PARTNER_LINK_TYPE, PARTNER_LINK_TYPE,
                "getPartnerLinkType", "setPartnerLinkType", null); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        prop = propUtil.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.MY_ROLE, MY_ROLE,
                "getMyRole", "setMyRole", "removeMyRole"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        prop = propUtil.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.PARTNER_ROLE, PARTNER_ROLE,
                "getPartnerRole", "setPartnerRole", "removePartnerRole"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        propUtil.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
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
            if (pLink == null) {
                return "";
            }
            WSDLReference<PartnerLinkType> pltRef = pLink.getPartnerLinkType();
            if (pltRef == null) {
                return "";
            }
            PartnerLinkType plt = pltRef.get();
            if (plt == null) {
                return "";
            }
            
            Lookup modellookup = plt.getModel().getModelSource().getLookup();
            FileObject modelFo = modellookup.lookup(FileObject.class);
            Project modelProject = Utils.safeGetProject(pLink.getBpelModel());
            String relativePath = ResolverUtility.safeGetRelativePath(modelFo, modelProject);
            
            return relativePath != null ? relativePath : modelFo.getPath();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return "";
        }
    }
    
    @Override
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new SimpleCustomEditor<PartnerLink>(
                this, PartnerLinkMainPanel.class, editingMode);
    }
    
//    protected String getImplShortDescription() {
//        PartnerLink pl = getReference();
//        if (pl == null) {
//            return super.getImplShortDescription();
//        }
//        
//        StringBuffer result = new StringBuffer();
//        WSDLReference myRoleRef = pl.getMyRole();
//        result.append(myRoleRef == null 
//                ? EMPTY_STRING 
//                : NbBundle.getMessage(
//                    BpelNode.class,
//                    "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
//                    PartnerLink.MY_ROLE, 
//                    myRoleRef.getRefString()
//                    )
//                ); 
//        
//        WSDLReference partnerRoleRef = pl.getPartnerRole();
//        result.append(partnerRoleRef == null 
//                ? EMPTY_STRING 
//                : NbBundle.getMessage(
//                    BpelNode.class,
//                    "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
//                    PartnerLink.PARTNER_ROLE, 
//                    partnerRoleRef.getRefString()
//                    )
//                ); 
//
//        return NbBundle.getMessage(BpelNode.class,
//                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", // NOI18N
//                getNodeType().getDisplayName(), 
//                getName(),
//                result.toString()
//                ); 
//    }

    @Override
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO,
            ActionType.GO_TO_REFERENCE,
            ActionType.SEPARATOR,
            ActionType.FIND_USAGES,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.OPEN_PL_IN_EDITOR,
            ActionType.UPDATE_WEB_SERVICE,
            ActionType.OPEN_WEB_SERVICE_MODULE,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }

    @Override
    public Action createAction(ActionType actionType) {
        switch (actionType) {
            case REMOVE: 
                return SystemAction.get(DeletePLinkAction.class);
            default: 
                return super.createAction(actionType);
        }
    }
    
}
