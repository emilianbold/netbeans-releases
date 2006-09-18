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
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Can we associated short description?
 *
 * @author Jaroslav Tulach
 */
public class ShortDescriptionTest extends TestCase implements Processor {
    private Option help;
    private Option descr;
    
    public ShortDescriptionTest(String s) {
        super(s);
    }

    private void setUpHelp() throws Exception {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        Provider.add(this, help);
    }

    private void setUpShort() {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        descr = Option.shortDescription(help, "org.netbeans.api.sendopts.TestBundle", "HELP");
        assertEquals("Option with description is the same", help, descr);
        assertEquals("Option with description has the same hashCode", help.hashCode(), descr.hashCode());
        Provider.add(this, descr);
    }
    
    public void testPrintedUsage() throws Exception {
        setUpHelp();
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-h.*--help").matcher(w.toString());
        if (!m.find()) {
            fail("-h, --help should be there:\n" + w.toString());
        }

        assertEquals("No help associated", w.toString().indexOf("shorthelp"), -1);
    }
    public void testPrintedUsageEiyhFrdvtipyion() throws Exception {
        setUpShort();
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-h.*--help").matcher(w.toString());
        if (!m.find()) {
            fail("-h, --help should be there:\n" + w.toString());
        }

        if (w.toString().indexOf("shorthelp") == -1) {
            fail("shorthelp associated: " + w.toString());
        }
    }
    public void testProvidedOwnDisplayName() throws Exception {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        Option shor = Option.shortDescription(help, "org.netbeans.api.sendopts.TestBundle", "HELP");
        assertEquals("Option with description is the same", help, shor);
        assertEquals("Option with description has the same hashCode", help.hashCode(), shor.hashCode());
        descr = Option.displayName(shor, "org.netbeans.api.sendopts.TestBundle", "NAMEHELP");
        assertEquals("Option with description is the same", help, descr);
        assertEquals("Option with description has the same hashCode", help.hashCode(), descr.hashCode());
        Provider.add(this, descr);
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-p.*--pomoc").matcher(w.toString());
        if (!m.find()) {
            fail("--pomoc should be there:\n" + w.toString());
        }

        if (w.toString().indexOf("shorthelp") == -1) {
            fail("shorthelp associated: " + w.toString());
        }
    }

    public void process(Env env, Map<Option, String[]> values) throws CommandException {
    }

}

