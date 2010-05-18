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
package org.netbeans.modules.edm.editor.ui.view.join;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.logging.Logger;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.SQLDBTable;

import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.utils.UIUtil;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * This is the main join dialog which provides a view to define join between various
 * tables.
 * 
 * @author Ritesh Adval
 */
public class JoinMainDialog extends JDialog {

    public static final int OK_BUTTON = 0;
    public static final int CANCEL_BUTTON = 1;
    private static JoinMainPanel joinMainPanel;
    private JButton okButton;
    private JButton cancelButton;
    private static JoinMainDialog dlg;
    private static int buttonState = OK_BUTTON;
    private static IGraphView graphView;
    private static transient final Logger mLogger = Logger.getLogger(JoinMainDialog.class.getName());

    /** Creates a new instance of JoinMainDialog */
    public JoinMainDialog() {
        this(WindowManager.getDefault().getMainWindow());
        this.setPreferredSize(new Dimension(780, 580));
    }

    public JoinMainDialog(Frame parent) {
        super(parent);
        this.setPreferredSize(new Dimension(780, 580));
        initGUI();
    }

    private void initGUI() {
        // initialize layout for dialog content panel
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        // initialize join main panel
        joinMainPanel = new JoinMainPanel(graphView);
        joinMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(joinMainPanel, BorderLayout.CENTER);

        ListTransferPanel listPanel = joinMainPanel.getListTransferPanel();
        // add a listener to listen for updates in list model so that
        // preview panel can be refreshed.
        listPanel.getDestinationJList().getModel().addListDataListener(new TargetListDataListener());

        // initialize the bottom button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JSeparator(), BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        okButton = new JButton(NbBundle.getMessage(JoinMainDialog.class, "OK_BUTTON_LABEL"));
        okButton.getAccessibleContext().setAccessibleName("Ok");
        okButton.getAccessibleContext().setAccessibleDescription("Ok");
        okButton.setMnemonic('O');
        cancelButton = new JButton(NbBundle.getMessage(JoinMainDialog.class, "CANCEL_BUTTON_LABEL"));
        cancelButton.setMnemonic('C');
        cancelButton.getAccessibleContext().setAccessibleName("Cancel");
        cancelButton.getAccessibleContext().setAccessibleDescription("Cancel");
        int maxBWidth = okButton.getPreferredSize().width;
        if (cancelButton.getPreferredSize().width > maxBWidth) {
            maxBWidth = cancelButton.getPreferredSize().width;
        }

        okButton.setPreferredSize(new Dimension(maxBWidth, okButton.getPreferredSize().height));
        cancelButton.setPreferredSize(new Dimension(maxBWidth, cancelButton.getPreferredSize().height));

        okButton.setEnabled(false);

        // add action listener to button
        ButtonActionListener listener = new ButtonActionListener();
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        // add all buttons to button panel
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JRootPane rp = this.getRootPane();
        UIUtil.addEscapeListener(rp, listener);

        this.addWindowListener(new JoinWindowAdapter());
    }

    public void reset() {
    }

    public static void showJoinDialog(Collection<DBTable> sList, Collection<DBTable> tList, IGraphView view, boolean enableButton) {
        graphView = view;
        if (dlg == null) {
            dlg = new JoinMainDialog();
        }
        dlg.setTitle(NbBundle.getMessage(JoinMainDialog.class, "CREATE_JOIN_LABEL"));
        dlg.getAccessibleContext().setAccessibleDescription("Create New Join View");
        joinMainPanel.reset(view);

        joinMainPanel.setSourceList(sList);
        joinMainPanel.setTargetList(tList);
        joinMainPanel.getListTransferPanel().enableButton(enableButton);
        makeDialogVisible();
    }

    public static void showJoinDialog(Collection sList, SQLJoinView jView, IGraphView view) {
        graphView = view;
        if (dlg == null) {
            dlg = new JoinMainDialog();
        }
        dlg.setTitle(NbBundle.getMessage(JoinMainDialog.class, "EDIT_JOIN_LABEL") + " (" + jView.getAliasName() + ")");

        joinMainPanel.reset(view);
        joinMainPanel.setSourceList(sList);
        joinMainPanel.setSQLJoinView(jView);

        makeDialogVisible();
    }

    public static void showJoinDialog(Collection sList, SourceTable sTable, SQLJoinView jView, IGraphView view) {
        graphView = view;
        if (dlg == null) {
            dlg = new JoinMainDialog();
        }
        dlg.setTitle(NbBundle.getMessage(JoinMainDialog.class, "EDIT_JOIN_LABEL") + " (" + jView.getAliasName() + ")");

        joinMainPanel.reset(view);
        joinMainPanel.setSourceList(sList);
        joinMainPanel.setEditSQLJoinView(sTable, jView);

        makeDialogVisible();
    }

    private static void makeDialogVisible() {
        Frame f = WindowManager.getDefault().getMainWindow();

        dlg.setModal(true);
        int width = (f.getWidth() * 4) / 5;
        int height = (f.getHeight() * 4) / 5;
        dlg.setSize(width, height);
        // call to pack resize dialog to preffered size
        joinMainPanel.setPreferredSize(new Dimension(width, height));

        int x = (f.getWidth() - dlg.getWidth()) / 2;
        int y = (f.getHeight() - dlg.getHeight()) / 2;

        dlg.setLocation(x, y);
        dlg.pack();
        joinMainPanel.setDividerLocation();
        dlg.setVisible(true);
        dlg.setSize(400, 400);
        dlg.setPreferredSize(new Dimension(400, 400));
    }

    public static void showJoinDialog() {
        if (dlg == null) {
            dlg = new JoinMainDialog();
        }

        dlg.setModal(true);
        dlg.setPreferredSize(new Dimension(400, 400));
        dlg.pack();
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        showJoinDialog();
    }

    class ButtonActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source.equals(okButton)) {
                buttonState = OK_BUTTON;
                JoinMainDialog.this.setVisible(false);
            } else {
                // Treat Escape button also as Cancel button
                buttonState = CANCEL_BUTTON;
                handleCancel();
                JoinMainDialog.this.setVisible(false);
            }
        }
    }

    private void handleCancel() {
        joinMainPanel.handleCancel();
    }

    class JoinWindowAdapter extends WindowAdapter {

        /**
         * Invoked when a window has been closed.
         */
        @Override
        public void windowClosing(WindowEvent e) {
            buttonState = CANCEL_BUTTON;
            handleCancel();
            JoinMainDialog.this.setVisible(false);
        }
    }

    public static SQLJoinView getSQLJoinView() {
        return joinMainPanel.getSQLJoinView();
    }

    public static List getTableColumnNodes() {
        return joinMainPanel.getTableColumnNodes();
    }

    public static int getClosingButtonState() {
        return buttonState;
    }

    private void changeButtonState(ListDataEvent e) {
        ListModel model = (ListModel) e.getSource();
        if (model.getSize() > 1) {
            this.okButton.setEnabled(true);
        } else {
            this.okButton.setEnabled(false);
        }
    }

    class TargetListDataListener implements ListDataListener {

        /**
         * Sent when the contents of the list has changed in a way that's too complex to
         * characterize with the previous methods. For example, this is sent when an item
         * has been replaced. Index0 and index1 bracket the change.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void contentsChanged(ListDataEvent e) {
            changeButtonState(e);
        }

        /**
         * Sent after the indices in the index0,index1 interval have been inserted in the
         * data model. The new interval includes both index0 and index1.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void intervalAdded(ListDataEvent e) {
            changeButtonState(e);
        }

        /**
         * Sent after the indices in the index0,index1 interval have been removed from the
         * data model. The interval includes both index0 and index1.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void intervalRemoved(ListDataEvent e) {
            changeButtonState(e);
        }
    }
}

