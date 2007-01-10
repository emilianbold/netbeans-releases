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

package org.netbeans.modules.java.editor.codegen;

import com.sun.source.util.TreePath;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jan Lahoda
 */
public class GeneratorUtilsTest extends NbTestCase {
    
    public GeneratorUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
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
    
    public void testImplementAllAbstractMethods1() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\npublic Test(){\n}\n }\n", 54, new RunnableValidator());
    }
    
//    public void testImplementAllAbstractMethods2() throws Exception {
//        performTest("package test;\npublic class Test implements Runnable {\n }\n", 54, new RunnableValidator());
//    }
//    
//    public void testImplementAllAbstractMethods3() throws Exception {
//        performTest("package test;\npublic class Test implements Runnable {\npublic void testMethod() {\n} }\n", 54, new RunnableValidator());
//    }
    
    public void testImplementAllAbstractMethods4() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\npublic Test(){\n}\npublic void testMethod() {\n} }\n", 54, new RunnableValidator());
    }
    
    public void testImplementAllAbstractMethods5() throws Exception {
        performTest("package test;import java.util.concurrent.*;\npublic class Test implements Future<String>{\npublic Test(){\n} }\n", 89, new SimpleFutureValidator("java.lang.String"));
    }
    
//    public void testImplementAllAbstractMethods6() throws Exception {
//        performTest("package test;import java.util.concurrent.*;\npublic class Test implements Future<java.util.List<? extends java.util.List>>{\npublic Test(){\n} }\n", 123, new FutureValidator() {
//            protected TypeMirror returnType(CompilationInfo info) {
//                return SourceUtils.parseType(info, "java.util.List<? extends java.util.List>", info.getElements().getTypeElement("test.Test"));
//            }
//        });
//    }
    
    public void testImplementAllAbstractMethods7() throws Exception {
        performTest("package test;\npublic class Test extends java.util.AbstractList{\npublic Test(){\n} }\n", 64, new Validator() {
            public void validate(CompilationInfo info) {
            }
        });
    }
    
    /** issue #85966
     */
//    public void testImplementAllAbstractMethods8() throws Exception {
//        performTest("package test;\npublic class Test implements XX {\npublic Test(){\n} }\ninterface XX {\npublic void test(String ... a);}", 42, new Validator() {
//            public void validate(CompilationInfo info) {
//                TypeElement clazz = info.getElements().getTypeElement("test.Test");
//                ExecutableElement method = ElementFilter.methodsIn(clazz.getEnclosedElements()).get(0);
//                
//                assertTrue(method.isVarArgs());
//            }
//        });
//    }
    
    public void testImplementAllAbstractMethods9() throws Exception {
        performTest("package test;\npublic class Test implements java.util.concurrent.ExecutorService {\npublic Test(){\n} }\n", 30, new Validator() {
            public void validate(CompilationInfo info) {
            }
        });
    }
    
    public void testImplementAllAbstractMethodsa() throws Exception {
        performTest("package test;\npublic class Test implements XX {\npublic Test(){\n} }\ninterface XX {public <T extends java.util.List> void test(T t);}", 30, new Validator() {
            public void validate(CompilationInfo info) {
            }
        });
    }
    
    public static interface Validator {
        
        public void validate(CompilationInfo info);
        
    }
    
    private final class RunnableValidator implements Validator {
        
        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            
            boolean foundRunMethod = false;
            
            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("run".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(foundRunMethod);
                        foundRunMethod = true;
                    }
                }
            }
            
            assertTrue(foundRunMethod);
        }
        
    }
    
    private final class SimpleFutureValidator extends FutureValidator {
        
        private String returnTypeName;
        
        public SimpleFutureValidator(String returnTypeName) {
            this.returnTypeName = returnTypeName;
        }
        
        protected TypeMirror returnType(CompilationInfo info) {
            TypeElement returnTypeElement = info.getElements().getTypeElement(returnTypeName);
            
            return returnTypeElement.asType();
        }
    }
    
    private abstract class FutureValidator implements Validator {
        
        protected abstract TypeMirror returnType(CompilationInfo info);

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            TypeMirror returnType = returnType(info);
            
            boolean hasShortGet = false;
            boolean hasLongGet = false;
            
            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("get".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(hasShortGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasShortGet = true;
                    }
                    if (ee.getParameters().size() == 2) {
                        assertFalse(hasLongGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasLongGet = true;
                    }
                }
            }
            
            assertTrue(hasShortGet);
            assertTrue(hasLongGet);
        }
        
    }
    
    private void performTest(String sourceCode, final int offset, final Validator validator) throws Exception {
        FileObject root = makeScratchDir(this);
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        
        FileObject source = sourceDir.createFolder("test").createData("Test.java");
        
        writeIntoFile(source, sourceCode);
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        
        JavaSource js = JavaSource.forFileObject(source);
        
        ModificationResult result = js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {
            }
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                TreePath tp = copy.getTreeUtilities().pathFor(offset);
                GeneratorUtils.generateAllAbstractMethodImplementations(copy, tp);
            }
        });
        
        result.commit();
        
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController controller) throws Exception {
                System.err.println("text:");
                System.err.println(controller.getText());
                controller.toPhase(Phase.RESOLVED);
                
                assertEquals(controller.getDiagnostics().toString(), 0, controller.getDiagnostics().size());
                
                validator.validate(controller);
            }
        }, true);
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
}
