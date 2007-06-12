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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda
 */
public class EmptyCancelForCancellableTaskTest extends TreeRuleTestBase {
    
    private static final String ERROR_MESSAGE = "Empty cancel()";
    
    public EmptyCancelForCancellableTaskTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import org.netbeans.api.java.source.*; public class Test implements CancellableTask<CompilationInfo> { public void cancel() {}}", 180 - 48, "0:129-0:135:verifier:" + ERROR_MESSAGE);
    }
    
    public void testSimple2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import org.netbeans.api.java.source.*; public class Test implements CancellableTask<CompilationInfo> { public void cancel(boolean b) {}}", 180 - 48);
    }
    
    public void testSimple3() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import org.netbeans.api.java.source.*; public class Test { public void cancel() {}}", 180 - 48);
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return new EmptyCancelForCancellableTask().run(info, path);
    }
    
    @Override
    protected FileObject[] extraClassPath() {
        FileObject api = URLMapper.findFileObject(CancellableTask.class.getProtectionDomain().getCodeSource().getLocation());
        
        assertNotNull(api);
        
        return new FileObject[] {FileUtil.getArchiveRoot(api)};
    }
    
}
