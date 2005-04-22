/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.text;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.actions.*;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.WindowManager;


/**
 */
public class AnnotationProviderTest extends NbTestCase {
    
    public AnnotationProviderTest(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(AnnotationProviderTest.class));
    }

    private FileSystem fs;
    
    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.text.AnnotationProviderTest$Lkp");
        
        clearWorkDir ();
        org.openide.filesystems.LocalFileSystem lfs = new org.openide.filesystems.LocalFileSystem ();
        lfs.setRootDirectory (getWorkDir ());
        fs = lfs;
    }
    
    protected void tearDown() throws Exception {
    }

    public void testAnnotationProviderIsCalledCorrectly() throws Exception {              
        Object o = Lookup.getDefault().lookup(AnnotationProvider.class);
        if(o == null) {
            fail("No  annotation provider found");
        }
        
        FileObject fo = fs.getRoot().createData("test", "txt");

        DataObject data = DataObject.find(fo);
        
        EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
        
        ConsistencyCheckProvider.called = 0;
        ec.open();
        
        CloneableEditorSupport ces = (CloneableEditorSupport)ec;
        
        assertEquals("Provider called exactly once", 1, ConsistencyCheckProvider.called);
        assertEquals("Consistent lookup content", data.getPrimaryFile(), ConsistencyCheckProvider.inLkp);

        Line l1 = ces.getLineSet().getCurrent(0);
        assertEquals("Exactly one annotation attached", 1, l1.getAnnotationCount());
        
        ec.close();
        // XXX
        Line l2 = ces.getLineSet().getCurrent(0);
        assertEquals ("Lines are the same", l1, l2);
        assertEquals("Exactly one annotation attached after close", 1, l2.getAnnotationCount());

        ConsistencyCheckProvider.called = 0;
        ec.open();
        // XXX
        assertEquals("Provider not called during reopen", 0, ConsistencyCheckProvider.called);
        assertEquals("Exactly one annotation attached after reopen", 1, ces.getLineSet().getCurrent(0).getAnnotationCount());
    }

    public void testContextLookupIsConsistentAfterMove() throws Exception {              
        // Prepare the data object (to initialize the lookup)
        FileObject fo = fs.getRoot().createData("test2", "txt");
        DataObject data = DataObject.find(fo);
        EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);

        // now move it (the lookup should update itself)
        FileObject fld = fs.getRoot().createFolder("folder1");
        DataFolder df = DataFolder.findFolder(fld);
        data.move(df);

        // now open the editor (invoke AnnotationProviders)
        // and check the lookup
        ec.open();
        assertEquals("Consistent lookup content", data.getPrimaryFile(), ConsistencyCheckProvider.inLkp);
    }
    
    private void forceGC () {
        for (int i = 0; i < 5; i++) {
            System.gc ();
        }
    }
    
    public void testContextLookupFiresDuringMove() throws Exception {              
        // Prepare the data object (to initialize the lookup)
        FileObject fo = fs.getRoot().createData("test3", "txt");
        DataObject data = DataObject.find(fo);
        EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);

        // open the editor and check the lookup before move
        ec.open();
        assertEquals("Lookup content consistent before move", data.getPrimaryFile(), ConsistencyCheckProvider.inLkp);

        forceGC ();
        
        // now move the file
        ConsistencyCheckProvider.called = 0;
        FileObject fld = fs.getRoot().createFolder("folder1");
        DataFolder df = DataFolder.findFolder(fld);
        data.move(df);

        forceGC ();
        
        // check the result
        assertEquals("Lookup fires one change during move", 1, ConsistencyCheckProvider.changes);
        assertEquals("Lookup content consistent after move", data.getPrimaryFile(), ConsistencyCheckProvider.inLkp);
    }
    
    public static class ConsistencyCheckProvider implements AnnotationProvider, LookupListener {
        
        private static Set myLines = new HashSet();
        private static int called;
        private static FileObject inLkp;
        private Lookup.Result result;
        private static int changes;
        
        public void annotate(org.openide.text.Line.Set set, org.openide.util.Lookup context) {
            result = context.lookup(new Lookup.Template(FileObject.class));
            result.addLookupListener(this);
            inLkp= (FileObject)result.allInstances().iterator().next();
            called++;

            Line act = set.getCurrent(0);
            
            myLines.add(act);
            act.addAnnotation(new MyAnnotation());
            
        }        
        
        public void resultChanged(org.openide.util.LookupEvent ev) {
            changes++;
            inLkp= (FileObject)result.allInstances().iterator().next();            
        }
        
    }

    
    // below is only irrelevant support stuff
    
    private static class MyAnnotation extends Annotation {
        
        public String getAnnotationType() {
            return "nowhere";
        }
        
        public String getShortDescription() {
            return "Test annotation";
        }
        
    }
    
    protected boolean runInEQ() {
        return true;
    }    
    
    //
    // Code from text module
    // 
    

    public static final class TXTDataLoader extends UniFileLoader {

        /** Generated serial version UID. */
        static final long serialVersionUID =-3658061894653334886L;    

        /** file attribute which forces a file to be considered a text file */
        static final String ATTR_IS_TEXT_FILE = "org.netbeans.modules.text.IsTextFile"; // NOI18N


        /** Creates new <code>TXTDataLoader</code>. */
        public TXTDataLoader() {
            super("org.netbeans.modules.text.TXTDataObject"); // NOI18N
        }

        /** Does initialization. Initializes extension list. */
        protected void initialize () {
            super.initialize();

            ExtensionList ext = new ExtensionList();
            ext.addExtension("txt"); // NOI18N
            ext.addExtension("doc"); // NOI18N
            ext.addExtension("me"); // for read.me files // NOI18N
            ext.addExtension("policy"); // NOI18N
            ext.addExtension("mf"); // for manifest.mf files // NOI18N
            ext.addExtension("MF"); //  -""- // NOI18N
            ext.addExtension("log"); // log files are nice to be readable // NOI18N
            setExtensions(ext);
        }

        /** Gets default display name. Overrides superclass method. */
        protected String defaultDisplayName() {
            return NbBundle.getBundle(TXTDataLoader.class).getString("PROP_TXTLoader_Name");
        }

        /** Gets default system actions. Overrides superclass method. */
        protected SystemAction[] defaultActions() {
            return new SystemAction[] {
                SystemAction.get(OpenAction.class),
                SystemAction.get (FileSystemAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                SystemAction.get(PasteAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                SystemAction.get(SaveAsTemplateAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
            };
        }

        /** Check whether a file is recognized.
         * It will be if the extension matches, or if it is marked to be a text file. */
        protected FileObject findPrimaryFile (FileObject fo) {
            boolean isSysFile;
            try {
                isSysFile = fo.getFileSystem () == Repository.getDefault ().getDefaultFileSystem ();
            } catch (FileStateInvalidException fsie) {
                // Never mind.
                isSysFile = false;
            }
            if (! isSysFile && Boolean.TRUE.equals (fo.getAttribute (ATTR_IS_TEXT_FILE)))
                return fo;
            return super.findPrimaryFile (fo);
        }

        /** Creates new <code>TXTDataObject</code> for specified <code>FileObject</code>.
         * @param fo FileObject
         * @return new TXTDataObject
         */
        protected MultiDataObject createMultiObject(final FileObject fo)
        throws IOException {
            return new TXTDataObject(fo, this);
        }

    } // end of TXTDataLoader

    
    public static final class TXTDataObject extends MultiDataObject implements CookieSet.Factory {

        /** Generated Serialized Version UID */
        static final long serialVersionUID = 4795737295255253334L;

        /** Editor support for text data object. */
        private transient TXTEditorSupport editorSupport;


        /** Constructor. */
        public TXTDataObject(final FileObject obj, final MultiFileLoader loader) throws DataObjectExistsException {
            super(obj, loader);

            getCookieSet().add(TXTEditorSupport.class, this);
        }


        /** Implements <code>CookieSet.Factory</code> interface. */
        public Node.Cookie createCookie(Class clazz) {
            if(clazz.isAssignableFrom(TXTEditorSupport.class))
                return getEditorSupport();
            else
                return null;
        }

        // Accessibility from TXTEditorSupport:
        CookieSet getCookieSet0() {
            return getCookieSet();
        }

        /** Gets editor support for this data object. */
        private TXTEditorSupport getEditorSupport() {
            if(editorSupport == null) {
                synchronized(this) {
                    if(editorSupport == null)
                        editorSupport = new TXTEditorSupport(this);
                }
            }

            return editorSupport;
        }

        /** Provides node that should represent this data object. When a node for representation
         * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
         * with only parent changed. This implementation creates instance <code>DataNode</code>.
         * <p>
         * This method is called only once.
         *
         * @return the node representation for this data object
         * @see DataNode
         */
        protected Node createNodeDelegate () {
            return new TXTNode(this);
        }

        /** Help context for this object.
         * @return help context
         */
        public HelpCtx getHelpCtx () {
            return new HelpCtx (TXTDataObject.class);
        }


        /** Text node implementation.
         * Leaf node, default action opens editor or instantiates template.
         * Icons redefined.
         */
        public static final class TXTNode extends DataNode {
            /** Icon base for the TXTNode node */
            private static final String TXT_ICON_BASE = "org/netbeans/modules/text/txtObject"; // NOI18N

            /** Constructs node. */
            public TXTNode (final DataObject dataObject) {
                super(dataObject, Children.LEAF);
                setIconBase(TXT_ICON_BASE);
            }

            /** Overrides default action from DataNode. */
            public SystemAction getDefaultAction () {
                SystemAction result = super.getDefaultAction();
                return result == null ? SystemAction.get(OpenAction.class) : result;
            }
        } // End of nested class TXTNode.

    } // TXTDataObject
    
    
    public static final class TXTEditorSupport extends DataEditorSupport
    implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie {

        /** SaveCookie for this support instance. The cookie is adding/removing 
         * data object's cookie set depending on if modification flag was set/unset. */
        private final SaveCookie saveCookie = new SaveCookie() {
            /** Implements <code>SaveCookie</code> interface. */
            public void save() throws IOException {
                TXTEditorSupport.this.saveDocument();
                TXTEditorSupport.this.getDataObject().setModified(false);
            }
        };


        /** Constructor. */
        TXTEditorSupport(TXTDataObject obj) {
            super(obj, new Environment(obj));

            setMIMEType("text/plain"); // NOI18N
        }

        /** 
         * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
         * @return true if the environment accepted being marked as modified
         *    or false if it has refused and the document should remain unmodified
         */
        protected boolean notifyModified () {
            if (!super.notifyModified()) 
                return false;

            addSaveCookie();

            return true;
        }

        /** Overrides superclass method. Adds removing of save cookie. */
        protected void notifyUnmodified () {
            super.notifyUnmodified();

            removeSaveCookie();
        }

        /** Helper method. Adds save cookie to the data object. */
        private void addSaveCookie() {
            TXTDataObject obj = (TXTDataObject)getDataObject();

            // Adds save cookie to the data object.
            if(obj.getCookie(SaveCookie.class) == null) {
                obj.getCookieSet0().add(saveCookie);
                obj.setModified(true);
            }
        }

        /** Helper method. Removes save cookie from the data object. */
        private void removeSaveCookie() {
            TXTDataObject obj = (TXTDataObject)getDataObject();

            // Remove save cookie from the data object.
            Node.Cookie cookie = obj.getCookie(SaveCookie.class);

            if(cookie != null && cookie.equals(saveCookie)) {
                obj.getCookieSet0().remove(saveCookie);
                obj.setModified(false);
            }
        }


        /** Nested class. Environment for this support. Extends
         * <code>DataEditorSupport.Env</code> abstract class.
         */

        private static class Environment extends DataEditorSupport.Env
        {
            private static final long serialVersionUID = 3499855082262173256L;

            /** Constructor. */
            public Environment(TXTDataObject obj) {
                super(obj);
            }


            /** Implements abstract superclass method. */
            protected FileObject getFile() {
                return getDataObject().getPrimaryFile();
            }

            /** Implements abstract superclass method.*/
            protected FileLock takeLock() throws IOException {
                return ((TXTDataObject)getDataObject()).getPrimaryEntry().takeLock();
            }

            /** 
             * Overrides superclass method.
             * @return text editor support (instance of enclosing class)
             */
            public CloneableOpenSupport findCloneableOpenSupport() {
                return (TXTEditorSupport)getDataObject().getCookie(TXTEditorSupport.class);
            }
        } // End of nested Environment class.

    } // TXTEditorSupport

    
    private static class MyPool extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration loaders() {
            return Collections.enumeration(Collections.singleton(
                TXTDataLoader.getLoader(TXTDataLoader.class)
            ));
        }
        
    }
    
    public static class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            
            ic.add (new MyPool ());
            ic.add (new ConsistencyCheckProvider ());
        }
    }
}
