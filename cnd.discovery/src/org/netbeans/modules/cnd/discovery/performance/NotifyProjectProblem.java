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
package org.netbeans.modules.cnd.discovery.performance;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Alexander Simon
 */
@Messages({
    "NotifyProjectProblem.title.text=Detected Project Performance Issues"
    ,"NotifyProjectProblem.action.text=details"
    ,"NotifyProjectProblem.open.message.text=Slow Project Opening"
    ,"NotifyProjectProblem.open.adviceL.text=<li>Copy the project to the local file system</li>\n"
                                           +"<li>Copy the project to a faster file system</li>\n"
                                           +"<li>Copy the project to the memory file system</li>\n"
                                           +"<li>Turn off encryption, virus scanning, compressing, snapshoting for the project</li>\n"
    ,"NotifyProjectProblem.open.adviceR.text=<li>Use VNC or X11 forwarding instead of remote</li>\n"
                                           +"<li>Copy the project to the local file system</li>\n"
    ,"# {0} - advice"
    ,"# {1} - details"
    ,"NotifyProjectProblem.open.explanation.text=The IDE has detected slowness while opening the project, which is caused by slow file system.<br>\n"
                                               +"To resolve this problem, you can try:<br>\n"
                                               +"<ul>\n"
                                               +"{0}"
                                               +"</ul>\n"
                                               +"Details:<br>\n"
                                               +"{1}"
    ,"NotifyProjectProblem.read.message.text=Slow Project Files Reading"
    ,"NotifyProjectProblem.read.adviceL.text=<li>Copy the project to the local file system</li>\n"
                                           +"<li>Copy the project to a faster file system</li>\n"
                                           +"<li>Copy the project to the memory file system</li>\n"
                                           +"<li>Turn off encryption, virus scanning, compressing, snapshoting for the project</li>\n"
    ,"NotifyProjectProblem.read.adviceR.text=<li>Use VNC or X11 forwarding instead of remote</li>\n"
                                           +"<li>Copy the project to the local file system</li>\n"
    ,"# {0} - advice"
    ,"# {1} - details"
    ,"NotifyProjectProblem.read.explanation.text=The IDE has detected slowness while reading project files, which is caused by slow file system.<br>\n"
                                               +"To resolve this problem, you can try:<br>\n"
                                               +"<ul>\n"
                                               +"{0}"
                                               +"</ul>\n"
                                               +"Details:<br>\n"
                                               +"{1}"
    ,"NotifyProjectProblem.parse.message.text=Slow Project Indexing"
    ,"NotifyProjectProblem.parse.adviceL.text=<li>Copy the project to the local file system</li>\n"
                                            +"<li>Copy the project to a faster file system</li>\n"
                                            +"<li>Copy the project to the memory file system</li>\n"
                                            +"<li>Turn off encryption, virus scanning, compressing, snapshoting for the project</li>\n"
                                            +"<li>Open the project on a more powerful computer by using VNC or X11 forwarding</li>\n"
                                            +"<li>Locate the IDE cache on the memory file system</li>\n"
                                            +"<li>Use the relocatable C/C++ index</li>\n"
    ,"NotifyProjectProblem.parse.adviceR.text=<li>Use VNC or X11 forwarding instead of remote</li>\n"
                                            +"<li>Copy the project to the local file system</li>\n"
    ,"# {0} - advice"
    ,"# {1} - details"
    ,"NotifyProjectProblem.parse.explanation.text=The IDE has detected slowness while parsing project files, which is caused by slow file system.<br>\n"
                                                +"To resolve this problem, you can try:<br>\n"
                                                +"<ul>\n"
                                                +"{0}"
                                                +"</ul>\n"
                                                +"Details:<br>\n"
                                                +"{1}"
})
public class NotifyProjectProblem extends javax.swing.JPanel {
    public static final int CREATE_PROBLEM = 1;
    public static final int READ_PROBLEM = 2;
    public static final int PARSE_PROBLEM = 3;

    /**
     * Creates new form NotifyProjectProblem
     */
    private NotifyProjectProblem(PerformanceIssueDetector detector, String details) {
        initComponents();
        explanation.setEditorKit(new HTMLEditorKit());
        explanation.setBackground(getBackground());
        explanation.setText(details);
    }

    public static void showNotification(final PerformanceIssueDetector detector, int problem, final String details,
            boolean isRemoteBuildHost, boolean isRemoteSources) {
        
        final String explanation;
        final String shortDescription;
        switch (problem) {
            case CREATE_PROBLEM: {
                String advice;
                if (isRemoteSources) {
                    advice = Bundle.NotifyProjectProblem_open_adviceR_text();
                } else {
                    advice = Bundle.NotifyProjectProblem_open_adviceL_text();
                }
                explanation = Bundle.NotifyProjectProblem_open_explanation_text(advice, details);
                shortDescription = Bundle.NotifyProjectProblem_open_message_text();
                break;
            }
            case READ_PROBLEM: {
                String advice;
                if (isRemoteSources) {
                    advice = Bundle.NotifyProjectProblem_read_adviceR_text();
                } else {
                    advice = Bundle.NotifyProjectProblem_read_adviceL_text();
                }
                explanation = Bundle.NotifyProjectProblem_read_explanation_text(advice, details);
                shortDescription = Bundle.NotifyProjectProblem_read_message_text();
                break;
            }
            case PARSE_PROBLEM: {
                String advice;
                if (isRemoteSources) {
                    advice = Bundle.NotifyProjectProblem_parse_adviceR_text();
                } else {
                    advice = Bundle.NotifyProjectProblem_parse_adviceL_text();
                }
                explanation = Bundle.NotifyProjectProblem_parse_explanation_text(advice, details);
                shortDescription = Bundle.NotifyProjectProblem_parse_message_text();
                break;
            }
            default:
                throw new IllegalArgumentException();
        }

        ActionListener onClickAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                NotifyProjectProblem panel = new NotifyProjectProblem(detector, explanation);
                DialogDescriptor descriptor = new DialogDescriptor(panel,
                        Bundle.NotifyProjectProblem_title_text(),
                        true, new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                        DialogDescriptor.DEFAULT_ALIGN, null, null);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                try {
                    dlg.setVisible(true);
                } catch (Throwable th) {
                    if (!(th.getCause() instanceof InterruptedException)) {
                        throw new RuntimeException(th);
                    }
                    descriptor.setValue(DialogDescriptor.CANCEL_OPTION);
                } finally {
                    dlg.dispose();
                }
            }
        };
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/discovery/performance/exclamation.gif", false); // NOI18N
        final Notification notification = NotificationDisplayer.getDefault().notify(shortDescription, icon,
                Bundle.NotifyProjectProblem_action_text(), onClickAction, NotificationDisplayer.Priority.HIGH); // NOI18N
        //RP.post(new Runnable() {
        //    @Override
        //    public void run() {
        //        notification.clear();
        //    }
        //}, 150 * 1000);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        explanation = new javax.swing.JTextPane();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.BorderLayout());

        scrollPane.setViewportView(explanation);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane explanation;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
