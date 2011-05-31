/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class OneServerNode extends AbstractNode {

    private static Logger logger = Logger.getLogger(OneServerNode.class.getCanonicalName());
    private static ResourceBundle bundle = NbBundle.getBundle(OneServerNode.class);
    private ChangeListener listener;
    private Properties serverProperties = null;
    private ServerConfig serverConfig = null;
    private String displayIconName = bundle.getString("ICON_OneServerNodeStop");
    private CoherenceServer server = null;
    private static final List<CommandlineProperty> commandlineProperties = new ArrayList<CommandlineProperty>();
    private String propertiesFilename = null;

    static {
        commandlineProperties.add(new CommandlineProperty("Coherence Classpath", "Classpath for coherence.jar etc", String.class));
        commandlineProperties.add(new CommandlineProperty("Additional Classpath", "Classpath containing your project jars", String.class));
        commandlineProperties.add(new CommandlineProperty("Java Flags", "Additional Java command line properties that will appear at the start of the command line but after the \"java\" statement. For example \n"
                + "-Xms128m\n"
                + "-Xmx1024m\n", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.cacheconfig", "Cache configuration descriptor filename", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.cluster", "Cluster name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.clusteraddress", "Cluster (multicast) IP address", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.clusterport", "Cluster (multicast) IP port", Long.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.distributed.backup", "Data backup storage location", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.distributed.backupcount", "Number of data backups", Integer.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.distributed.localstorage", "Local partition management enabled", Boolean.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.distributed.threads", "Thread pool size", Integer.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.distributed.transfer", "Partition transfer threshold", Long.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.edition", "Product edition", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.invocation.threads", "Invocation service thread pool size", Integer.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.localhost", "Unicast IP address", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.localport", "Unicast IP port", Long.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.localport.adjust", "Unicast IP port auto assignment", Boolean.class, "true"));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.log", "Logging destination", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.log.level", "Logging level", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.log.limit", "Log output character limit", Long.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.machine", "Machine name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.management", "JMX management mode", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.management.readonly", "JMX management read-only flag", Boolean.class, "false"));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.management.remote", "Remote JMX management enabled flag", Boolean.class, "false"));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.member", "Member name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.mode", "Operational mode", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.override", "Deployment configuration override filename", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.priority", "Priority", Integer.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.process", "Process name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.proxy.threads", "Coherence*Extend service thread pool size", Integer.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.rack", "Rack name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.role", "Role name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.security", "Cache access security enabled flag", Boolean.class, "false"));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.security.keystore", "Security access controller keystore file name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.security.password", "Keystore or cluster encryption password", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.security.permissions", "Security access controller permissions file name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.shutdownhook", "Shutdown listener action", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.site", "Site name", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.tcmp.enabled", "TCMP enabled flag", Boolean.class, "true"));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.tcpring", "TCP Ring enabled flag", Boolean.class, "false"));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.ttl", "Multicast packet time to live (TTL)", Long.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.wka", "Well known IP address", String.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.coherence.wka.port", "Well known IP port", Long.class));
        commandlineProperties.add(new CommandlineProperty("tangosol.pof.enabled", "Enable POF Serialization", Boolean.class, "false"));
        commandlineProperties.add(new CommandlineProperty("tangosol.pof.config", "Configuration file containing POF Serialization class information", String.class));
        commandlineProperties.add(new CommandlineProperty("Custom Properties", "User defined additional command line properties that will appear at the end of the commandline. For example \n"
                + "-Dcom.sun.management.jmx.remote\n"
                + "-Dcom.sun.management.jmxremote.port=5678\n"
                + "-Dcom.sun.management.jmxremote.authenticate=false\n"
                + "-Dcom.sun.management.jmxremote.ssl=false\n", String.class));
    }

    public OneServerNode(ServerConfig key) {
        super(Children.LEAF);
        this.serverConfig = key;
        this.serverProperties = key.getProperties();
        this.propertiesFilename = serverProperties.getProperty(ServerPropertyFileManager.FILENAME_KEY);
        setIconBaseWithExtension(displayIconName);
        super.setName(serverProperties.getProperty(ServerPropertyFileManager.SERVERNAME_KEY));
        setShortDescription(bundle.getString("HINT_OneServerNode"));
    }

    public Action[] getActions(boolean context) {
        Action startStopAction = null;
        Action cloneAction = new CloneAction();
        Action resetAction = new ResetAction();
        Action[] result = null;

        if (server != null && server.isRunning()) {
            startStopAction = new StopServerAction();

            result = new Action[]{
                        startStopAction,
                        null,
                        cloneAction,
                        null,
                        SystemAction.get(ToolsAction.class),
                        SystemAction.get(PropertiesAction.class),};
        } else {
            if (displayIconName.equals(bundle.getString("ICON_OneServerNodeStart"))) {
                displayIconName = bundle.getString("ICON_OneServerNodeStop");
                setIconBaseWithExtension(displayIconName);
            }
            startStopAction = new StartServerAction();

            result = new Action[]{
                        startStopAction,
                        null,
                        cloneAction,
                        SystemAction.get(DeleteAction.class),
                        null,
                        SystemAction.get(ToolsAction.class),
                        SystemAction.get(PropertiesAction.class),
                        resetAction
                    };
        }

        return result;
    }

    private class StartServerAction extends AbstractAction {

        public StartServerAction() {
            super(bundle.getString("ACTION_StartServer"));
        }

        public void actionPerformed(ActionEvent evt) {
            if (server == null) {
                server = new CoherenceServer(serverProperties);
            }
            // Start Server
            server.start();

            if (server.isRunning()) {
                displayIconName = bundle.getString("ICON_OneServerNodeStart");
            } else {
                displayIconName = bundle.getString("ICON_OneServerNodeStop");
            }
            setIconBaseWithExtension(displayIconName);
        }
    }

    private class StopServerAction extends AbstractAction {

        public StopServerAction() {
            super(bundle.getString("ACTION_StopServer"));
        }

        public void actionPerformed(ActionEvent evt) {
            if (server == null) {
                server = new CoherenceServer(serverProperties);
            }
            // Start Server
            server.stop();

            if (server.isRunning()) {
                displayIconName = bundle.getString("ICON_OneServerNodeStart");
            } else {
                displayIconName = bundle.getString("ICON_OneServerNodeStop");
            }
            setIconBaseWithExtension(displayIconName);
        }
    }

    private class ResetAction extends AbstractAction {

        public ResetAction() {
            super(bundle.getString("ACTION_ResetServer"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
//                String name = serverProperties.getProperty(ServerPropertyFileManager.SERVERNAME_KEY);
//                String filename = serverProperties.getProperty(ServerPropertyFileManager.FILENAME_KEY);
//                String ccp = serverProperties.getProperty("coherence.classpath");

                /*
                 * We only want to reset the Coherence properties, i.e. those
                 * that start with tangosol.
                 */
                Properties resetProperties = new Properties();
                Enumeration propEnum = serverProperties.propertyNames();
                String key = null;
                while (propEnum.hasMoreElements()) {
                    key = propEnum.nextElement().toString();
                    if (!key.startsWith("tangosol.")) {
                        resetProperties.put(key, serverProperties.get(key));
                    }
                }

//                resetProperties.setProperty(ServerPropertyFileManager.SERVERNAME_KEY, name);
//                resetProperties.setProperty(ServerPropertyFileManager.FILENAME_KEY, filename);
//                if (ccp != null)resetProperties.setProperty("coherence.classpath", ccp);

                serverProperties = resetProperties;
                ServerPropertyFileManager.saveProperties(serverProperties);
                AllServersNotifier.changed();
                firePropertySetsChange(null, getPropertySets());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private class CloneAction extends AbstractAction {

        public CloneAction() {
            super(bundle.getString("ACTION_CloneServer"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String title = bundle.getString("LBL_CloneServerDialogTitle");
                String msg = bundle.getString("MSG_CloneServerDialogMsg");


//                Properties cloneProperties = new Properties(serverProperties);
                Properties cloneProperties = new Properties();
                Enumeration keys = serverProperties.propertyNames();
                String key = null;
                String value = null;
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    value = serverProperties.getProperty(key);
                    cloneProperties.setProperty(key, value);
                    logger.log(Level.INFO, "*** APH-I1 : CloneAction() Cloning " + key + " = " + value);
                }

                cloneProperties.remove(ServerPropertyFileManager.FILENAME_KEY);
                String serverName = cloneProperties.getProperty(ServerPropertyFileManager.SERVERNAME_KEY);
                cloneProperties.setProperty(ServerPropertyFileManager.SERVERNAME_KEY, serverName + " - Copy");

                NewServerPanel nsp = new NewServerPanel(cloneProperties);
                Dialog d = DialogDisplayer.getDefault().createDialog(new DialogDescriptor(nsp, title, true, nsp.getButtonActionListener()));
                d.pack();
                d.setVisible(true);
                d = null;
                AllServersNotifier.changed();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public Action getPreferredAction() {
        if (server != null) return new StopServerAction();
        return new StartServerAction();
    }

    public Node cloneNode() {
        return new OneServerNode(serverConfig);
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set props = null; sheet.get(Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }

        props.put(new PropertySupport.Name(this));

        for (CommandlineProperty key : commandlineProperties) {
            if (!key.getPropertyName().startsWith("tangosol"))
            props.put(new CommandLineProp(key));
        }

        // Coherence
        Sheet.Set set = null; //sheet.get(Sheet.PROPERTIES);
        if (set == null) {
            set = Sheet.createPropertiesSet();
            set.setName("coherence");
            set.setDisplayName("Coherence Properties");
            sheet.put(set);
        }

        set.put(new PropertySupport.Name(this));

        for (CommandlineProperty key : commandlineProperties) {
            if (key.getPropertyName().startsWith("tangosol"))
            set.put(new CommandLineProp(key));
        }

        AllServersNotifier.addChangeListener(listener = new ChangeListener() {

            public void stateChanged(ChangeEvent ev) {
                logger.log(Level.INFO, "State Change " + ev.getSource());
                logger.log(Level.INFO, "Name " + serverProperties.getProperty(ServerPropertyFileManager.SERVERNAME_KEY));
//                firePropertyChange("server", null, null);
            }
        });

        return sheet;
    }

    class CommandLineProp extends PropertySupport.ReadWrite {

        private CommandlineProperty key = null;

        public CommandLineProp(CommandlineProperty key) {
//            super(key.getDisplayName(), key.getClazz(), key.getDisplayName(), key.getHint());
            super(key.getDisplayName(), key.getClazz() == Boolean.class ? Boolean.class : String.class, key.getDisplayName(), key.getHint());
            this.key = key;
        }

        public Object getValue() {
//            if (serverProperties.getProperty(key.getPropertyName()) != null) {
            if (key.getClazz() == Boolean.class) {
                return Boolean.parseBoolean(serverProperties.getProperty(key.getPropertyName(), key.getDefaultValue() == null ? "false" : key.getDefaultValue()));
            }
//            }
            // Default return
            return serverProperties.getProperty(key.getPropertyName(), key.getDefaultValue() == null ? "" : key.getDefaultValue());
        }

        public void setValue(Object nue) {
            try {
                logger.log(Level.INFO, "*** APH-I3 : Class instanceof " + nue.getClass().getSimpleName());
                if (nue == null || (nue instanceof String && nue.toString().length() == 0)
                        || (nue instanceof Integer && ((Integer) nue).intValue() <= 0)
                        || (nue instanceof Long && ((Long) nue).longValue() <= 0)) {
                    serverProperties.remove(key.getPropertyName());
                } else {
                    try {
                        if (key.getClazz() == Integer.class) {
                            Integer i = Integer.parseInt(nue.toString());
                        } else if (key.getClazz() == Long.class) {
                            Long l = Long.parseLong(nue.toString());
                        }
                        serverProperties.setProperty(key.getPropertyName(), nue.toString());
                    } catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, "*** APH-I3 : Property Value " + nue.toString() + " is not a number");
                        serverProperties.remove(key.getPropertyName());
                    }
                }
                ServerPropertyFileManager.saveProperties(serverProperties);
                AllServersNotifier.changed();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean canRename() {
        return true;
    }

    @Override
    public void setName(String s) {
        super.setName(s);
        try {
            serverProperties.setProperty(ServerPropertyFileManager.SERVERNAME_KEY, s);
            ServerPropertyFileManager.saveProperties(serverProperties);
            AllServersNotifier.changed();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        logger.log(Level.INFO, "*** APH-I1 : Deleting");
        try {
            ServerPropertyFileManager.deleteProperties(serverProperties.getProperty(ServerPropertyFileManager.FILENAME_KEY));
            AllServersNotifier.changed();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (listener != null) {
            AllServersNotifier.removeChangeListener(listener);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OneServerNode other = (OneServerNode) obj;
        if ((this.propertiesFilename == null) ? (other.propertiesFilename != null) : !this.propertiesFilename.equals(other.propertiesFilename)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.propertiesFilename != null ? this.propertiesFilename.hashCode() : 0);
        return hash;
    }

}
