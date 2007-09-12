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
        PartnerLink[] pLinks = bpelModel.getProcess().getPartnerLinkContainer().getPartnerLinks();
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
                        }
                    }
                }
            


            Role pLTypeForPLinkTypeRole2 = pLTypeForPLinkType.getRole2();
            if (pLTypeForPLinkTypeRole2 != null) {
                NamedComponentReference nmrRole2 = pLTypeForPLinkTypeRole2.getPortType();
                if (nmrRole2 != null ) {
                    QName portTypeQName2 = nmrRole2.getQName();
                    if (portTypeQName2 != null) {
                    }
                }
            }                     
            }
            WSDLReference partnerMyRoleWSDLRef = pLinks[index].getMyRole();
            partnerMyRoleWSDLRef = pLinks[index].getPartnerRole();
            if (partnerMyRoleWSDLRef != null) {
                QName qName = partnerMyRoleWSDLRef.getQName();
                if (qName != null) {
                }
            }
        }
    }
}
