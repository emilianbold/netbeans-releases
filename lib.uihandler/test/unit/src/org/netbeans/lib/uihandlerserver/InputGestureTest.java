/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandlerserver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.InputGesture;
import org.netbeans.lib.uihandler.TestHandler;

/**
 *
 * @author Jaroslav Tulach
 */
public class InputGestureTest extends NbTestCase {
    private Logger LOG;
    
    public InputGestureTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected int timeOut() {
        return 0; //5000;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected void tearDown() throws Exception {
    }
    
    public void testIsLowLevelAction() throws Exception {
        String s = "<record>n"
+ "<date>2007-03-27T18:45:54</date>n"
+ "<millis>1175013954455</millis>n"
+ "<sequence>1236</sequence>n"
+ "<level>FINE</level>n"
+ "<thread>13</thread>n"
+ "<message>UI_ACTION_EDITOR</message>n"
+ "<param>java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=null,when=1175013954453,modifiers=] on org.openide.text.QuietEditorPane[,0,-2250,1086x12960,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@1976f3a,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@b8df14,typeHandlers=]</param>n"
+ "<param>java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=null,when=1175013954453,modifiers=] on org.openide.text.QuietEditorPane[,0,-1500,1086x12960,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@1976f3a,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@b8df14,typeHandlers=]</param>n"
+ "<param>org.netbeans.editor.BaseKit$PageDownAction@596079</param>n"
+ "<param>org.netbeans.editor.BaseKit$PageDownAction@596079</param>n"
+ "<param>page-down</param>n"
+ "</record>";
        
        InputStream is = new ByteArrayInputStream(s.getBytes());
        TestHandler records = new TestHandler(is);
        LogRecord r = records.read();
        assertNotNull(r);
        
        assertNull("No gesture", InputGesture.valueOf(r));
    }

    public void testReadALogAndTestInputGestures() throws Exception {
        InputStream is = getClass().getResourceAsStream("NB1216449736.0");
        SortedMap<Integer,InputGesture> expectedGestures = new TreeMap<Integer,InputGesture>();
        expectedGestures.put(35, InputGesture.MENU);
        expectedGestures.put(59, InputGesture.KEYBOARD);
        expectedGestures.put(66, InputGesture.MENU);
        expectedGestures.put(80, InputGesture.MENU);
        expectedGestures.put(81, InputGesture.MENU);
        expectedGestures.put(177, InputGesture.KEYBOARD);
        expectedGestures.put(197, InputGesture.KEYBOARD);
        expectedGestures.put(205, InputGesture.MENU);
        TestHandler records = new TestHandler(is);
        for (int cnt = 0;; cnt++) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = records.read();
            if (r == null) {
                break;
            }
            if (r.getSequenceNumber() > expectedGestures.lastKey()) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record, seq {1}", new Object[] { cnt, r.getSequenceNumber() });

            InputGesture g = InputGesture.valueOf(r);
            InputGesture exp = expectedGestures.get((int)r.getSequenceNumber());
            assertEquals(cnt + ": For: " + r.getSequenceNumber() + " txt:\n`"+ r.getMessage() +
                "\nkey: " + r.getResourceBundleName()
                , exp, g);
        }
        is.close();
    }

    public void testReadAToolbar() throws Exception {
        InputStream is = getClass().getResourceAsStream("NB_PROF634066243");
        SortedMap<Integer,InputGesture> expectedGestures = new TreeMap<Integer,InputGesture>();
        expectedGestures.put(62, InputGesture.TOOLBAR);
        expectedGestures.put(63, InputGesture.MENU);
        TestHandler records = new TestHandler(is);
        for (int cnt = 0;; cnt++) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = records.read();
            if (r == null) {
                break;
            }
            if (r.getSequenceNumber() > expectedGestures.lastKey()) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record, seq {1}", new Object[] { cnt, r.getSequenceNumber() });

            InputGesture g = InputGesture.valueOf(r);
            InputGesture exp = expectedGestures.get((int)r.getSequenceNumber());
            assertEquals(cnt + ": For: " + r.getSequenceNumber() + " txt:\n`"+ r.getMessage() +
                "\nkey: " + r.getResourceBundleName()
                , exp, g);
        }
        is.close();
    }
}
