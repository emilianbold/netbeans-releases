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

package org.netbeans.modules.options.advanced;

import java.awt.BorderLayout;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.netbeans.modules.options.*;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AdvancedPanel extends JPanel {    
    JTabbedPane     tabbedPanel;    
    private LookupListener listener = new LookupListenerImpl();
    private Model   model = new Model (listener);
    private ChangeListener changeListener = new ChangeListener () {
            public void stateChanged(ChangeEvent e) {
                handleTabSwitched();
            }
        };
    
    AdvancedPanel () {}
        
    public void update () {
        String category = tabbedPanel.getTitleAt(tabbedPanel.getSelectedIndex());
        model.update (category);        
    }
    
    public void applyChanges () {
        model.applyChanges ();
    }
    
    public void cancel () {
        model.cancel ();
    }
    
    public HelpCtx getHelpCtx () {
        return model.getHelpCtx ((JComponent)tabbedPanel.getSelectedComponent ());
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
    
    void init (Lookup masterLookup) {
        // init components
        tabbedPanel = new JTabbedPane();        
        tabbedPanel.setBorder (null);        
        
        // define layout
        setLayout (new BorderLayout ());
        add (tabbedPanel, BorderLayout.CENTER);
        initTabbedPane (masterLookup);
    }
    
    private void initTabbedPane(Lookup masterLookup) {
        tabbedPanel.removeChangeListener(changeListener);
        tabbedPanel.removeAll();
        Iterator it = model.getCategories().iterator();
        while (it.hasNext()) {
            String category = (String) it.next ();
            tabbedPanel.addTab(category, new JLabel(category));
        }
        tabbedPanel.addChangeListener(changeListener);
        handleTabSwitched();
    }

    private void handleTabSwitched() {        
        final int selectedIndex = tabbedPanel.getSelectedIndex() >= 0 ? tabbedPanel.getSelectedIndex() : 0;
        String category = tabbedPanel.getTitleAt(selectedIndex);
        if (tabbedPanel.getSelectedComponent() instanceof JLabel) {
            tabbedPanel.setComponentAt(tabbedPanel.getSelectedIndex(), model.getPanel(category));
        }
        model.update(category);
        firePropertyChange (OptionsPanelController.PROP_HELP_CTX, null, null);        
    }
    
    private class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            Lookup masterLookup = model.getLookup();
            model = new Model(listener);
            initTabbedPane(masterLookup);
        }        
    }
}
