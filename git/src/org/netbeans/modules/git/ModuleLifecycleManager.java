/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.git;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Handles module events distributed by NetBeans module
 * framework.
 *
 * <p>It's registered and instantiated from module manifest.
 *
 * @author Petr Kuzel
 * @author Maros Sandor
 * @author Tomas Stupka
 */
public final class ModuleLifecycleManager extends ModuleInstall implements ErrorHandler, EntityResolver {

    static final String [] otherGitModules = {
        "org.nbgit" // NOI18N
    };
    
    @Override
    public void restored() {
        disableOtherModules();
    }

    private void disableOtherModules() {
        Runnable runnable = new Runnable() {
            public void run() {
                boolean notify = false;
                outter: for (int i = 0; i < otherGitModules.length; i++) {
                    FileLock lock = null;
                    OutputStream os = null;
                    try {
                        String newModule = otherGitModules[i];
                        String newModuleXML = "Modules/" + newModule.replace('.', '-') + ".xml"; // NOI18N
                        FileObject fo = FileUtil.getConfigFile(newModuleXML);
                        if (fo == null) continue;
                        Document document = readModuleDocument(fo);

                        NodeList list = document.getDocumentElement().getElementsByTagName("param"); // NOI18N
                        int n = list.getLength();
                        for (int j = 0; j < n; j++) {
                            Element node = (Element) list.item(j);
                            if ("enabled".equals(node.getAttribute("name"))) { // NOI18N
                                Text text = (Text) node.getChildNodes().item(0);
                                String value = text.getNodeValue();
                                if ("true".equals(value)) { // NOI18N
                                    text.setNodeValue("false"); // NOI18N
                                    break;
                                } else {
                                    continue outter;
                                }
                            }
                        }
                        notify = true;                            
                        lock = fo.lock();
                        os = fo.getOutputStream(lock);
                        
                        XMLUtil.write(document, os, "UTF-8"); // NOI18N
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    } finally {
                        if (os != null) try { os.close(); } catch (IOException ex) {}
                        if (lock != null) lock.releaseLock();
                    }
                }
                if(notify) {
                    OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if(OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                                try {
                                    if(InstallWarningPanel.open()) {
                                        LifecycleManager.getDefault().markForRestart();
                                        LifecycleManager.getDefault().exit();                                
                                    }
                                } finally {
                                    OpenProjects.getDefault().removePropertyChangeListener(this);
                                }
                           }
                       }
                    });
                }
            }
        };
        RequestProcessor.getDefault().post(runnable);
    }

    private Document readModuleDocument(FileObject fo) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setEntityResolver(this);
        parser.setErrorHandler(this);
        InputStream is = fo.getInputStream();
        Document document = parser.parse(is);
        is.close();
        return document;
    }

    @Override
    public void uninstalled() { }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }
    
    @Override
    public void error(SAXParseException exception) {
        Git.LOG.log(Level.INFO, null, exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        Git.LOG.log(Level.INFO, null, exception);
    }

    @Override
    public void warning(SAXParseException exception) {
        Git.LOG.log(Level.INFO, null, exception);
    }
    
    private static class InstallWarningPanel extends javax.swing.JPanel {

        /** Creates new form InstallWarningPanel */
        public InstallWarningPanel() {
            initComponents();
        }

        static boolean open() {
            InstallWarningPanel panel= new InstallWarningPanel();
            DialogDescriptor dd = 
                    new DialogDescriptor(
                        panel, 
                        NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning_Title"), 
                        true, 
                        DialogDescriptor.YES_NO_OPTION, 
                        null, null);        

            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
            dialog.pack();
            dialog.setVisible(true);
            return dd.getValue() == DialogDescriptor.YES_OPTION;
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();

            jLabel1.setText(org.openide.util.NbBundle.getMessage(InstallWarningPanel.class, "MSG_Install_Warning")); // NOI18N

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }// </editor-fold>

        // Variables declaration - do not modify
        private javax.swing.JLabel jLabel1;
        // End of variables declaration
    }

}
