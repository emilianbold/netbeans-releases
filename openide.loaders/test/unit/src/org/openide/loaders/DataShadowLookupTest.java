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

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/** Test things about shadows and broken shadows, etc.
 * @author Jaroslav Tulach
 */
public class DataShadowLookupTest extends NbTestCase
implements java.net.URLStreamHandlerFactory {
    /** original object */
    private DataObject original;
    /** folder to work with */
    private DataFolder folder;
    
    static {
        // to handle nbfs urls...
      //  java.net.URL.setURLStreamHandlerFactory (new DataShadowLookupTest(null));
        MockLookup.setInstances(new Pool());
    }
    
    public DataShadowLookupTest (String name) {
        super(name);
    }

    protected @Override Level logLevel() {
        return Level.INFO;
    }
    
    protected @Override void setUp() throws Exception {
        
        FileObject[] delete = FileUtil.getConfigRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }

        
        FileObject fo = FileUtil.createData (FileUtil.getConfigRoot (), getName () + "/folder/original.string");
        assertNotNull(fo);
        original = DataObject.find (fo);
        assertFalse ("Just to be sure that this is not shadow", original instanceof DataShadow);
        assertEquals ("It is the right class", StringObject.class, original.getClass ());
        fo = FileUtil.createFolder (FileUtil.getConfigRoot (), getName () + "/modify");
        assertNotNull(fo);
        assertTrue (fo.isFolder ());
        folder = DataFolder.findFolder (fo);
    }
    
    public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals ("nbfs")) {
            return FileUtil.nbfsURLStreamHandler ();
        }
        return null;
    }
    
    public void testStringIsInLookupOfDataShadow() throws Exception {
        DataShadow shade = original.createShadow(folder);

        {
            String s = original.getNodeDelegate().getLookup().lookup(String.class);
            assertNotNull("String is in the original's lookup", s);
        }
        
        assertSame(shade.getOriginal(), original);
        String s = shade.getNodeDelegate().getLookup().lookup(String.class);
        assertNotNull("String is in the lookup", s);
        assertEquals("It is the name of the original", original.getName(), s);
    }

    private static final class Pool extends DataLoaderPool {
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(StringLoader.findObject(StringLoader.class, true));
        }
        
    }
    
    private static final class StringLoader extends UniFileLoader {
        public StringLoader() {
            super("org.openide.loaders.DataShadowLookupTest$StringObject");
            getExtensions().addExtension("string");
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new StringObject(this, primaryFile);
        }
        
    } // end of StringLoader
    
    private static final class StringObject extends MultiDataObject {
        public StringObject(StringLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }

        protected @Override Node createNodeDelegate() {
            return new DataNode(this, Children.LEAF, Lookups.singleton(getName()));
        }
    } // end of StringObject
    
    
}
