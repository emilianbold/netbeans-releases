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
package org.netbeans.modules.jira.options;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.AbstractAction;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import static org.netbeans.modules.jira.client.spi.JiraConnectorProvider.Type.REST;
import static org.netbeans.modules.jira.client.spi.JiraConnectorProvider.Type.XMLRPC;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.LifecycleManager;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public final class JiraOptionsController extends OptionsPanelController {
    
    private final JiraOptionsPanel panel;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private Notification restartNotification;

    public JiraOptionsController() {
        panel = new JiraOptionsPanel();
    }
    
    @Override
    public void update() {
        JiraConnectorProvider.Type connectorType = JiraConfig.getInstance().getActiveConnector();
        switch(connectorType) {
            case REST:
                panel.restRadioButton.setSelected(true);
                break;
            case XMLRPC:
                panel.xmlrpcRadioButton.setSelected(true);
                break;
    }
    }
    
    @Override
    public void applyChanges() {
        boolean connectorChanged = connectorChanged();
        JiraConnectorProvider.Type currentConnector = JiraConfig.getInstance().getActiveConnector();
        if(panel.restRadioButton.isSelected()) {
            JiraConfig.getInstance().setActiveConnector(REST);
        } else {
            JiraConfig.getInstance().setActiveConnector(XMLRPC);
        }
                
        if(connectorChanged && currentConnector != null) {
            askForRestart();
    }
    }
    
    @Override
    public void cancel() {
        update();
    }
    
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return connectorChanged();
    }
        
    private boolean connectorChanged() {
        JiraConnectorProvider.Type connectorType = JiraConfig.getInstance().getActiveConnector();
        switch(connectorType) {
            case REST:
                return panel.xmlrpcRadioButton.isSelected();
            case XMLRPC:
                return panel.restRadioButton.isSelected();
        }   
        return false;
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(getClass());
    }
    
    @Override
    public javax.swing.JComponent getComponent(org.openide.util.Lookup masterLookup) {
        return panel;
    }
    
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    @NbBundle.Messages({"CTL_Restart=Restart IDE",
                        "CTL_RestartClickHere=Click here to restart IDE before the JIRA connector change will have any effect."})
    private void askForRestart() {
        if( null != restartNotification ) {
            restartNotification.clear();
        }
        restartNotification = NotificationDisplayer.getDefault().notify( 
                Bundle.CTL_Restart(),
                ImageUtilities.loadImageIcon( "org/netbeans/modules/jira/resources/restart.png", true ), //NOI18N
                Bundle.CTL_RestartClickHere(), new AbstractAction(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LifecycleManager.getDefault().markForRestart();
                        LifecycleManager.getDefault().exit();
                    }
                },
                NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.INFO);
    }    
    
}
