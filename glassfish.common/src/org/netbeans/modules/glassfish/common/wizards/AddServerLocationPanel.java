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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.CommonServerSupport;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.netbeans.spi.glassfish.ServerUtilities;
import org.netbeans.spi.glassfish.TreeParser;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Ludo
 */
public class AddServerLocationPanel implements WizardDescriptor.Panel, ChangeListener {
    
    private static final String DEFAULT_DOMAIN_DIR = "domains/domain1";
    private static final String DOMAIN_XML_PATH = "config/domain.xml";
    
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18   
    
    private ServerWizardIterator wizardIterator;
    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient Set <ChangeListener>listeners = new HashSet<ChangeListener>(1);

    private int httpPort = GlassfishInstance.DEFAULT_HTTP_PORT;
    private int httpsPort = GlassfishInstance.DEFAULT_HTTPS_PORT;
    private int adminPort = GlassfishInstance.DEFAULT_ADMIN_PORT;
    
    /**
     * 
     * @param instantiatingIterator 
     */
    public AddServerLocationPanel(ServerWizardIterator wizardIterator){
        this.wizardIterator = wizardIterator;
    }
    
    /**
     * 
     * @param ev 
     */
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
    
    private void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    /**
     * 
     * @return 
     */
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel();
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
                File installDir = new File(locationStr);
                File glassfishDir = getGlassfishRoot(installDir);
                if(!installDir.exists()) {
                    if(canCreate(installDir)) {
                        if(downloadState == AddServerLocationVisualPanel.DownloadState.AVAILABLE) {
                            panel.updateMessageText(NbBundle.getMessage(
                                    AddServerLocationPanel.class, "LBL_InstallDirWillBeUsed", locationStr));
                            wizard.putProperty(PROP_ERROR_MESSAGE, panel.getStatusText());
                            return false;
                        } else {
                            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                    AddServerLocationPanel.class, "ERR_InstallDirDoesNotExist", locationStr));
                            return false;
                        }
                    } else {
                        wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                                AddServerLocationPanel.class, "ERR_CannotCreate", locationStr));
                        return false;
                    }
                } else if(!isValidV3Install(installDir, glassfishDir)) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "ERR_InstallDirInvalid", locationStr));
                    return false;
                } else if(!isValidV3Domain(getDefaultDomain(glassfishDir))) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "ERR_DefaultDomainInvalid", locationStr));
                    return false;
                } else {
                    String uri = "[" + installDir + "]" + CommonServerSupport.URI_PREFIX + 
                            ":" + GlassfishInstance.DEFAULT_HOST_NAME + ":" + Integer.toString(httpPort);
                    if(GlassfishInstanceProvider.getDefault().hasServer(uri)) {
                        wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                            AddServerLocationPanel.class, "ERR_DomainExists", locationStr));
                        return false;
                    } else {
                        String statusText = panel.getStatusText();
                        if(statusText != null && statusText.length() > 0) {
                            wizard.putProperty(PROP_ERROR_MESSAGE, statusText);
                            return false;
                        }
                    }
                }

                wizard.putProperty(PROP_ERROR_MESSAGE, null);
                wizardIterator.setInstallRoot(locationStr);
                wizardIterator.setGlassfishRoot(glassfishDir.getAbsolutePath());
                wizardIterator.setHttpPort(httpPort);
                wizardIterator.setHttpsPort(httpsPort);
                wizardIterator.setAdminPort(adminPort);

                return true;
            } finally {
                isValidating.set(false);
            }
        }
        return true;
    }
    
    private boolean canCreate(File dir) {
        while(dir != null && !dir.exists()) {
            dir = dir.getParentFile();
        }
        return dir != null ? dir.canRead() && dir.canWrite() : false;
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
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
    
    private boolean isValidV3Install(File installRoot, File glassfishRoot) {
        File jar = ServerUtilities.getJarName(glassfishRoot.getAbsolutePath(), ServerUtilities.GFV3_PREFIX_JAR_NAME);
        if(jar == null || !jar.exists()) {
            return false;          
        }
        
        File containerRef = new File(glassfishRoot, "config" + File.separator + "glassfish.container");
        if(!containerRef.exists()) {
            return false;
        }
        
        File domainRef = getDefaultDomain(glassfishRoot);
        if(!domainRef.exists()) {
            return false;
        }
        
        return true;
    }
    
    private boolean isValidV3Domain(File domainDir) {
        return readServerConfiguration(domainDir);
    }
    
    private File getGlassfishRoot(File installRoot) {
        File glassfishRoot = new File(installRoot, "glassfish");
        if(!glassfishRoot.exists()) {
            glassfishRoot = installRoot;
        }
        return glassfishRoot;
    }
    
    private File getDefaultDomain(File installDir) {
        return new File(installDir, DEFAULT_DOMAIN_DIR);
    }
    
    private boolean readServerConfiguration(File domainDir) {
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
                            boolean enabled = "true".equals(attributes.getValue("enabled"));
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
                adminPort = adminData != null ? adminData.getPort() : -1;
                
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
                
                httpPort = httpData != null ? httpData.getPort() : -1;
                httpsPort = httpsData != null ? httpsData.getPort() : -1;
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