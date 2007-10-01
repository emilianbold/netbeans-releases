/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
