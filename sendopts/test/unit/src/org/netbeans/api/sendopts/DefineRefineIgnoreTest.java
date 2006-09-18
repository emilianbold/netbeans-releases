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

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Various types of processor and option closures tested.
 *
 * @author Jaroslav Tulach
 */
public class DefineRefineIgnoreTest extends TestCase {
    private OneArgProc proc = new OneArgProc();
    private Option define;
    private Option refine;
    private Option ignore;
    private Option files;
    
    public DefineRefineIgnoreTest(String s) {
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
    }
    
    public void testDefineRefinePair() throws CommandException {
        Option pair = OptionGroups.allOf(define, refine);
        Provider.add(proc, pair);
            
        CommandLine l = CommandLine.getDefault();
        l.process(new String[] { "--define=1", "--refine", "2" });
        
        assertEquals("V1", "1", proc.clone.get(define)[0]);
        assertEquals("V2", "2", proc.clone.get(refine)[0]);
    }
    
    public void testWithoutAdditonal() throws CommandException {
        Option pair = OptionGroups.allOf(ignore, files);
        Provider.add(proc, pair);
            
        CommandLine l = CommandLine.getDefault();
        l.process(new String[] { "--ignore", "--files", "30" });
        
        assertTrue("V1", proc.clone.containsKey(ignore));
        assertEquals("V2", "30", proc.clone.get(files)[0]);
    }
    
    static final class OneArgProc implements Processor {
        Map<Option, String[]> clone;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("No clone yet", clone);
            clone = new HashMap<Option, String[]>(values);
        }
    }
}
