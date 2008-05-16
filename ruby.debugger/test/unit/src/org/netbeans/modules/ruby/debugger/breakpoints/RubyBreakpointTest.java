/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.ruby.debugger.breakpoints;

import org.netbeans.modules.ruby.debugger.TestBase;
import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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
        assertEquals("two Ruby breakpoints", 3, RubyBreakpointManager.getBreakpoints().length);
        // by files
        assertEquals("two Ruby breakpoints for fruit.rb", 2, RubyBreakpointManager.getBreakpoints(fruitFO).length);
        assertEquals("one Ruby breakpoint for vegetable.rb", 1, RubyBreakpointManager.getBreakpoints(vegetableFO).length);
    }
    
    public void testSetCondition() throws Exception {
        if (tryToSwitchToRDebugIDE()) {
            String[] testContent = {
                "1.upto(10) do |i|",
                "  sleep 0.01",
                "  sleep 0.01",
                "end"
            };
            File testF = createScript(testContent);
            FileObject testFO = FileUtil.toFileObject(testF);
            RubyBreakpoint bp = addBreakpoint(testFO, 2);
            bp.setCondition("i>7");
            Process p = startDebugging(testF);
            doContinue(); // i == 8
            doContinue(); // i == 9
            doContinue(); // i == 10
            p.waitFor();
        }
    }
}
