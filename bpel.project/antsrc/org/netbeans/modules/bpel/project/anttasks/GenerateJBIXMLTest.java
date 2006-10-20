package org.netbeans.modules.bpel.project.anttasks;

import javax.xml.namespace.QName;
import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;

public class GenerateJBIXMLTest {
    private ClassLoader m_contextClassLoader = null;
    private BPELCatalogModel m_bpelCtlgModel = null;
    public GenerateJBIXMLTest() {
    }
    
    public void setContextClassLoader(ClassLoader cl) {
        m_contextClassLoader = cl;
    }
    
    public void setBPELCatalogModel(Object bpelCtlgMdl ) {
        m_bpelCtlgModel =(BPELCatalogModel) bpelCtlgMdl;
        m_bpelCtlgModel = m_bpelCtlgModel.getDefault();
    }
    
    public void execute() {
        System.setProperty("org.openide.util.Lookup","org.netbeans.modules.bpel.project.anttasks.MyLookup" );
        File sourceFile = new File("C:/Documents and Settings/sgenipudi/BpelModule7/src/echo_1.bpel");
        Thread.currentThread().setContextClassLoader(m_contextClassLoader);

        BpelModel bpelModel = null;
        try  {
             bpelModel = BPELCatalogModel.getDefault().getBPELModel(sourceFile.toURI());
        }catch (Exception ex) {
            throw new BuildException("Creation of Bpel model failed!", ex);
        }
        System.out.println("Gettng partner links!");
        PartnerLink[] pLinks = bpelModel.getProcess().getPartnerLinkContainer().getPartnerLinks();
        System.out.println("Got partner links!");
        for (int index =0; index < pLinks.length; index++) {
            String partnerLinkName = pLinks[index].getName();
            WSDLReference partnerLinkTypeWSDLRef = pLinks[index].getPartnerLinkType();
            QName partnerLinkQName = pLinks[index].getPartnerLinkType().getQName();
            String partnerLinkQNameNSPrefix = partnerLinkQName.getPrefix();
            String partnerLinkQNameLocalPart = partnerLinkQName.getLocalPart();
            String partnerLinkNameSpaceURI = partnerLinkQName.getNamespaceURI();
            PartnerLinkType pLTypeForPLinkType = (PartnerLinkType)partnerLinkTypeWSDLRef.get();
            if (pLTypeForPLinkType != null) {                 
                Role pLTypeForPLinkTypeRole1 = pLTypeForPLinkType.getRole1();
                if (pLTypeForPLinkTypeRole1 != null) {
                    NamedComponentReference nmrRole1 = pLTypeForPLinkTypeRole1.getPortType();
                    if (nmrRole1 != null ) {
                        QName portTypeQName1 = nmrRole1.getQName();
                        if (portTypeQName1 != null) {
                            System.out.println("Port Type1 = "+portTypeQName1.getLocalPart()+ " Prefix = "+portTypeQName1.getPrefix()+ "Port Type1 = "+portTypeQName1.getNamespaceURI());
                        }
                    }
                }
            


            Role pLTypeForPLinkTypeRole2 = pLTypeForPLinkType.getRole2();
            if (pLTypeForPLinkTypeRole2 != null) {
                NamedComponentReference nmrRole2 = pLTypeForPLinkTypeRole2.getPortType();
                if (nmrRole2 != null ) {
                    QName portTypeQName2 = nmrRole2.getQName();
                    if (portTypeQName2 != null) {
                        System.out.println("Port Type2 = "+portTypeQName2.getLocalPart()+ " Prefix = "+portTypeQName2.getPrefix()+ "Port Type = "+portTypeQName2.getNamespaceURI());
                    }
                }
            }                     
            }
             System.out.println("Partner Link Name = "+partnerLinkQNameLocalPart+ " Prefix = "+partnerLinkQNameNSPrefix + "partnerLinkQNameLocalPart = "+partnerLinkQNameLocalPart +" partnerLinkNameSpaceURI = "+partnerLinkNameSpaceURI);
            WSDLReference partnerMyRoleWSDLRef = pLinks[index].getMyRole();
            System.out.println(" My ROLE = "+partnerMyRoleWSDLRef.getQName().getLocalPart()+" Prefix = "+partnerMyRoleWSDLRef.getQName().getPrefix()+" Namepsace URI "+partnerMyRoleWSDLRef.getQName().getNamespaceURI());
            partnerMyRoleWSDLRef = pLinks[index].getPartnerRole();
            if (partnerMyRoleWSDLRef != null) {
                QName qName = partnerMyRoleWSDLRef.getQName();
                if (qName != null) {
                System.out.println(" Partner ROLE = "+partnerMyRoleWSDLRef.getQName().getLocalPart()+" Prefix = "+partnerMyRoleWSDLRef.getQName().getPrefix()+" Namepsace URI "+partnerMyRoleWSDLRef.getQName().getNamespaceURI());
                }
            }

        }

    }
}
