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
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Modules;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
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
public abstract class JiraConnectorLifecycleManager extends ModuleInstall implements ErrorHandler, EntityResolver {

    private static final Logger LOG = JiraConnectorProvider.LOG;
    
    protected void deactivateRestConnector() {
        
    }
    
    protected void deactivateXmlRpcConnector() {
        
    }
    
//    @NbBundle.Messages({"MSG_Install_Warning_Title=JIRA Connector installation",
//                       "# {0} - to be enabled a plugins display name", "# {1} - to be disabled a plugins display name",
//                       "MSG_Install_Warning=In order to finish installing the {0} functionality,\n"
//                               + "the IDE needs to disable the existing {1} functionality.\n"
//                               + "You can switch back to {1}\n"
//                               + "by enabling it in (Tools > Plugins).\n\n"
//                               + "It is neccessary to restart the IDE after this installation completes."})
    protected void disableOtherConnector(final String toDisableCNB, final String toEnableCNB, final String toEnableDisplayName) {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                
//                ModuleInfo m = Modules.getDefault().findCodeNameBase(toDisableCNB);
//                if(m == null || !m.isEnabled()) {
//                    return;
//                }
//                String toDisableDisplayName = m.getDisplayName();
//                
//                FileLock lock = null;
//                OutputStream os = null;
//                try {
//                    String newModuleXML = "Modules/" + toDisableCNB.replace('.', '-') + ".xml"; // NOI18N
//                    FileObject fo = FileUtil.getConfigFile(newModuleXML);
//                    if (fo == null) {
//                        return;
//                    }
//                    Document document = readModuleDocument(fo);
//
//                    NodeList list = document.getDocumentElement().getElementsByTagName("param"); // NOI18N
//                    int n = list.getLength();
//                    for (int j = 0; j < n; j++) {
//                        Element node = (Element) list.item(j);
//                        if ("enabled".equals(node.getAttribute("name"))) { // NOI18N
//                            Text text = (Text) node.getChildNodes().item(0);
//                            String value = text.getNodeValue();
//                            if ("true".equals(value)) { // NOI18N
//                                text.setNodeValue("false"); // NOI18N
//                                break;
//                            } else {
//                                return;
//                            }
//                        } 
//                    }
//                    JOptionPane.showMessageDialog(
//                            null, 
//                            Bundle.MSG_Install_Warning(toEnableDisplayName, toDisableDisplayName), 
//                            Bundle.MSG_Install_Warning_Title(), 
//                            JOptionPane.WARNING_MESSAGE);
//                    lock = fo.lock();
//                    os = fo.getOutputStream(lock);
//
//                    XMLUtil.write(document, os, "UTF-8"); // NOI18N
//                } catch (ParserConfigurationException | SAXException | IOException | DOMException | MissingResourceException | HeadlessException e) {
//                    LOG.log(Level.INFO, null, e);
//                } finally {
//                    if (os != null) try { os.close(); } catch (IOException ex) {}
//                    if (lock != null) lock.releaseLock();
//                }
//            }
//        };
//        RequestProcessor.getDefault().post(runnable);
    }

    private Document readModuleDocument(FileObject fo) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setEntityResolver(this);
        parser.setErrorHandler(this);
        Document document;
        try (InputStream is = fo.getInputStream()) {
            document = parser.parse(is);
        }
        return document;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }
    
    @Override
    public void error(SAXParseException exception) {
        LOG.log(Level.INFO, null, exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        LOG.log(Level.INFO, null, exception);
    }

    @Override
    public void warning(SAXParseException exception) {
        LOG.log(Level.INFO, null, exception);
    }
}
