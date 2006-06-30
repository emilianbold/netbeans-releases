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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
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
    
    
    static {
        IDEInitializer.setup (
            new String[] {
                "org/netbeans/modules/options/editor/mf-layer.xml",
                "org/netbeans/modules/defaults/mf-layer.xml"
            },
            new Object[] {}
        );
    }
    
    public EditorOptionsTest (String testName) {
        super (testName);
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
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        it = controllers.iterator ();
        while (it.hasNext ()) {
            OptionsPanelController pc = (OptionsPanelController) it.next ();
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
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        it = controllers.iterator ();
        while (it.hasNext ()) {
            OptionsPanelController pc = (OptionsPanelController) it.next ();
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
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        it = controllers.iterator ();
        while (it.hasNext ()) {
            OptionsPanelController pc = (OptionsPanelController) it.next ();
            JComponent c = pc.getComponent (masterLookup);
            pc.applyChanges ();
        }
    }
    
    public void testCancel () {
        
        // 1) load PanelControllers and init master lookup
        List controllers = new ArrayList ();
        List lookups = new ArrayList ();
        Iterator it = getCategories ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory oc = (OptionsCategory) it.next ();
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        
        // 2) create panels & call cancel on all PanelControllers
        it = controllers.iterator ();
        while (it.hasNext ()) {
            OptionsPanelController pc = (OptionsPanelController) it.next ();
            JComponent c = pc.getComponent (masterLookup);
            pc.cancel ();
        }
    }
    
    public void testChangedAndValid () {
        
        // 1) load PanelControllers and init master lookup
        List controllers = new ArrayList ();
        List lookups = new ArrayList ();
        Iterator it = getCategories ().iterator ();
        while (it.hasNext ()) {
            OptionsCategory oc = (OptionsCategory) it.next ();
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup 
            ((Lookup[]) lookups.toArray (new Lookup [lookups.size ()]));
        
        // 2) create panels & call cancel on all PanelControllers
        it = controllers.iterator ();
        while (it.hasNext ()) {
            OptionsPanelController pc = (OptionsPanelController) it.next ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            JComponent c = pc.getComponent (masterLookup);
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.cancel ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.applyChanges ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
        }
        
        it = controllers.iterator ();
        while (it.hasNext ()) {
            OptionsPanelController pc = (OptionsPanelController) it.next ();
            JComponent c = pc.getComponent (masterLookup);
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
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


