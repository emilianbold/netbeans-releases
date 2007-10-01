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

package org.netbeans.test.editor.suites.focus;

import java.awt.*;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.jellytools.modules.editor.Find;
import org.netbeans.jellytools.modules.editor.GoToLine;
import org.netbeans.jellytools.modules.editor.KeyBindings;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.test.editor.LineDiff;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**This is test in very development stage. I put it into the CVS mainly because
 * it simplifies testing on different platforms. This test may or may not
 * be reliable and may or may not work at all.
 *
 * @author  eh103527
 */
public class FocusTestPerformer extends JellyTestCase {
    
    private EditorOperator editor = null;
    private EditorOperator editor2 = null;
    
    String TEXT=" INSERTING TEXT ";
    
    int[] lines=new int[] {10,26,50,42,12,18,32,128,64,256,333,401,54,7,450,220,125,444};//max 459
    
    String[] texts=new String[] {"a1","a2","a3","a4","a5","a6","a7","a8","a9"};
    
    public FocusTestPerformer(String name) {
        super(name);
    }
    
    public synchronized EditorOperator getEditor() {
        return editor;
    }
    
    public EditorOperator getEditor2() {
        return editor2;
    }
    
    public synchronized void createEditor(String fileName) {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/editor/suites/focus/data/testfiles/FocusTestPerformer/"+fileName+".java");
        try {
            DataObject   od = DataObject.find(fo);
            EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
            ec.open();
            
            if (editor == null) {
                editor = new EditorOperator(fileName);
                editor.pushKey(KeyEvent.VK_HOME, KeyEvent.CTRL_MASK);
            } else if (editor2 == null) {
                editor2 = new EditorOperator(fileName);
                editor2.pushKey(KeyEvent.VK_HOME, KeyEvent.CTRL_MASK);
            }
        } catch (DataObjectNotFoundException e) {
            assertTrue(false);
        }
    }
    
    public void prepareLongFileEditor() {
        try {
            EditorOperator.closeDiscardAll();
            log("Closed Welcome screen.");
        } catch (Exception ex) {
        }
        createEditor("Test");
    }
    
    public void prepareShortFileEditor() {
        try {
            EditorOperator.closeDiscardAll();
            log("Closed Welcome screen.");
        } catch (Exception ex) {
        }
        createEditor("Test2");
    }
    
    public void prepare2FilesEditors() {
        try {
            EditorOperator.closeDiscardAll();
            log("Closed Welcome screen.");
        } catch (Exception ex) {
        }
        //open 2 empty files
        createEditor("Test3");
        createEditor("Test4");
    }
    
    
    protected void flushResult() {
        getRef().print(getEditor().getText());
        getRef().flush();
        try {
            assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"),
            new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
        } catch (IOException e) {
            assertTrue("IOException: " + e.getMessage(), false);
        }
    }
    
    public void finishEditor() {
        if (editor != null) {
            getEditor().closeDiscard();
        }
        if (editor2 != null) {
            getEditor2().closeDiscard();
        }
    }
    
    
    public void setUp() {
        log("Starting Focus test.");
        System.setOut(getLog());
    }
    
    public void tearDown() throws Exception {
        log("Ending Focus test.");
    }
    
    private void robotWrite(String s,Robot robot) {
        int c;
        for (int i=0;i < s.length();i++) {
            c=(int)(s.charAt(i));
            if (c == '\n') {
                c=(int)(KeyEvent.VK_ENTER);
            }
            if (Character.isUpperCase((char)c)) {
                robot.keyPress(KeyEvent.VK_SHIFT);
            }
            robot.keyPress(c);
            robot.delay(5);
            robot.keyRelease(c);
            robot.delay(5);
            if (Character.isUpperCase((char)c)) {
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
    }
    
    public void robotPushKey(int keycode, int mask, Robot robot) {
        if ((mask & KeyEvent.CTRL_MASK) > 0) {
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.delay(1);
        }
        if ((mask & KeyEvent.SHIFT_MASK) > 0) {
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.delay(1);
        }
        if ((mask & KeyEvent.ALT_MASK) > 0) {
            robot.keyPress(KeyEvent.VK_ALT);
            robot.delay(1);
        }
        robot.keyPress(keycode);
        robot.delay(50);
        robot.keyRelease(keycode);
        robot.delay(1);
        if ((mask & KeyEvent.ALT_MASK) > 0) {
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.delay(1);
        }
        if ((mask & KeyEvent.SHIFT_MASK) > 0) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.delay(1);
        }
        if ((mask & KeyEvent.CTRL_MASK) > 0) {
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.delay(1);
        }
        robot.waitForIdle();
    }
    
    public void testGoToLine() {
        try {
            log("testGoToLine starting");
            prepareLongFileEditor();
            log("testGoToLine editor prepared");
            Robot robot=new Robot();
            log("testGoToLine Robot created");
            getEditor().grabFocus();
            robot.waitForIdle();
            log("Idle occured.");
            for (int i=0;i < lines.length;i++) {
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.delay(1);
                robot.keyPress(KeyEvent.VK_G);
                robot.delay(50);
                robot.keyRelease(KeyEvent.VK_G);
                robot.delay(1);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.waitForIdle();
                robot.delay(50);
                GoToLine.goToLine(lines[i],robot);
                robot.delay(1000);
                robotWrite(TEXT,robot);
                robot.delay(500);
            }
            log("testGoToLine flush results:");
            flushResult();
        } catch (AWTException ex) {
            assertTrue("Robot couldn't be created: AWTException "+ex.getClass().getName()+": "+ex.getMessage(),false);
        } finally {
            log("testGoToLine closing editor:");
            finishEditor();
            log("testGoToLine finished");
        }
    }
    
    public void testFind() {
        try {
            log("testFind starting");
            prepareShortFileEditor();
            log("testFind editor prepared");
            Robot robot=new Robot();
            log("testFind Robot created");
            getEditor().grabFocus();
            robot.waitForIdle();
            for (int i=0;i < texts.length;i++) {
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.delay(1);
                robot.keyPress(KeyEvent.VK_F);
                robot.delay(20);
                robot.keyRelease(KeyEvent.VK_F);
                robot.delay(1);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.waitForIdle();
                Find.find(texts[i],robot);
                robot.delay(500);
                robotWrite(TEXT,robot);
                robot.delay(500);
            }
            log("testFind flush results:");
            flushResult();
        } catch (AWTException ex) {
            assertTrue("Robot couldn't be created: AWTException "+ex.getClass().getName()+": "+ex.getMessage(),false);
        } finally {
            log("testFind closing editor:");
            finishEditor();
            log("testFind finished");
        }
    }
    
    /**
     *  This test case should not be run with solaris operation system.
     *  Alt+Left/Right switch the X workspaces there.
     *
     */
    public void testTabsSwitching() {
        try {
            log("TabsSwitching starting");
            
            String osname=System.getProperty("os.name").toLowerCase();
            
            if (osname.indexOf("solaris") > -1 || osname.indexOf("sunos") > -1) {
                log("This test case couldn't be run in Solaris platform.");
                this.assertTrue("Solaris system.",true);
                
                return;
            }
            Robot robot=new Robot();
            log("testFind Robot created");
            prepare2FilesEditors();
            getEditor().grabFocus();
            for (int i=0;i < 10;i++) {
                robotPushKey(KeyEvent.VK_RIGHT,KeyEvent.ALT_MASK,robot);
                robot.waitForIdle();
                robotPushKey(KeyEvent.VK_LEFT,KeyEvent.ALT_MASK,robot);
                robot.waitForIdle();
                robotWrite(TEXT+"\n",robot);
                robot.waitForIdle();
                robot.delay(500);
            }
            flushResult();
        } catch (AWTException ex) {
            assertTrue("Robot couldn't be created: AWTException "+ex.getClass().getName()+": "+ex.getMessage(),false);
        } finally {
            log("TabsSwitching closing editor:");
            finishEditor();
            log("TabsSwitching finished");
        }
    }
    
    public static void main(String[] args) throws Exception {
        Object o=org.openide.options.SystemOption.findObject(org.openide.actions.NextTabAction.class);
        

        if (o != null) {
            org.openide.actions.NextTabAction act=(org.openide.actions.NextTabAction)o;
            Object o2=act.getActionMapKey();
            System.out.println(o2);
        }
        //new CheckActionsListPerformer("testCheckActions").testCheckActions();
    }
    
}
