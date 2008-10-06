/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.text;

import java.io.PrintStream;
import java.util.logging.Level;
import org.netbeans.junit.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;

/** DefaultDataObject is supposed to have open operation that shows the text
 * editor or invokes a dialog with questions.
 *
 * @author  Jaroslav Tulach
 */
public final class SimpleDESTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    private FileSystem lfs;
    private DataObject obj;
    
    /** Creates a new instance of DefaultSettingsContextTest */
    public SimpleDESTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        clearWorkDir ();
        
        System.setProperty("org.openide.util.Lookup", "org.openide.text.SimpleDESTest$Lkp");
        super.setUp();
        
        LocalFileSystem l = new LocalFileSystem ();
        l.setRootDirectory (getWorkDir ());
        lfs = l;
        
        FileObject fo = FileUtil.createData (lfs.getRoot (), "AA/" + getName () + ".test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);
        
        assertEquals ("The right class", obj.getClass (), SO.class);
    }

    @RandomlyFails // NB-Core-Build #1210
    public void testHasEditorCookieForResonableContentOfFiles () throws Exception {
        doCookieCheck (true);
    }
    
    private void doCookieCheck (boolean hasEditCookie) throws Exception {
        EditorCookie c = tryToOpen (
            "Ahoj Jardo," +
            "how are you" +
            "\t\n\rBye"
        );
        assertNotNull (c);
        
        assertEquals (
            "Next questions results in the same cookie", 
            c, 
            obj.getCookie(EditorCookie.class)
        );
        assertEquals (
            "Print cookie is provided",
            c,
            obj.getCookie(org.openide.cookies.PrintCookie.class)
        );
        assertEquals (
            "CloseCookie as well",
            c,
            obj.getCookie(org.openide.cookies.CloseCookie.class)
        );
        
        if (hasEditCookie) {
            assertEquals (
                "EditCookie as well",
                c,
                obj.getCookie(org.openide.cookies.EditCookie.class)
            );
        } else {
            assertNull (
                "No EditCookie",
                obj.getCookie(org.openide.cookies.EditCookie.class)
            );
            
        }
        
        OpenCookie open = (OpenCookie)obj.getCookie (OpenCookie.class);
        open.open ();
        
        javax.swing.text.Document d = c.getDocument();
        assertNotNull (d);
        
        d.insertString(0, "Kuk", null);
        
        assertNotNull (
            "Now there is a save cookie", 
            obj.getCookie (org.openide.cookies.SaveCookie.class)
        );
    }
    
    public void testItIsPossibleToMaskEditCookie () throws Exception {
        doCookieCheck (false);
    }
    
    private EditorCookie tryToOpen (String content) throws Exception {
        FileObject fo = obj.getPrimaryFile();
        FileLock lock = fo.lock();
        PrintStream os = new PrintStream (fo.getOutputStream(lock));
        os.print (content);
        os.close ();
        lock.releaseLock();
        
        return (EditorCookie)obj.getCookie (EditorCookie.class);
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new DLP ());
        }
    }
    
    private static final class SL extends org.openide.loaders.UniFileLoader {
        public SL () {
            super (SO.class.getName ());
            getExtensions().addExtension("test");
        }
        protected org.openide.loaders.MultiDataObject createMultiObject(FileObject primaryFile) throws org.openide.loaders.DataObjectExistsException, java.io.IOException {
            return new SO (primaryFile);
        }
    } // end of SL
    
    private static final class SO extends org.openide.loaders.MultiDataObject implements org.openide.nodes.CookieSet.Factory {
        private org.openide.nodes.Node.Cookie cookie = (org.openide.nodes.Node.Cookie)DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet ());
        
        
        public SO (FileObject fo) throws org.openide.loaders.DataObjectExistsException {
            super (fo, (SL)SL.getLoader(SL.class));
            
            if (fo.getNameExt().indexOf ("MaskEdit") == -1) {
                getCookieSet ().add (cookie);
            } else {
                getCookieSet ().add (new Class[] { 
                    OpenCookie.class, 
                    org.openide.cookies.CloseCookie.class, EditorCookie.class, 
                    org.openide.cookies.PrintCookie.class
                }, this); 
            }
        }
        
        
        public org.openide.nodes.Node.Cookie createCookie (Class c) {
            return cookie;
        }
    } // end of SO

    private static final class DLP extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration loaders() {
            return java.util.Collections.enumeration(
                java.util.Collections.singleton(
                    SL.getLoader (SL.class)
                )
            );
        }
    } // end of DataLoaderPool
}
