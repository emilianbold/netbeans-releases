/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin.provider;

import java.io.Reader;
import java.io.StringReader;
import org.netbeans.api.diff.Difference;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.diff.DiffProvider;

/**
 * The test of the built-in diff provider
 *
 * @author Martin Entlicher
 */
public class BuiltInDiffProviderTest extends NbTestCase {
    
    private static final String[] SIMPLE1 = {
        "Hi",
        "there!",
        "  ",
        "Oops,",
        "end."
    };
    
    /** Creates a new instance of BuiltInDiffProviderTest */
    public BuiltInDiffProviderTest(String name) {
        super(name);
    }
    
    private static DiffProvider createDiffProvider() {
        return new BuiltInDiffProvider();
        // Use CmdlineDiffProvider as a reference to check the test is O.K.
        //return org.netbeans.modules.diff.cmdline.CmdlineDiffProvider.createDefault();
    }
    
    // A simple ADD difference
    public void testSimpleAdd() throws Exception {
        DiffProvider bdp = createDiffProvider();
        String s1 = linesToString(SIMPLE1);
        String[] simple2add = new String[SIMPLE1.length + 1];
        String added = "Added Line";
        for (int i = 0; i <= SIMPLE1.length; i++) {
            for (int j = 0; j < simple2add.length; j++) {
                if (i == j) {
                    simple2add[j] = added;
                } else if (j < i) {
                    simple2add[j] = SIMPLE1[j];
                } else {
                    simple2add[j] = SIMPLE1[j-1];
                }
            }
            String s2 = linesToString(simple2add);
            Difference[] diff = bdp.computeDiff(new StringReader(s1),
                                                new StringReader(s2));
            assertEquals("WAS COMPARING:\n"+s1+"WITH:\n"+s2, 1, diff.length);
            String rightDiff = "Difference(ADD, "+i+", "+0+", "+(i+1)+", "+(i+1)+")";
            assertEquals(diff[0].toString()+" != "+rightDiff+"\nWAS COMPARING:\n"+s1+"WITH:\n"+s2, rightDiff, diff[0].toString());
        }
    }
    
    // A simple DELETE difference
    public void testSimpleDelete() throws Exception {
        DiffProvider bdp = createDiffProvider();
        String s1 = linesToString(SIMPLE1);
        String[] simple2delete = new String[SIMPLE1.length - 1];
        for (int i = 0; i < SIMPLE1.length; i++) {
            for (int j = 0; j < simple2delete.length; j++) {
                if (j < i) {
                    simple2delete[j] = SIMPLE1[j];
                } else {
                    simple2delete[j] = SIMPLE1[j+1];
                }
            }
            String s2 = linesToString(simple2delete);
            Difference[] diff = bdp.computeDiff(new StringReader(s1),
                                                new StringReader(s2));
            assertEquals("WAS COMPARING:\n"+s1+"WITH:\n"+s2, 1, diff.length);
            String rightDiff = "Difference(DELETE, "+(i+1)+", "+(i+1)+", "+i+", "+0+")";
            assertEquals(diff[0].toString()+" != "+rightDiff+"\nWAS COMPARING:\n"+s1+"WITH:\n"+s2, rightDiff, diff[0].toString());
        }
    }
    
    // A simple CHANGE difference
    public void testSimpleChange() throws Exception {
        DiffProvider bdp = createDiffProvider();
        String s1 = linesToString(SIMPLE1);
        String[] simple2delete = new String[SIMPLE1.length];
        for (int i = 0; i < SIMPLE1.length; i++) {
            for (int j = 0; j < simple2delete.length; j++) {
                if (i == j) {
                    simple2delete[j] = "Changed Line";
                } else if (j < i) {
                    simple2delete[j] = SIMPLE1[j];
                } else {
                    simple2delete[j] = SIMPLE1[j];
                }
            }
            String s2 = linesToString(simple2delete);
            Difference[] diff = bdp.computeDiff(new StringReader(s1),
                                                new StringReader(s2));
            assertEquals("WAS COMPARING:\n"+s1+"WITH:\n"+s2, 1, diff.length);
            String rightDiff = "Difference(CHANGE, "+(i+1)+", "+(i+1)+", "+(i+1)+", "+(i+1)+")";
            assertEquals(diff[0].toString()+" != "+rightDiff+"\nWAS COMPARING:\n"+s1+"WITH:\n"+s2, rightDiff, diff[0].toString());
        }
    }
    
    private static String linesToString(String[] lines) {
        String newline = System.getProperty("line.separator");
        StringBuffer sb1 = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            sb1.append(lines[i]);
            sb1.append(newline);
        }
        return sb1.toString();
    }
    
}
