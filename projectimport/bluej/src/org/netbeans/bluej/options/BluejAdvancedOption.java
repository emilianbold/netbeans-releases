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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.bluej.options;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class BluejAdvancedOption extends org.netbeans.spi.options.OptionsCategory {

    /** Creates a new instance of BluejAdvancedOption */
    public BluejAdvancedOption() {
    }

    public OptionsPanelController create() {
        return new Controller();
    }

    public String getCategoryName() {
        return NbBundle.getBundle(BluejAdvancedOption.class).getString("AdvanceOption_title"); // NOI18N
    }

    public String getTitle() {
        return NbBundle.getBundle(BluejAdvancedOption.class).getString("AdvancedOption_tooltip"); // NOI18N
    }

    public Icon getIcon() {
        return new ImageIcon(Utilities.loadImage("org/netbeans/bluej/options/Category.png")); // NOI18N
    }
    
    
    static class Controller extends OptionsPanelController {
        private List listeners = new ArrayList();
        private BlueJPanel panel;
        
        public void update() {
            File fil = BlueJSettings.getDefault().getHome();
            getPanel().setBlueJHome(fil != null ? fil.getAbsolutePath() : "");  // NOI18N
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
