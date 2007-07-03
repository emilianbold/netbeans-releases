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

package org.netbeans.modules.ruby.debugger.breakpoints;

import org.netbeans.modules.ruby.debugger.TestBase;
import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public final class RubyBreakpointTest extends TestBase {
    
    public RubyBreakpointTest(String testName) {
        super(testName);
    }
    
    public void testGetBreakpoints() throws Exception {
        String[] vegetableContent = {
            "puts 'pea, cucumber, cauliflower, broccoli'",
        };
        File vegetableF = createScript(vegetableContent, "vegetable.rb");
        FileObject vegetableFO = FileUtil.toFileObject(vegetableF);
        
        String[] fruitContent = {
            "puts 'apple, pear'",
            "puts 'banana, melon'",
        };
        File fruitF = createScript(fruitContent, "fruit.rb");
        FileObject fruitFO = FileUtil.toFileObject(fruitF);
        addBreakpoint(fruitFO, 1);
        addBreakpoint(vegetableFO, 1);
        addBreakpoint(fruitFO, 2);
        // all
        assertEquals("two Ruby breakpoints", 3, RubyBreakpoint.getBreakpoints().length);
        // by files
        assertEquals("two Ruby breakpoints for fruit.rb", 2, RubyBreakpoint.getBreakpoints(fruitFO).length);
        assertEquals("one Ruby breakpoint for vegetable.rb", 1, RubyBreakpoint.getBreakpoints(vegetableFO).length);
    }
    
}
