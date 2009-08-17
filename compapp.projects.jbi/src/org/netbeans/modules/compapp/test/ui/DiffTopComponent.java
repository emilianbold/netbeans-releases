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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * DiffTopComponent.java
 *
 * Created on February 16, 2006, 6:08 PM
 *
 * @author Bing Lu
 * @author Jun Qian
 */
public class DiffTopComponent extends TopComponent {
    private static final Logger mLogger = Logger.getLogger("org.netbeans.modules.compapp.test.ui.DiffTopComponent"); // NOI18N
    
    private final static ImageIcon FIRST_ICON =
            new ImageIcon(TestcaseNode.class.getResource("/org/netbeans/modules/compapp/test/ui/resources/first.gif")); // NOI18N
    private final static ImageIcon LAST_ICON =
            new ImageIcon(TestcaseNode.class.getResource("/org/netbeans/modules/compapp/test/ui/resources/last.gif")); // NOI18N
    private final static ImageIcon PREV_ICON =
            new ImageIcon(TestcaseNode.class.getResource("/org/netbeans/modules/compapp/test/ui/resources/prev.gif")); // NOI18N
    private final static ImageIcon NEXT_ICON =
            new ImageIcon(TestcaseNode.class.getResource("/org/netbeans/modules/compapp/test/ui/resources/next.gif")); // NOI18N
    private final static ImageIcon REFRESH_ICON =
            new ImageIcon(TestcaseNode.class.getResource("/org/netbeans/modules/compapp/test/ui/resources/refresh.gif")); // NOI18N
    
    private TestcaseNode mTestcaseNode;
    
    private Action mFirstAct = new DiffAction("First", NbBundle.getMessage(DiffTopComponent.class, "LBL_First"), FIRST_ICON) { // NOI18N
        public void actionPerformed(ActionEvent e) {
            if (mDiffView == null || mDiffCount == 0) {
                return;
            }
            if (0 < mDiffNo) {
                mDiffNo = 0;
                mDiffView.setCurrentDifference(mDiffNo);
                refreshButtons();
            }
        }
    };
    private Action mLastAct = new DiffAction("Last", NbBundle.getMessage(DiffTopComponent.class, "LBL_Last"), LAST_ICON) { // NOI18N
        public void actionPerformed(ActionEvent e) {
            if (mDiffView == null || mDiffCount == 0) {
                return;
            }
            if (mDiffNo < mDiffCount - 1) {
                mDiffNo = mDiffCount - 1;
                mDiffView.setCurrentDifference(mDiffNo);
                refreshButtons();
            }
        }
        
    };
    private Action mNextAct = new DiffAction("Next", NbBundle.getMessage(DiffTopComponent.class, "LBL_Next"), NEXT_ICON) { // NOI18N
        public void actionPerformed(ActionEvent e) {
            if (mDiffView == null || mDiffCount == 0) {
                return;
            }
            int curNo = mDiffView.getCurrentDifference();
            if (mDiffNo < mDiffCount - 1) {
                mDiffNo++;
                mDiffView.setCurrentDifference(mDiffNo);
                refreshButtons();
            }
        }
    };
    private Action mPrevAct = new DiffAction("Previous", NbBundle.getMessage(DiffTopComponent.class, "LBL_Previous"), PREV_ICON) { // NOI18N
        public void actionPerformed(ActionEvent e) {
            if (mDiffView == null || mDiffCount == 0) {
                return;
            }
            if (0 < mDiffNo) {
                mDiffNo--;
                mDiffView.setCurrentDifference(mDiffNo);
                refreshButtons();
            }
        }
    };
    private Action mRefreshAct = new DiffAction("Refresh", NbBundle.getMessage(DiffTopComponent.class, "LBL_Refresh"), REFRESH_ICON) { // NOI18N
        public void actionPerformed(ActionEvent e) {
            refreshView(true);
        }
    };
    
    private JToolBar mToolBar;
    private JLabel mGoToLbl = new JLabel(NbBundle.getMessage(TestcaseNode.class, "LBL_Goto")); // NOI18N
    private JComboBox mDiffComboBox;
    private ActionListener mDiffActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            mDiffNo = mDiffComboBox.getSelectedIndex();
            mDiffView.setCurrentDifference(mDiffNo);
            refreshButtons();
        }
    };
    private int mDiffCount;
    private int mDiffNo;
    
    private JComboBox mActualComboBox;
    private ActionListener mActualActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            mActualFileName = (String)mActualComboBox.getSelectedItem();
            refreshView(true);
        }
    };
    private String mActualFileName;
    private DiffView mDiffView;
    
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(DiffTopComponent.class, "ACSN_DiffmToolBar")); // NOI18N
        toolBar.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(DiffTopComponent.class, "ACSD_DiffmToolBar")); // NOI18N
        toolBar.addSeparator();
        mActualComboBox =  new JComboBox();
        mActualComboBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                String actualFileName = (String)value;
                String timeStamp = TestCaseResultNode.getActualResultTimeStamp(actualFileName);
                return super.getListCellRendererComponent(list, timeStamp, index, isSelected, cellHasFocus);
            }
        });
        int h = (int)mActualComboBox.getMinimumSize().getHeight();
        mActualComboBox.setMinimumSize(new Dimension(60, h));
        mActualComboBox.addActionListener(mActualActionListener);
        mActualComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_ActualComboBox")); // NOI18N
        mActualComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_ActualComboBox")); // NOI18N        
        toolBar.add(mActualComboBox);
        toolBar.addSeparator();
        toolBar.add(mFirstAct);
        toolBar.add(mPrevAct);
        toolBar.add(mNextAct);
        toolBar.add(mLastAct);
        toolBar.addSeparator();
        toolBar.add(mGoToLbl);
        mDiffComboBox = new JComboBox();
        mDiffComboBox.setMinimumSize(new Dimension(30, h));
        mDiffComboBox.setMaximumSize(new Dimension(30, h));
        mDiffComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_DiffComboBox")); // NOI18N
        mDiffComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_DiffComboBox")); // NOI18N
        mDiffComboBox.addActionListener(mDiffActionListener);
        toolBar.add(mDiffComboBox);
        toolBar.addSeparator();
        toolBar.add(mRefreshAct);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.setBorderPainted(false);     
        
        return toolBar;
    }
    
    private void refreshButtons() {
        mFirstAct.setEnabled(mDiffNo > 0);
        mPrevAct.setEnabled(mDiffNo > 0);
        mNextAct.setEnabled(mDiffNo + 1 < mDiffCount);
        mLastAct.setEnabled(mDiffNo + 1 < mDiffCount);
        if (0 < mDiffCount) {
            mDiffComboBox.removeActionListener(mDiffActionListener);
            mDiffComboBox.setSelectedIndex(mDiffNo);
            mDiffComboBox.repaint();
            mDiffComboBox.addActionListener(mDiffActionListener);
        }
    }
    
    void refreshView(boolean needsRevalidate, boolean clearCurrentSelection) {
        if (clearCurrentSelection) {
            mActualFileName = null;
        }
        refreshView(needsRevalidate);
    }
    
    void refreshView(boolean needsRevalidate, String actualFileName) {
        mActualFileName = actualFileName;
        refreshView(needsRevalidate);
    }
    
    private void refreshView(boolean needsRevalidate) {
        try {
            removeAll();
            setLayout(new BorderLayout());
            mToolBar = createToolBar();
            //ActualComboBox
            List list = mTestcaseNode.getSortedResultFileNameList(true);
            int actualCount = list.size();
            mActualComboBox.removeActionListener(mActualActionListener);
            mActualComboBox.removeAllItems();
            for (int i = 0; i < actualCount; i++) {
                mActualComboBox.addItem(list.get(actualCount-i-1));
            }
            if (mActualFileName == null || !list.contains(mActualFileName)) {
                if (actualCount > 0) {
                    mActualFileName = (String)list.get(actualCount-1);
                } else  {
                    mActualFileName = null;
                }
            }
            
            if (mActualFileName != null) {
                // set selection before adding action listener
                mActualComboBox.setSelectedItem(mActualFileName);
                mActualComboBox.addActionListener(mActualActionListener);
                
                Diff diff = Diff.getDefault();
                org.netbeans.api.diff.StreamSource expected = mTestcaseNode.getExpectedStreamSource();
                org.netbeans.api.diff.StreamSource actual = mTestcaseNode.getActualStreamSource(mActualFileName);
                mDiffView = diff.createDiff(actual, expected);
                mDiffCount = mDiffView.getDifferenceCount();
                mDiffNo = 0;
                if (mDiffCount > 0) {
                    mDiffView.setCurrentDifference(mDiffNo);
                }
                mDiffComboBox.removeActionListener(mDiffActionListener);
                mDiffComboBox.removeAllItems();
                for (int i = 0; i < mDiffCount; i++) {
                    mDiffComboBox.addItem(new Integer(i));
                }
                mDiffComboBox.addActionListener(mDiffActionListener);
                add(mDiffView.getComponent(), BorderLayout.CENTER);
                add(mToolBar, BorderLayout.NORTH);   
                refreshButtons();            
                setFocusable(true);
            }
            
            if (needsRevalidate) {
                invalidate();
                validate();
            }
        } catch (Exception e) {
            mLogger.log(Level.SEVERE,
                    NbBundle.getMessage(TestcaseNode.class, "MSG_Fail_to_setup_diff_view", mTestcaseNode.getName()), // NOI18N
                    e);
        }
    }
    
    /** Creates a new instance of TestcaseDiffTopComponent */
    public DiffTopComponent(TestcaseNode testcaseNode) {
        mTestcaseNode = testcaseNode;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(TestcaseNode.class, "ACSN_Diff_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TestcaseNode.class, "ACSD_Diff_Top_Component")); // NOI18N
        refreshView(false);
        
        JbiProject jbiProject = mTestcaseNode.getProject();
        Set topComponentSet = (Set) jbiProject.getLookup().lookup(Set.class);
        if (topComponentSet != null) {
            topComponentSet.add(this);
        }
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(TestcaseNode.class, "LBL_Diff_Top_Component", mTestcaseNode.getName()); // NOI18N
    }
    
    public int getPersistenceType(){
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    protected void componentClosed() {
        super.componentClosed();
        mTestcaseNode.releaseDiffTopComponent();
        
        JbiProject jbiProject = mTestcaseNode.getProject();
        Set topComponentSet = (Set) jbiProject.getLookup().lookup(Set.class);
        if (topComponentSet != null && topComponentSet.contains(this)) {
            topComponentSet.remove(this);
        }
    }
    
    protected String preferredID(){
        return "TestcaseDiffTopComponent" + Math.random();    // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
     
    ////////////////////////////////////////////////////////////////////////////
    
    abstract class DiffAction extends AbstractAction {        
        /** Creates a new instance of DiffAction */
        public DiffAction(String name, String shortDescription, Icon icon) {
            super(name, icon);
            putValue(SHORT_DESCRIPTION, shortDescription);
        }        
    }    
}
