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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author radval
 */
public class PartnerLinkTypeGenerator implements Command {
    
    private PortType mPortType;
    
    private WSDLModel mModel;
    
    private ExtensibilityElement mPartnerLinkTypeElement = null;
            
    private static final QName partnerLinkTypeQName = BPELQName.PARTNER_LINK_TYPE.getQName();
    private static final QName partnerLinkTypeRoleQName = BPELQName.ROLE.getQName();
            
    /** Creates a new instance of PartnerLinkTypeGenerator */
    public PartnerLinkTypeGenerator(PortType portType, WSDLModel model) {
        this.mPortType = portType;
        this.mModel = model;
    }
    
    public ExtensibilityElement getPartnerLinkType() {
        return mPartnerLinkTypeElement;
    }
    
    public void execute() {
        String portTypeName = this.mPortType.getName();
        String wsdlDefinitionName = this.mModel.getDefinitions().getName();
        
        if(portTypeName != null && wsdlDefinitionName != null) {
            String portTypeNamespace = this.mPortType.getModel().getDefinitions().getTargetNamespace();
            if(portTypeNamespace != null) {
                String prefix = ((AbstractDocumentComponent) this.mModel.getDefinitions()).lookupPrefix(portTypeNamespace);
                if(prefix != null) {
                    ExtensibilityElement partnerLinkType = (ExtensibilityElement) this.mModel.getFactory().create(this.mModel.getDefinitions(), partnerLinkTypeQName);
                    ExtensibilityElement partnerLinkTypeRole = (ExtensibilityElement) this.mModel.getFactory().create(partnerLinkType, partnerLinkTypeRoleQName);
                    partnerLinkType.addExtensibilityElement(partnerLinkTypeRole);
                    
                    String partnerLinkTypeName = NameGenerator.getInstance().generateUniquePartnerLinkType(wsdlDefinitionName, partnerLinkTypeQName, this.mModel);
                    if(partnerLinkTypeName != null) {
                        partnerLinkType.setAttribute("name", partnerLinkTypeName);    //NOI18N
                        partnerLinkTypeRole.setAttribute("name",  portTypeName + "Role"); //NOI18N
                        partnerLinkTypeRole.setAttribute("portType", prefix + ":" + portTypeName); //NOI18N
                        mPartnerLinkTypeElement = partnerLinkType;
                    }
                }
            }
        }
        
        
    }
    
    
    
}
