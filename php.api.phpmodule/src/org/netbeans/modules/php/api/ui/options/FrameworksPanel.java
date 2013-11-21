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
package org.netbeans.modules.php.api.ui.options;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Visual panel for Frameworks and Tools options.
 * 
 * @author S. Aubrecht
 */
@org.netbeans.api.annotations.common.SuppressWarnings("SE_BAD_FIELD_STORE")
@OptionsPanelController.Keywords(keywords={"php"},
        location=UiUtils.OPTIONS_PATH, tabTitle="#LBL_FrameworksTabTitle")
final class FrameworksPanel extends javax.swing.JPanel {

    private final Lookup masterLookup;
    private final FrameworksOptionsPanelController masterController;
    private final ArrayList<AdvancedOption> options;
    private final Map<AdvancedOption, OptionsPanelController> option2controller;
    private final Map<AdvancedOption, JComponent> option2panel;
    private final Map<AdvancedOption, List<String>> option2keywords;
    private final PropertyChangeListener changeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            masterController.fireChange(evt);
        }
    };

    FrameworksPanel(FrameworksOptionsPanelController masterController, Lookup masterLookup, 
            ArrayList<AdvancedOption> options) {
        this.masterLookup = masterLookup;
        this.masterController = masterController;
        initComponents();
        this.options = new ArrayList<>(options);
        final Collator collator = Collator.getInstance();
        Collections.sort(this.options, new Comparator<AdvancedOption>() {
            @Override
            public int compare(AdvancedOption o1, AdvancedOption o2) {
                return collator.compare(o1.getDisplayName(), o2.getDisplayName());
            }
        });
        option2controller = new HashMap<>(options.size());
        option2panel = new HashMap<>(options.size());
        option2keywords = new HashMap<>(options.size());
        DefaultListModel model = new DefaultListModel();
        for( AdvancedOption ao : this.options ) {
            model.addElement( ao.getDisplayName() );
        }
        listCategories.setModel(model);
        listCategories.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                switchPanel();
            }
        });
        listCategories.setSelectedIndex(0);
        listCategories.setVisibleRowCount(Math.min(model.getSize(), 20));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblCategories = new javax.swing.JLabel();
        scrollCategories = new javax.swing.JScrollPane();
        listCategories = new javax.swing.JList();
        panelContent = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblCategories, NbBundle.getBundle(FrameworksPanel.class).getString("FrameworksPanel.lblCategories.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(lblCategories, gridBagConstraints);

        listCategories.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollCategories.setViewportView(listCategories);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(scrollCategories, gridBagConstraints);

        panelContent.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(panelContent, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCategories;
    private javax.swing.JList listCategories;
    private javax.swing.JPanel panelContent;
    private javax.swing.JScrollPane scrollCategories;
    // End of variables declaration//GEN-END:variables

    OptionsPanelController getSelectedController() {
        int selIndex = listCategories.getSelectedIndex();
        if( selIndex < 0 )
            return null;
        AdvancedOption option = getSelectedOption();
        if( null == option )
            return null;
        return option2controller.get(option);
    }
    
    private AdvancedOption getSelectedOption() {
        int selIndex = listCategories.getSelectedIndex();
        if( selIndex < 0 )
            return null;
        return options.get(selIndex);
    }
    
    
    private void switchPanel() {
        panelContent.removeAll();
        
        AdvancedOption selOption = getSelectedOption();
        if( null != selOption ) {
            OptionsPanelController controller = getController(selOption);
            controller.update();
        }
        OptionsPanelController selection = getSelectedController();
        if( null != selection ) {
            panelContent.add(selection.getComponent(Lookup.EMPTY), BorderLayout.CENTER);
        }
        panelContent.invalidate();
        panelContent.revalidate();
        panelContent.repaint();
    }
    
    private OptionsPanelController getController( AdvancedOption option ) {
        OptionsPanelController controller = option2controller.get(option);
        if( null == controller ) {
            controller = option.create();
            option2controller.put(option, controller);
            JComponent panel = controller.getComponent(masterLookup);
            option2panel.put(option, panel);
            controller.addPropertyChangeListener(changeListener);
        }
        return controller;
    }
    
    void handleSearch( List<String> matchedKeywords ) {
        for( AdvancedOption option : options ) {
            List<String> keywords = option2keywords.get(option);
            if( null == keywords ) {
                keywords = loadKeywords( option );
                option2keywords.put(option, keywords);
            }
            for( String kw : matchedKeywords ) {
                if( keywords.contains(kw) ) {
                    setSelecteOption(option);
                    return;
                }
            }
        }
    }
    
    private List<String> loadKeywords( AdvancedOption option ) {
        OptionsPanelController controller = getController(option);
        JComponent panel = controller.getComponent(masterLookup);
        String id = "OptionsDialog/Keywords/" + panel.getClass().getName(); //NOI18N
        ArrayList<String> res = new ArrayList<>(20);
        FileObject keywordsFO = FileUtil.getConfigFile(id);
        if( null != keywordsFO ) {
            Enumeration<String> attributes = keywordsFO.getAttributes();
            while(attributes.hasMoreElements()) {
                String attribute = attributes.nextElement();
                if(attribute.startsWith("keywords")) { //NOI18N
                    String word = keywordsFO.getAttribute(attribute).toString();
                    res.add(word.toUpperCase());
                }
            }
        }
        
        return res;
    }

    void update() {
        for( OptionsPanelController controller : option2controller.values() ) {
            controller.update();
        }
    }

    void applyChanges() {
        for( OptionsPanelController controller : option2controller.values() ) {
            controller.applyChanges();
        }
    }

    void cancel() {
        for( OptionsPanelController controller : option2controller.values() ) {
            controller.cancel();
        }
    }

    boolean isControllerValid() {
        for( OptionsPanelController controller : option2controller.values() ) {
            if( !controller.isValid() )
                return false;
        }
        return true;
    }

    boolean isChanged() {
        for( OptionsPanelController controller : option2controller.values() ) {
            if( controller.isChanged())
                return true;
        }
        return false;
    }

    void setSelecteOption(AdvancedOption option) {
        int index = options.indexOf(option);
        if( index >= 0 )
            listCategories.setSelectedIndex(index);
    }
}
