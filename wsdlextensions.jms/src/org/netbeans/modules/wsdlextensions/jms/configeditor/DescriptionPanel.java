/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * DescriptionPanel.java
 *
 * Created on Jul 13, 2008, 4:46:45 PM
 */

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author jalmero
 */
public class DescriptionPanel extends javax.swing.JPanel {

    public static String SHOW_DESCRIPTION = "Show Description";  //NOI18N

    /** style document for description area **/
    private StyledDocument mDoc = null;
    private String[] mStyles = new String[]{"bold", "regular"};

    private static final Logger mLogger = Logger.
            getLogger(DescriptionPanel.class.getName());
    private MyPopupAction mPopupAction = null;
    private JPopupMenu mPopupMenu = null;
    private JCheckBoxMenuItem mShowDescriptionMenu = null;
    private boolean mConfigShow = true;
    private DescriptionPanel mInstance = null;

    public DescriptionPanel(boolean configShow) {
        this();
        mConfigShow = configShow;
    }

    /** Creates new form DescriptionPanel */
    public DescriptionPanel() {
        initComponents();
        mInstance = this;
//        initListener();        
    }

    public void setText(String title, String desc) {
        if (desc != null) {

            try {
                if (mDoc.getLength() > 0) {
                    mDoc.remove(0, mDoc.getLength());
                }
                if ((title != null) && (title.length() > 0)) {
                    mDoc.insertString(mDoc.getLength(), title,
                            mDoc.getStyle(mStyles[0]));
                }
                mDoc.insertString(mDoc.getLength(), desc,
                        mDoc.getStyle(mStyles[1]));

                descriptionTextPane.setCaretPosition(0);
                
                String newDesc = "";
                boolean isHTML = false;
                if ((title != null) && (title.length() > 0)) {
                    int htmlPos = desc.indexOf("<html>");                    
                    if (htmlPos != -1) {
                        newDesc = desc.substring(htmlPos + 6);
                        newDesc = "<html><strong>" + title + 
                                "</strong><br><br>" + newDesc;
                        isHTML = true;
                    }                                   
                } 
                if (!isHTML) {
                    newDesc = "<html><strong>" + title + "</strong><br><br>" + 
                            desc + "</html>";
                }
                desc = newDesc;
                editorPane.setText(desc);
                editorPane.setCaretPosition(0);                
            } catch(BadLocationException ble) {
                mLogger.log(Level.FINER, ble.getMessage());
            }
            return;
        }

    }

    private void initListener() {

        if (descriptionTextPane != null) {
            descriptionTextPane.addMouseListener(new MouseAdapter(){
                public void mouseReleased(MouseEvent evt) {
                    showPopup(evt);
                }
            });
        }
    }
    
    public void setText(String desc) {
        setText("", desc);
    }

    public class MyPopupAction extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            if (mShowDescriptionMenu.isSelected()) {
                jPanel1.setVisible(true);
                jPanel1.revalidate();
                firePropertyChange(DescriptionPanel.SHOW_DESCRIPTION, false, true);
            } else {
                jPanel1.setVisible(false);
                firePropertyChange(DescriptionPanel.SHOW_DESCRIPTION, true, false);
            }
        }
    }

    void showPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            if (mPopupAction == null) {
                mPopupAction = new MyPopupAction();

                mPopupMenu = new JPopupMenu();
                mShowDescriptionMenu = new JCheckBoxMenuItem(mPopupAction);
                mPopupMenu.add(mShowDescriptionMenu);
                mShowDescriptionMenu.setText(org.openide.util.NbBundle.getMessage(InboundOneWayMessagePanel.class,
                    "InboundMessagePanel.ShowDescription"));
            }

            Point p = evt.getPoint();
            mPopupMenu.show(evt.getComponent(), p.x, p.y);

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

        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextPane = new javax.swing.JTextPane();
        mDoc = descriptionTextPane.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};
        Style def = StyleContext.getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = mDoc.addStyle("regular", def);
        Style s = mDoc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        jPanel1 = new javax.swing.JPanel();
        editorPaneScrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();

        descriptionScrollPane.setMinimumSize(new java.awt.Dimension(200, 50));
        descriptionScrollPane.setName("descriptionScrollPane"); // NOI18N

        descriptionTextPane.setEditable(false);
        descriptionTextPane.setName("descriptionTextPane"); // NOI18N
        descriptionScrollPane.setViewportView(descriptionTextPane);
        javax.swing.JPanel tmpPanel = new javax.swing.JPanel();
        descriptionTextPane.setBackground(tmpPanel.getBackground());

        setMinimumSize(new java.awt.Dimension(200, 50));
        setName("DescriptionPanel"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        editorPaneScrollPane.setName("editorPaneScrollPane"); // NOI18N

        editorPane.setName("editorPane"); // NOI18N
        editorPaneScrollPane.setViewportView(editorPane);

        editorPane.setOpaque(false);
        editorPane.setBackground(getBackground());
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.getAccessibleContext().setAccessibleName( "Description");//NbBundle.getMessage(DescriptionComponent.class, "ACS_Description") );
    editorPane.getAccessibleContext().setAccessibleDescription( "Description");//NbBundle.getMessage(DescriptionComponent.class, "ACSD_Description") );

    HTMLEditorKit htmlKit = new HTMLEditorKit();
    if (htmlKit.getStyleSheet().getStyleSheets() == null) {
        javax.swing.text.html.StyleSheet css = new javax.swing.text.html.StyleSheet();
        java.awt.Font f = new JLabel().getFont();
        css.addRule(new StringBuffer("body { font-size: ").append(f.getSize()) // NOI18N
            .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
        css.addStyleSheet(htmlKit.getStyleSheet());
        htmlKit.setStyleSheet(css);
    }
    editorPane.setEditorKit( htmlKit );
    editorPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DescriptionPanel.class, "DescriptionPanel.editorPane.AccessibleContext.accessibleName")); // NOI18N

    jPanel1.add(editorPaneScrollPane, java.awt.BorderLayout.CENTER);

    add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextPane descriptionTextPane;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JScrollPane editorPaneScrollPane;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
