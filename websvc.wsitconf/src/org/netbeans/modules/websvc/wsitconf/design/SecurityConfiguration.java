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

package org.netbeans.modules.websvc.wsitconf.design;

import java.awt.Component;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Grebac
 */
public class SecurityConfiguration  implements WSConfiguration {
  
    private Service service;
    private FileObject implementationFile;
    private Project project;
    
    private Collection<FileObject> createdFiles = new LinkedList();
    
    /** Creates a new instance of WSITWsConfiguration */

    public SecurityConfiguration(Service service, FileObject implementationFile) {
        this.service = service;
        this.implementationFile = implementationFile;
        this.project = FileOwnerQuery.getOwner(implementationFile);
    }
    
    public Component getComponent() {
        return null;
    }

    public String getDescription() {
        return "WSIT Configuration";
    }

    public Icon getIcon() {
        return new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/websvc/wsitconf/resources/designer-security.gif"));
    }

    public String getDisplayName() {
        return "WSIT Configuration";
    }
  
    public void set() {
        Binding binding = getBinding();
        if (binding == null) return;
        if (!(SecurityPolicyModelHelper.isSecurityEnabled(binding))) {
            
            // default profile with the easiest setup
            ProfilesModelHelper.setSecurityProfile(binding, ComboConstants.PROF_MUTUALCERT);
            
            // enable secure conversation by default - better performance, and no need to hassle with RM set/unset
            ProfilesModelHelper.enableSecureConversation(binding, true, ComboConstants.PROF_MUTUALCERT);
            
            // setup default keystore values
            ProprietarySecurityPolicyModelHelper.setStoreLocation(binding, Util.getServerStoreLocation(project, false), false, false);
            ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(binding, "xws-security-server", false); //NOI18N
            ProprietarySecurityPolicyModelHelper.setStorePassword(binding, "changeit", false, false); //NOI18N

            // setup default truststore values
            ProprietarySecurityPolicyModelHelper.setStoreLocation(binding, Util.getServerStoreLocation(project, true), false, false);
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(binding, "xws-security-client", false); //NOI18N
            ProprietarySecurityPolicyModelHelper.setStorePassword(binding, "changeit", true, false); //NOI18N
        }
    }

    public void unset() {
        Binding binding = getBinding();
        if (binding == null) return;
        if (SecurityPolicyModelHelper.isSecurityEnabled(binding)) {
            SecurityPolicyModelHelper.disableSecurity(binding, true);
        }
    }

    //TODO: Need a way to determine binding that the user wants
    //For now just get the first one (if there is one)
    private Binding getBinding() {
        WSDLModel model = WSITModelSupport.getModelForService(service, implementationFile, project, true, createdFiles);
        Definitions definitions = model.getDefinitions();
        Collection<Binding> bindings = definitions.getBindings();
        if(bindings.size() > 0){
            return bindings.iterator().next();
        }
        return null;
    }    


}
