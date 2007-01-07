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

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jackpot.transform.Transformer;

/**
 *
 * @author Pavel Flaska
 */

public class CompareTreeTest extends GeneratorTest {
    
    /** Creates a new instance of CompareTreeTest */
    public CompareTreeTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CompareTreeTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodTest1.java");
    }
    /*
    public void testMethodModifiers() throws IOException {
        final TokenHierarchy[] cut = new TokenHierarchy[2];
        getJavaSource(getTestFile()).runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController cc) {
                cut[0] = cc.getTokenHierarchy();
            }
        });
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("firstMethod".contentEquals(node.getName())) {
                        ModifiersTree origMods = node.getModifiers();
                        Set<Modifier> njuMods = new HashSet<Modifier>();
                        njuMods.add(Modifier.PRIVATE);
                        njuMods.add(Modifier.STATIC);
                        changes.rewrite(origMods, make.Modifiers(njuMods));
                    }
                    return null;
                }
            }
        );
        getJavaSource(getTestFile()).runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController cc) {
                cut[1] = cc.getTokenHierarchy();
            }
        });
        Map<Object, CharSequence[]> result = TreeChecker.compareTokens(cut[0], cut[1]);
        //CharSequence[][] cs = .iterator().next().
        for (Map.Entry<Object, CharSequence[]> item : result.entrySet()) {
            System.out.println(item.getKey() + ": '" + item.getValue()[0] + "' != '" + item.getValue()[1] + "'");
        }
    }*/
    
    public void testMethodName() throws IOException {
        final TokenHierarchy[] cut = new TokenHierarchy[2];
        getJavaSource(getTestFile()).runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController cc) {
                cut[0] = cc.getTokenHierarchy();
            }
        },true);
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("secondMethod".contentEquals(node.getName())) {
                        MethodTree njuMethod = make.setLabel(node, "druhaMetoda");
                        changes.rewrite(node, njuMethod);
                    }
                    return null;
                }
            }
        );
        getJavaSource(getTestFile()).runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController cc) {
                cut[1] = cc.getTokenHierarchy();
            }
        },true);
        Map<Object, CharSequence[]> result = TreeChecker.compareTokens(cut[0], cut[1]);
        //CharSequence[][] cs = .iterator().next().
        for (Map.Entry<Object, CharSequence[]> item : result.entrySet()) {
            System.out.println(item.getKey() + ": '" + item.getValue()[0] + "' != '" + item.getValue()[1] + "'");
        }
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/MethodTest1/MethodTest1/";
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }
    
}
