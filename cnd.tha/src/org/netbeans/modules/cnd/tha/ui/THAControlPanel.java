/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.tha.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.tha.actions.THAActionsProvider;
import org.netbeans.modules.dlight.perfan.tha.api.THAConfiguration;
import org.openide.util.ImageUtilities;

/**
 * 
 * @author mt154047
 */
final class THAControlPanel extends JToolBar{
    private final Project project;
    private final THAConfiguration thaConfiguration;

    static synchronized final THAControlPanel create(Project project, THAConfiguration thaConfiguration){
        return new THAControlPanel(project, thaConfiguration);
    }

    public THAControlPanel(Project project, final THAConfiguration thaConfiguration) {
        this.project = project;
        this.thaConfiguration = thaConfiguration;
        final THAActionsProvider actionsSupport = THAActionsProvider.getSupportFor(project, thaConfiguration);
        final JLabel statusLabel = new JLabel();
        actionsSupport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (THAActionsProvider.SUSPEND_COMMAND.equals(e.getActionCommand())){
                    statusLabel.setText( "Paused " + (thaConfiguration.collectDataRaces() ? "Races and Deadlocks" : "Deadlocks"));
                    statusLabel.setForeground(Color.RED);
                }else if (THAActionsProvider.RESUME_COMMAND.equals(e.getActionCommand())){
                    statusLabel.setText( "Recording " + (thaConfiguration.collectDataRaces() ? "Races and Deadlocks" : "Deadlocks"));
                    statusLabel.setForeground(Color.GREEN);
                }
            }
        });
        Action suspendAction = actionsSupport.getSuspendCollectionAction();
        Action resumeAction = actionsSupport.getResumeCollectionAction();

        statusLabel.setText((thaConfiguration.collectFromBeginning() ? "Recording " :  "Paused ") + (thaConfiguration.collectDataRaces() ? "Races and Deadlocks" : "Deadlocks"));
        statusLabel.setForeground(thaConfiguration.collectFromBeginning()  ? Color.GREEN : Color.RED);
        JButton suspendButton = new JButton(suspendAction);
        suspendButton.setDisabledIcon(ImageUtilities.image2Icon((Image)suspendAction.getValue("disabledIcon")));//NOI18N
        add(suspendButton);
        addSeparator();
        JButton resumeButton = new JButton(resumeAction);
        resumeButton.setDisabledIcon(ImageUtilities.image2Icon((Image)resumeAction.getValue("disabledIcon")));//NOI18N
        add(resumeButton);
        addSeparator();
        add(actionsSupport.getStopAction());
        add(statusLabel);
    }
    

}
