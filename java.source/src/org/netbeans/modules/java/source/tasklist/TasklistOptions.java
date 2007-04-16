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
package org.netbeans.modules.java.source.tasklist;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class TasklistOptions extends AdvancedOption {

    public TasklistOptions() {
    }

    public String getDisplayName() {
        return NbBundle.getMessage(TasklistOptions.class, "DN_Tasklist");// "Java Tasklist - Temporary settings";
    }

    public String getTooltip() {
        return NbBundle.getMessage(TasklistOptions.class, "TP_Tasklist");// "Java Tasklist - Temporary settings";
    }

    public OptionsPanelController create() {
        return new TasklistOptionsPanelController();
    }

    private static class TasklistOptionsPanelController extends OptionsPanelController implements ChangeListener {

        private TasklistOptionsPanel panel;
        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public void update() {
            assert SwingUtilities.isEventDispatchThread();
            if (panel == null) {
                getComponent(null);//XXX: should not happen
            }
            
            panel.setDependenciesEnabled(TasklistSettings.isDependencyTrackingEnabled());
            panel.setBadgesEnabled(TasklistSettings.isBadgesEnabled());
            panel.setTasklistEnabled(TasklistSettings.isTasklistEnabled());
        }

        public void applyChanges() {
            if (panel == null)
                return ;
            TasklistSettings.setTasklistsEnabled(panel.getTasklistEnabled());
            TasklistSettings.setDependencyTrackingEnabled(panel.getDependenciesEnabled());
            TasklistSettings.setBadgesEnabled(panel.getBadgesEnabled());
        }

        public void cancel() {
            if (panel == null)
                return ;
            panel.setTasklistEnabled(TasklistSettings.isTasklistEnabled());
            panel.setDependenciesEnabled(TasklistSettings.isDependencyTrackingEnabled());
            panel.setBadgesEnabled(TasklistSettings.isBadgesEnabled());
        }

        public boolean isValid() {
            return true;
        }

        public boolean isChanged() {
            if (panel == null)
                return false;

            return    TasklistSettings.isTasklistEnabled() != panel.getTasklistEnabled()
                   || TasklistSettings.isDependencyTrackingEnabled() != panel.getDependenciesEnabled()
                   || TasklistSettings.isBadgesEnabled() != panel.getBadgesEnabled();
        }

        public JComponent getComponent(Lookup masterLookup) {
            if (panel == null) {
                panel = new TasklistOptionsPanel();
                panel.addChangeListener(this);
            }
            return panel;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        public void stateChanged(ChangeEvent e) {
            pcs.firePropertyChange(PROP_CHANGED, null, null);
        }
        
    }
}
