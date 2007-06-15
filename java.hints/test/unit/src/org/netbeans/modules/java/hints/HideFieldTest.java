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

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jaroslav Tulach
 */
public class HideFieldTest extends TreeRuleTestBase {
    private String sourceLevel = "1.5";
    
    public HideFieldTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Locale.setDefault(Locale.US);
    }
    
    
    
    public void testClassWithOnlyStaticMethods() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "}" +
            "class Test2 extends Test {" +
            "  private float val";
        String after =         "ue;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:92-0:97:verifier:Field hides another field"
        );
    }
    public void testNoHintWithMethod() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value() { return 1 };" +
            "}" +
            "class Test2 extends Test {" +
            "  private float val";
        String after =         "ue;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new HideField().run(info, path);
    }
    
    
}
