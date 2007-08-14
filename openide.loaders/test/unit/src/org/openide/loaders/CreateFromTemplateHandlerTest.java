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
 * Software is Sun Microsystems, Inc. 
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc. 
 */

package org.openide.loaders;

import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;

/**
 *
 * @author Jaroslav Tulach
 */
public class CreateFromTemplateHandlerTest extends NbTestCase {
    
    public CreateFromTemplateHandlerTest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    protected void setUp() throws Exception {
        Hand.acceptObject = new ArrayList<FileObject>();
        Hand.fileObject = new ArrayList<FileObject>();
        Hand.origObject = new ArrayList<FileObject>();
        Hand.name = null;
        Hand.parameters = null;
        
        MockServices.setServices(Hand.class, Attr.class, DD.class, Pool.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateFromTemplate() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("type", "empty");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());
        
        assertEquals("The right source", fo, Hand.origObject.get(0));
        assertEquals("The right source in query", fo, Hand.acceptObject.get(0));
        assertEquals("The right destiny folder", folder.getPrimaryFile(), Hand.fileObject.get(0));
        assertEquals("The right name", "complex", Hand.name);
        if (Hand.parameters.size() < 2) {
            fail("As least two: " + Hand.parameters + " but was " + Hand.parameters.size());
        }
        assertEquals("empty", Hand.parameters.get("type"));
        assertEquals("complex", Hand.parameters.get("name"));
        try {
            Hand.parameters.put("kuk", "buk");
        } catch (UnsupportedOperationException ex) {
            // ok
            return;
        }
        fail("Modifications shall be unsupported");
    }
    
    public void testTemplateWizardCopiesItsPropertiesToMap() throws Exception {
        doTemplateWizardCopiesItsPropertiesToMap("simpleObject.txt");
    }
    
    public void testTemplateWizardCopiesItsPropertiesToMapForOverridenEntry() throws Exception {
        DataObject obj = doTemplateWizardCopiesItsPropertiesToMap("simpleObject.prima");
        assertEquals("The right loader", SimpleLoader.class, obj.getLoader().getClass());
    }

    public void testTemplateWizardCopiesItsPropertiesToMapForOverridenEntryOnMoreEntries() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.java");
        FileObject fo2 = FileUtil.createData(root, "simpleObject.form");
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("type", "empty");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex", n.getName());
        
        assertEquals("The right source1", fo, Hand.origObject.get(0));
        assertEquals("The right source2", fo2, Hand.origObject.get(1));
        assertEquals("The right source in query", fo, Hand.acceptObject.get(0));
        assertEquals("The right source in query2", fo2, Hand.acceptObject.get(1));
        assertEquals("The right destiny folder", folder.getPrimaryFile(), Hand.fileObject.get(0));
        assertEquals("The right destiny folder", folder.getPrimaryFile(), Hand.fileObject.get(1));
        assertEquals("The right name", "complex", Hand.name);
        if (Hand.parameters.size() < 2) {
            fail("As least two: " + Hand.parameters + " but was " + Hand.parameters.size());
        }
        assertEquals("empty", Hand.parameters.get("type"));
        assertEquals("complex", Hand.parameters.get("name"));
        try {
            Hand.parameters.put("kuk", "buk");
        } catch (UnsupportedOperationException ex) {
            // ok
            return;
        }
        fail("Modifications shall be unsupported");
    }
    
    private DataObject doTemplateWizardCopiesItsPropertiesToMap(String... fileName) throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = null;
        for (String fn : fileName) {
            fo = FileUtil.createData(root, fn);
        }
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        TemplateWizard t = new TemplateWizard();
        t.putProperty("type", "empty");
        Set<DataObject> created = t.instantiate(obj, folder);
        assertNotNull(created);
        assertEquals("One is created: " + created, 1, created.size());
        
        DataObject n = created.iterator().next();
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", fileName[0], n.getName());
        
        assertEquals("The right source", fo, Hand.origObject.get(0));
        assertEquals("The right source in query", fo, Hand.acceptObject.get(0));
        assertEquals("The right destiny folder", folder.getPrimaryFile(), Hand.fileObject.get(0));
        assertEquals("The right name", "simpleObject", Hand.name);
        assertTrue("At least two elements: " + Hand.parameters, 2 <= Hand.parameters.size());
        assertEquals("empty", Hand.parameters.get("wizard.type"));
        assertEquals("There was no name, just default", null, Hand.parameters.get("name"));
        assertTrue("the argument is there", Hand.parameters.containsKey("name"));
        Object date = Hand.parameters.get("date");
        assertNotNull(date);
        assertEquals(String.class, date.getClass());
        Object time = Hand.parameters.get("time");
        assertNotNull(time);
        assertEquals(String.class, time.getClass());
        try {
            Hand.parameters.put("kuk", "buk");
        } catch (UnsupportedOperationException ex) {
            // ok
            return obj;
        }
        fail("Modifications shall be unsupported");
        throw new NullPointerException();
    }

    public static final class Hand extends CreateFromTemplateHandler {
        public static List<FileObject>  fileObject, origObject, acceptObject;
        public static String name;
        public static Map<String, Object> parameters;
    
        protected boolean accept(FileObject fo) {
            acceptObject.add(fo);
            return true;
        }

        protected FileObject createFromTemplate(
            FileObject orig, FileObject f, String n,
            Map<String, Object> p
        ) throws IOException {
            origObject.add(orig);
            fileObject.add(f);
            name = n;
            parameters = p;

            return FileUtil.copyFile(orig, f, name);
        }
    }
    
    public static final class Attr implements CreateFromTemplateAttributesProvider {
        
    
        public Map<String, ? extends Object> attributesFor(
            DataObject template,
            DataFolder target,
            String name
        ) {
            return Collections.singletonMap("name", name);
        }
    }
    
    public static final class DD extends DialogDisplayer {
        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dialog createDialog(final DialogDescriptor descriptor) {
            return new JDialog() {
                @Deprecated
                public void show() {
                    for (Object object : descriptor.getOptions()) {
                        if (object instanceof JButton) {
                            JButton b = (JButton)object;
                            if (b.getText().equals("Finish")) {
                                descriptor.setValue(WizardDescriptor.FINISH_OPTION);
                                b.doClick();
                                return;
                            }
                        }
                    }
                    fail("Cannot find Finish button: " + Arrays.asList(descriptor.getOptions()));
                }
            };
        }
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>array(new DataLoader[] { 
                SimpleLoader.getLoader(SimpleLoader.class),
                TwoPartLoader.getLoader(TwoPartLoader.class),
            });
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("prima")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    
    private static final class FE extends FileEntry {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            fail("I do not want to be called");
            return null;
        }

        
        
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
        
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }
    
    

    public static final class TwoPartLoader extends MultiFileLoader {
        public TwoPartLoader() {
            super(TwoPartObject.class.getName ());
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("java") || fo.hasExt("form")) {
                return org.openide.filesystems.FileUtil.findBrother(fo, "java");
            } else {
                return null;
            }
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new TwoPartObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FE(obj, secondaryFile);
        }
    }
    public static final class TwoPartObject extends MultiDataObject {
        public TwoPartObject(TwoPartLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }
    }
    
}
