/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.advanced;

import java.awt.Color;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.options.ui.TabbedPanelModel;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Jancura
 */
public final class Model extends TabbedPanelModel {
    
    private Map/*<String,AdvancedOption>*/ categoryToOption = new HashMap();
    private Map         categoryToPanel = new HashMap ();
    private Map         categoryToController = new HashMap ();
    private Lookup      masterLookup;

    
    public List getCategories () {
        init ();
        List l = new ArrayList (categoryToOption.keySet ());
        Collections.sort(l, Collator.getInstance());
        return l;
    }
    
    public String getToolTip (String category) {
        AdvancedOption option = (AdvancedOption) categoryToOption.get (category);
        return option.getTooltip ();
    }
    
    public JComponent getPanel (String category) {
        init ();
        JComponent panel = (JComponent) categoryToPanel.get (category);
        if (panel != null) return panel;
        AdvancedOption option = (AdvancedOption) categoryToOption.get (category);
        OptionsPanelController controller = option.create ();
        categoryToController.put (category, controller);
        panel = controller.getComponent (masterLookup);
        categoryToPanel.put (category, panel);
        Border b = panel.getBorder ();
        if (b != null)
            b = new CompoundBorder (
                new EmptyBorder (6, 16, 6, 6),
                b
            );
        else
            b = new EmptyBorder (6, 16, 6, 6);
        panel.setBorder (b);
        panel.setBackground (Color.white);
        panel.setMaximumSize (panel.getPreferredSize ());
        return panel;
    }
    
    
    // implementation ..........................................................
    
    void update () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((OptionsPanelController) it.next ()).update ();
    }
    
    void applyChanges () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((OptionsPanelController) it.next ()).applyChanges ();
    }
    
    void cancel () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            ((OptionsPanelController) it.next ()).cancel ();
    }
    
    boolean isValid () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (!((OptionsPanelController) it.next ()).isValid ())
                return false;
        return true;
    }
    
    boolean isChanged () {
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            if (((OptionsPanelController) it.next ()).isChanged ())
                return true;
        return false;
    }
    
    Lookup getLookup () {
        List lookups = new ArrayList ();
        Iterator it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            lookups.add (((OptionsPanelController) it.next ()).getLookup ());
        return new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
    }
    
    HelpCtx getHelpCtx (JComponent panel) {
        Iterator it = categoryToPanel.keySet ().iterator ();
        while (it.hasNext ()) {
            String category = (String) it.next ();
            if (panel == categoryToPanel.get (category)) {
                OptionsPanelController controller = (OptionsPanelController) 
                    categoryToController.get (category);
                return controller.getHelpCtx ();
            }
        }
        return new HelpCtx ("netbeans.optionsDialog.advanced");
    }
    
    private boolean initialized = false;
    
    private void init () {
        if (initialized) return;
        initialized = true;
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
            findResource ("OptionsDialog/Advanced");
        if (fo == null) return;
        Lookup lookup = new FolderLookup (DataFolder.findFolder (fo)).
            getLookup ();
        Iterator it = lookup.lookup (new Lookup.Template (AdvancedOption.class)).
            allInstances ().iterator ();
        while (it.hasNext ()) {
            AdvancedOption option = (AdvancedOption) it.next ();
            categoryToOption.put (option.getDisplayName (), option);
        }
    }
    
    void setLoookup (Lookup masterLookup) {
        this.masterLookup = masterLookup;
    }
}


