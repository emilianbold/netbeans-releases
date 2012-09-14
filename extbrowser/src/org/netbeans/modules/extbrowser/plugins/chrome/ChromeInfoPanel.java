/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.extbrowser.plugins.chrome;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;

import org.netbeans.modules.extbrowser.plugins.ExtensionManager.ExtensitionStatus;
import org.netbeans.modules.extbrowser.plugins.PluginLoader;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author ads
 */
class ChromeInfoPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 5394629966593049098L;
    static final Logger LOGGER = Logger.getLogger(ChromeInfoPanel.class.getName());


    ChromeInfoPanel(String pluginPath, final PluginLoader loader,
            ExtensitionStatus currentStatus)
    {
        initComponents();

        File file = new File(pluginPath);
        String name = file.getName();
        String parent = file.getParent();
        StringBuilder text = new StringBuilder("<html>");                       // NOI18N
        if ( currentStatus == ExtensitionStatus.NEEDS_UPGRADE ){
            text.append(NbBundle.getMessage(ChromeInfoPanel.class, "TXT_RequestUpgrade"));// NOI18N
            text.append(" ");           // NOI18N
        }
        text.append(NbBundle.getMessage(ChromeInfoPanel.class,
                "TXT_PluginIstallationIssue" , "file:///"+parent, name ));      // NOI18N
        text.append("</html>");         // NOI18N
        myEditorPane.setText(text.toString());

        HyperlinkListener listener = new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate( HyperlinkEvent e ) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = e.getURL();
                    if (url.getProtocol().equals("file")) { // NOI18N
                        // first, try java api
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.OPEN)) {
                            try {
                                desktop.open(Utilities.toFile(url.toURI()));
                            } catch (IOException ex) {
                                LOGGER.log(Level.FINE, null, ex);
                                openNativeFileManager(url);
                            } catch (URISyntaxException ex) {
                                LOGGER.log(Level.WARNING, null, ex);
                                openNativeFileManager(url);
                            }
                        } else {
                            openNativeFileManager(url);
                        }
                    } else {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                    }
                }
            }

            private void openNativeFileManager(URL url) {
                String executable;
                if (Utilities.isWindows()) {
                    executable = "explorer.exe"; // NOI18N
                } else if (Utilities.isMac()) {
                    executable = "open"; // NOI18N
                } else {
                    assert Utilities.isUnix() : "Unix expected";
                    executable = "xdg-open"; // NOI18N
                }
                try {
                    Process process = new ExternalProcessBuilder(executable)
                            .addArgument(url.toURI().toString())
                            .redirectErrorStream(true)
                            .call();
                    InputReaderTask task = InputReaderTask.newTask(InputReaders.forStream(process.getInputStream(), Charset.defaultCharset()), null);
                    RequestProcessor.getDefault().post(task);
                } catch (URISyntaxException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        };
        myEditorPane.addHyperlinkListener(listener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myScrollPane = new javax.swing.JScrollPane();
        myEditorPane = new javax.swing.JEditorPane();

        myEditorPane.setEditable(false);
        myEditorPane.setContentType("text/html"); // NOI18N
        myEditorPane.setText(org.openide.util.NbBundle.getMessage(ChromeInfoPanel.class, "TXT_PluginIstallationIssue")); // NOI18N
        myScrollPane.setViewportView(myEditorPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(myScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(myScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane myEditorPane;
    private javax.swing.JScrollPane myScrollPane;
    // End of variables declaration//GEN-END:variables
}
