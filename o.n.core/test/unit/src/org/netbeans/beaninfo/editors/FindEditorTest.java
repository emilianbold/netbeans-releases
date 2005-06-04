/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorManager;
import junit.framework.AssertionFailedError;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.HashMap;

/** Test finding of property editors registred by core.
 * @author Jiri Rechtacek
 */
public class FindEditorTest extends NbTestCase {
    
    static {
        //issue 31879, force registration of NB editors so this
        //test can pass when editor tests are run standalone
       org.netbeans.core.startup.Main.registerPropertyEditors(); 
    }
    
    public FindEditorTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(FindEditorTest.class));
    }
    
    public void testFindStringEditor () throws Exception {
        assertFind (String.class, org.netbeans.beaninfo.editors.StringEditor.class);
    }

    public void testFindStringArrayEditor () throws Exception {
        assertFind ((new String[] { "" }).getClass (), org.netbeans.beaninfo.editors.StringArrayEditor.class);
    }

    public void testFindDataObjectEditor () throws Exception {
        assertFind (org.openide.loaders.DataObject.class, org.netbeans.beaninfo.editors.DataObjectEditor.class);
    }

    public void testFindDataObjectArrayEditor () throws Exception {
        org.openide.filesystems.FileObject fo = org.openide.filesystems.Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        Object obj = new org.openide.loaders.DataObject[] { org.openide.loaders.DataObject.find (fo) };
        assertFind (obj.getClass (), org.netbeans.beaninfo.editors.DataObjectArrayEditor.class);
    }
    
    private void assertFind (Class propertyTypeClass, Class editorClass) {
        assertNotNull ("PropertyEditor for " + propertyTypeClass + " found.", PropertyEditorManager.findEditor (propertyTypeClass));
        assertEquals ("Editor is instance of ", editorClass, PropertyEditorManager.findEditor (propertyTypeClass).getClass ());
    }
}
