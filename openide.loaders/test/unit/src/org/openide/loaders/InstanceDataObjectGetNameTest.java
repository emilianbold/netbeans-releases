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

import java.io.File;
import java.io.FileWriter;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;


/** Check that we cache getName
 * @author Jaroslav Tulach
 */
public class InstanceDataObjectGetNameTest extends NbTestCase {
    private DataObject obj;
    private FileSystem fs;

    /** Creates new DataFolderTest */
    public InstanceDataObjectGetNameTest(String name) {
        super (name);
    }
    
    private static String name;
    private static int cnt;
    public static String computeName() {
        cnt++;
        return name;
    }
    
    protected void setUp () throws Exception {
        
        cnt = 0;
        
        File f = new File(getWorkDir(), "layer.xml");
        FileWriter w = new FileWriter(f);
        w.write("<filesystem><file name='x.instance'> ");
        w.write("  <attr name='name' methodvalue='" + InstanceDataObjectGetNameTest.class.getName() + ".computeName'/> ");
        w.write("</file></filesystem> ");
        w.close();

        fs = new MultiFileSystem(new FileSystem[] { 
            FileUtil.createMemoryFileSystem(), 
            new XMLFileSystem(f.toURL())
        });
        FileObject fo = fs.findResource("x.instance");
        assertNotNull(fo);
        
        assertNull(fo.getAttribute("name"));
        assertEquals("One call", 1, cnt);
        // clean
        cnt = 0;

        obj = DataObject.find(fo);
        
        assertEquals("No calls now", 0, cnt);
    }
    
    public void testNameIsCached() throws Exception {
        if (!(obj instanceof InstanceDataObject)) {
            fail("We need IDO : " + obj);
        }
        
        name = "Ahoj";
        assertEquals("We can influence a name", "Ahoj", obj.getName());
        assertEquals("one call", 1, cnt);
        assertEquals("Name stays the same", "Ahoj", obj.getName());
        assertEquals("no new call", 1, cnt);
        
        name = "kuk";
        assertEquals("Name stays the same", "Ahoj", obj.getName());
        assertEquals("no new call", 1, cnt);

        obj.getPrimaryFile().setAttribute("someattr", "new");
        
        assertEquals("Name changes as attribute changes fired", "kuk", obj.getName());
        assertEquals("of course new call is there", 2, cnt);
        
    }
}
