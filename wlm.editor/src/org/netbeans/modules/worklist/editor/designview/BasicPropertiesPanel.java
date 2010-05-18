/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTimeout;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.worklist.editor.designview.components.ExTabbedPane;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.StyledLabel;
import org.netbeans.modules.worklist.editor.nodes.TaskNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 *
 * @author anjeleevich
 */
public class BasicPropertiesPanel extends DesignViewPanel implements Widget, 
        FocusListener
{
    private TaskAttributesPanel taskPropertiesPanel;
    private AssignmentPanel assignmentPanel;
    private TimeoutsPanel timeoutsPanel;
    private KeywordsPanel keywordsPanel;
    
    private JComponent assignmentTitle;
    private JComponent timeoutsTitle;
    private JComponent keywordsTitle;
    
    private ExTabbedPane.HeaderRow assignmentHeader = null;
    private ExTabbedPane.HeaderRow timeoutsHeader = null;
    
    public BasicPropertiesPanel(DesignView designView) {
        super(designView);

        ExUtils.setA11Y(this, "BasicPropertiesPanel"); // NOI18N

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        taskPropertiesPanel = new TaskAttributesPanel(designView);
        assignmentPanel = new AssignmentPanel(this, designView, getTask());
        timeoutsPanel = new TimeoutsPanel(this, designView);
        keywordsPanel = new KeywordsPanel(this, designView);
        
        assignmentTitle = assignmentPanel.getView();
        timeoutsTitle = timeoutsPanel.getView();
        keywordsTitle = keywordsPanel.getView();
        
        add(taskPropertiesPanel);
        add(Box.createVerticalStrut(10)).setFocusable(false);
        add(assignmentTitle);
        add(Box.createVerticalStrut(10)).setFocusable(false);
        add(timeoutsTitle);
        add(Box.createVerticalStrut(10)).setFocusable(false);
        add(keywordsTitle);
        
//        updateHeaderCounters();
    }
    
    public void processWLMModelChanged() {
        assignmentPanel.processWLMModelChanged();
        timeoutsPanel.processWLMModelChanged();
        taskPropertiesPanel.processWLMModelChanged();
        keywordsPanel.processWLMModelChanged();
//        updateHeaderCounters();
    }

    void addToTabbedPane(ExTabbedPane tabbedPane) {
        ExTabbedPane.Tab tab = tabbedPane.addTab(NAME, this, true,
                getDesignView().getLDAPCompactBrowser(), null);
        tab.addHeaderRow(getMessage("LBL_BASIC_PROPERTIES"), null, // NOI18N
                StyledLabel.PLAIN_STYLE);
//
//        assignmentHeader = tab.addHeaderRow(
//                getMessage("LBL_ASSIGNMENT"), // NOI18N
//                "(0/0)", StyledLabel.SMALL_STYLE); // NOI18N
//        timeoutsHeader = tab.addHeaderRow(getMessage("LBL_TIMEOUTS"), // NOI18N
//                "(0)", StyledLabel.SMALL_STYLE); // NOI18N
//
//        updateHeaderCounters();
    }
    
//    private void updateHeaderCounters() {
//        if (assignmentHeader == null && timeoutsHeader == null) {
//            return;
//        }
//
//        TTask task = getTask();
//
//        int userCount = 0;
//        int groupCount = 0;
//        int deadlineCount = 0;
//        int durationCount = 0;
//
//        if (task != null) {
//            List<TTimeout> timeouts = task.getTimeouts();
//            if (timeouts != null && !timeouts.isEmpty()) {
//                for (TTimeout timeout : timeouts) {
//                    if (timeout.getDuration() != null) {
//                        durationCount++;
//                    } else if (timeout.getDeadline() != null) {
//                        deadlineCount++;
//                    } else {
//                        durationCount++;
//                    }
//                }
//            }
//
//            TAssignment assignment = task.getAssignment();
//            if (assignment != null) {
//                List<User> users = assignment.getUsers();
//                if (users != null) {
//                    userCount = users.size();
//                }
//
//                List<Group> groups = assignment.getGroups();
//                if (groups != null) {
//                    groupCount = groups.size();
//                }
//            }
//        }
//
//        if (assignmentHeader != null) {
//            assignmentHeader.setCount("(" + userCount + "/" // NOI18N
//                    + groupCount + ")"); // NOI18N
//        }
//
//        if (timeoutsHeader != null) {
//            timeoutsHeader.setCount("(" + durationCount + "/" // NOI18N
//                    + deadlineCount + ")"); // NOI18N
//        }
//    }
    
    public static final String NAME = "BASIC_PROPERTIES"; // NOI18N

    private static String getMessage(String key) {
        return NbBundle.getMessage(BasicPropertiesPanel.class, key);
    }

    public Widget getWidget(int index) {
        if (index == 0) {
            return taskPropertiesPanel.getTitleWidget();
        }

        if (index == 1) {
            return taskPropertiesPanel.getPriorityWidget();
        }

        if (index == 2) {
            return assignmentPanel;
        }

        if (index == 3) {
            return timeoutsPanel;
        }

        if (index == 4) {
            return keywordsPanel;
        }

        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        return 5;
    }

    public Node getWidgetNode() {
        return new TaskNode(getTask(), Children.LEAF, getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showBasicPropertiesTab();
    }

    public WLMComponent getWidgetWLMComponent() {
        return getTask();
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.TASK;
    }

    public Widget getWidgetParent() {
        return getDesignView();
    }

    public void focusGained(FocusEvent e) {
        selectWidget(this);
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }
}
