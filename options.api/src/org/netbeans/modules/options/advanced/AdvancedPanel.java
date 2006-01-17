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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.netbeans.modules.options.ui.TabbedPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
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
                firePropertyChange (OptionsPanelController.PROP_HELP_CTX, null, null);
            }
        });
        JScrollPane scrollPane = new JScrollPane (tabbedPanel);
        scrollPane.getVerticalScrollBar ().setUnitIncrement (20);
        
        // define layout
        setLayout (new BorderLayout ());
        add (scrollPane, BorderLayout.CENTER);
        
        int preferredWith = 0;
        Iterator it = model.getCategories ().iterator ();
        while (it.hasNext ()) {
            String category = (String) it.next ();
            JComponent component = model.getPanel (category);
            preferredWith = Math.max (
                preferredWith, 
                component.getPreferredSize ().width + 22
            );
        }
        setPreferredSize (new Dimension (preferredWith, 100));
    }
    
    public void update () {
        model.update ();
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
        return model.isChanged ();
    }
    
    public Lookup getLookup () {
        return model.getLookup ();
    }
    
    void setLoookup (Lookup masterLookup) {
        model.setLoookup (masterLookup);
    }
}
