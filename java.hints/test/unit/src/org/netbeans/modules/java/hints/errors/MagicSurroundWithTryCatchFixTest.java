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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class MagicSurroundWithTryCatchFixTest extends ErrorHintsTestBase {
    
    public MagicSurroundWithTryCatchFixTest(String testName) {
        super(testName);
    }

    public void test104085() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {try {}finally{new java.io.FileInputStream(\"\");}}}",
                       127 - 43,
                       "FixImpl",
                       "package test; import java.io.FileNotFoundException; import java.util.logging.Level; import java.util.logging.Logger; public class Test {public void test() {try {}finally{try { new java.io.FileInputStream(\"\"); } catch (FileNotFoundException ex) { Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); } }}}");
    }
    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new UncaughtException().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof MagicSurroundWithTryCatchFix) {
            return "FixImpl";
        }
        
        return super.toDebugString(info, f);
    }
    
}
