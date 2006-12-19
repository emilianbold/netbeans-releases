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
    
    private Map<String,AdvancedOption> categoryToOption = new HashMap<String,AdvancedOption>();
    private Map<String, JComponent> categoryToPanel = new HashMap<String, JComponent> ();
    private Map<String, OptionsPanelController> categoryToController = new HashMap<String, OptionsPanelController>();
    private Lookup masterLookup;

    
    public List getCategories () {
        init ();
        List<String> l = new ArrayList<String>(categoryToOption.keySet ());
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
        List<Lookup> lookups = new ArrayList<Lookup> ();
        Iterator<OptionsPanelController> it = categoryToController.values ().iterator ();
        while (it.hasNext ())
            lookups.add (it.next ().getLookup ());
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
        Lookup lookup = new FolderLookup (DataFolder.findFolder (fo)).getLookup ();
        Iterator<? extends AdvancedOption> it = lookup.lookup (new Lookup.Template<AdvancedOption> (AdvancedOption.class)).
                allInstances ().iterator ();
        while (it.hasNext ()) {
            AdvancedOption option = it.next ();
            categoryToOption.put (option.getDisplayName (), option);
        }
    }
    
    void setLoookup (Lookup masterLookup) {
        this.masterLookup = masterLookup;
    }
}


