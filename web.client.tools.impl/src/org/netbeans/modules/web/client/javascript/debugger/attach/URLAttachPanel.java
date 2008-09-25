/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * URLAttachPanel.java
 *
 * Created on Jun 30, 2008, 1:48:56 PM
 */

package org.netbeans.modules.web.client.javascript.debugger.attach;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSFileObjectLocation;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.NbJSLocation;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.netbeans.modules.web.client.tools.impl.DebugConstants;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sc32560
 */
public class URLAttachPanel extends javax.swing.JPanel implements Controller {

    private static Preferences preferences = NbPreferences.forModule(URLAttachPanel.class);
    private static final String DEBUG_URL = "debugURL";
    private static final String BROWSER = "browser";
    private static final Object[] MAPPERS = { new FileToFileUriMapper(), new FileUriToFileMapper() };
    
    private final boolean ieBrowserSupported;
    private final boolean ffBrowserSupported;

    /** Creates new form URLAttachPanel */
    public URLAttachPanel() {
        initComponents();        
        debugURLTextField.setText(preferences.get(DEBUG_URL,""));
        debugURLTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
            }

            private void update() {                
                firePropertyChange(PROP_VALID, null, null);
                messageTextField.setText("");
            }
        });        

        ieBrowserSupported = WebClientToolsProjectUtils.isInternetExplorerSupported();
        ffBrowserSupported = WebClientToolsProjectUtils.isFirefoxSupported();

        if (ieBrowserSupported && ffBrowserSupported) {
            String globalBrowser = preferences.get(DebugConstants.BROWSER, WebClientToolsProjectUtils.Browser.FIREFOX.name());
            String browser = preferences.get(BROWSER, globalBrowser);
            firefoxRadioButton.setSelected(WebClientToolsProjectUtils.Browser.valueOf(browser) == WebClientToolsProjectUtils.Browser.FIREFOX);
            internetExplorerRadioButton.setSelected(WebClientToolsProjectUtils.Browser.valueOf(browser) == WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER);
        } else if (!ieBrowserSupported && !ffBrowserSupported) {
            debugURLTextField.setEnabled(false);
            debugURLTextField.setEditable(false);
            firefoxRadioButton.setEnabled(false);
            internetExplorerRadioButton.setEnabled(false);
            messageTextField.setText(NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel_noSupportedBrowserMsg"));            
        } else {            
            firefoxRadioButton.setEnabled(ffBrowserSupported);
            firefoxRadioButton.setSelected(ffBrowserSupported);
            
            internetExplorerRadioButton.setEnabled(ieBrowserSupported);
            internetExplorerRadioButton.setSelected(ieBrowserSupported);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        browserButtonGroup = new javax.swing.ButtonGroup();
        debugURLLabel = new javax.swing.JLabel();
        debugURLTextField = new javax.swing.JTextField();
        firefoxRadioButton = new javax.swing.JRadioButton();
        internetExplorerRadioButton = new javax.swing.JRadioButton();
        messageTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        debugURLLabel.setLabelFor(debugURLTextField);
        org.openide.awt.Mnemonics.setLocalizedText(debugURLLabel, org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.debugURLLabel.text")); // NOI18N

        browserButtonGroup.add(firefoxRadioButton);
        firefoxRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(firefoxRadioButton, org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.firefoxRadioButton.text")); // NOI18N

        browserButtonGroup.add(internetExplorerRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(internetExplorerRadioButton, org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.internetExplorerRadioButton.text")); // NOI18N
        internetExplorerRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.internetExplorerRadioButton.tooltip")); // NOI18N

        messageTextField.setBackground(javax.swing.UIManager.getColor("Panel.background"));
        messageTextField.setEditable(false);
        messageTextField.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(messageTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(debugURLLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(internetExplorerRadioButton)
                            .add(firefoxRadioButton)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(debugURLTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(debugURLLabel)
                    .add(browseButton)
                    .add(debugURLTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(firefoxRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(internetExplorerRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(messageTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        debugURLTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.debugURLTextField.AccessibleContext.accessibleName")); // NOI18N
        debugURLTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.debugURLTextField.AccessibleContext.accessibleDescription")); // NOI18N
        firefoxRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.firefoxRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(URLAttachPanel.class, "URLAttachPanel.jButton1.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    
    String currentFilePath = null;
    try {
        String text = debugURLTextField.getText().trim();
        if (text.length() > 0) {
            URI uri = new URI(text);
            File f = new File(uri);
            currentFilePath = f.getAbsoluteFile().getAbsolutePath();
        }
    } catch (IllegalArgumentException ex) {
        currentFilePath = null;
    } catch (URISyntaxException ex) {
        currentFilePath = null;
    }
    
    
    File fileName = currentFilePath == null ? null : new File(currentFilePath);
    if (fileName != null) {
        chooser.setSelectedFile(fileName);
    }
    
    int result = chooser.showOpenDialog(this);
    File f = chooser.getSelectedFile();
    if (result == JFileChooser.APPROVE_OPTION && f != null) {
        if (f != null) {
            debugURLTextField.setText(f.toURI().toASCIIString());
        }
    }
}//GEN-LAST:event_browseButtonActionPerformed

    @Override
    public boolean isValid() {
        return ieBrowserSupported || ffBrowserSupported;
    }

    public boolean ok() {
        preferences.put(DEBUG_URL, debugURLTextField.getText());
        if (Utilities.isWindows() && internetExplorerRadioButton.isSelected()) {
            preferences.put(BROWSER, WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER.name());
        } else {
            preferences.put(BROWSER, WebClientToolsProjectUtils.Browser.FIREFOX.name());            
        }
        if (WebClientToolsSessionStarterService.isAvailable()) {
            try {
                String text = debugURLTextField.getText().trim();
                if (text.length() == 0) {
                    return false;
                }

                URI uri = new URI(text);
                try {
                    Factory htmlBrowserFactory = null;
                    if (internetExplorerRadioButton.isSelected()) {
                        htmlBrowserFactory = WebClientToolsProjectUtils.getInternetExplorerBrowser();
                    } else {
                        htmlBrowserFactory = WebClientToolsProjectUtils.getFirefoxBrowser();
                    }
                    if (htmlBrowserFactory == null) {
                        // TODO Display message
                        try {
                            // Simply launch the URL in the browser
                            HtmlBrowser.URLDisplayer.getDefault().showURL(uri.toURL());
                        } catch (MalformedURLException ex) {
                            messageTextField.setText(ex.getMessage());
                            return false;
                        }
                    } else {
                        WebClientToolsSessionStarterService.startSession(uri, htmlBrowserFactory, Lookups.fixed(MAPPERS));
                    }
                } catch (WebClientToolsSessionException ex) {
                    StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                }
            } catch (URISyntaxException ex) {
                messageTextField.setText(ex.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean cancel() {        
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup browserButtonGroup;
    private javax.swing.JLabel debugURLLabel;
    private javax.swing.JTextField debugURLTextField;
    private javax.swing.JRadioButton firefoxRadioButton;
    private javax.swing.JRadioButton internetExplorerRadioButton;
    private javax.swing.JTextField messageTextField;
    // End of variables declaration//GEN-END:variables

    private static final class FileUriToFileMapper implements JSToNbJSLocationMapper {
        
        public NbJSLocation getNbJSLocation(JSLocation jsLocation, Lookup lookup) {
            if (jsLocation instanceof JSURILocation) {
                JSURILocation jsURILocation = (JSURILocation) jsLocation;
                URI uri = jsURILocation.getURI();
                try {
                    File f = new File(uri);
                    if (f.exists() && f.isFile()) {
                        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                        if (hasSupportedMIMEType(fo)) {
                            return new NbJSFileObjectLocation(fo, jsURILocation.getLineNumber(), jsURILocation.getColumnNumber());
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    return null;
                }
                
            }
            return null;
        }
        
    }
    
    private static final class FileToFileUriMapper implements NbJSToJSLocationMapper {

        public JSLocation getJSLocation(NbJSLocation nbJSLocation, Lookup lookup) {
            if (nbJSLocation instanceof NbJSFileObjectLocation) {
                NbJSFileObjectLocation nbJSFileObjectLocation = (NbJSFileObjectLocation) nbJSLocation;
                FileObject fo = nbJSFileObjectLocation.getFileObject();
                if (hasSupportedMIMEType(fo)) {
                    File file = FileUtil.toFile(fo);
                    if (file != null) {
                        URI uri = file.toURI();
                        return new JSURILocation(uri, nbJSFileObjectLocation.getLineNumber(), nbJSFileObjectLocation.getColumnNumber());
                    }
                }
            }
            return null;
        }
        
    }
    
    private static final String[] SUPPORTED_MIME_TYPES = {
        "text/html",       // NOI18N
        "text/javascript"  // NOI18N
    };
     
    private static boolean hasSupportedMIMEType(FileObject fo) {
        if (fo == null) {
            return false;
        }
        String mime = fo.getMIMEType();
        for (String supportedType : SUPPORTED_MIME_TYPES) {
            if (supportedType.equals(mime)) {
                return true;
            }
        }

        return false;
    }
}
