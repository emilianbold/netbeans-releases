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

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Ludo
 * @author vince
 */
public class AddServerLocationPanel implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private static final String DOMAIN_XML_PATH = "config/domain.xml";
    
    private final String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE;
    private final String PROP_WARNING_MESSAGE = WizardDescriptor.PROP_WARNING_MESSAGE;
    private final String PROP_INFO_MESSAGE = WizardDescriptor.PROP_INFO_MESSAGE;

    private ServerWizardIterator wizardIterator;
    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    
    /**
     * 
     * @param instantiatingIterator 
     */
    public AddServerLocationPanel(ServerWizardIterator wizardIterator){
        this.wizardIterator = wizardIterator;
        wizard = null;
    }
    
    /**
     * 
     * @param ev 
     */
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
    
    private void fireChangeEvent(ChangeEvent ev) {
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }
    
    /**
     * 
     * @return 
     */
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel(wizardIterator);
            component.addChangeListener(this);
        }
        return component;
    }
    
    /**
     * 
     * @return 
     */
    public HelpCtx getHelp() {
        // !PW FIXME correct help context
        return new HelpCtx("registering_app_server_hk2_location"); //NOI18N
    }

    private AtomicBoolean isValidating = new AtomicBoolean();
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        if(isValidating.compareAndSet(false, true)) {
            try {
                wizardIterator.setHttpPort(-1);
                AddServerLocationVisualPanel panel = (AddServerLocationVisualPanel) getComponent();

                AddServerLocationVisualPanel.DownloadState downloadState = panel.getDownloadState();
                if(downloadState == AddServerLocationVisualPanel.DownloadState.DOWNLOADING) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, panel.getStatusText());
                    return false;
                }

                String locationStr = panel.getHk2HomeLocation();
                locationStr = (locationStr != null) ? locationStr.trim() : null;
                if(locationStr == null || locationStr.length() == 0) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "ERR_BlankInstallDir"));
                    return false;
                }

                // !PW Replace some or all of this with a single call to a validate method
                // that throws an exception with a precise reason for validation failure.
                // e.g. domain dir not found, domain.xml corrupt, no ports defined, etc.
                //
                File installDir = new File(locationStr).getAbsoluteFile();
                File glassfishDir = getGlassfishRoot(installDir);
                File domainDir = getDefaultDomain(glassfishDir);
                if(!installDir.exists()) {
                    if(!isLegalFolder(installDir)) {
                        wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                AddServerLocationPanel.class, "ERR_InstallDirInvalid", locationStr));
                        return false;
                    } else if(canCreate(installDir)) {
                        if(downloadState == AddServerLocationVisualPanel.DownloadState.AVAILABLE) {
                            panel.updateMessageText(NbBundle.getMessage(
                                    AddServerLocationPanel.class, "LBL_PreludeInstallDirWillBeUsed", getSanitizedPath(installDir),
                                    wizardIterator.getNameOfBits()));
                            wizard.putProperty(PROP_ERROR_MESSAGE, panel.getStatusText());
                            return false;
                        } else {
                            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                    AddServerLocationPanel.class, "ERR_InstallDirDoesNotExist", getSanitizedPath(installDir)));
                            return false;
                        }
                    } else {
                        wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                AddServerLocationPanel.class, "ERR_CannotCreate", getSanitizedPath(installDir)));
                        return false;
                    }
                } else if(!wizardIterator.isValidInstall(installDir, glassfishDir,wizard)) {
                    return false;
                } else if(!isRegisterableDomain(domainDir)) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "ERR_DefaultDomainInvalid", getSanitizedPath(installDir)));
                } else {
                    readServerConfiguration(domainDir, wizardIterator);
                    String uri = wizardIterator.formatUri(glassfishDir.getAbsolutePath(),
                            GlassfishInstance.DEFAULT_HOST_NAME, wizardIterator.getHttpPort());
                    if(wizardIterator.hasServer(uri)) {
                        wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "MSG_DefaultDomainExists",
                            getSanitizedPath(installDir), GlassfishInstance.DEFAULT_DOMAIN_NAME));
                        wizardIterator.setHttpPort(-1); // FIXME this is a hack - disables finish button
                    } else {
                        String statusText = panel.getStatusText();
                        if(statusText != null && statusText.length() > 0) {
                            wizard.putProperty(PROP_ERROR_MESSAGE, statusText);
                            return false;
                        } else {
                            wizard.putProperty(PROP_ERROR_MESSAGE, null);
                            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(
                                AddServerLocationPanel.class, "MSG_NextForSpecial"));
                        }
                    }
                }

                // message has already been set, do not clear it here (see above).
                
                // finish initializing the registration data
                wizardIterator.setInstallRoot(installDir.getAbsolutePath());
                wizardIterator.setGlassfishRoot(glassfishDir.getAbsolutePath());
                wizardIterator.setDomainLocation(domainDir.getAbsolutePath());

                return true;
            } finally {
                isValidating.set(false);
            }
        }
        return true;
    }

    private static String getSanitizedPath(File dir) {
        return FileUtil.normalizeFile(dir).getPath();
    }

    // These characters ( ? * : | < > " ) are illegal on Windows (NTFS).
    // The first four are detected by getCanonicalFile(), but the last 3 are not
    // so check for them specifically.
    private static Pattern ILLEGAL_WINDOWS_CHARS = Pattern.compile("<|>|\\\"");

    private static boolean isLegalFolder(File installDir) {
        return getCanonicalFile(installDir) != null &&
                (!Utilities.isWindows() || ILLEGAL_WINDOWS_CHARS.matcher(installDir.getPath()).find() == false);
    }

    private static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            return null;
        }
    }

    static boolean canCreate(File dir) {
        if (dir.exists()) {
            return false;
        }
        while(dir != null && !dir.exists()) {
            dir = dir.getParentFile();
        }
        return dir != null ? dir.canRead() && Utils.canWrite(dir) : false;
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * 
     * @param settings 
     */
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }
    
    /**
     * 
     * @param settings 
     */
    public void storeSettings(Object settings) {
    }
    
    public boolean isFinishPanel() {
        return wizardIterator.getHttpPort() != -1;
    }
    
    static boolean isRegisterableDomain(File domainDir) {
        File testFile = new File(domainDir, "logs"); // NOI18N
        if (!testFile.exists()) {
            testFile = domainDir;
        }
        return Utils.canWrite(testFile) && readServerConfiguration(domainDir, null);
    }
    
    private File getGlassfishRoot(File installDir) {
        File glassfishDir = new File(installDir, "glassfish");
        if(!glassfishDir.exists()) {
            glassfishDir = installDir;
        }
        return glassfishDir;
    }
    
    private File getDefaultDomain(File glassfishDir) {
        File retVal = new File(glassfishDir, GlassfishInstance.DEFAULT_DOMAINS_FOLDER + 
                File.separator + GlassfishInstance.DEFAULT_DOMAIN_NAME); // NOI18N
        if (!isRegisterableDomain(retVal)) {
            // see if there is some other domain that will work.
            File domainsDir = new File(glassfishDir, GlassfishInstance.DEFAULT_DOMAINS_FOLDER); // NOI18N
            File candidates[] = domainsDir.listFiles();
            if (null != candidates && candidates.length > 0) {
                // try to pick a candidate
                for (File c : candidates) {
                    if (isRegisterableDomain(retVal)) {
                        retVal = c;
                        break;
                    }
                }
            }
        }
        return retVal;
    }
    
    static boolean readServerConfiguration(File domainDir, ServerWizardIterator wi) {
        boolean result = false;
        File domainXml = new File(domainDir, DOMAIN_XML_PATH);
        final Map<String, HttpData> httpMap = new LinkedHashMap<String, HttpData>();
        
        if (domainXml.exists()) {
            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path("/domain/configs/config/http-service/http-listener",
                    new TreeParser.NodeReader() {
                @Override
                public void readAttributes(String qname, Attributes attributes) throws SAXException {
                    // <http-listener 
                    //   id="http-listener-1" port="8080" xpowered-by="true" 
                    //   enabled="true" address="0.0.0.0" security-enabled="false" 
                    //   family="inet" default-virtual-server="server" 
                    //   server-name="" blocking-enabled="false" acceptor-threads="1">
                    try {
                        String id = attributes.getValue("id");
                        if(id != null && id.length() > 0) {
                            int port = Integer.parseInt(attributes.getValue("port"));
                            boolean secure = "true".equals(attributes.getValue("security-enabled"));
                            boolean enabled = !"false".equals(attributes.getValue("enabled"));
                            if(enabled) {
                                HttpData data = new HttpData(id, port, secure);
                                Logger.getLogger("glassfish").log(Level.FINER, " Adding " + data);
                                httpMap.put(id, data);
                            } else {
                                Logger.getLogger("glassfish").log(Level.FINER, "http-listener " + id + " is not enabled and won't be used.");
                            }
                        } else {
                            Logger.getLogger("glassfish").log(Level.FINEST, "http-listener found with no name");
                        }
                    } catch(NumberFormatException ex) {
                        throw new SAXException(ex);
                    }
                }
            }));
            
            pathList.add(new TreeParser.Path("/domain/configs/config/network-config/network-listeners/network-listener",
                    new TreeParser.NodeReader() {
                @Override
                public void readAttributes(String qname, Attributes attributes) throws SAXException {
                    // <http-listener
                    //   id="http-listener-1" port="8080" xpowered-by="true"
                    //   enabled="true" address="0.0.0.0" security-enabled="false"
                    //   family="inet" default-virtual-server="server"
                    //   server-name="" blocking-enabled="false" acceptor-threads="1">
                    try {
                        String id = attributes.getValue("name");
                        if(id != null && id.length() > 0) {
                            int port = Integer.parseInt(attributes.getValue("port"));
                            boolean secure = "true".equals(attributes.getValue("security-enabled"));
                            boolean enabled = !"false".equals(attributes.getValue("enabled"));
                            if(enabled) {
                                HttpData data = new HttpData(id, port, secure);
                                Logger.getLogger("glassfish").log(Level.FINER, " Adding " + data);
                                httpMap.put(id, data);
                            } else {
                                Logger.getLogger("glassfish").log(Level.FINER, "http-listener " + id + " is not enabled and won't be used.");
                            }
                        } else {
                            Logger.getLogger("glassfish").log(Level.FINEST, "http-listener found with no name");
                        }
                    } catch(NumberFormatException ex) {
                        throw new SAXException(ex);
                    }
                }
            }));

            try {
                TreeParser.readXml(domainXml, pathList);
                
                // !PW This probably more convoluted than it had to be, but while
                // http-listeners are usually named "http-listener-1", "http-listener-2", ...
                // technically they could be named anything.
                // 
                // For now, the logic is as follows:
                //   admin port is the one named "admin-listener"
                //   http port is the first non-secure enabled port - typically http-listener-1
                //   https port is the first secure enabled port - typically http-listener-2
                // disabled ports are ignored.
                //
                HttpData adminData = httpMap.remove("admin-listener");
                if (null != wi) {
                    wi.setAdminPort(adminData != null ? adminData.getPort() : -1);
                }
                
                HttpData httpData = null;
                HttpData httpsData = null;
                
                for(HttpData data: httpMap.values()) {
                    if(data.isSecure()) {
                        if(httpsData == null) {
                            httpsData = data;
                        }
                    } else {
                        if(httpData == null) {
                            httpData = data;
                        }
                    }
                    if(httpData != null && httpsData != null) {
                        break;
                    }
                }
                
                int httpPort = httpData != null ? httpData.getPort() : -1;
                if (null != wi) {
                    wi.setHttpPort(httpPort);
                    wi.setHttpsPort(httpsData != null ? httpsData.getPort() : -1);
                }
                result = httpPort != -1;
            } catch(IllegalStateException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return result;
    }
    
    private static class HttpData {

        private final String id;
        private final int port;
        private final boolean secure;
        
        public HttpData(String id, int port, boolean secure) {
            this.id = id;
            this.port = port;
            this.secure = secure;
        }
        
        public String getId() {
            return id;
        }

        public int getPort() {
            return port;
        }

        public boolean isSecure() {
            return secure;
        }
        
        @Override
        public String toString() {
            return "{ " + id + ", " + port + ", " + secure + " }";
        }
        
    }
}
