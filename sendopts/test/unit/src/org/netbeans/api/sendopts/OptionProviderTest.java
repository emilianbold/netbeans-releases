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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/** The basic test to check semantics of getopts behaviour.
 *
 * @author Jaroslav Tulach
 */
public class OptionProviderTest extends TestCase {
    private CommandLine l;
    private static Option help;
    private static Option ok;
    
    static {
        help = Option.withoutArgument('h', "help");
        ok = Option.withoutArgument('o', "ok");

        OP.arr = new Option[] { ok, help };
        MockServices.setServices(OP.class);
    }
    
    public OptionProviderTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        
        OP.values = null;
        
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-h" });
        assertEquals("Processor found", true, OP.values.containsKey(help));
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--help" });
        assertEquals("Processor found for long name", true, OP.values.containsKey(help));
    }

    public void testTwoOptionsRecognized() throws Exception {
        l.process(new String[] { "-ho" });
        assertEquals("Processor for help", true, OP.values.containsKey(help));
        assertEquals("Processor for ok", true, OP.values.containsKey(ok));
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name", true, OP.values.containsKey(help));
    }
    

    public void testIncorrectOptionIdentified() throws Exception {
        try {
            l.process(new String[] { "--hell" });
            fail("This option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", OP.values);
    }

    public void testNoProcessorCalledWhenOneOptionIsNotKnown() throws Exception {
        try {
            l.process(new String[] { "-h", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", OP.values);
    }
    
    public static final class OP extends OptionProcessor {
        static Option[] arr;
        static Map<Option, String[]> values;
        
        protected Set<Option> getOptions() {
            return new HashSet<Option>(Arrays.asList(arr));
        }

        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            values = optionValues;
        }
        
    }
}
