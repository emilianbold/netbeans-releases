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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandlerserver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.lib.uihandler.ProjectOp;

/**
 *
 * @author Jaroslav Tulach
 */
public class ProjectOpTest extends NbTestCase {
    private Logger LOG;
    
    public ProjectOpTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.FINEST;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected void tearDown() throws Exception {
    }

    public void testOpenAndCloseAProject() throws Exception {
        String what = "<record>" +
            "<date>2007-02-05T14:14:17</date>" +
            "<millis>1170681257194</millis>" +
            "<sequence>1148</sequence>" +
            "<logger>org.netbeans.ui.projects</logger>" +
            "<level>CONFIG</level>" +
            "<thread>11</thread>" +
            "<message>Closing 1 NbModuleProject Projects</message>" +
            "<key>UI_CLOSED_PROJECTS</key>" +
            "<catalog>&lt;null&gt;</catalog>" +
            "<param>org.netbeans.modules.apisupport.project.NbModuleProject</param>" +
            "<param>NbModuleProject</param>" +
            "<param>1</param>" +
            "</record>" +
            "<record>" +
              "<date>2007-02-06T09:08:03</date>" +
              "<millis>1170749283986</millis>" +
              "<sequence>1441</sequence>" +
              "<logger>org.netbeans.ui.actions.editor</logger>" +
              "<level>FINE</level>" +
              "<thread>11</thread>" +
              "<message>Invoking copy-to-clipboard implemented as org.netbeans.editor.BaseKit$CopyAction@e29e2c thru java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=,when=1170749283985,modifiers=Ctrl] on org.openide.text.QuietEditorPane[,0,-931,1048x1485,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@aec245,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@1e6cecc,typeHandlers=]</message>" +
              "<key>UI_ACTION_EDITOR</key>" +
              "<catalog>&lt;null&gt;</catalog>" +
              "<param>java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=,when=1170749283985,modifiers=Ctrl] on org.openide.text.QuietEditorPane[,0,-931,1041x1515,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@aec245,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@1e6cecc,typeHandlers=]</param>" +
              "<param>java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=,when=1170749283985,modifiers=Ctrl] on org.openide.text.QuietEditorPane[,0,-931,1048x1485,layout=javax.swing.plaf.basic.BasicTextUI$UpdateHandler,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.basic.BasicBorders$MarginBorder@aec245,flags=296,maximumSize=,minimumSize=,preferredSize=,caretColor=java.awt.Color[r=255,g=255,b=255],disabledTextColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],editable=true,margin=java.awt.Insets[top=0,left=0,bottom=0,right=0],selectedTextColor=sun.swing.PrintColorUIResource[r=51,g=51,b=51],selectionColor=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],kit=org.netbeans.modules.editor.java.JavaKit@1e6cecc,typeHandlers=]</param>" +
              "<param>org.netbeans.editor.BaseKit$CopyAction@e29e2c</param>" +
              "<param>org.netbeans.editor.BaseKit$CopyAction@e29e2c</param>" +
              "<param>copy-to-clipboard</param>" +
            "</record>" +
            "<record>" +
            "  <date>2007-02-06T09:05:59</date>" +
            "  <millis>1170749159147</millis>" +
            "  <sequence>1399</sequence>" +
            "  <logger>org.netbeans.ui.projects</logger>" +
            "  <level>CONFIG</level>" +
            "  <thread>11</thread>" +
            "  <message>Opening 1 NbModuleProject Projects</message>" +
            "  <key>UI_OPEN_PROJECTS</key>" +
            "  <catalog>&lt;null&gt;</catalog>" +
            "  <param>org.netbeans.modules.apisupport.project.NbModuleProject</param>" +
            "  <param>NbModuleProject</param>" +
            "  <param>1</param>" +
            "</record>";

        InputStream is = new ByteArrayInputStream(what.getBytes());
        LogRecord rec = LogRecords.read(is);
        LogRecord rec2 = LogRecords.read(is);
        LogRecord rec3 = LogRecords.read(is);
        is.close();
        
        ProjectOp op = ProjectOp.valueOf(rec);
        
        assertNotNull("This record is project operation", op);
        assertEquals("org.netbeans.modules.apisupport.project.NbModuleProject", op.getProjectType());
        assertEquals(-1, op.getDelta());
        assertEquals("NbModuleProject", op.getProjectDisplayName());
        
        
        op = ProjectOp.valueOf(rec2);
        assertNull("No project operation", op);
        
        op = ProjectOp.valueOf(rec3);
        assertNotNull("This record is project operation", op);
        assertEquals("org.netbeans.modules.apisupport.project.NbModuleProject", op.getProjectType());
        assertEquals("One project added", 1, op.getDelta());
        assertEquals("NbModuleProject", op.getProjectDisplayName());

    }
    
      
      
}


