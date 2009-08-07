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

package org.netbeans.modules.glassfish.common.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.CreateDomain;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;


/**
 * @author Ludo
 */
public class ServerWizardIterator implements WizardDescriptor.InstantiatingIterator, ChangeListener {
    
    private transient AddServerLocationPanel locationPanel = null;
    private transient AddDomainLocationPanel locationPanel2 = null;
    
    private WizardDescriptor wizard;
    private transient int index = 0;
    private transient WizardDescriptor.Panel[] panels = null;
        
    private transient List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private String domainsDir;
    private String domainName;
    private GlassfishInstanceProvider gip;

    public ServerWizardIterator(GlassfishInstanceProvider gip) {
        assert null != gip;
        this.gip = gip;
        setHostName("localhost");
    }

    private ServerWizardIterator() {
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    public void previousPanel() {
        index--;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public String name() {
        return gip.getDisplayName() + " AddInstanceIterator";  // NOI18N
    }
    
    public static void showInformation(final String msg,  final String title){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }
    
    public Set instantiate() throws IOException {
        Set<ServerInstance> result = new HashSet<ServerInstance>();
        File ir = new File(installRoot);
        ensureExecutable(ir);
        if (null != domainsDir) {
            handleLocalDomains(result, ir);
        } else {
            handleRemoteDomains(result,ir);
        }
        // lookup the javadb register service here and use it.
        RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
        if (null != db) {
            File f = new File(ir, "javadb");
            if (f.exists() && f.isDirectory() && f.canRead()) {
                db.initialize(f.getAbsolutePath());
            }
        }
        NbPreferences.forModule(this.getClass()).put(gip.getInstallRootKey(), installRoot);
        return result;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(ServerWizardIterator.class, "STEP_ServerLocation"),  // NOI18N
            NbBundle.getMessage(ServerWizardIterator.class, "STEP_Domain"), // NOI18N
        };
    }
    
    protected final String[] getSteps() {
        if (steps == null) {
            steps = createSteps();
        }
        return steps;
    }
    
    protected final WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        if (locationPanel == null) {
            locationPanel = new AddServerLocationPanel(this);
            locationPanel.addChangeListener(this);
        }
        if (locationPanel2 == null) {
            locationPanel2 = new AddDomainLocationPanel(this);
            locationPanel2.addChangeListener(this);
        }
        
        return new WizardDescriptor.Panel[] {
            (WizardDescriptor.Panel) locationPanel,
            (WizardDescriptor.Panel) locationPanel2,
//            (WizardDescriptor.Panel)propertiesPanel
        };
    }
    
    private transient String[] steps = null;
    
    protected final int getIndex() {
        return index;
    }
    
    public WizardDescriptor.Panel current() {
        WizardDescriptor.Panel result = getPanels()[index];
        JComponent component = (JComponent)result.getComponent();
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getSteps());  // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(getIndex()));// NOI18N
        return result;
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        fireChangeEvent();
    }
    
    protected final void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }
    
    private int httpPort = -1; // GlassfishInstance.DEFAULT_HTTP_PORT;
    private int httpsPort = GlassfishInstance.DEFAULT_HTTPS_PORT;
    private int adminPort = GlassfishInstance.DEFAULT_ADMIN_PORT;
//    private String userName;
//    private String password;
    private String installRoot;
    private String glassfishRoot;
    private String hostName;

    public String formatUri(String glassfishRoot, String host, int port) {
        return gip.formatUri(glassfishRoot, host, port);
    }

    String getDefaultInstallDirectoryName() {
        return gip.getDefaultInstallName(); // "GlassFish_v3_Prelude"; // NOI18N
    }

    int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }
    
    public void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }
   
    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }
    
//    public void setUserName(String userName) {
//        this.userName = userName;
//    }
//    
//    public void setPassword(String password) {
//        this.password = password;
//    }
    
    public void setInstallRoot(String installRoot) {
        this.installRoot = installRoot;
    }
    
    String getGlassfishRoot() {
        return this.glassfishRoot;
    }
    
    public void setGlassfishRoot(String glassfishRoot) {
        this.glassfishRoot = glassfishRoot;
    }

    String getInstallRootProperty() {
        return gip.getInstallRootProperty();
    }

    String getNameOfBits() {
        return gip.getDisplayName(); // NbBundle.getMessage(ServerWizardIterator.class, "V3_PRELUDE_NAME"); // NOI18N
    }

    boolean hasServer(String uri) {
        return gip.hasServer(uri);
    }

    boolean isValidInstall(File installDir, File glassfishDir, WizardDescriptor wizard) {
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "ERR_PreludeInstallationInvalid",
                            FileUtil.normalizeFile(installDir).getPath())); // getSanitizedPath(installDir)));
        File jar = ServerUtilities.getJarName(glassfishDir.getAbsolutePath(), ServerUtilities.GFV3_JAR_MATCHER);
        if(jar == null || !jar.exists()) {
            return false;
        }

        File containerRef = new File(glassfishDir, "config" + File.separator + "glassfish.container");
        if(!containerRef.exists()) {
            return false;
        }
        for (String s : gip.getRequiredFiles()) {
            containerRef = new File(glassfishDir, s);
            if (!containerRef.exists()) {
                return false;
            }
        }
        for (String s : gip.getExcludedFiles()) {
            containerRef = new File(glassfishDir, s);
            if (containerRef.exists()) {
                return false;
            }
        }

        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "   ");
        return true;
    }

    // expose for qa-functional tests
    public void setDomainLocation(String absolutePath) {
        if (null == absolutePath) {
            domainsDir = null;
            domainName = null;
        } else {
            int dex = absolutePath.lastIndexOf(File.separator);
            this.domainsDir = absolutePath.substring(0,dex);
            this.domainName = absolutePath.substring(dex+1);
        }
    }

    // Borrowed from RubyPlatform...
    private void ensureExecutable(File installDir) {
        // No excute permissions on Windows. On Unix and Mac, try.
        if(Utilities.isWindows()) {
            return;
        }

        if(!Utils.canWrite(installDir)) {
            // for unwritable installs (e.g root), don't even bother.
            return;
        }

        List<File> binList = new ArrayList<File>();
        for(String binPath: new String[] { "bin", "glassfish/bin", "javadb/bin", // NOI18N
                "javadb/frameworks/NetworkServer/bin", "javadb/frameworks/embedded/bin" }) { // NOI18N
            File dir = new File(installDir, binPath);
            if(dir.exists()) {
                binList.add(dir);
            }
        }

        if(binList.size() == 0) {
            return;
        }

        // Ensure that the binaries are installed as expected
        // The following logic is from CLIHandler in core/bootstrap:
        File chmod = new File("/bin/chmod"); // NOI18N

        if(!chmod.isFile()) {
            // Mac & Linux use /bin, Solaris /usr/bin, others hopefully one of those
            chmod = new File("/usr/bin/chmod"); // NOI18N
        }

        if(chmod.isFile()) {
            try {
                for(File binDir: binList) {
                    List<String> argv = new ArrayList<String>();
                    argv.add(chmod.getAbsolutePath());
                    argv.add("u+rx"); // NOI18N

                    String[] files = binDir.list();
                    for(String file : files) {
                        if(file.indexOf('.') == -1 || file.endsWith(".ksh")) {
                            argv.add(file);
                        }
                    }

                    ProcessBuilder pb = new ProcessBuilder(argv);
                    pb.directory(binDir);
                    Process process = pb.start();
                    int chmoded = process.waitFor();

                    if(chmoded != 0) {
                        throw new IOException(NbBundle.getMessage(
                                Retriever.class, "ERR_ChmodFailed", argv, chmoded)); // NOI18N
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
            }
        } else {
            String message = NbBundle.getMessage(Retriever.class, "ERR_ChmodNotFound"); // NOI18N
            StringBuilder builder = new StringBuilder(message.length() + 50 * binList.size());
            builder.append(message);
            for(File binDir: binList) {
                builder.append('\n'); // NOI18N
                builder.append(binDir);
            }
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    builder.toString(), NotifyDescriptor.WARNING_MESSAGE));
        }
    }

    String getIndirect() {
        return gip.getIndirectDownloadUrl(); //"http://serverplugins.netbeans.org/glassfishv3/preludezipfilename.txt"; // NOI18N
    }

    String getDirect() {
        return gip.getDirectDownloadUrl(); //"http://java.net/download/glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip"; // NOI18N
    }

    String getInstallRootKey() {
        return gip.getInstallRootKey(); // "last-install-root"; // NOI18N
    }

    private void handleLocalDomains(Set<ServerInstance> result, File ir) {
        File domainDir = new File(domainsDir, domainName);
        String canonicalPath = null;
        try {
            canonicalPath = domainDir.getCanonicalPath();
        } catch (IOException ioe) {
            Logger.getLogger("glassfish").log(Level.INFO, domainDir.getAbsolutePath(), ioe); // NOI18N
        }
        if (null != canonicalPath && !canonicalPath.equals(domainDir.getAbsolutePath())) {
            setDomainLocation(canonicalPath);
            domainDir = new File(domainsDir, domainName);
        }
        if (!domainDir.exists() && AddServerLocationPanel.canCreate(domainDir)) {
            // Need to create a domain right here!
            Map<String, String> ip = new HashMap<String, String>();
            ip.put(GlassfishModule.INSTALL_FOLDER_ATTR, installRoot);
            ip.put(GlassfishModule.GLASSFISH_FOLDER_ATTR, glassfishRoot);
            ip.put(GlassfishModule.DISPLAY_NAME_ATTR, (String) wizard.getProperty("ServInstWizard_displayName")); // NOI18N
            ip.put(GlassfishModule.DOMAINS_FOLDER_ATTR, domainsDir);
            ip.put(GlassfishModule.DOMAIN_NAME_ATTR, domainName);
            CreateDomain cd = new CreateDomain("anonymous", "", new File(glassfishRoot), ip, gip);
            cd.start();
            result.add(gip.getInstance(domainsDir));
        } else {
            GlassfishInstance instance = GlassfishInstance.create((String) wizard.getProperty("ServInstWizard_displayName"), installRoot, glassfishRoot, domainsDir, domainName, httpPort, adminPort, formatUri(glassfishRoot, "localhost", adminPort), gip.getUriFragment(), gip);
            result.add(instance.getCommonInstance());
        }
    }

    private void handleRemoteDomains(Set<ServerInstance> result, File ir) {
        // TODO - vbk : get the real port from the server. Doable, but hard to do right.
        httpPort = 8080;
        GlassfishInstance instance = GlassfishInstance.create((String) wizard.getProperty("ServInstWizard_displayName"), installRoot, glassfishRoot, null, null, httpPort, adminPort, formatUri(glassfishRoot, getHostName(), adminPort), gip.getUriFragment(), gip);
        result.add(instance.getCommonInstance());
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}