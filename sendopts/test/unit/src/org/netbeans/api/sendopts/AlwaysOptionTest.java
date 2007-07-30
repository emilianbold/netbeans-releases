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
package org.netbeans.api.sendopts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** The basic test to check semantics of always option.
 *
 * @author Jaroslav Tulach
 */
public class AlwaysOptionTest extends TestCase {
    private CommandLine l;
    private NoArgProc proc = new NoArgProc();
    private Option help;
    private NoArgProc okProc = new NoArgProc();
    private Option ok;
    
    private Always always = new Always();
    private Option all;
    
    public AlwaysOptionTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        
        help = Option.withoutArgument('h', "help");
        Provider.clearAll();
        Provider.add(proc, help);
        ok = Option.withoutArgument('o', "ok");
        Provider.add(okProc, ok);

        all = Option.always();
        Provider.add(always, all);
        
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-h" });
        assertEquals("Processor found", help, proc.option);
        assertEquals("Always", all, always.option);
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--help" });
        assertEquals("Processor found for long name", help, proc.option);
        assertEquals("Always", all, always.option);
    }

    public void testTwoOptionsRecognized() throws Exception {
        l.process(new String[] { "-ho" });
        assertEquals("Processor for help", help, proc.option);
        assertEquals("Processor for ok", ok, okProc.option);
        assertEquals("Always", all, always.option);
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name", help, proc.option);
        assertEquals("Always", all, always.option);
        
        proc.option = null;
        always.option = null;
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name again", help, proc.option);
        assertEquals("Always", all, always.option);
    }
    

    public void testIncorrectOptionIdentified() throws Exception {
        try {
            l.process(new String[] { "--hell" });
            fail("This option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
        assertEquals("Always not called in case of failure", null, always.option);
    }

    public void testNoProcessorCalledWhenOneOptionIsNotKnown() throws Exception {
        try {
            l.process(new String[] { "-h", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
        assertEquals("Always not called in case of failure", null, always.option);
    }
    
    static final class NoArgProc implements Processor {
        Option option;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            assertEquals(1, values.size());
            option = values.keySet().iterator().next();
            assertNotNull("An option is provided", option);

            String[] args = values.get(option);
            assertNotNull("Values is always [0]", args);
            assertEquals("Values is always [0]", 0, args.length);
        }
    }
    static final class Always implements Processor {
        Option option;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            assertEquals(1, values.size());
            option = values.keySet().iterator().next();
            assertNotNull("An option is provided", option);

            String[] args = values.get(option);
            assertNotNull("Values is always [0]", args);
            assertEquals("Values is always [0]", 0, args.length);
        }
    }
}
