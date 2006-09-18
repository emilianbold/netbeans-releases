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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Is the environment correctly passed to processors?
 *
 * @author Jaroslav Tulach
 */
public class EnvTest extends TestCase {
    private CommandLine l;
    private OneArgProc proc = new OneArgProc();
    private Option define;
    private Option refine;
    private Option ignore;
    private Option files;
    
    public EnvTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Provider.clearAll();
        
        define = Option.optionalArgument('D', "define");
        refine = Option.requiredArgument('R', "refine");
        ignore = Option.withoutArgument('I', "ignore");
        files = Option.additionalArguments('F', "files");
        
        Provider.add(proc, define, refine, ignore, files);
        
        l = CommandLine.getDefault();
    }
    
    public void testDefaultEnv() throws Exception {
        proc.expIs = System.in;
        proc.expOs = System.out;
        proc.expErr = System.err;
        proc.expDir = new File(System.getProperty("user.dir"));
        
        l.process(new String[] { "-Dx", "-Ry", "-I", "-F", "somefile" });
        assertEquals("one checks", 1, proc.cnt);
        assertEquals("but on four options", 4, proc.values.size());
    }
        
    public void testOwnEnv() throws Exception {
        proc.expIs = new ByteArrayInputStream(new byte[0]);
        proc.expOs = new PrintStream(new ByteArrayOutputStream());
        proc.expErr = new PrintStream(new ByteArrayOutputStream());
        proc.expDir = new File("c:/");
        
        l.process(new String[] { "-Dx", "-Ry", "-I", "-F", "somefile" }, proc.expIs, proc.expOs, proc.expErr, proc.expDir);
        
        assertEquals("one check", 1, proc.cnt);
        assertEquals("but on four options", 4, proc.values.size());
    }
    
    static final class OneArgProc implements Processor {
        InputStream expIs;
        OutputStream expOs;
        OutputStream expErr;
        File expDir;
        int cnt;
        Map<Option,String[]> values;
        
        private void assertEnv(Env env) {
            assertEquals(expIs, env.getInputStream());
            assertEquals(expOs, env.getOutputStream());
            assertEquals(expErr, env.getErrorStream());
            assertEquals(expDir, env.getCurrentDirectory());
            cnt++;
        }

        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertEnv(env);
            this.values = new HashMap<Option,String[]>(values);
        }
    }
}
