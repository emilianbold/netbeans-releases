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

package org.netbeans.test.editor.suites.keybindings;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.jellytools.modules.editor.KeyBindings;
import org.netbeans.test.editor.LineDiff;

/**This is test in very development stage. I put it into the CVS mainly because
 * it simplifies testing on different platforms. This test may or may not
 * be reliable and may or may not work at all.
 *
 * @author  Jan Lahoda
 */
public class CheckActionsListPerformer extends JellyTestCase {
    
    public static String[] TESTED_EDITORS={"Java Editor","Plain Editor","HTML Editor"};
    String editorName;
    
    public CheckActionsListPerformer(String name) {
        super(name);
    }
    
    /**
     * @param args the command line arguments
     */
    public void doTest() throws Exception {
        log("doTest start");
        log("Editor name: "+editorName);
        try {
            Hashtable table;
            log("Grabbing actions...");
            table = KeyBindings.listAllKeyBindings(editorName);
            Object[] keys=table.keySet().toArray();
            Arrays.sort(keys);
            List list;
            log("Writting to ref file...");
            File f=new File(getWorkDir(),editorName+" actions.ref");
            PrintWriter pw=new PrintWriter(new FileWriter(f));
            for (int i=0;i < keys.length;i++) {
                pw.print(keys[i]+": ");
                list=(List)table.get(keys[i]);
                for (int j=0;j < list.size();j++) {
                    pw.print(list.get(j)+" ");
                }
                pw.println();
            }
            pw.close();
        } finally {
            log("doTest finished");
        }
    }
    
    public void setUp() {
        log("Starting check Key Bindings actions test.");
    }
    
    public void tearDown() throws Exception {
        log("Ending check Key Bindings actions test.");
        File ref=new File(getWorkDir(),editorName+" actions.ref");
        assertFile("Some actions aren't same as before the split.", getGoldenFile(editorName+" actions.pass"), ref, new File(getWorkDir(),editorName+" actions.diff"), new LineDiff(false));
        ref.delete();
    }
    
    public void testCheckPlainActions() throws Exception {
        editorName="Plain Editor";
        doTest();
    }
    
    public void testCheckJavaActions() throws Exception {
        editorName="Java Editor";
        doTest();
    }
    
    public void testCheckHTMLActions() throws Exception {
        editorName="HTML Editor";
        doTest();
    }
    
    public static void main(String[] args) throws Exception {
        //new CheckActionsListPerformer("testCheckActions").testCheckActions();
    }
    
}
