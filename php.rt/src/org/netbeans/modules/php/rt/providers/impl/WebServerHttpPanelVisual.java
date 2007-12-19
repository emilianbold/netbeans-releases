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
package org.netbeans.modules.php.rt.providers.impl;

import org.netbeans.modules.php.rt.providers.impl.local.*;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author  den
 */
public abstract class WebServerHttpPanelVisual extends JPanel{

    private static final long serialVersionUID = -8570337844141338913L;

    private static final String TXT_DEFAULT_PORT = "TXT_DefaultPort"; // NOI18N
    private static final String MSG_INCORRECT_DOMAIN = "MSG_IncorrectSymbolsInDomainName"; // NOI18N
    private static final String MSG_INCORRECT_PATH = "MSG_IncorrectSymbolsInPath"; // NOI18N

    /** Creates new form WebServerHttpPanelVisual */
    WebServerHttpPanelVisual() {
        initComponents();

    }

    protected abstract void setErrorMessage(String message);
    protected abstract void setDefaults();
    protected abstract void stateChanged();

    /**
     * adds listeners to editable text fields.
     * Should be invoked from inheritor's constructor 
     * (XXXServerCustomizerComponent, XXXConfigPanelComponent).
     * It is not invoked from WebServerHttpPanelVisual constructor
     * to make it possible for inheritor to set default values.
     */
    protected void initListeners() {
        setDefaults();

        DocumentListener listener = new TextFieldListener();
        
        myPort.getDocument().addDocumentListener(listener);
        myDomain.getDocument().addDocumentListener(listener);
        myBaseDirectoryPath.getDocument().addDocumentListener(listener);
    }

    public boolean doContentValidation() {
        return  validatePort() 
                && validateDomain()
                && validateBaseDirectory();
    }

    public void doFinalContentValidation() {
        validateDomainFinaly();
    }


    protected String getDomain() {
        return myDomain.getText();
    }

    protected String getPort() {
        String port = myPort.getText();
        if (port == null || port.trim().length() == 0) {
            port = getMessage(TXT_DEFAULT_PORT);
        }
        return port;
    }

    protected String getBaseDirectory() {
        return myBaseDirectoryPath.getText();
    }

    protected void setDomain(String value) {
        myDomain.setText(value);
    }

    protected void setPort(String value) {
        String port = value;
        if (port == null || port.trim().length() == 0) {
            port = getMessage(TXT_DEFAULT_PORT);
        }
        myPort.setText(value);
    }

    protected void setBaseDirectory(String value) {
        myBaseDirectoryPath.setText(value);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myDomainLabel = new javax.swing.JLabel();
        myPortLabel = new javax.swing.JLabel();
        myDomain = new javax.swing.JTextField();
        myPort = new javax.swing.JTextField();
        myBaseDirectoryPathLbl = new javax.swing.JLabel();
        myBaseDirectoryPath = new javax.swing.JTextField();

        myDomainLabel.setLabelFor(myDomain);
        org.openide.awt.Mnemonics.setLocalizedText(myDomainLabel, org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "LBL_Domain")); // NOI18N

        myPortLabel.setLabelFor(myPort);
        org.openide.awt.Mnemonics.setLocalizedText(myPortLabel, org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "LBL_Port")); // NOI18N

        myDomain.setText("");

        myPort.setDocument(new IntegerDocument());
        myPort.setText(org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "TXT_DefaultPort")); // NOI18N

        myBaseDirectoryPathLbl.setLabelFor(myBaseDirectoryPath);
        org.openide.awt.Mnemonics.setLocalizedText(myBaseDirectoryPathLbl, org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "LBL_BaseDirectory")); // NOI18N

        myBaseDirectoryPath.setText(org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "LBL_BaseDirectory_txt")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myBaseDirectoryPathLbl)
                    .add(myDomainLabel))
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(myDomain, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(myPortLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myBaseDirectoryPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myDomainLabel)
                    .add(myPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myPortLabel)
                    .add(myDomain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myBaseDirectoryPathLbl)
                    .add(myBaseDirectoryPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        myDomainLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "A11_DomainNameLbl")); // NOI18N
        myPortLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "A11_PortLbl")); // NOI18N
        myDomain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "A11_DomainName")); // NOI18N
        myPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "A11_Port")); // NOI18N
        myBaseDirectoryPathLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebServerHttpPanelVisual.class, "A11_BaseDirectory")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private boolean validatePort() {
        /*
         * This is check only for sure. No message will appear
         * as error in the case of error becuase port should
         * not be incorrect. It is prevented by special document in
         * port text field.
         */
        return isValidNumber(getPort());
    }

    private boolean validateDomain() {
        String domain = getDomain();
        if (domain == null || domain.trim().length() == 0) {
            // TODO warn about incomplete form
            return true;
        }

        if (!URIValidationUtils.isHostNameValid(domain)) {
            setErrorMessage(getMessage(MSG_INCORRECT_DOMAIN));
            return false;
        }
        
        return true;
    }

    private void validateDomainFinaly() {
        /*
         * check is performed for notification only.
         * User could don't have direct connect to Internet and
         * in this case Proxy can be used ( for name resolution also ).
         */
        String domain = getDomain();
        if (domain == null || domain.trim().length() == 0) {
            // allow empty
            return;
        }
        URIValidationUtils.validateInetAddress(domain);
    }

    private boolean validateBaseDirectory() {
        String baseDir = getBaseDirectory();
        if (!URIValidationUtils.isPathValid(baseDir)) {
            setErrorMessage(getMessage(MSG_INCORRECT_PATH));
            return false;
        }
        return true;
    }
    
    private String getMessage(String key, Object... args) {
        return NbBundle.getMessage(WebServerHttpPanelVisual.class, key, args);
    }

    static boolean isValidNumber(String str) {
        boolean flag;
        try {
            Integer.parseInt(str);
            flag = true;
        } catch (NumberFormatException e) {
            flag = false;
        }
        return flag;
    }

    /*
    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }
     */

    private static class IntegerDocument extends PlainDocument {

        private static final long serialVersionUID = -6127390393638310191L;

        @Override
        public void insertString(int offs, String str, AttributeSet a) 
                throws BadLocationException 
        {

            if (str == null) {
                return;
            }

            if (isValidNumber(str)) {
                super.insertString(offs, str, a);
            }
        }
    }

    private class TextFieldListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            actionPerformed();
        }

        public void insertUpdate(DocumentEvent e) {
            actionPerformed();
        }

        public void removeUpdate(DocumentEvent e) {
            actionPerformed();
        }

        private void actionPerformed() {
            stateChanged();
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField myBaseDirectoryPath;
    private javax.swing.JLabel myBaseDirectoryPathLbl;
    private javax.swing.JTextField myDomain;
    private javax.swing.JLabel myDomainLabel;
    private javax.swing.JTextField myPort;
    private javax.swing.JLabel myPortLabel;
    // End of variables declaration//GEN-END:variables


}
