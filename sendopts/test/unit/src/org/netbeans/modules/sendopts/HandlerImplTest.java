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

package org.netbeans.modules.sendopts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.Processor;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.Lookup;

/**
 *
 * @author jarda
 */
public class HandlerImplTest extends NbTestCase {
    static Object key;
    static Object[] args;
    ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    static ResourceBundle bundle;
    
    public HandlerImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        OP.arr = new Option[] { Option.withoutArgument(Option.NO_SHORT_NAME, "haha") };
        MockServices.setServices(OP.class);
        bundle = ResourceBundle.getBundle("org.netbeans.modules.sendopts.TestBundle");
    }

    public void testErrorMessageIsPrinted() {
        key = "SIMPLEERROR";

        int ret = HandlerImpl.execute(new String[] { "--haha" }, is, os, err, new File("."));

        assertEquals("Execution returns 337", 337, ret);
        assertEquals("No out", 0, os.toByteArray().length);

        String msg = bundle.getString("SIMPLEERROR");
        assertEquals("error is as expected", msg, err.toString().replace("\n", "").replace("\r", ""));
    }
    public void testErrorMessageIsPrintedWithArgs() {
        key = "ARGS";
        args = new Object[] { "Y" };

        int ret = HandlerImpl.execute(new String[] { "--haha" }, is, os, err, new File("."));

        assertEquals("Execution returns 337", 337, ret);
        assertEquals("No out", 0, os.toByteArray().length);

        assertEquals("error is as expected", "XYZ", err.toString().replace("\n", "").replace("\r", ""));
    }
    public void testErrorMessageForInlinedThrowable() {
        key = new Exception() {
            public String getLocalizedMessage() {
                return "LOC";
            }
        };

        int ret = HandlerImpl.execute(new String[] { "--haha" }, is, os, err, new File("."));

        assertEquals("Execution returns 221", 221, ret);
        assertEquals("No out", 0, os.toByteArray().length);

        assertEquals("error is as expected", "LOC", err.toString().replace("\n", "").replace("\r", ""));
    }

    public static final class OP extends OptionProcessor {
        static Option[] arr;
        static Map<Option, String[]> values;
        
        protected Set<Option> getOptions() {
            return new HashSet<Option>(Arrays.asList(arr));
        }

        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            values = optionValues;
            assertNotNull("each test needs to assign a key", key);
            if (key instanceof Throwable) {
                CommandException ex = new CommandException(221);
                ex.initCause((Throwable)key);
                throw ex;
            }

            String locMsg = MessageFormat.format(bundle.getString((String) key), args);
            throw new CommandException(337, locMsg);
        }
        
    }
    
}
