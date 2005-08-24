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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.options.ui.TabbedPanelModel;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsCategory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class Model extends TabbedPanelModel {
    
    private Map categoryToOption = new HashMap ();
    private Map categoryToPanel = new HashMap ();

    
    public List getCategories () {
        init ();
        List l = new ArrayList (categoryToOption.keySet ());
        Collections.sort (l);
        return l;
    }
    
    public JComponent getPanel (String category) {
        init ();
        JComponent p = (JComponent) categoryToPanel.get (category);
        if (p != null) return p;
        AdvancedOption option = (AdvancedOption) categoryToOption.get (category);
        p = option.getPane ();
        categoryToPanel.put (category, p);
        Border b = p.getBorder ();
        if (b != null)
            b = new CompoundBorder (
                new EmptyBorder (6, 16, 6, 6),
                b
            );
        else
            b = new EmptyBorder (6, 16, 6, 6);
        p.setBorder (b);
        p.setBackground (Color.white);
        p.setMaximumSize (p.getPreferredSize ());
        return p;
    }
    
    
    // implementation ..........................................................
    
    void applyChanges () {
        Iterator it = categoryToPanel.values ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory.Panel p = (OptionsCategory.Panel) it.next ();
            p.applyChanges ();
        }
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
}


