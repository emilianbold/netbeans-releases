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
package org.netbeans.modules.java.hints;

import org.netbeans.modules.java.hints.AddOverrideAnnotation;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jan Lahoda
 */
public class AddOverrideAnnotationTest extends TreeRuleTestBase {
    
    public AddOverrideAnnotationTest(String testName) {
        super(testName);
    }
    
    public void testAddOverride1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test extends java.util.ArrayList {public int size() {return 0;}}", 121-48, "0:72-0:76:verifier:Add @Override Annotation");
    }

    public void testAddOverride2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test implements Runnable {public void run() {}}", 115-48);
    }
    
    public void testAddOverride3() throws Exception {
        sourceLevel = "1.6";
        performAnalysisTest("test/Test.java", "package test; public class Test implements Runnable {public void run() {}}", 115-48, "0:65-0:68:verifier:Add @Override Annotation");
    }
    
    public void testAddOverride4() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class UUUU {public void () {} private static class W extends UUUU {public void () {}}}", 150-48);
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new AddOverrideAnnotation().run(info, path);
    }
    
    private String sourceLevel = "1.5";
    
}
