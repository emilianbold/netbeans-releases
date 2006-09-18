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

/** Simple anyOf test.
 *
 * @author Jaroslav Tulach
 */
public class MasterTest extends TestCase {
    /** a shared option part of some API */
    static final Option SHARED = Option.requiredArgument(Option.NO_SHORT_NAME, "shared");
    
    public MasterTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(P1.class);
    }
    
    public void testSharedSelected() throws Exception {
        try {
            CommandLine.getDefault().process(new String[] { "--shared", "Ahoj" });
            fail("Should fail with CommandException");
        } catch (CommandException ex) {
            if (ex.getExitCode() == 1) {
                fail("Real working that p1 needs to be used shall be printed");
            }
        }
    }
    public void testP1() throws Exception {
        try {
            CommandLine.getDefault().process(new String[] { "--p1" });
            fail("Should fail with CommandException thrown during processing of our OptionProcessor");
        } catch (CommandException ex) {
            if (ex.getExitCode() != 1) {
                throw ex;
            }
        }
    }
    public void testNothing() throws Exception {
        CommandLine.getDefault().process(new String[] { });
    }
    public void testAll() throws Exception {
        try {
            CommandLine.getDefault().process(new String[] { "--shared", "ble", "--p1" });
            fail("Should fail with CommandException thrown during processing of our OptionProcessor");
        } catch (CommandException ex) {
            if (ex.getExitCode() != 1) {
                throw ex;
            }
        }
    }

    public static final class P1 extends OptionProcessor {
        private static final Option P1 = Option.withoutArgument(Option.NO_SHORT_NAME, "p1");
        
        protected Set<Option> getOptions() {
            return Collections.singleton(OptionGroups.allOf(P1, OptionGroups.anyOf(SHARED)));
        }
        
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            // signal P1 was called
            throw new CommandException(1);
        }
    }
}

