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

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.CookieSet;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.io.NbMarshalledObject;


/**
 * Java vs. Form in wrong order. Based on KitFox's report in #73037.
 *
 * @author Jaroslav Tulach
 */
public class FightWithWrongOrderOfLoadersTest extends LoggingTestCaseHid
implements DataLoader.RecognizedFiles {

    private CharSequence log;
    private FileObject f0;
    private FileObject f1;
    private FileObject f2;
    
    public FightWithWrongOrderOfLoadersTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        log = Log.enable("org.openide.loaders", Level.SEVERE);

        registerIntoLookup(new Pool());

        FileObject fo = FileUtil.createFolder(FileUtil.createMemoryFileSystem().getRoot(), "test");

        f0 = FileUtil.createData(fo, "j1.java");
        f1 = FileUtil.createData(fo, "f.formKit");
        f2 = FileUtil.createData(fo, "f.java");
    }

    protected Level logLevel() {
        return Level.INFO;
    }

    public void testRecognizeToDefault() throws Exception {
        DataObject obj0 = DataObject.find(f0);
        DataObject obj1 = DataObject.find(f1);
        DataObject obj2 = DataObject.find(f2);

        assertEquals(DataLoader.getLoader(JavaDataLoader.class), obj0.getLoader());
        assertEquals(DataLoader.getLoader(JavaDataLoader.class), obj2.getLoader());


        if (obj1 == obj2) {
            fail("They should be different: " + obj1);
        }

        assertEquals("It is default loader", obj1.getLoader(), DataLoaderPool.getDefaultFileLoader());
    }

    public void testRecognizeJavaFirstFormKitLater() throws Exception {
        DataObject obj0 = DataObject.find(f0);
        DataObject obj2 = DataObject.find(f2);
        DataObject obj1 = DataObject.find(f1);

        assertEquals(DataLoader.getLoader(JavaDataLoader.class), obj0.getLoader());
        assertEquals(DataLoader.getLoader(JavaDataLoader.class), obj2.getLoader());

        if (obj1 == obj2) {
            fail("They should be different: " + obj1);
        }

        assertEquals("It is default loader", obj1.getLoader(), DataLoaderPool.getDefaultFileLoader());
    }



    public void markRecognized(FileObject fo) {
    }


    private static final class Pool extends DataLoaderPool {
        private static DataLoader[] ARR = {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            FormKitDataLoader.getLoader(FormKitDataLoader.class),
        };


        protected Enumeration loaders() {
            return Enumerations.array(ARR);
        }
    }

    public static class JavaDataLoader extends MultiFileLoader {

        public static final String JAVA_EXTENSION = "java";

        public JavaDataLoader() {
            super(JavaDO.class.getName());
        }

        protected JavaDataLoader(String s) {
            super(s);
        }

        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt(JAVA_EXTENSION)) {
                return fo;
            } else {
                return null;
            }
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new JavaDO(primaryFile, this);
        }

        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }

        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            throw new UnsupportedOperationException();
        }

        static class JavaDO extends MultiDataObject {
            public JavaDO(FileObject fo, JavaDataLoader l) throws DataObjectExistsException {
                super(fo, l);
            }
        }
    }


    public static class FormKitDataLoader extends JavaDataLoader {
        private static Logger LOG = Logger.getLogger(FormKitDataLoader.class.getName());

        public static final String REQUIRED_MIME = "text/x-java";
        public static final String FORM_EXTENSION = "formKit"; // NOI18N

        private static final long serialVersionUID = 1L;

        public FormKitDataLoader()
        {
            super(FormKitDataObject.class.getName());
        }

        protected String defaultDisplayName()
        {
            return NbBundle.getMessage(FormKitDataLoader.class, "LBL_FormKit_loader_name");
        }

        protected String actionsContext()
        {
            return "Loaders/" + REQUIRED_MIME + "/Actions";
        }

        protected FileObject findPrimaryFile(FileObject fo)
        {
            LOG.info("FormKitDataLoader.findPrimaryFile(): " + fo.getNameExt());

            String ext = fo.getExt();
            if (ext.equals(FORM_EXTENSION))
            {
                return FileUtil.findBrother(fo, JAVA_EXTENSION);
            }
            if (ext.equals(JAVA_EXTENSION) && FileUtil.findBrother(fo, FORM_EXTENSION) != null)
            {
                return fo;
            }
            return null;
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, java.io.IOException
        {
            LOG.info("FormKitDataLoader.createMultiObject(): " + primaryFile.getNameExt());

            return new FormKitDataObject(FileUtil.findBrother(primaryFile, FORM_EXTENSION),
                    primaryFile,
                    this);
        }

        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject multiDataObject, FileObject fileObject)
        {
            if (fileObject.getExt().equals(FORM_EXTENSION))
            {
                FileEntry formEntry = new FileEntry(multiDataObject, fileObject);
                return formEntry;
            }
            return super.createSecondaryEntry(multiDataObject, fileObject);
        }

        public final class FormKitDataObject extends JavaDO {
            FileEntry formEntry;

            public FormKitDataObject(FileObject ffo, FileObject jfo, FormKitDataLoader loader) throws DataObjectExistsException, IOException
            {
                super(jfo, loader);
                formEntry = (FileEntry)registerEntry(ffo);

                CookieSet cookies = getCookieSet();
                //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
            }


        }
    }

}
