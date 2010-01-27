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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CheckListSupport extends MouseAdapter implements ListSelectionListener, ActionListener {
    
    private ListSelectionModel sModel = new DefaultListSelectionModel();

    private JList list = new JList();
    
    private int checkBoxWidth = new JCheckBox().getPreferredSize().width;

    public CheckListSupport(JList list){
        this.list = list;
        list.setCellRenderer(new KenaiCheckListRenderer(list.getCellRenderer(), sModel));
        list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
        list.addMouseListener(this);
        sModel.addListSelectionListener(this);
    }

    public ListSelectionModel getSelectionModel(){
        return sModel;
    }

    private void select(int index){
        if(index<0)
            return;

        if(sModel.isSelectedIndex(index))
            sModel.removeSelectionInterval(index, index);
        else
            sModel.addSelectionInterval(index, index);
    }

    @Override
    public void mouseClicked(MouseEvent me){
        int index = list.locationToIndex(me.getPoint());
        if(index<0)
            return;
        if(me.getX()>list.getCellBounds(index, index).x+checkBoxWidth)
            return;
        select(index);
    }

    @Override
    public void valueChanged(ListSelectionEvent e){
        list.repaint(list.getCellBounds(e.getFirstIndex(), e.getLastIndex()));
    }

    @Override
    public void actionPerformed(ActionEvent e){
        select(list.getSelectedIndex());
    }
}