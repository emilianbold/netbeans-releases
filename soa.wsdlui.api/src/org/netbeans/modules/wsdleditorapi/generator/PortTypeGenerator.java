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

/*
 * PortTypeGenerator.java
 *
 * Created on September 6, 2006, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdleditorapi.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.util.NbBundle;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;


/**
 *
 * @author mbhasin
 */
public class PortTypeGenerator implements Command {
    
    private WSDLModel mModel;
    
    private PortType mPortType;
    
    private Operation mOperation;
    
    private Map mConfigurationMap;
    
    private List<Message> mNewMessageList = new ArrayList<Message>();
    
    private Collection<Import> mImports = new ArrayList<Import>();
    
    private ExtensibilityElement mPartnerLinkTypeElement = null;
    
    private Comment mComment;
    
    /** Creates a new instance of PortTypeGenerator */
    public PortTypeGenerator(WSDLModel model, Map configurationMap) {
        this.mModel = model;
        this.mConfigurationMap = configurationMap;
    }
    
    public PortType getPortType() {
        return this.mPortType;
    }
    
    public Operation getOperation() {
        return this.mOperation;
    }
    
    public List<Message> getNewMessages() {
        return this.mNewMessageList;
    }
    
    
    public Collection<Import> getImports() {
        return this.mImports;
    }
    public ExtensibilityElement getPartnerLinkType() {
        return mPartnerLinkTypeElement;
    }
    
    public Comment getComment() {
        return this.mComment;
    }
    
    public void execute() {
        if(mModel != null) {
            //portType
            String portTypeName = (String) this.mConfigurationMap.get(WSDLWizardConstants.PORTTYPE_NAME);
            if (portTypeName == null) return;
            
            this.mPortType = mModel.getFactory().createPortType();
            this.mPortType.setName(portTypeName);
            mModel.getDefinitions().addPortType(this.mPortType);
            
            OperationGenerator og = new OperationGenerator(this.mModel, this.mPortType, this.mConfigurationMap);
            og.execute();
            this.mOperation = og.getOperation();
            this.mNewMessageList = og.getNewMessages();
            mImports.addAll(og.getImports());
            
            
            Boolean autoGenPLT = (Boolean) mConfigurationMap.get(WSDLWizardConstants.AUTOGEN_PARTNERLINKTYPE);
            if (autoGenPLT != null && !autoGenPLT) {
                return;
            }
            //automatically generate a partnerLinkType
            PartnerLinkTypeGenerator pltGen = new PartnerLinkTypeGenerator(this.mPortType, this.mModel);
            pltGen.execute();
            mPartnerLinkTypeElement = pltGen.getPartnerLinkType();
            if (mPartnerLinkTypeElement != null) {
                this.mModel.getDefinitions().addExtensibilityElement(mPartnerLinkTypeElement);
                List<WSDLComponent> children = mPartnerLinkTypeElement.getChildren();
                if (children != null && children.size() > 0) {
                    WSDLComponent role = children.get(0);
                    Element pltElement = mPartnerLinkTypeElement.getPeer();
                    Element roleElement = role.getPeer();
                    this.mComment = this.mModel.getAccess().getDocumentRoot().createComment(NbBundle.getMessage(PortTypeGenerator.class, "LBL_partnerLinkType_comment"));
                    this.mModel.getAccess().insertBefore(pltElement, this.mComment, roleElement, (AbstractDocumentComponent) this.mPartnerLinkTypeElement);
                }
            }
        }
    }
}
