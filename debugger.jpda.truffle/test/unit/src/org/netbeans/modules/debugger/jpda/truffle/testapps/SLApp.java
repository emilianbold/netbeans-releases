/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.truffle.testapps;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SLApp {
    public static void main(String... args) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PolyglotEngine engine = PolyglotEngine.newBuilder().
            setOut(os).
            build();

        Source src = Source.newBuilder(
            "function main() {\n" +
            "  x = 42;\n" +
            "  println(x);\n" +
            "  return x;\n" +
            "}\n"+
            "function init() {\n"+
            "  obj = new();\n"+
            "  obj.fourtyTwo = main;\n"+
            "  return obj;\n"+
            "}\n"
        ).name("Meaning of world.sl").mimeType("application/x-sl").build();
        
        Object result = engine.eval(src).get();                         // LBREAKPOINT
        //Was before: assertNull("No code executed yet", result);
        /*
        PolyglotEngine.Value main = engine.findGlobalSymbol("main");
        assertNotNull("main method found", main);
        result = main.execute().get();                                  // L XX BREAKPOINT
        */
        // Is now:
        assertEquals("Expected result", 42L, result);
        assertEquals("Expected output", "42\n", os.toString("UTF-8"));
        
        // dynamic generated interface
        PolyglotEngine.Value init = engine.findGlobalSymbol("init");
        assertNotNull("init method found", init);
        Compute c = init.execute().as(Compute.class);                   // LBREAKPOINT
        result = c.fourtyTwo();                                         // LBREAKPOINT
        assertEquals("Expected result", 42L, result);
        assertEquals("Expected output", "42\n42\n", os.toString("UTF-8"));
    }
    
    public static interface Compute {
        public Number fourtyTwo();
    }
}
