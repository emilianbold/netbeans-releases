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
package org.netbeans.modules.versioning.util;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * Versioning options panel combined from panels for various settings.
 *
 * @author Pavel Buzek
 */
public class VcsAdvancedOptions extends AdvancedOption {
    public String getTooltip() {
        return getDisplayName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(VcsAdvancedOptions.class, "LBL_OptionsPanelName");
    }
    
    public OptionsPanelController create() {
        return new Controller();
    }
    
    private static final class Controller extends OptionsPanelController {
        private VcsAdvancedOptionsPanel panel;
        private boolean initialized = false;
        private Map<String,AdvancedOption> categoryToOption = new HashMap<String,AdvancedOption>();
        private Map<String, JComponent> categoryToPanel = new HashMap<String, JComponent> ();
        private Map<String, OptionsPanelController> categoryToController = new HashMap<String, OptionsPanelController>();
        private List<String> categoriesOrdered = new ArrayList<String>();
        
        private void init(Lookup masterLookup) {
            if (initialized) return;
            initialized = true;
            panel = new VcsAdvancedOptionsPanel();
            
            Lookup lookup = Lookups.forPath("VersioningOptionsDialog"); // NOI18N
            Iterator<? extends AdvancedOption> it = lookup.lookup(new Lookup.Template<AdvancedOption> (AdvancedOption.class)).
                    allInstances().iterator();
            while (it.hasNext()) {
                AdvancedOption option = it.next();
                String category = option.getDisplayName();
//                categoriesOrdered.add(category);
//                categoryToOption.put(category, option);
                OptionsPanelController controller = option.create();
                categoryToController.put(category, controller);
                
//                JPanel panel = new JPanel();
//                panel.setBorder(BorderFactory.createTitledBorder(category));
//                panel.setLayout(new BorderLayout());
                panel.addPanel(category, controller.getComponent(masterLookup));
//                categoryToPanel.put(category, panel);
            }
        }
        
        public JComponent getComponent(Lookup masterLookup) {
            init(masterLookup);
            return panel;
//            if (panel == null) {
//                panel = new VcsAdvancedOptionsPanel();
//                panel.setLayout(new GridBagLayout());
//                int index = 0;
//                for(String category : categoriesOrdered) {
//                    JComponent component = categoryToPanel.get(category);
//                    GridBagConstraints gbc = new GridBagConstraints();
//                    gbc.anchor = GridBagConstraints.NORTHWEST;
//                    gbc.fill = GridBagConstraints.BOTH;
//                    gbc.weightx = 1.0;
//                    gbc.weighty = 1.0;
//                    gbc.gridy = index++;
//                    gbc.gridx = 0;
//                    panel.add(component,gbc);
//                }
//            }
//            
//            return panel;
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void update() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                ((OptionsPanelController) it.next()).update();
        }
        
        public void applyChanges() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                ((OptionsPanelController) it.next()).applyChanges();
        }
        
        public void cancel() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                ((OptionsPanelController) it.next()).cancel();
        }
        
        public boolean isValid() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                if (!((OptionsPanelController) it.next()).isValid())
                    return false;
            return true;
        }
        
        public boolean isChanged() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                if (((OptionsPanelController) it.next()).isChanged())
                    return true;
            return false;
        }
        
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(VcsAdvancedOptions.class);
        }
    }
}
