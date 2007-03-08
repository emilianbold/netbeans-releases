/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer.jsf.ui;

import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.ErrorPanel;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.ErrorPanelCallback;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.openide.util.NbBundle;


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
public class RenderErrorPanelImpl extends JPanel implements ActionListener, ErrorPanel {
//    private WebForm webform;
    private final FacesModel facesModel;
    private final ErrorPanelCallback errorPanelCallback;
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
    public RenderErrorPanelImpl(/*WebForm webform*/FacesModel facesModel, ErrorPanelCallback errorPanelCallback, RenderFailureProvider renderFailureProvider) {
//        this.webform = webform;
        this.facesModel = facesModel;
        this.errorPanelCallback = errorPanelCallback;
        this.renderFailureProvider = renderFailureProvider;

        initComponents();
        updateErrors();

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
        boolean missingBodyElement = facesModel.getHtmlBody() == null;
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
        errorPanelCallback.handleRefresh(facesModel.isBusted());
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

        setLayout(new java.awt.GridBagLayout());

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        jPanel2.setBackground(java.awt.Color.red);
        jLabel3.setFont(new java.awt.Font("Dialog", 1, 24));
        jLabel3.setForeground(java.awt.Color.white);
        jLabel3.setText(NbBundle.getMessage(RenderErrorPanelImpl.class, "CompRenderError"));
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

        continueButton.setText(org.openide.util.NbBundle.getBundle(RenderErrorPanelImpl.class).getString("Continue"));
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

    }
    // </editor-fold>//GEN-END:initComponents


    // XXX
    public interface RenderFailureProvider {
        public Exception getRenderFailureException();
        public MarkupDesignBean getRenderFailureComponent();
    } // End of RenderFailureProvider

}
