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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import static com.sun.source.tree.Tree.Kind.*;
import java.util.*;
import java.io.IOException;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.junit.NbTestSuite;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * Tests the method generator.
 *
 * @author  Pavel Flaska
 */
public class ConstructorTest extends GeneratorTest {
    
    /** Need to be defined because of JUnit */
    public ConstructorTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ConstructorTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testAddConstructor() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ConstructorTest.java");
        
        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                // exactly one class in compilation unit
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().iterator().next();
                for (Tree member : topLevel.getMembers()) {
                    // for the first inner class in top level
                    if (CLASS == member.getKind()) {
                        
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                        
                        List<VariableTree> arguments = new ArrayList<VariableTree>();
                        arguments.add(make.Variable(
                                make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                "a",
                                make.PrimitiveType(TypeKind.BOOLEAN), null)
                        );
                        
                        MethodTree newConstructor = make.Constructor(
                                mods, 
                                Collections.<TypeParameterTree>emptyList(), 
                                arguments, 
                                Collections.<ExpressionTree>emptyList(), 
                                make.Block(Collections.<StatementTree>emptyList(), false)
                        );
                        ClassTree newInner = make.addClassMember((ClassTree) member, newConstructor);
                        workingCopy.rewrite(member, newInner);
                    }
                }
            }
                
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddConstructor.pass");
    }
        
    public void testAddConstructor2() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ConstructorTest2.java");
        
        JavaSource src = getJavaSource(testFile);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                // exactly one class in compilation unit
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().iterator().next();
                
                ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                List<VariableTree> arguments = new ArrayList<VariableTree>();
                arguments.add(make.Variable(
                        make.Modifiers(EnumSet.noneOf(Modifier.class)),
                        "a", 
                        make.PrimitiveType(TypeKind.BOOLEAN), null)
                );
                MethodTree newConstructor = make.Constructor(
                        mods, 
                        Collections.<TypeParameterTree>emptyList(), 
                        arguments, 
                        Collections.<ExpressionTree>emptyList(), 
                        make.Block(Collections.<StatementTree>emptyList(), false)
                );

                ClassTree newClass = make.addClassMember(topLevel, newConstructor);
                workingCopy.rewrite(topLevel, newClass);
            }
                
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddConstructor2.pass");
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ConstructorTest/ConstructorTest/";
    }

}
