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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects;

import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JComponent;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.railsprojects.ui.FoldersListSettings;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Based on options panel in stripwhitespace module by Andrei Badea
 *
 * @author Tor Norbye
 */
public class RailsProjectOption extends AdvancedOption {
    
    public String getTooltip() {
        return getDisplayName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(RailsProjectOption.class, "RailsOption");
    }

    public OptionsPanelController create() {
        return new Controller();
    }

    private static final class Controller extends OptionsPanelController {
        private RailsOptionPanel component;

        public JComponent getComponent(Lookup masterLookup) {
            if (component == null) {
                component = new RailsOptionPanel();
            }

            return component;
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void update() {
            Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        boolean logical = FoldersListSettings.getDefault().getLogicalView();
                        component.setLogicalChosen(logical);
                    }
                });
        }

        public boolean isValid() {
            return true;
        }

        public boolean isChanged() {
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void cancel() {
        }

        public void applyChanges() {
            Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        boolean logical = component.getLogicalChosen();
                        FoldersListSettings.getDefault().setLogicalView(logical);
                    }
                });
        }
    }
}
