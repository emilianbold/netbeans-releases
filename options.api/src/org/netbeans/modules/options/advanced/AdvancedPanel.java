/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.advanced;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.modules.options.ui.TabbedPanel;
import org.netbeans.spi.options.OptionsCategory.PanelController;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.options.*;

  
/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AdvancedPanel extends JPanel {

    
    TabbedPanel     tabbedPanel;
    private Model   model;
    
    
    AdvancedPanel () {
        
        // init components
        tabbedPanel = new WhiteTabbedPanel (
            model = new Model (), 
            TabbedPanel.EXPAND_SOME // expansionPolicy 
        );
        tabbedPanel.setBorder (null);
        tabbedPanel.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                firePropertyChange (PanelController.PROP_HELP_CTX, null, null);
            }
        });
        JScrollPane scrollPane = new JScrollPane (tabbedPanel);
        scrollPane.getVerticalScrollBar ().setUnitIncrement (20);
        
        // define layout
        FormLayout layout = new FormLayout (
            "p:g",      // cols
            "p:g");     // rows
        PanelBuilder builder = new PanelBuilder (layout, this);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder.add (scrollPane, cc.xy (1, 1, "f,f"));
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (AdvancedPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }
    
    public void applyChanges () {
        model.applyChanges ();
    }
    
    public void cancel () {
        model.cancel ();
    }
    
    public HelpCtx getHelpCtx () {
        return model.getHelpCtx (tabbedPanel.getSelectedComponent ());
    }
    
    public boolean dataValid () {
        return model.isValid ();
    }
    
    public boolean isChanged () {
        return model.isValid ();
    }
}