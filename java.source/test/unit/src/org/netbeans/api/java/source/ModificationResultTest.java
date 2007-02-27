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

package org.netbeans.api.java.source;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;

/**
 *
 * @author Jan Lahoda
 */
public class ModificationResultTest extends NbTestCase {
    
    /** Creates a new instance of ModificationResultTest */
    public ModificationResultTest(String name) {
        super(name);
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    private FileObject testFile;
    private CloneableEditorSupport ces;
    
    private void prepareTest() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject root = fs.getRoot();
        testFile = FileUtil.createData(root, "test/test.java");
        
        writeIntoFile(testFile, "test\ntest\ntest\n");
        
        DataObject od = DataObject.find(testFile);
        
        ces = (CloneableEditorSupport) od.getCookie(EditorCookie.class);
    }
    
    private ModificationResult prepareInsertResult() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, start1, start1, "", "new-test1\n");
        PositionRef start2 = ces.createPositionRef(10, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, start2, start2, "", "new-test2\n");
        
        ModificationResult result = new ModificationResult(null);
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    private void performTestToFile(String creator) throws Exception {
        prepareTest();
        
        Method m = ModificationResultTest.class.getDeclaredMethod(creator, new Class[0]);
        
        ModificationResult result = (ModificationResult) m.invoke(this, new Object[0]);
        
        result.commit();
        
        Document doc = ces.openDocument();
        
        ref(doc.getText(0, doc.getLength()));
        
        compareReferenceFiles();
    }
    
    private void performTestToDocument(String creator) throws Exception {
        prepareTest();
        
        Document doc = ces.openDocument();
        
        Method m = ModificationResultTest.class.getDeclaredMethod(creator, new Class[0]);
        
        ModificationResult result = (ModificationResult) m.invoke(this, new Object[0]);
        
        result.commit();
        
        ref(doc.getText(0, doc.getLength()));
        
        compareReferenceFiles();
    }
    
    private ModificationResult prepareRemoveResult() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        PositionRef end1 = ces.createPositionRef(9, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.REMOVE, start1, end1, "test", "");
        PositionRef start2 = ces.createPositionRef(11, Bias.Forward);
        PositionRef end2 = ces.createPositionRef(12, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.REMOVE, start2, end2, "e", "");
        
        ModificationResult result = new ModificationResult(null);
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    private ModificationResult prepareModificationResult1() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        PositionRef end1 = ces.createPositionRef(9, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start1, end1, "test", "ab");
        PositionRef start2 = ces.createPositionRef(11, Bias.Forward);
        PositionRef end2 = ces.createPositionRef(13, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start2, end2, "es", "a");
        
        ModificationResult result = new ModificationResult(null);
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    private ModificationResult prepareModificationResult2() throws Exception {
        PositionRef start1 = ces.createPositionRef(5, Bias.Forward);
        PositionRef end1 = ces.createPositionRef(9, Bias.Forward);
        ModificationResult.Difference diff1 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start1, end1, "test", "abcde");
        PositionRef start2 = ces.createPositionRef(11, Bias.Forward);
        PositionRef end2 = ces.createPositionRef(13, Bias.Forward);
        ModificationResult.Difference diff2 = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, start2, end2, "es", "a");
        
        ModificationResult result = new ModificationResult(null);
        
        result.diffs = new HashMap<FileObject, List<ModificationResult.Difference>>();
        result.diffs.put(testFile, Arrays.asList(diff1, diff2));
        
        return result;
    }
    
    public void testInsertToFile() throws Exception {
        performTestToFile("prepareInsertResult");
    }
    
    public void testInsertToDocument() throws Exception {
        performTestToDocument("prepareInsertResult");
    }
    
    public void testRemoveFromFile() throws Exception {
        performTestToFile("prepareRemoveResult");
    }
    
    public void testRemoveFromDocument() throws Exception {
        performTestToDocument("prepareRemoveResult");
    }
    
    public void testModification1ToFile() throws Exception {
        performTestToFile("prepareModificationResult1");
    }
    
    public void testModification1ToDocument() throws Exception {
        performTestToDocument("prepareModificationResult1");
    }
    
    public void testModification2ToFile() throws Exception {
        performTestToFile("prepareModificationResult2");
    }
    
    public void testModification2ToDocument() throws Exception {
        performTestToDocument("prepareModificationResult2");
    }
    
}
