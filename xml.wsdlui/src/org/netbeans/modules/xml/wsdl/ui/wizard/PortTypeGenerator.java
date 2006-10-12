/*
 * PortTypeGenerator.java
 *
 * Created on September 6, 2006, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

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
 * @author radval
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
            String portTypeName = (String) this.mConfigurationMap.get(WizardPortTypeConfigurationStep.PORTTYPE_NAME);
            if (portTypeName == null) return;
            
            this.mPortType = mModel.getFactory().createPortType();
            this.mPortType.setName(portTypeName);
            mModel.getDefinitions().addPortType(this.mPortType);
            
            OperationGenerator og = new OperationGenerator(this.mModel, this.mPortType, this.mConfigurationMap);
            og.execute();
            this.mOperation = og.getOperation();
            this.mNewMessageList = og.getNewMessages();
            mImports.addAll(og.getImports());
                    
            //automatically generate a partnerLinkType
            PartnerLinkTypeGenerator pltGen = new PartnerLinkTypeGenerator(this.mPortType, this.mModel);
            pltGen.execute();
            mPartnerLinkTypeElement = pltGen.getPartnerLinkType();
            if(mPartnerLinkTypeElement != null) {
                this.mModel.getDefinitions().addExtensibilityElement(mPartnerLinkTypeElement);
                List<WSDLComponent> children = mPartnerLinkTypeElement.getChildren();
                if(children != null && children.size() > 0) {
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
