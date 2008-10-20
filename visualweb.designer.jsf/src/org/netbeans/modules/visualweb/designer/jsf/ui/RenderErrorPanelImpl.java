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
package org.netbeans.modules.visualweb.designer.jsf.ui;

import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.openide.util.NbBundle;


// XXX Moved from designer/RenderErrorPanel.


// XXX Moved from designer/RenderErrorPanel.
/**
 * Panel which shows an error label, and a listbox containing errors
 *
 * @todo Unify this code with the code in ImportPagePanel such that JSP tag
 *   handling etc. is handled correctly in both places. There's already some
 *   deviation in what these do.
 * @todo Trap the case where conversion fails, and tell the user that it failed,
 *   in addition to graying out the buttons.
 *
 * @author  Tor Norbye
 */
public class RenderErrorPanelImpl extends JPanel implements ActionListener, JsfForm.ErrorPanel {
//    private WebForm webform;
//    private final FacesModel facesModel;
    private final JsfForm jsfForm;
    private final JsfForm.ErrorPanelCallback errorPanelCallback;
    private final RenderFailureProvider renderFailureProvider;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton continueButton;
    private javax.swing.JTextArea exceptions;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables

    /** Creates new form ErrorPanel */
    public RenderErrorPanelImpl(/*WebForm webform*/JsfForm jsfForm, JsfForm.ErrorPanelCallback errorPanelCallback, RenderFailureProvider renderFailureProvider) {
//        this.webform = webform;
//        this.facesModel = facesModel;
        this.jsfForm = jsfForm;
        this.errorPanelCallback = errorPanelCallback;
        this.renderFailureProvider = renderFailureProvider;

        initComponents();
        updateErrors();

        // XXX #100175 Do not hardcode font sizes.
        // But how to provide larger font nicely?
        Font titleFont = jLabel3.getFont();
        if (titleFont != null) {
            int size = titleFont.getSize();
            float newSize = 2 * size;
            Font newFont = titleFont.deriveFont(newSize);
            jLabel3.setFont(newFont);
        }
        
        continueButton.addActionListener(this);
        textArea.setEnabled(false);

        //textArea.setColor(Color.BLACK);
        textArea.setDisabledTextColor(Color.BLACK);
        textArea.setFont((Font)UIManager.getDefaults().get("Label.font")); // NOI18N

//        webform.setRenderFailureShown(true);
        errorPanelCallback.setRenderFailureShown(true);
    }

    public void updateErrors() {
//        boolean missingBodyElement = webform.getHtmlBody(false) == null;
        boolean missingBodyElement = jsfForm.getHtmlBody(false) == null;
        if (missingBodyElement) {
            jLabel3.setText(NbBundle.getMessage(RenderErrorPanelImpl.class, "LBL_MissingBodyElement"));
            textArea.setText(NbBundle.getMessage(RenderErrorPanelImpl.class, "TXT_MissingBodyElement"));
            // XXX
            continueButton.setVisible(false);
            jScrollPane1.setVisible(false);
        } else {
            jLabel3.setText(NbBundle.getMessage(RenderErrorPanelImpl.class, "CompRenderError"));

//            Exception e = webform.getRenderFailure();
//            MarkupDesignBean bean = webform.getRenderFailureComponent();
//            Exception e = errorPanelCallback.getRenderFailure();
//            MarkupDesignBean bean = errorPanelCallback.getRenderFailureComponent();
            Exception e = renderFailureProvider.getRenderFailureException();
            MarkupDesignBean bean = renderFailureProvider.getRenderFailureComponent();

            if (e == null) {
                // XXX #150606 Error, but no exception with it.
                log(new IllegalStateException("There is no render failure exception for render failure component, bean=" + bean)); // NOI18N
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String trace = sw.toString();
                // XXX #6416590 Very suspicious potentionaly dangerous code.
                // Why to hide the internals from stack, if they might be cause of the issue??
    //            // Chop off Creator internals
    //            while (true) {
    //                int insync = trace.indexOf("org.netbeans.modules.visualweb.insync"); // NOI18N
    //                if (insync != -1) {
    //                    int cause = trace.indexOf("Caused"); // NOI18N
    //                    if (cause == -1) {
    //                        trace = trace.substring(0, insync-3);
    //                    } else {
    //                        trace = trace.substring(0, insync-3) + "\n" + trace.substring(cause);
    //                    }
    //                } else {
    //                    break;
    //                }
    //            }
                exceptions.setText(trace);
            }
            String instanceName = bean.getInstanceName();
            textArea.setText(NbBundle.getMessage(RenderErrorPanelImpl.class, "CompErrorDescription", 
                       instanceName, e.toString()));
            
            continueButton.setVisible(true);
            jScrollPane1.setVisible(true);
        }
    }

    public void actionPerformed(ActionEvent e) {
//        // Continue from the error panel to the designview
//        webform.getTopComponent().showErrors(webform.getModel().isBusted());
//        // 6274302: See if the user has cleared the error
////        webform.refresh(true);
//        webform.refreshModel(true);
//        errorPanelCallback.handleRefresh(jsfForm.getFacesModel().isBusted());
        errorPanelCallback.handleRefresh(jsfForm.isModelBusted());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the NetBeans Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        textArea = new javax.swing.JTextArea();
        continueButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        exceptions = new javax.swing.JTextArea();

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        setLayout(new java.awt.GridBagLayout());

        jPanel2.setBackground(java.awt.Color.red);

        jLabel3.setForeground(java.awt.Color.white);
        jLabel3.setText(NbBundle.getMessage(RenderErrorPanelImpl.class, "CompRenderError")); // NOI18N
        jPanel2.add(jLabel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(textArea, gridBagConstraints);

        continueButton.setText(org.openide.util.NbBundle.getBundle(RenderErrorPanelImpl.class).getString("Continue")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 11);
        add(continueButton, gridBagConstraints);

        jPanel1.setBackground(java.awt.Color.white);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        jScrollPane1.setViewportView(exceptions);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private static void log(Exception ex) {
        Logger.getLogger(RenderErrorPanelImpl.class.getName()).log(Level.INFO, null, ex);
    }


    // XXX
    public interface RenderFailureProvider {
        public Exception getRenderFailureException();
        public MarkupDesignBean getRenderFailureComponent();
    } // End of RenderFailureProvider

}
