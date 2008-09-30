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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.compapp.test.ui;

import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import java.awt.Image;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseResultCookie;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseResultDiffAction;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseResultSaveAsOutputAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.EditAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * A node representing a test case result.
 *
 * @author Jun Qian
 */
public class TestCaseResultNode extends FilterNode {
    private static final java.util.logging.Logger mLogger =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.TestCaseResultNode"); // NOI18N
    
    private static final Image RESULT_SUCCESS_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/result_success.png", true); // NOI18N
    
    private static final Image RESULT_FAILURE_ICON = ImageUtilities.loadImage(
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
            String failedResultSuffix = NbBundle.getMessage(TestCaseResultNode.class, 
                    "FAILED_RESULT_SUFFIX"); // NOI18N
            displayName += failedResultSuffix;
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
    // overwriting for debugging purpose (See #85289)
    public boolean canDestroy() {
        return true;
    }
    
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
    
    @SuppressWarnings("unchecked")
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        if (type == TestCaseResultCookie.class) {
            return (T) mTestCaseResultCookie;
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
        final TestcaseNode testCaseNode = getTestCaseNode();
        
        super.destroy();
        
        // refresh diff view
        if (testCaseNode.isDiffTopComponentVisible()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    testCaseNode.refreshDiffTopComponent();
                }
            });            
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
