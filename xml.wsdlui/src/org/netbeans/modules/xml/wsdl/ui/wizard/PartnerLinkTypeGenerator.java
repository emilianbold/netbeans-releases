/*
 * PartnerLinkTypeGenerator.java
 *
 * Created on September 13, 2006, 11:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.util.HashMap;
import java.util.Map;
import javax.swing.text.AbstractDocument;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radval
 */
public class PartnerLinkTypeGenerator implements Command {
    
    private PortType mPortType;
    
    private WSDLModel mModel;
    
    private ExtensibilityElement mPartnerLinkTypeElement = null;
            
    private static final QName partnerLinkTypeQName = new QName("http://schemas.xmlsoap.org/ws/2004/03/partner-link/", "partnerLinkType", "plink"); //NOI18N
    private static final QName partnerLinkTypeRoleQName = new QName("http://schemas.xmlsoap.org/ws/2004/03/partner-link/", "role", "plink"); //NOI18N
            
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
