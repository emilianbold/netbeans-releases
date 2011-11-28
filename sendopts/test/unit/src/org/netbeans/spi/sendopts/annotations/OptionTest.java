/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.sendopts.annotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OptionTest extends NbTestCase {
    private CommandLine cmd;
    
    static {
        System.setProperty("org.openide.util.Lookup.paths", "Services");
    }

    public OptionTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        cmd = CommandLine.getDefault();
    
        enabled = false;
        withParam = null;
        additionalParams = null;
        methodCalled = false;
    }

    public void testParseEnabled() throws Exception {
        cmd.process("-e");
        assertTrue("enabled set", enabled);
    }

    public void testParseEnabledWithParamFails() {
        try {
            cmd.process("-e", "Param");
            fail("Parse shall not succeed");
        } catch (CommandException ex) {
            // oK
        }
        assertFalse("enabled not set", enabled);
    }

    public void testParseWithParam() throws CommandException {
        cmd.process("-pParam");
        assertFalse("enabled not set", enabled);
        assertEquals("Param", withParam);
        assertTrue("Method called", methodCalled);
    }
    public void testParseWithoutParamFails() throws CommandException {
        try {
            cmd.process("-p");
            fail("Missing param for -p");
        } catch (CommandException ex) {
            // OK
            assertFalse("No method called", methodCalled);
        }
    }
    public void testParseAdditionalParam() throws CommandException {
        cmd.process("no", "-a", "Param");
        assertFalse("enabled not set", enabled);
        assertNotNull("additionalParams set", additionalParams);
        assertEquals("two", 2, additionalParams.length);
        assertEquals("no", additionalParams[0]);
        assertEquals("Param", additionalParams[1]);
    }
    public void testParseLongAdditional() throws CommandException {
        cmd.process("no", "--additional", "Param");
        assertFalse("enabled not set", enabled);
        assertNotNull("additionalParams set", additionalParams);
        assertEquals("two", 2, additionalParams.length);
        assertEquals("no", additionalParams[0]);
        assertEquals("Param", additionalParams[1]);
    }
    public void testHelp() throws CommandException {
        StringWriter w = new StringWriter();
        cmd.usage(new PrintWriter(w));
        assertTrue("contains additionalParams:\n" + w, w.toString().contains(("AddOnParams")));
        assertTrue("contains short help:\n" + w, w.toString().contains(("ShortHelp")));
    }

    
    @Option(shortName='e')
    private static boolean enabled;
    
    @Option(shortName='p')
    static String withParam;
    
    @Description(displayName="#NAME", shortDescription="#SHORT")
    @Option(shortName='a', longName="additional")
    @NbBundle.Messages({
        "NAME=AddOnParams", 
        "SHORT=ShortHelp"
    })
    public static String[] additionalParams;
    

    private static boolean methodCalled;
    @PostProcess
    private static void callMeAfterArgumentsAreSet() {
        methodCalled = true;
    } 
    
    public void testCheckForStatic() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.netbeans.spi.sendopts.annotations.Option;\n" +
            "public class A {\n" +
            "  @Option(shortName='a')" +
            "  public String nonStatic;" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("static")) {
            fail(os.toString());
        }
    }
    
}


