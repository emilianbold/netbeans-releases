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
       org.netbeans.core.CoreBridgeImpl.getDefault().registerPropertyEditors(); 
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
