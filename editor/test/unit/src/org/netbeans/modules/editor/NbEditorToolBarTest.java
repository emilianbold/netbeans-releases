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
 */

package org.netbeans.modules.editor;

import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 */
public class NbEditorToolBarTest extends NbTestCase {

    public NbEditorToolBarTest(String testName) {
        super(testName);
    }

    public boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
        super.setUp();

        clearWorkDir();

        EditorTestLookup.setLookup(
            new URL[] {
                EditorTestConstants.EDITOR_LAYER_URL,
            },
            new Object[] {},
            getClass().getClassLoader()
        );
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * is the first Lookup.Provider ancestor.
     */
    public void testActionContextAncestorsLookupProviderIsPreferred() throws Exception {
        JPanel parent1 = new LookupPanel(Lookups.singleton(new Foo() { }));
        JPanel parent2 = new LookupPanel(Lookups.singleton(new Bar() { }));
        parent1.add(parent2);
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent2.add(editor);
        DataObject docDataObject = createDataObject();
        assertNotNull(docDataObject);
        editor.getDocument().putProperty(Document.StreamDescriptionProperty, docDataObject);

        NbEditorToolBar toolbar = new NbEditorToolBar(editor);
        Lookup actionContext = toolbar.createActionContext();
        assertNotNull(actionContext.lookup(Bar.class));
        assertNotNull(actionContext.lookup(Node.class));
        assertNull(actionContext.lookup(Foo.class));
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * is the DataObject corresponding to the current document if there is no
     * Lookup.Provider ancestor.
     */
    public void testActionContextFallbackToDataObject() throws Exception {
        JPanel parent = new JPanel();
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent.add(editor);
        DataObject docDataObject = createDataObject();
        assertNotNull(docDataObject);
        editor.getDocument().putProperty(Document.StreamDescriptionProperty, docDataObject);

        NbEditorToolBar toolbar = new NbEditorToolBar(editor);
        Lookup actionContext = toolbar.createActionContext();
        assertNotNull(actionContext.lookup(Node.class));
        assertNull(actionContext.lookup(Foo.class));
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * is null if there is no Lookup.Provider ancestor and no DataObject
     * corresponding to the current document.
     */
    public void testActionContextNullWhenNoDataObject() {
        JPanel parent = new JPanel();
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent.add(editor);

        NbEditorToolBar toolbar = new NbEditorToolBar(editor);
        Lookup actionContext = toolbar.createActionContext();
        assertNull(actionContext);
    }

    /**
     * Tests that the action context for the context-aware toolbar actions
     * contains the node corresponding to the current document only once, even
     * though the node is both contained in an ancestor Lookup.Provider and
     * obtained as the node delegate of the DataObject of the current document.
     */
    public void testActionContextLookupContainsNodeOnlyOnce() throws Exception {
        DataObject docDataObject = createDataObject();
        assertNotNull(docDataObject);
        JPanel parent = new LookupPanel(Lookups.fixed(new Object[] { new Bar() { }, docDataObject.getNodeDelegate().getLookup() }));
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new NbEditorKit());
        parent.add(editor);
        editor.getDocument().putProperty(Document.StreamDescriptionProperty, docDataObject);

        NbEditorToolBar toolbar = new NbEditorToolBar(editor);
        Lookup actionContext = toolbar.createActionContext();
        assertNotNull(actionContext.lookup(Bar.class));
        assertNotNull(actionContext.lookup(Node.class));
        assertEquals(1, actionContext.lookup(new Lookup.Template(Node.class)).allInstances().size());
    }

    private DataObject createDataObject() throws Exception {
        getWorkDir().mkdirs();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        FileObject fo = lfs.getRoot().createData("file", "txt");
        return DataObject.find(fo);
    }

    private static final class LookupPanel extends JPanel implements Lookup.Provider {

        private final Lookup lookup;

        public LookupPanel(Lookup lookup) {
            this.lookup = lookup;
        }

        public Lookup getLookup() {
            return lookup;
        }
    }

    private static interface Foo {
    }

    private static interface Bar {
    }
}
