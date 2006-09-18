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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/** Can one option be shared among two processors?
 *
 * @author Jaroslav Tulach
 */
public class SharedOptionTest extends TestCase {
    /** a shared option part of some API */
    static final Option SHARED = Option.requiredArgument(Option.NO_SHORT_NAME, "shared");
    /** a method to convert the option to something meaningful */
    static String getSharedMessage(Map<Option,String[]> args, Class<?> who) {
        String[] v = args.get(SHARED);
        return v == null ? "NOMSG" : "Shared msg " + v[0] + "from " + who.getName();
    }
    
    
    public SharedOptionTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(P1.class, P2.class);
        P1.called = null;
        P2.called = null;
    }
    
    public void testP1IsSelected() throws Exception {
        CommandLine.getDefault().process(new String[] { "--shared", "Ahoj", "--p1" });
        
        assertNull("No P2", P2.called);
        assertNotNull("P1 called", P1.called);
        
        CommandException ex = (CommandException) P1.called;
        if (ex.getLocalizedMessage().indexOf("Shared msg Ahoj") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }
        if (ex.getLocalizedMessage().indexOf("P1") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }
    }

    public void testP2IsSelected() throws Exception {
        CommandLine.getDefault().process(new String[] { "--shared", "Ahoj", "--p2" });

    
        assertNull("No P1", P1.called);
        assertNotNull("P2 called", P2.called);
        
        CommandException ex = (CommandException) P2.called;
        if (ex.getLocalizedMessage().indexOf("Shared msg Ahoj") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }
        if (ex.getLocalizedMessage().indexOf("P2") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }
    }
    
    public void testBothSelected() throws Exception {
        CommandLine.getDefault().process(new String[] { "--shared", "Ahoj", "--p2", "--p1" });

    
        assertNotNull("P1 called", P1.called);
        CommandException ex = (CommandException) P1.called;
        if (ex.getLocalizedMessage().indexOf("Shared msg Ahoj") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }
        if (ex.getLocalizedMessage().indexOf("P1") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }

        assertNotNull("P2 called", P2.called);
        
        ex = (CommandException) P2.called;
        if (ex.getLocalizedMessage().indexOf("Shared msg Ahoj") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }
        if (ex.getLocalizedMessage().indexOf("P2") == -1) {
            fail("Value of shared option influences the localized message: " + ex.getLocalizedMessage());
        }
    }

    public void testNothingCalled() throws Exception {
        try {
            CommandLine.getDefault().process(new String[] { "--shared", "Ahoj" });
            fail("Just shared is not valid option");
        } catch (CommandException ex) {
            // ok
        }
        
        assertNull("No P2", P2.called);
        assertNull("No P1", P1.called);
    }

    public void testJustP1Called() throws Exception {
        CommandLine.getDefault().process(new String[] { "--p1" });
        
        assertNull("No P2", P2.called);
        assertNotNull("Yes P1", P1.called);
        assertEquals("NOMSG", P1.called.getLocalizedMessage());
    }
    
    public void testJustP2Called() throws Exception {
        CommandLine.getDefault().process(new String[] { "--p2" });
        
        assertNull("No P1", P1.called);
        assertNotNull("Yes P2", P2.called);
        assertEquals("NOMSG", P2.called.getLocalizedMessage());
    }

    public void testJustBothPsCalled() throws Exception {
        CommandLine.getDefault().process(new String[] { "--p1", "--p2" });
        
        assertNotNull("Yes P1", P1.called);
        assertNotNull("Yes P2", P2.called);
        assertEquals("NOMSG", P1.called.getLocalizedMessage());
        assertEquals("NOMSG", P2.called.getLocalizedMessage());
    }
    
    static final Option createMasterSlaveOption(Option master, Option slave) {
        return OptionGroups.allOf(master, OptionGroups.anyOf(slave));
    }
    
    public static final class P1 extends OptionProcessor {
        private static final Option P1 = Option.withoutArgument(Option.NO_SHORT_NAME, "p1");
        static Throwable called;
        
        protected Set<Option> getOptions() {
            return Collections.singleton(createMasterSlaveOption(P1, SHARED));
        }
        
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            assertNull("Not called yet", called);
            // signal P1 was called
            called = new CommandException(1, getSharedMessage(optionValues, getClass()));
        }
    }
    public static final class P2 extends OptionProcessor {
        private static final Option P2 = Option.withoutArgument(Option.NO_SHORT_NAME, "p2");
        static Throwable called;

        protected Set<Option> getOptions() {
            return Collections.singleton(createMasterSlaveOption(P2, SHARED));
        }
        
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            assertNull("Not called yet", called);
            // signal P2 was called
            called = new CommandException(2, getSharedMessage(optionValues, getClass()));
        }
    }
}

