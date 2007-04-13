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

package org.netbeans.modules.tasklist.ui.checklist;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;

/**
 * List with checkboxes.
 */
public class CheckList extends JList {

    private static final long serialVersionUID = 1;

    /**
     * Constructs a <code>CheckList</code> that displays the elements in the
     * specified, non-<code>null</code> model. 
     * All <code>CheckList</code> constructors delegate to this one.
     *
     * @param dataModel   the data model for this list
     * @exception IllegalArgumentException   if <code>dataModel</code>
     *						is <code>null</code>
     */    
    public CheckList(CheckListModel dataModel) {
        super(dataModel);
        setCellRenderer(new DefaultCheckListCellRenderer());
        Action action = new CheckAction();
        getActionMap().put("check", action); //NOI18N
        registerKeyboardAction(action, KeyStroke.getKeyStroke(' '), 
            JComponent.WHEN_FOCUSED);
        addMouseListener(
            new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JList list = (JList) e.getComponent();
                    
                    int index = list.locationToIndex(e.getPoint());
                    if (index < 0)
                        return;

                    if (e.getX() > 15)
                        return;

                    CheckListModel model = (CheckListModel) getModel();
                    model.setChecked(index, !model.isChecked(index));
                    
                    e.consume();
                    repaint();
                }
            }
        );
    }

    /**
     * Constructs a <code>JList</code> that displays the elements in
     * the specified array.  This constructor just delegates to the
     * <code>ListModel</code> constructor.
     * 
     * @param state state of the checkboxes
     * @param  listData  the array of Objects to be loaded into the data model
     */
    public CheckList(boolean[] state, Object[] listData) {
        this(new DefaultCheckListModel(state, listData));
    }

    /**
     * Constructs a <code>CheckList</code> with an empty model.
     */
    public CheckList() {
        this(new AbstractCheckListModel() {
            public boolean isChecked(int index) {
                return false;
            }
            public void setChecked(int index, boolean c) {
            }
            public int getSize() {
                return 0;
            }
            public Object getElementAt(int index) {
                return null;
            }
        });
    }
    
    /**
     * Check/uncheck currently selected item
     */
    public static class CheckAction extends AbstractAction {

        private static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent e) {
	    JList list = (JList) e.getSource();
            int index = list.getSelectedIndex();
            if (index < 0)
                return;
            CheckListModel model = (CheckListModel) list.getModel();
            model.setChecked(index, !model.isChecked(index));
        }
    }
    
    /**
     * Sets new model
     *
     * @param m new model != null
     */
    public void setModel(CheckListModel m) {
        super.setModel(m);
    }
}
