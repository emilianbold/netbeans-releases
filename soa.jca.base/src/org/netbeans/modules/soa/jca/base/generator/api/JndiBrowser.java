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

package org.netbeans.modules.soa.jca.base.generator.api;

import org.netbeans.modules.soa.jca.base.generator.impl.JndiBrowserPanel;
import java.io.InputStream;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
//import org.netbeans.spi.server.ServerInstance;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author echou
 */
public class JndiBrowser {

    public enum Category { CONNECTOR_RESOURCE, ADMIN_OBJECT }

    // pop up a Glassfish server browser
    public static String popupJndiBrowserDialog(Project project, Category browseCategory) {
        // pop up a Glassfish server browser
        try {
            String serverInstanceDisplayName = null;
            FileObject projectProp = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            if (projectProp != null) {
                EditableProperties ep = new EditableProperties();
                InputStream is = projectProp.getInputStream();
                try {
                    ep.load(is);
                } finally {
                    is.close();
                }
                String servInstID = ep.getProperty("j2ee.server.instance"); // NOI18N
                if (servInstID != null && servInstID.length() > 0) {
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                    if (platform != null) {
                        serverInstanceDisplayName = platform.getDisplayName();
                    }
                }
            }
            if (serverInstanceDisplayName == null || serverInstanceDisplayName.equals("")) {
                NotifyDescriptor d = new NotifyDescriptor.Message(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/generator/api/Bundle").getString("Unable_to_determine_server_instance"));
                DialogDisplayer.getDefault().notify(d);
                return null;
            }

            Node serverNode = null;
            ServerRegistry serverRegistry = ServerRegistry.getInstance();
            for (ServerInstanceProvider instanceProvider : serverRegistry.getProviders()) {
                for (ServerInstance instance : instanceProvider.getInstances()) {
                    if (instance.getDisplayName().equals(serverInstanceDisplayName)) {
                        serverNode = instance.getFullNode();
                    }
                }
            }
            if (serverNode == null) {
                NotifyDescriptor d = new NotifyDescriptor.Message(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/generator/api/Bundle").getString("Unable_to_locate_Server_Node"));
                DialogDisplayer.getDefault().notify(d);
                return null;
            }

            final JndiBrowserPanel browserPanel = new JndiBrowserPanel(serverNode, serverInstanceDisplayName, browseCategory);

            final DialogDescriptor d = new DialogDescriptor(
                    browserPanel,  // innerPane
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/generator/api/Bundle").getString("Choose_JNDI_name_of_Connector_Resource"),  // title
                    true,  // modal
                    NotifyDescriptor.OK_CANCEL_OPTION,  // optionType
                    NotifyDescriptor.OK_OPTION,  // initialValue
                    DialogDescriptor.BOTTOM_ALIGN,  // align
                    HelpCtx.DEFAULT_HELP,  // helpctx
                    null  // actionListener
                    );

            browserPanel.setDialogDescriptor(d);

            DialogDisplayer.getDefault().notify(d);
            if (d.getValue() == NotifyDescriptor.OK_OPTION) {
                Node node = browserPanel.getSelectedNodes()[0];
                return node.getDisplayName();
            }

        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
            return null;
        }

        return null;
    }

    static class ServerRegistry {

        public static final String SERVERS_PATH = "Servers"; // NOI18N

        private static ServerRegistry registry;

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private final Lookup.Result<ServerInstanceProvider> result;

        private ServerRegistry() {
            Lookup lookup = Lookups.forPath(SERVERS_PATH);
            result = lookup.lookupResult(ServerInstanceProvider.class);
        }

        public static synchronized ServerRegistry getInstance() {
            if (registry == null) {
                registry = new ServerRegistry();
                registry.result.addLookupListener(new ProviderLookupListener(registry.changeSupport));
            }
            return registry;
        }

        public Collection<? extends ServerInstanceProvider> getProviders() {
            return result.allInstances();
        }

        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        private static class ProviderLookupListener implements LookupListener {

            private final ChangeSupport changeSupport;

            public ProviderLookupListener(ChangeSupport changeSupport) {
                this.changeSupport = changeSupport;
            }

            public void resultChanged(LookupEvent ev) {
                changeSupport.fireChange();
            }

        }
    }
}
