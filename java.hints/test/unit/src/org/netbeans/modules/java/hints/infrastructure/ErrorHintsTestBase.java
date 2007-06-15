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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.util.TreePath;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * @author Jan Lahoda
 */
public abstract class ErrorHintsTestBase extends NbTestCase {
    
    public ErrorHintsTestBase(String name) {
        super(name);
        
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String[] additionalLayers = getAdditionalLayers();
        String[] layers = new String[additionalLayers.length + 1];
        
        System.arraycopy(additionalLayers, 0, layers, 1, additionalLayers.length);
        layers[0] = "org/netbeans/modules/java/editor/resources/layer.xml";
        
        SourceUtilsTestUtil.prepareTest(layers, new Object[] {JavaDataLoader.class});
    }
    
    private void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);
        
        assertNotNull(dataFile);
        
        TestUtilities.copyStringToFile(dataFile, code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;
    
    protected abstract List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception;
    
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.toString();
    }
    
    protected String[] getAdditionalLayers() {
        return new String[0];
    }
            
    protected void performAnalysisTest(String fileName, String code, int pos, String... golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        List<Fix> fixes = computeFixes(info, pos, path);
        List<String> fixesNames = new LinkedList<String>();
        
        fixes = fixes != null ? fixes : Collections.<Fix>emptyList();
        
        for (Fix e : fixes) {
            fixesNames.add(toDebugString(info, e));
        }
        
        assertTrue(fixesNames.toString(), Arrays.equals(golden, fixesNames.toArray(new String[0])));
    }
    
    protected void performFixTest(String fileName, String code, int pos, String fixCode, String golden) throws Exception {
        performFixTest(fileName, code, pos, fixCode, fileName, golden);
    }
    
    protected void performFixTest(String fileName, String code, int pos, String fixCode, String goldenFileName, String golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        List<Fix> fixes = computeFixes(info, pos, path);
        List<String> fixesNames = new LinkedList<String>();
        
        fixes = fixes != null ? fixes : Collections.<Fix>emptyList();
        
        Fix fix = null;
        
        for (Fix e : fixes) {
            String debugString = toDebugString(info, e);
            
            fixesNames.add(debugString);
            
            if (fixCode.equals(debugString))
                fix = e;
        }
        
        assertNotNull(fixesNames.toString(), fix);
        
        fix.implement();
        
        FileObject toCheck = sourceRoot.getFileObject(goldenFileName);
        
        assertNotNull(toCheck);
        
        DataObject toCheckDO = DataObject.find(toCheck);
        EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
        Document toCheckDocument = ec.openDocument();
        
        String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());
        
        //ignore whitespaces:
        realCode = realCode.replaceAll("[ \t\n]+", " ");
        
        assertEquals(golden, realCode);
        
        LifecycleManager.getDefault().saveAll();
    }
    
}
