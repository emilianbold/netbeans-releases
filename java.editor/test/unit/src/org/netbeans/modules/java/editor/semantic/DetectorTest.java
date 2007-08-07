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
package org.netbeans.modules.java.editor.semantic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.java.editor.semantic.SemanticHighlighter.ErrorDescriptionSetter;
import org.netbeans.modules.java.editor.semantic.TestBase.Performer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class DetectorTest extends TestBase {
    
    public DetectorTest(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        LifecycleManager.getDefault().saveAll();
    }

    public void testUnusedImports() throws Exception {
        performTest("UnusedImports");
    }

    public void testColorings1() throws Exception {
        performTest("Colorings1");
    }

    public void testReadUseInstanceOf() throws Exception {
        performTest("ReadUseInstanceOf");
    }
    
    public void testReadUseTypeCast() throws Exception {
        performTest("ReadUseTypeCast");
    }
    
    public void testReadUseArrayIndex() throws Exception {
        performTest("ReadUseArrayIndex");
    }
    
    public void testReadUseUnaryOperator() throws Exception {
        performTest("ReadUseUnaryOperator");
    }
    
    public void testReadUseReturn() throws Exception {
        performTest("ReadUseReturn");
    }
    
    public void testCompoundPackage() throws Exception {
	performTest("CompoundPackage");
    }
    
    public void testSemanticInnerClasses() throws Exception {
	performTest("SemanticInnerClasses");
    }
    
    public void testForEach() throws Exception {
	performTest("ForEach");
    }
    
    public void testWriteUseArgument() throws Exception {
	performTest("WriteUseArgument");
    }
    
    public void testReturnType() throws Exception {
	performTest("ReturnType");
    }
    
    public void testFieldByThis1() throws Exception {
	performTest("FieldByThis1");
    }
    
    public void testFieldByThis2() throws Exception {
        performTest("FieldByThis2");
    }
    
    public void testWriteUseCatch() throws Exception {
	performTest("WriteUseCatch");
    }
    
    public void testReadWriteUseArgumentOfAbstractMethod() throws Exception {
	performTest("ReadWriteUseArgumentOfAbstractMethod");
    }
    
    public void testReadUseExprIsIdent1() throws Exception {
	performTest("ReadUseExprIsIdent1");
    }
    
    public void testReadUseExprIsIdent2() throws Exception {
	performTest("ReadUseExprIsIdent2");
    }
    
    public void testReadUseExprIsIdent3() throws Exception {
	performTest("ReadUseExprIsIdent3");
    }
    
    public void testReadUseExprIsIdent4() throws Exception {
	performTest("ReadUseExprIsIdent4");
    }
    
    public void testClassUseNewInstance() throws Exception {
	performTest("ClassUseNewInstance");
    }
    
    public void testExecUseMethodCall() throws Exception {
	performTest("ExecUseMethodCall");
    }
    
    public void testReadUseArrayInit() throws Exception {
	performTest("ReadUseArrayInit");
    }
    
    public void testReadUseNewArrayIndex() throws Exception {
	performTest("ReadUseNewArrayIndex");
    }
    
    public void testUsages2() throws Exception {
        performTest("Usages2");
    }

    public void testCommentedGenerics() throws Exception {
        performTest("CommentedGenerics");
    }
    
    public void testRetentionPolicy() throws Exception {
        performTest("RetentionPolicyTest");
    }

    public void testSimpleGeneric() throws Exception {
        performTest("SimpleGeneric");
    }
    
    public void testReadUseMathSet() throws Exception {
        performTest("ReadUseMathSet");
    }
    
    public void testReadUseMathSet2() throws Exception {
        performTest("ReadUseMathSet2");
    }
    
    public void testReadUseTernaryOperator() throws Exception {
        performTest("ReadUseTernaryOperator");
    }
    
    public void testUseInGenerics() throws Exception {
        performTest("UseInGenerics");
    }
    
    public void testFieldIsWritten1() throws Exception {
        performTest("FieldIsWritten1");
    }
    
    public void testFieldIsWritten2() throws Exception {
        performTest("FieldIsWritten2");
    }
    
    public void testConstructorsAreMethods() throws Exception {
        performTest("ConstructorsAreMethods");
    }
    
    public void testConstructorsAreMethods2() throws Exception {
        performTest("ConstructorsAreMethods2");
    }
    
    public void testDoubleBrackets() throws Exception {
        performTest("DoubleBrackets");
    }
    
    public void testConstructorsAreMethods3() throws Exception {
        performTest("ConstructorsAreMethods3");
    }
    
    public void testMethodWithArrayAtTheEnd() throws Exception {
        performTest("MethodWithArrayAtTheEnd");
    }
    
    public void testReadUseAssert() throws Exception {
        performTest("ReadUseAssert");
    }
    
    public void testSuperIsKeyword() throws Exception {
        performTest("SuperIsKeyword");
    }
    
    public void testNewArrayIsClassUse() throws Exception {
        performTest("NewArrayIsClassUse");
    }
    
    public void testNotKeywords() throws Exception {
        performTest("NotKeywords");
    }
    
    public void testArrayThroughInitializer() throws Exception {
        performTest("ArrayThroughInitializer");
    }
    
    public void testReadUseAssert2() throws Exception {
        performTest("ReadUseAssert2");
    }
    
    public void testConstructorUsedBySuper1() throws Exception {
        performTest("ConstructorUsedBySuper1");
    }
    
    public void testConstructorUsedBySuper2() throws Exception {
        performTest("ConstructorUsedBySuper2");
    }
    
    public void testConstructorUsedByThis() throws Exception {
        performTest("ConstructorUsedByThis");
    }
    
    public void testUnresolvableImportsAreNotUnused() throws Exception {
        performTest("UnresolvableImportsAreNotUnused");
    }
    
    public void testEnums() throws Exception {
        performTest("Enums");
    }
    
    public void testReadUseThrow() throws Exception {
        performTest("ReadUseThrow");
    }
    
    public void testGenericBoundIsClassUse() throws Exception {
        performTest("GenericBoundIsClassUse");
    }
    
    public void testBLE91246() throws Exception {
        final boolean wasThrown[] = new boolean[1];
        Logger.getLogger(Utilities.class.getName()).addHandler(new Handler() {
            public void publish(LogRecord lr) {
                if (lr.getThrown() != null && lr.getThrown().getClass() == BadLocationException.class) {
                    wasThrown[0] = true;
                }
            }
            public void close() {}
            public void flush() {}
        });
        performTest("BLE91246");
        
        assertFalse("BLE was not thrown", wasThrown[0]);
    }
    
    public void testArrayAccess() throws Exception {
        performTest("ArrayAccess");
    }
    
    public void test88119() throws Exception {
        performTest("package-info");
    }
    
    public void test111113() throws Exception {
        performTest("UnusedImport111113");
    }
    
    private void performTest(String fileName) throws Exception {
        performTest(fileName, new Performer() {
            public void compute(CompilationController parameter, Document doc, ErrorDescriptionSetter setter) {
                SemanticHighlighter.ERROR_DESCRIPTION_SETTER = setter;
                
                new SemanticHighlighter(parameter.getFileObject()).process(parameter, doc);
            }
        });
    }
    
    public void testSimpleRemoveImport() throws Exception {
        performRemoveUnusedImportTest("SimpleRemoveImport");
    }
    
    public void testRemoveImportNotLine1() throws Exception {
        performRemoveUnusedImportTest("RemoveImportNotLine1");
    }
    
//    public void testRemoveImportNotLine2() throws Exception {
//        performRemoveUnusedImportTest("RemoveImportNotLine2");
//    }
    
    public void testRemoveImportDocStart() throws Exception {
        performRemoveUnusedImportTest("RemoveImportDocStart");
    }
    
    public void testRemoveImportTrim() throws Exception {
        performRemoveUnusedImportTest("RemoveImportTrim");
    }
    
    public void testRemoveImportDocStartTrim() throws Exception {
        performRemoveUnusedImportTest("RemoveImportDocStartTrim");
    }
    
    public void testRemoveAllImports() throws Exception {
        performRemoveUnusedImportTest("RemoveAllImports", 2, 2, 0, 1);
    }
    
    private FileObject testSourceFO;
    
    protected void performRemoveUnusedImportTest(String fileName) throws Exception {
        performRemoveUnusedImportTest(fileName, 1, 1, 0, 0);
    }
    
    protected void performRemoveUnusedImportTest(String fileName, int errorCount, int fixesCount, int errorToFix, int fixToPerform) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        
	FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
	FileObject cache   = scratch.createFolder("cache");
	
        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");
        
        testSource.getParentFile().mkdirs();
        
        File dataFolder = new File(getDataDir(), "org/netbeans/modules/java/editor/semantic/data/");
        
        for (File f : dataFolder.listFiles()) {
            copyToWorkDir(f, new File(wd, "test/" + f.getName()));
        }
        
        testSourceFO = FileUtil.toFileObject(testSource);

        assertNotNull(testSourceFO);
        
        File testBuildTo = new File(wd, "test-build");
        
        testBuildTo.mkdirs();

        SourceUtilsTestUtil.prepareTest(FileUtil.toFileObject(dataFolder), FileUtil.toFileObject(testBuildTo), cache);
        SourceUtilsTestUtil.compileRecursively(FileUtil.toFileObject(dataFolder));

        final Document doc = getDocument(testSourceFO);
        
        JavaSource source = JavaSource.forFileObject(testSourceFO);
        
        assertNotNull(source);
        
        final List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        
        SemanticHighlighter.ErrorDescriptionSetter oldSetter = SemanticHighlighter.ERROR_DESCRIPTION_SETTER;
        
        try {
            SemanticHighlighter.ERROR_DESCRIPTION_SETTER = new SemanticHighlighter.ErrorDescriptionSetter() {
                public void setErrors(Document doc, List<ErrorDescription> errs, List<TreePathHandle> allUnusedImports) {
                    errors.addAll(errs);
                }
            
                public void setHighlights(Document doc, OffsetsBag highlights) {
                }

                public void setColorings(Document doc,
                                         Map<Token, Coloring> colorings,
                                         Set<Token> addedTokens,
                                         Set<Token> removedTokens) {
                }
            };
            
            source.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) {
                    try {
                        parameter.toPhase(Phase.UP_TO_DATE);
                        new SemanticHighlighter(parameter.getFileObject()).process(parameter, doc);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, true);
            
            assertEquals(errors.toString(), errorCount, errors.size());
            
            List<Fix> fixes = errors.get(errorToFix).getFixes().getFixes();
            
            assertEquals(fixesCount, fixes.size());
            
            fixes.get(fixToPerform).implement();
            
            ref(doc.getText(0, doc.getLength()));
            
            compareReferenceFiles();
        } finally {
            SemanticHighlighter.ERROR_DESCRIPTION_SETTER = oldSetter;
        }
    }
}
