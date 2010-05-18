/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.identity.profile.ui.editor;

import javax.swing.JComponent;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.identity.profile.ui.SecurityView;
import org.netbeans.modules.identity.profile.ui.WSCSecurityView;
import org.netbeans.modules.identity.profile.ui.WSPSecurityView;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.nodes.Node;
import org.netbeans.modules.identity.profile.ui.MessagePanel;
import org.openide.util.NbBundle;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper;
import org.netbeans.modules.identity.profile.ui.support.WsdlData;

/**
 * Implements the Security editor for the web service attribute editor.
 *
 * Created on April 10, 2006, 5:53 PM
 *
 * @author Vidhya Narayanan
 */
public class SecurityWSEditor implements WSEditor {
    private J2eeProjectHelper helper;
    private JPanel iPanel;
    private JaxWsModel jaxWsModel;
    
    /** Creates a new instance of SecurityWSEditor */
    public SecurityWSEditor(JaxWsModel jaxWsModel) {
        this.jaxWsModel = jaxWsModel;
    }
    
    public JComponent createWSEditorComponent(Node node) {
        helper = J2eeProjectHelper.newInstance(node, jaxWsModel);
   
        if (!helper.isAppServerSun()) {
            iPanel = new MessagePanel(NbBundle.getMessage(
                    SecurityWSEditor.class, "LBL_AppSrvNotSun")); //NOI18N
            return iPanel;
        }
        
        if (helper.isSecurable()) {
            // Refresh the am-deploy.xml if necessary.
            helper.refreshBuildScript();
            
            // Check to see if we can parse the wsdl for the client
            List<WsdlData> wsdlData = helper.getWsdlData();
            if (!helper.isServer() && (wsdlData != null)) {
                for (WsdlData w : wsdlData) {
                    if (w == null || !w.isValid()) {
                        iPanel = new MessagePanel(NbBundle.getMessage(
                                SecurityWSEditor.class, "LBL_CannotParseWSDL")); //NOI18N
                        return iPanel;
                    }
                }
            }
            
            //if (helper.providerExists())
            //   iPanel = displaySunDDAlert();
            
            if (iPanel == null)
                iPanel = setUpSecurityPanel();
        } else {
            String msg = null;
            
            if (helper.noServiceRefExists()) {
                msg = NbBundle.getMessage(SecurityWSEditor.class, "LBL_NoServiceRefs");
            } else {
                msg = NbBundle.getMessage(SecurityWSEditor.class, "LBL_UnsupportedProject");
            }
            
            iPanel = new MessagePanel(msg);
        }
        return iPanel;
    }
    
    public String getTitle() {
        return NbBundle.getMessage(SecurityWSEditor.class,
                "LBL_WSSecurityProviderTitle"); //NOI18N
    }
    
    public void save(Node node) {
        if (iPanel instanceof SecurityView) {
            ((SecurityView) iPanel).save();
        }
    }
    
    public void cancel(Node node) {
        if (iPanel instanceof SecurityView) {
            ((SecurityView) iPanel).cancel();
        }
    }
    
    //    private JPanel displaySunDDAlert() {
    //        boolean isServer = helper.isServer();
    //        JPanel p = null;
    //        String providerName = null;
    //        File sunDD = helper.getSunDD();
    //        if (isServer) {
    //            String pcName = helper.getPortComponentName();
    //            String descName = helper.getServiceDescriptionName();
    //            providerName = SunDDBridge.getEndPointProvider(sunDD, descName, pcName);
    //        } else {
    //            List<String> refNames = helper.getAllServiceRefNames();
    //            WsdlData wsdlData = helper.getWsdlData().get(0);
    //            String namespace = null;
    //            String localPart = null;
    //            if (wsdlData != null) {
    //                namespace = wsdlData.getTargetNameSpace();
    //                localPart = wsdlData.getPort();
    //            }
    //            providerName = SunDDBridge.getSvcRefProvider(sunDD,
    //                    (String)refNames.get(0), namespace, localPart);
    //        }
    //        String message = NbBundle.getMessage(SecurityWSEditor.class,
    //                "LBL_AuthNProviderExists") + " " + providerName + "." + " " + //NOI18N
    //                NbBundle.getMessage(SecurityWSEditor.class,
    //                "LBL_AuthNProviderOverwrite"); //NOI18N
    //        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
    //                message, NbBundle.getMessage(
    //                SecurityWSEditor.class, "LBL_NotifyTitle"), //NOI18N
    //                NotifyDescriptor.YES_NO_OPTION);
    //        DialogDisplayer.getDefault().notify(d);
    //        if (d.getValue() == NotifyDescriptor.NO_OPTION) {
    //            p = new MessagePanel(NbBundle.getMessage(
    //                    SecurityWSEditor.class, "LBL_AuthNProviderExists") + //NOI18N
    //                    providerName);
    //        }
    //        return p;
    //    }
    
    private JPanel setUpSecurityPanel() {
        if (helper.isServer()) {
            return new WSPSecurityView(helper);
        } else {
            return new WSCSecurityView(helper);
        }
    }
    
    public String getDescription() {
        return NbBundle.getMessage(SecurityWSEditor.class, "AM_SECURITY_DESC");
    }
}
