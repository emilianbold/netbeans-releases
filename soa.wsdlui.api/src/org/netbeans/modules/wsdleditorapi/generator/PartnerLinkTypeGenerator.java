/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.wsdleditorapi.generator;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author mbhasin
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
                    
                    String partnerLinkTypeName = NameGenerator.generateUniquePartnerLinkType(wsdlDefinitionName, partnerLinkTypeQName, this.mModel);
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
