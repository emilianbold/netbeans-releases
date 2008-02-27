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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.glassfish.common.CommonServerSupport;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        AddServerLocationVisualPanel panel = (AddServerLocationVisualPanel) getComponent();
        String locationStr = panel.getHk2HomeLocation();
        locationStr = (locationStr != null) ? locationStr.trim() : null;
        if(locationStr == null || locationStr.length() == 0) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                    AddServerLocationPanel.class, "ERR_BLANK_INSTALL_DIR"));
            return false;
        }
            
        // !PW Replace some or all of this with a single call to a validate method
        // that throws an exception with a precise reason for validation failure.
        // e.g. domain dir not found, domain.xml corrupt, no ports defined, etc.
        //
        File installDir = new File(locationStr);
        if(!installDir.exists()) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                    AddServerLocationPanel.class, "ERR_INSTALL_DIR_NOT_EXIST", locationStr));
            return false;
        } else if(!isValidV3Install(installDir)) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                    AddServerLocationPanel.class, "ERR_INSTALL_DIR_NOT_VALID", locationStr));
            return false;
        } else if(!isValidV3Domain(getDefaultDomain(installDir))) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                    AddServerLocationPanel.class, "ERR_DEFAULT_DOMAIN_NOT_VALID", locationStr));
            return false;
        } else {
            String uri = "[" + installDir + "]" + CommonServerSupport.URI_PREFIX + 
                    ":" + GlassfishInstance.DEFAULT_HOST_NAME + ":" + Integer.toString(httpPort);
            if(GlassfishInstanceProvider.getDefault().hasServer(uri)) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(
                    AddServerLocationPanel.class, "ERR_DOMAIN_ALREADY_ADDED", locationStr));
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
        wizardIterator.setHk2HomeLocation(locationStr);
        wizardIterator.setHttpPort(httpPort);
        wizardIterator.setHttpsPort(httpsPort);
        wizardIterator.setAdminPort(adminPort);
        
        return true;
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
    
    private boolean isValidV3Install(File installDir) {
        File glassfishRef = new File(installDir, "modules" + File.separator + "glassfish-10.0-SNAPSHOT.jar");
        if(!glassfishRef.exists()) {
            // !PW Older V3 installs (pre 12/01/07) put snapshot jar in lib folder.
            glassfishRef = new File(installDir, "lib" + File.separator + "glassfish-10.0-SNAPSHOT.jar");
            if(!glassfishRef.exists()) {
                return false;
            }
        }
        
        File containerRef = new File(installDir, "config" + File.separator + "glassfish.container");
        if(!containerRef.exists()) {
            return false;
        }
        
        File domainRef = new File(installDir, "domains" + File.separator + "domain1");
        if(!domainRef.exists()) {
            return false;
        }
        
        return true;
    }
    
    private boolean isValidV3Domain(File domainDir) {
        return readServerConfiguration(domainDir);
    }
    
    private File getDefaultDomain(File installDir) {
        return new File(installDir, DEFAULT_DOMAIN_DIR);
    }
    
    private boolean readServerConfiguration(File domainDir) {
        boolean result = false;
        File domainXml = new File(domainDir, DOMAIN_XML_PATH);

        if (domainXml.exists()) {
            InputStream is = null;
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                // !PW If namespace-aware is enabled, make sure localpart and
                // qname are treated correctly in the handler code.
                //                
                factory.setNamespaceAware(false);
                SAXParser saxParser = factory.newSAXParser();
                DomainXmlParser handler = new DomainXmlParser();
                is = new BufferedInputStream(new FileInputStream(domainXml));
                saxParser.parse(new InputSource(is), handler);
                httpPort = handler.getHttpPort();
                httpsPort = handler.getHttpsPort();
                adminPort = handler.getAdminPort();
                result = true;
            } catch (ParserConfigurationException ex) {
                // Badly formed domain.xml, fail.
            } catch (SAXException ex) {
                // Badly formed domain.xml, fail.
            } catch (IOException ex) {
                // Badly formed domain.xml, fail.
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }

        return result;
    }

    /**
     * This is a weak parser that attempts to locate the http, https, and admin
     * port definitions in domain.xml.
     * 
     * Obvious improvements include accurate tracking of xml nodes to be certain
     * we read /domain/configs/config for the correct server (e.g. clusters) and
     * the correct http-listener entries therein.
     */
    private static final class DomainXmlParser extends DefaultHandler {

        // Parser state
        private Tag wantedTag;
        private Tag currentTag;
        
        // Results
        private int httpPort;
        private int httpsPort;
        private int adminPort;

        DomainXmlParser() {
        }

        @Override
        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(wantedTag.element().equals(qname)) {
                currentTag = wantedTag;
                wantedTag = wantedTag.next();
            } else {
                currentTag = null;
            }
            if(currentTag == Tag.HTTP_LISTENER) {
                try {
                    String id = attributes.getValue("id");
                    int port = Integer.parseInt(attributes.getValue("port"));
                    
                    if("admin-listener".equals(attributes.getValue("id"))) {
                        adminPort = port;
                    } else {
                        String secure = attributes.getValue("security-enabled");
                        if("true".equals(secure)) {
                            httpsPort = port;
                        } else {
                            httpPort = port;
                        }
                    }
                } catch(NumberFormatException ex) {
                    throw new SAXException(ex);
                }
            }
        }
        
        @Override
        public void endElement(String uri, String localname, String qname) throws SAXException {
        }

        @Override
        public void startDocument() throws SAXException {
            wantedTag = Tag.DOMAIN;
            currentTag = null;
            
            httpPort = -1;
            httpsPort = -1;
            adminPort = -1;
        }

        @Override
        public void endDocument() throws SAXException {
        }

        public int getHttpPort() {
            return httpPort;
        }
        
        public int getHttpsPort() {
            return httpsPort;
        }
        
        public int getAdminPort() {
            return adminPort;
        }
        
        private static enum Tag {
            
            DOMAIN { 
                public String element() { return "domain"; } 
                public Tag next() { return APPLICATIONS; }
            },
            APPLICATIONS { 
                public String element() { return "applications"; } 
                public Tag next() { return RESOURCES; }
            },
            RESOURCES { 
                public String element() { return "resources"; } 
                public Tag next() { return SERVERS; }
            },
            SERVERS { 
                public String element() { return "servers"; } 
                public Tag next() { return CONFIGS; }
            },
            CONFIGS { 
                public String element() { return "configs"; } 
                public Tag next() { return CONFIG; }
            },
            CONFIG { 
                public String element() { return "config"; } 
                public Tag next() { return HTTP_LISTENER; }
            },
            HTTP_LISTENER { 
                public String element() { return "http-listener"; } 
                public Tag next() { return HTTP_LISTENER; }
            };
            
            public abstract String element();
            public abstract Tag next();
            
        }
        
    }
    
}