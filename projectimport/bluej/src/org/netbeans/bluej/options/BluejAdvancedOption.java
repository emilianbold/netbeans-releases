/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.bluej.options;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
public class BluejAdvancedOption extends AdvancedOption {
    
    /** Creates a new instance of BluejAdvancedOption */
    public BluejAdvancedOption() {
    }

    public String getDisplayName() {
        return org.openide.util.NbBundle.getBundle(BluejAdvancedOption.class).getString("AdvanceOption_title");
    }

    public String getTooltip() {
        return org.openide.util.NbBundle.getBundle(BluejAdvancedOption.class).getString("AdvancedOption_tooltip");
    }

    public OptionsPanelController create() {
        return new Controller();
    }
    
    
    class Controller extends OptionsPanelController {
        private List listeners = new ArrayList();
        private BlueJPanel panel;
        
        public void update() {
            File fil = BlueJSettings.getDefault().getHome();
            getPanel().setBlueJHome(fil != null ? fil.getAbsolutePath() : "");
        }

        public void applyChanges() {
            String str = getPanel().getBlueJHome().trim();
            File fil = null;
            if (str.length() > 0) {
                fil = new File(str);
            } 
            BlueJSettings.getDefault().setHome(fil);
        }

        public void cancel() {
        }

        public boolean isValid() {
            return getPanel().isValidData();
        }

        public boolean isChanged() {
            return getPanel().isChangedData();
        }

        public JComponent getComponent(Lookup masterLookup) {
            return getPanel();
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            listeners.add(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            listeners.remove(l);
        }

        private BlueJPanel getPanel() {
            if(panel == null) {
                panel = new BlueJPanel();
            }
            return panel;
        }
        
    }
}
