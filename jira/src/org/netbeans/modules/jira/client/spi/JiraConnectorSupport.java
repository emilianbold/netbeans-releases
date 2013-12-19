/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.client.spi;

import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import static org.netbeans.modules.jira.client.spi.JiraConnectorProvider.Type.REST;
import static org.netbeans.modules.jira.client.spi.JiraConnectorProvider.Type.XMLRPC;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author tomas
 */
public final class JiraConnectorSupport {
    
    private static JiraConnectorSupport instance;

    protected final static Logger LOG = Logger.getLogger(JiraConnectorProvider.class.getName());
    private JiraConnectorProvider connector;
    private JiraConnectorProvider.Type connectorType;

    private JiraConnectorSupport() { }
    
    public static synchronized JiraConnectorSupport getInstance() {
        if(instance == null) {
            instance = new JiraConnectorSupport();
        }
        return instance;
    }

    public synchronized void setConnectorType(JiraConnectorProvider.Type type) {
        this.connectorType = type;
    }
    
    public synchronized JiraConnectorProvider getConnector() {
        if (connector == null) {
            connector = forType(connectorType);
            // XXX do not enable/disable connectors until both rest and xmlrpc are provided
//            if(connector == null) {
//                Jira.LOG.log(Level.WARNING, "Connector {0} not available for JIRA", connectorType.getCnb());
//                switch(connectorType) {
//                    case REST:
//                        connectorType = XMLRPC;
//                        tryFallback();
//                        break;
//                    case XMLRPC:
//                        connectorType = REST;
//                        tryFallback();
//                        break;
//                }
//            }
            Jira.LOG.log(Level.INFO, "Selected JIRA connector is {0}", connectorType.getCnb());
        }
        return connector;
    }

    // XXX do not enable/disable connectors until both rest and xmlrpc are provided
//    private void tryFallback() {
//        Jira.LOG.log(Level.WARNING, "Falling back on ", connectorType.getCnb());
//        JiraConfig.getInstance().setActiveConnector(connectorType);
//        enableConnector(connectorType);
//        connector = forType(connectorType);
//    }
    
    /**
     *
     * @param type
     * @return
     */
    private JiraConnectorProvider forType(JiraConnectorProvider.Type type) {
        Collection<? extends JiraConnectorProvider> connectors = Lookup.getDefault().lookupAll(JiraConnectorProvider.class);
        if(LOG.isLoggable(Level.FINE)) {
            for (JiraConnectorProvider p : connectors) {
                LOG.log(Level.FINE, "registered JIRA Connector : {0}", p.toString());
            }
        }
        for (JiraConnectorProvider p : connectors) {
            if(p.getType() == type) {
                return p;
            }
        }
        return null; // XXX handle this
    }

    // XXX do not enable/disable connectors until both rest and xmlrpc are provided
//    @NbBundle.Messages({"MSG_Install_Warning_Title=JIRA Connector installation",
//                       "# {0} - to be enabled a plugins display name", "# {1} - to be disabled a plugins display name",
//                       "MSG_Install_Warning=In order to finish installing the {0} functionality,\n"
//                               + "the IDE needs to disable the existing {1} functionality.\n"
//                               + "You can switch back to {1}\n"
//                               + "by enabling it in (Tools > Plugins).\n\n"
//                               + "It is neccessary to restart the IDE after this installation completes."})
//    public static JiraConnectorProvider.Type enableConnector(JiraConnectorProvider.Type toEnable) {
//        JiraConnectorProvider.Type toDisable = toEnable == XMLRPC ? REST : XMLRPC;
//        
//        if(changeConnectorConfig(toEnable, true)) {
//            JiraConfig.getInstance().setActiveConnector(toEnable);
//            changeConnectorConfig(toDisable, false);
//            return toEnable;
//        } else {
//            Jira.LOG.log(Level.FINE, "JIRA did no succed disabling {0}. Falling back on ", toDisable.getCnb());
//            changeConnectorConfig(toDisable, true);
//            return toDisable;
//        }
//    }
//
//    public static boolean changeConnectorConfig(JiraConnectorProvider.Type type, boolean enable) {
//        String cnb = type.getCnb();
//        
//        Jira.LOG.log(Level.INFO, "JIRA is trying to {0} module {1}", new Object[] {enable ? "enable" : "disable", cnb});
//        
//        FileLock lock = null;
//        OutputStream os = null;
//        try {
//            String newModuleXML = "Modules/" + cnb.replace('.', '-') + ".xml"; // NOI18N
//            FileObject fo = FileUtil.getConfigFile(newModuleXML);
//            if (fo == null) {
//                return true;
//            }
//            Document document = readModuleDocument(fo);
//            NodeList list = document.getDocumentElement().getElementsByTagName("param"); // NOI18N
//            int n = list.getLength();
//            for (int j = 0; j < n; j++) {
//                Element node = (Element) list.item(j);
//                if ("enabled".equals(node.getAttribute("name"))) {
//                    // NOI18N
//                    Text text = (Text) node.getChildNodes().item(0);
//                    String value = text.getNodeValue();
//                    if (Boolean.valueOf(value) != enable) {
//                        text.setNodeValue(Boolean.valueOf(enable).toString());
//
//                        lock = fo.lock();
//                        os = fo.getOutputStream(lock);
//                        XMLUtil.write(document, os, "UTF-8"); // NOI18N
//            
//                        if(enable) {
//                            Jira.LOG.log(Level.INFO, "JIRA enabled module {0}", cnb);
//                        } else {
//                            Jira.LOG.log(Level.INFO, "JIRA disabled module {0}", cnb);
//                        }
//                        break;
//                    } else {
//                        Jira.LOG.log(Level.INFO, " {0} already {1}", new Object[] {cnb, enable ? "enabled" : "disabled"});
//                    }
//                } 
//            }
//            return true;
//
//        }catch (ParserConfigurationException | SAXException | IOException | DOMException | MissingResourceException | HeadlessException e) {
//            Jira.LOG.log(Level.INFO, null, e);
//        } finally {
//            if (os != null) try { os.close(); } catch (IOException ex) {}
//            if (lock != null) lock.releaseLock();
//        }
//        return false;
//    }
//
//    private static Document readModuleDocument(FileObject fo) throws ParserConfigurationException, SAXException, IOException {
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setValidating(false);
//        DocumentBuilder parser = dbf.newDocumentBuilder();
//        
//        ParserSupport ps = new ParserSupport();
//        parser.setEntityResolver(ps);
//        parser.setErrorHandler(ps);
//        Document document;
//        try (InputStream is = fo.getInputStream()) {
//            document = parser.parse(is);
//        }
//        return document;
//    }
//
//    private static class ParserSupport implements ErrorHandler, EntityResolver {
//        @Override
//        public InputSource resolveEntity(String publicId, String systemId) {
//            return new InputSource(new ByteArrayInputStream(new byte[0]));
//        }
//
//        @Override
//        public void error(SAXParseException exception) {
//            Jira.LOG.log(Level.INFO, null, exception);
//        }
//
//        @Override
//        public void fatalError(SAXParseException exception) {
//            Jira.LOG.log(Level.INFO, null, exception);
//        }
//
//        @Override
//        public void warning(SAXParseException exception) {
//            Jira.LOG.log(Level.INFO, null, exception);
//        }
//    }
    
    

}
