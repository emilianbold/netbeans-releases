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

package org.netbeans.modules.options.editor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class FolderBasedController extends OptionsPanelController {
    
    private final Lookup.Result<? extends OptionsPanelController> lookupResult;
    private final LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            rebuild();
        }
    };
    private final HelpCtx helpCtx;
    
    private Collection<? extends OptionsPanelController> delegates;
    private JComponent component;
    
    public FolderBasedController(String path, HelpCtx helpCtx) {
        this.helpCtx = helpCtx;
        
        Lookup lookup = Lookups.forPath(path);
        lookupResult = lookup.lookupResult(OptionsPanelController.class);
        lookupResult.addLookupListener(WeakListeners.create(
                LookupListener.class,
                lookupListener,
                lookupResult
                ));
        rebuild();
    }
    
    public final void update() {
        Collection<? extends OptionsPanelController> controllers = delegates;
        for(OptionsPanelController c : controllers) {
            c.update();
        }
    }
    
    public final void applyChanges() {
        Collection<? extends OptionsPanelController> controllers = delegates;
        for(OptionsPanelController c : controllers) {
            c.applyChanges();
        }
    }
    
    public final void cancel() {
        Collection<? extends OptionsPanelController> controllers = delegates;
        for(OptionsPanelController c : controllers) {
            c.cancel();
        }
    }
    
    public final boolean isValid() {
        Collection<? extends OptionsPanelController> controllers = delegates;
        for(OptionsPanelController c : controllers) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true;
    }
    
    public final boolean isChanged() {
        Collection<? extends OptionsPanelController> controllers = delegates;
        for(OptionsPanelController c : controllers) {
            if (c.isChanged()) {
                return true;
            }
        }
        return false;
    }
    
    public final HelpCtx getHelpCtx() {
        return helpCtx;
    }
    
    public final JComponent getComponent(Lookup masterLookup) {
        if (component == null) {
            Collection<JComponent> panels = new ArrayList<JComponent>();
            Collection<? extends OptionsPanelController> controllers = delegates;
            
            for(OptionsPanelController c : controllers) {
                panels.add(c.getComponent(masterLookup));
            }
            
            component = createComponent(panels);
        }
        
        return component;
    }

    protected JComponent createComponent(Collection<? extends JComponent> panels) {
        return new TabbedPanel(panels);
    }
    
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        Collection<? extends OptionsPanelController> controllers = delegates;
        for(OptionsPanelController c : controllers) {
            c.addPropertyChangeListener(l);
        }
    }
    
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        Collection<? extends OptionsPanelController> controllers = delegates;
        for(OptionsPanelController c : controllers) {
            c.removePropertyChangeListener(l);
        }
    }
    
    private void rebuild() {
        this.delegates = lookupResult.allInstances();
        this.component = null;
    }
    
    private static final class TabbedPanel extends JPanel {
        
        private JTabbedPane tabbedPane = new JTabbedPane();
        
        public TabbedPanel(Collection<? extends JComponent> panels) {
            JLabel label = new JLabel(); // Only for setting tab names
            
            for(JComponent p : panels) {
                p.setBorder(new EmptyBorder(8, 8, 8, 8));
                
                String tabName = p.getName();
                Mnemonics.setLocalizedText(label, tabName);
                tabbedPane.addTab(label.getText(), p);
                
                int idx = Mnemonics.findMnemonicAmpersand(tabName);
                if (idx != -1 && idx + 1 < tabName.length()) {
                    tabbedPane.setMnemonicAt(tabbedPane.getTabCount() - 1, Character.toUpperCase(tabName.charAt(idx + 1)));
                }
            }
            
            setLayout(new BorderLayout());
            add(tabbedPane, BorderLayout.CENTER);
        }
    } // End of TabbedPane class
}
