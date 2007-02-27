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

package org.netbeans.modules.identity.profile.ui.editor;

import javax.swing.JComponent;
import java.io.File;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.identity.profile.api.bridgeapi.SunDDBridge;
import org.netbeans.modules.identity.profile.ui.SecurityView;
import org.netbeans.modules.identity.profile.ui.WSCSecurityView;
import org.netbeans.modules.identity.profile.ui.WSPSecurityView;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.nodes.Node;
import org.netbeans.modules.identity.profile.ui.MessagePanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    
    /** Creates a new instance of SecurityWSEditor */
    public SecurityWSEditor() {
    }
    
    public JComponent createWSEditorComponent(Node node,
            JaxWsModel jaxWsModel) {
        helper = new J2eeProjectHelper(node, jaxWsModel);
        
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
            
            if (helper.providerExists())
                iPanel = displaySunDDAlert();
            
            if (iPanel == null)
                iPanel = setUpSecurityPanel();
        } else {
            iPanel = new MessagePanel(NbBundle.getMessage(
                    SecurityWSEditor.class, "LBL_UnsupportedProject")); //NOI18N
        }
        return iPanel;
    }
    
    public String getTitle() {
        return NbBundle.getMessage(SecurityWSEditor.class,
                "LBL_WSSecurityProviderTitle"); //NOI18N
    }
    
    public void save(Node node, JaxWsModel jaxWsModel) {
        if (iPanel instanceof SecurityView) {
            ((SecurityView) iPanel).save();
        }
    }
    
    public void cancel(Node node, JaxWsModel jaxWsModel) {
        if (iPanel instanceof SecurityView) {
            ((SecurityView) iPanel).cancel();
        }
    }
    
    private JPanel displaySunDDAlert() {
        boolean isServer = helper.isServer();
        JPanel p = null;
        String providerName = null;
        File sunDD = helper.getSunDD();
        if (isServer) {
            String pcName = helper.getPortComponentName();
            String descName = helper.getServiceDescriptionName();
            providerName = SunDDBridge.getEndPointProvider(sunDD, descName, pcName);
        } else {
            List<String> refNames = helper.getAllServiceRefNames();
            WsdlData wsdlData = helper.getWsdlData().get(0);
            String namespace = null;
            String localPart = null;
            if (wsdlData != null) {
                namespace = wsdlData.getTargetNameSpace();
                localPart = wsdlData.getPort();
            }
            providerName = SunDDBridge.getSvcRefProvider(sunDD,
                    (String)refNames.get(0), namespace, localPart);
        }
        String message = NbBundle.getMessage(SecurityWSEditor.class,
                "LBL_AuthNProviderExists") + " " + providerName + "." + " " + //NOI18N
                NbBundle.getMessage(SecurityWSEditor.class,
                "LBL_AuthNProviderOverwrite"); //NOI18N
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                message, NbBundle.getMessage(
                SecurityWSEditor.class, "LBL_NotifyTitle"), //NOI18N
                NotifyDescriptor.YES_NO_OPTION);
        DialogDisplayer.getDefault().notify(d);
        if (d.getValue() == NotifyDescriptor.NO_OPTION) {
            p = new MessagePanel(NbBundle.getMessage(
                    SecurityWSEditor.class, "LBL_AuthNProviderExists") + //NOI18N
                    providerName);
        }
        return p;
    }
    
    private JPanel setUpSecurityPanel() {
        helper.closeSunDDEditor();
        
        if (helper.isServer()) {
            return new WSPSecurityView(helper);
        } else {
            if (helper.closeSunDDEditor()) {
                return new WSCSecurityView(helper);
            } else {
                return  new MessagePanel(NbBundle.getMessage(SecurityWSEditor.class,
                        "LBL_SunDDEditorOpen"));
            }
        }
    }
}
