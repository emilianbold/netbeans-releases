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
import org.openide.filesystems.Repository;
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
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;


/**
 * How to run from IDE:
 *   1. Mount jar: junit.jar
 *   2. Mount dir: openide/src
 *   3. Mount dir: openide/test/regr/src
 *   4. Run class UnstableTest from dir openide/test/regr/src in internal execution 
 *   (inside IDE VM - set execution type in Properties window)
 *   It will open new window in Editor. When deadlock is there IDE hangs.
 * How to run from command line: 
 *   In directory: <NetBeans>/openide/test/
 *   Command: ant -Dxtest.testtypes=regression -Dxtest.attribs=stable
 *
 * @author  Peter Zavadsky
 */
public class SaveDocumentTest extends NbTestCase {

    /** Creates new TextTest */
    public SaveDocumentTest(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(SaveDocumentTest.class));
    }

    protected void setUp() throws Exception {
        TXTDataLoader loader = (TXTDataLoader)TXTDataLoader.getLoader (TXTDataLoader.class);
        org.openide.loaders.AddLoaderManuallyHid.addRemoveLoader (loader, true);
    }
    
    protected void tearDown() throws Exception {
        TXTDataLoader loader = (TXTDataLoader)TXTDataLoader.getLoader (TXTDataLoader.class);
        org.openide.loaders.AddLoaderManuallyHid.addRemoveLoader (loader, false);
    }

    
    /** Tests the #17714. */
    public void testSaveDocument() throws Exception {
        System.err.println("Test Save Document");
        
        FileObject fo = Repository.getDefault().getDefaultFileSystem()
            .getRoot().createData("test", "txt");

        DataObject data = DataObject.find(fo);
        
        EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
        
        if(!(ec instanceof CloneableEditorSupport)) {
            throw new IllegalStateException("Bad editor cookie type");
        }

        CloneableEditorSupport ces = (CloneableEditorSupport)ec;
        System.err.println("CloneableEditorSupport="+ces);

        if(ces.isModified()) {
            throw new IllegalStateException("Cloneable editor support should be marked as unmodified!");
        }
        System.err.println("Saving unmodified document");
        ces.saveDocument();
        
        final StyledDocument doc = ces.openDocument();
        
        NbDocument.runAtomicAsUser(doc, new Runnable() {
            public void run() {
                try {
                    doc.insertString(0, "Inserted string", null);
                } catch(BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        });

        System.err.println("doc="+ces.getDocument());
        
        System.err.println("ec isModified="+ces.isModified());
        if(!ec.isModified()) {
            throw new IllegalStateException("CloneableEditorSupport should be marked as modified already!");
        }
        System.err.println("Saving modified document");
        
        ces.saveDocument();
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
    
}
