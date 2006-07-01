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

package org.openide.loaders;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.DialogDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;

/** To check issue 61600
 *
 * @author  Jaroslav Tulach
 */
public final class DefaultVersusXMLDataObjectTest extends NbTestCase {
    /** Creates a new instance of DefaultVersusXMLDataObjectTest */
    public DefaultVersusXMLDataObjectTest(String n) {
        super(n);
    }
    
    public void testCreateFromTemplateResultsInXMLDataObject() throws Exception {
        FileObject fo = FileUtil.createData(Repository.getDefault().getDefaultFileSystem().getRoot(), "Templates/Unknown/EmptyFile");
        DataObject obj = DataObject.find(fo);
        obj.setTemplate(true);
        
        WeakReference ref = new WeakReference(obj);
        obj = null;
        assertGC("obj is gone", ref);
        
        obj = DataObject.find(fo);
        assertEquals ("Right type", DefaultDataObject.class, obj.getClass());
        assertTrue ("Is the template", obj.isTemplate());
        
        FileObject ff = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "CreateAt");
        DataFolder f = DataFolder.findFolder(ff);
        
        DataObject result = obj.createFromTemplate(f, "my.xml");
        
        if (result instanceof DefaultDataObject) {
            fail("Bad, the object should be of XMLDataObject type: " + result);
        }
        
        assertEquals("it is xml DataObject", XMLDataObject.class, result.getClass());
    }
}
