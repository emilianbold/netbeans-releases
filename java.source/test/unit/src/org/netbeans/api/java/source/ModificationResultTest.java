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

package org.netbeans.api.java.source;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
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
    
    private void performTestToGuardedDocument(String creator) throws Exception {
        prepareTest();
        
        StyledDocument doc = ces.openDocument();
        
        NbDocument.markGuarded(doc, 4, 6);
        
        Method m = ModificationResultTest.class.getDeclaredMethod(creator, new Class[0]);
        
        ModificationResult result = (ModificationResult) m.invoke(this, new Object[0]);
        
        for (FileObject fo : result.getModifiedFileObjects()) {
            for (ModificationResult.Difference diff : result.getDifferences(fo)) {
                diff.setCommitToGuards(true);
            }
        }

        
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
    
    public void testInsertToGuardedDocument() throws Exception {
        performTestToGuardedDocument("prepareInsertResult");
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
