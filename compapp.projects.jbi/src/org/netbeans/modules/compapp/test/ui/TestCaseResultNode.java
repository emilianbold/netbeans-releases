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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.compapp.test.ui;

import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import java.awt.Image;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.Action;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseResultCookie;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseResultDiffAction;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseResultSaveAsOutputAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.EditAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;

/**
 * DOCUMENT ME!
 *
 * @author Jun Qian
 */
public class TestCaseResultNode extends FilterNode {
    private static final java.util.logging.Logger mLogger =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.TestCaseResultNode"); // NOI18N
    
    private static final Image RESULT_SUCCESS_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/result_success.png", true); // NOI18N
    
    private static final Image RESULT_FAILURE_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/result_failure.png", true); // NOI18N
    
    
    private JbiProject mProject;
    private FileObject mActualFile;
    private boolean mSuccess;
    private TestCaseResultCookie mTestCaseResultCookie;
    
    /**
     * Creates a new TestInputNode object.
     *
     * @param jpp DOCUMENT ME!
     * @param mProject DOCUMENT ME!
     */
    public TestCaseResultNode(JbiProject project, DataObject dataObject) {
        super(dataObject.getNodeDelegate(), Children.LEAF);
        mProject = project;
        mActualFile = dataObject.getPrimaryFile();   
        
        String fileName = mActualFile.getName();
        mSuccess = fileName.endsWith("_S"); // NOI18N
        
        mTestCaseResultCookie = new TestCaseResultCookie(this);
    }
    
    public String getDisplayName() {
        String fileName = mActualFile.getName();
        String displayName = getActualResultTimeStamp(fileName);
        if (!mSuccess) {
            displayName += " - Failed"; // NOI18N
        }
        return displayName;
    }
    
    public boolean isSuccessful() {
        return mSuccess;
    }
       
    
//    public boolean canCut() {
//        return false;
//    }
    
//    public boolean canCopy() {
//        return false;
//    }
    
//    public boolean canDestroy() {
//        return false;
//    }
    
    public boolean canRename() {
        return false;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Image getIcon(int type) {
        return mSuccess ? RESULT_SUCCESS_ICON : RESULT_FAILURE_ICON;
    }
        
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(TestCaseResultDiffAction.class),
            SystemAction.get(TestCaseResultSaveAsOutputAction.class),
            null,
            SystemAction.get(EditAction.class),
            SystemAction.get(DeleteAction.class)
        };
    }
    
    public Node.Cookie getCookie(Class type) {
        if (type == TestCaseResultCookie.class) {
            return mTestCaseResultCookie;
        }
        return super.getCookie(type);
    }
    
    public JbiProject getProject() {
        return mProject;
    }
    
    public TestcaseNode getTestCaseNode() {
        return (TestcaseNode) getParentNode()/*.getParentNode()*/;
    }
        
    public void destroy() throws java.io.IOException {
        TestcaseNode testCaseNode = getTestCaseNode();
        
        super.destroy();
        
        // refresh diff view
        if (testCaseNode.isDiffTopComponentVisible()) {
            testCaseNode.refreshDiffTopComponent();
        }
    }
    
    public static String getActualResultTimeStamp(String fileName) {
        // e.x., Actual_20060803211027.xml, Actual_20060803211027_F.xml, Actual_20060803211027_S.xml
        
        String timeStamp;
        try {
            String yearStr = fileName.substring(7, 11);
            String monthStr = fileName.substring(11, 13);
            String dayStr = fileName.substring(13, 15);
            String hourStr = fileName.substring(15, 17);
            String minuteStr = fileName.substring(17, 19);
            String secondStr = fileName.substring(19, 21);
            
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);
            int day = Integer.parseInt(dayStr);
            int hour = Integer.parseInt(hourStr);
            int minute = Integer.parseInt(minuteStr);
            int second = Integer.parseInt(secondStr);
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month-1, day, hour, minute, second);
            Date date = calendar.getTime();
            Object[] arguments = { date };
            String pattern = "{0, date} {0, time}";     // NOI18N
            timeStamp = MessageFormat.format(pattern, arguments);
        } catch (Exception e) {
            timeStamp = "<Unknown Time Stamp>"; // NOI18N
        }
        return timeStamp;
    }
}
