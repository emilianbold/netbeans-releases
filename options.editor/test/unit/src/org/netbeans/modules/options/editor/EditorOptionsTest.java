/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsCategory.PanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;

import org.netbeans.modules.options.macros.MacrosPanelController;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 *
 * @author Jan Jancura
 */
public class EditorOptionsTest extends NbTestCase {
    
    public EditorOptionsTest (String testName) {
        super (testName);
    }

    protected void setUp() throws Exception {
        super.setUp ();
        EditorTestLookup.setLookup (
            getWorkDir (),
            new URL[] {
                getClass ().getClassLoader ().getResource 
                    ("org/netbeans/modules/options/editor/mf-layer.xml"),
                getClass ().getClassLoader ().getResource 
                    ("org/netbeans/modules/defaults/mf-layer.xml")
            },
            new Object[] {},
            getClass ().getClassLoader ()
        );
    }
    
    public void testOptions () {
        assertEquals (4, getCategories ().size ());
    }
    
    public void testOptionsCategories () {
        Iterator it = getCategories ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory oc = (OptionsCategory) it.next ();
            assertNotNull (oc.getCategoryName ());
            assertNotNull (oc.getIconBase ());
            assertNotNull (oc.getTitle ());
        }
    }
    
    public void testUpdateOk () {
        List controllers = new ArrayList ();
        List lookups = new ArrayList ();
        Iterator it = getCategories ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory oc = (OptionsCategory) it.next ();
            PanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        it = controllers.iterator ();
        while (it.hasNext ()) {
            PanelController pc = (PanelController) it.next ();
            JComponent c = pc.getComponent (masterLookup);
            pc.update ();
            pc.applyChanges ();
        }
    }
    
    public void testUpdateCancel () {
        List controllers = new ArrayList ();
        List lookups = new ArrayList ();
        Iterator it = getCategories ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory oc = (OptionsCategory) it.next ();
            PanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        it = controllers.iterator ();
        while (it.hasNext ()) {
            PanelController pc = (PanelController) it.next ();
            JComponent c = pc.getComponent (masterLookup);
            pc.update ();
            pc.cancel ();
        }
    }
    
    public void testOk () {
        List controllers = new ArrayList ();
        List lookups = new ArrayList ();
        Iterator it = getCategories ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory oc = (OptionsCategory) it.next ();
            PanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        it = controllers.iterator ();
        while (it.hasNext ()) {
            PanelController pc = (PanelController) it.next ();
            JComponent c = pc.getComponent (masterLookup);
            pc.applyChanges ();
        }
    }
    
    public void testCancel () {
        List controllers = new ArrayList ();
        List lookups = new ArrayList ();
        Iterator it = getCategories ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory oc = (OptionsCategory) it.next ();
            PanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        it = controllers.iterator ();
        while (it.hasNext ()) {
            PanelController pc = (PanelController) it.next ();
            JComponent c = pc.getComponent (masterLookup);
            pc.cancel ();
        }
    }

    private List getCategories () {
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
            findResource ("OptionsDialog");
        Lookup lookup = new FolderLookup (DataFolder.findFolder (fo)).
            getLookup ();
        return new ArrayList (lookup.lookup (
            new Lookup.Template (OptionsCategory.class)
        ).allInstances ());
    }
}


