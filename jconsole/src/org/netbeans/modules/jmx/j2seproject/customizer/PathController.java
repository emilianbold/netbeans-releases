/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.jmx.j2seproject.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author jfdenise
 */
public class PathController implements ActionListener, ListSelectionListener, ListDataListener {
    private JList l;
    private JButton add;
    private JButton remove;
    private JButton up;
    private JButton down;
    private JFileChooser chooser;
    private DefaultListModel model;
    private JLabel label;
    private ListDataListener lstnr;
    public PathController(JList l, JLabel label, JButton add, JFileChooser chooser, JButton remove, JButton up, JButton down, ListDataListener lstnr) {
        this(l,label, createModel(""),add,chooser,remove,up,down, lstnr);
    }
    public PathController(JList l, JLabel label, String items, JButton add, JFileChooser chooser, JButton remove, JButton up, JButton down, ListDataListener lstnr) {
        this(l,label,createModel(items),add,chooser,remove,up,down, lstnr);
    }

    /** Creates a new instance of PathController */
    public PathController(JList l, JLabel label, DefaultListModel model, JButton add, JFileChooser chooser, JButton remove, JButton up, JButton down, ListDataListener lstnr) {
        this.l = l;
        this.label = label;
        this.model = model;
        this.add = add;
        this.remove = remove;
        this.up = up;
        this.down = down;
        this.chooser = chooser;
        
        this.lstnr = lstnr;
        
        l.setModel(model);
        if(model != null) {
            model.addListDataListener(this);
        }
        add.setActionCommand("add");// NOI18N
        remove.setActionCommand("remove");// NOI18N
        up.setActionCommand("up");// NOI18N
        down.setActionCommand("down");// NOI18N
        add.addActionListener(this);
        remove.addActionListener(this);
        up.addActionListener(this);
        down.addActionListener(this);
        l.addListSelectionListener(this);
        
        remove.setEnabled(false);
        up.setEnabled(false);
        down.setEnabled(false);
    }
    
    public void setEnabled(boolean b) {
        l.setEnabled(b);
        label.setEnabled(b);
        add.setEnabled(b);
        remove.setEnabled(remove.isEnabled() && b);
        up.setEnabled(up.isEnabled() && b);
        down.setEnabled(down.isEnabled() && b);
    }
    public void setVisible(boolean b) {
        l.setVisible(b);
        label.setVisible(b);
        add.setVisible(b);
        remove.setVisible(b);
        up.setVisible(b);
        down.setVisible(b);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals( "add")) {// NOI18N
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File[] selection = chooser.getSelectedFiles();
                int size = selection.length;
                StringBuffer buff = new StringBuffer();
                int end = l.getModel().getSize();
                for(int i = 0; i < size; i++) {
                    String path = selection[i].getAbsolutePath();
                    if(!model.contains(path))
                        model.add(end + i, path);
                }
            }
            return;
        }
        if (e.getActionCommand().equals( "remove")) {// NOI18N
            Object[] values = l.getSelectedValues();
            
            for(int i = 0; i < values.length; i++) {
                model.removeElement(values[i]);
            }
            if(model.getSize() == 0) {
                up.setEnabled(false);
                down.setEnabled(false);
                remove.setEnabled(false);
            }
            l.setSelectedIndex(0);
        }
        
        if (e.getActionCommand().equals("up")) {// NOI18N
            int selectedI = l.getSelectedIndex();
            Object selected = l.getSelectedValue();
            int newIndex = selectedI - 1;
            Object previous = model.getElementAt(newIndex);
            model.setElementAt(selected, newIndex);
            model.setElementAt(previous, selectedI);
            l.setSelectedIndex(newIndex);
            return;
        }
        
        if (e.getActionCommand().equals("down")) {// NOI18N
            int selectedI = l.getSelectedIndex();
            Object selected = l.getSelectedValue();
            int newIndex = selectedI + 1;
            Object next = model.getElementAt(newIndex);
            model.setElementAt(selected, newIndex);
            model.setElementAt(next, selectedI);
            l.setSelectedIndex(newIndex);
            return;
        }
    }
    
    // return the list of selected items
    public String toString() {
        Enumeration pluginsPath = model.elements();
        StringBuffer buffer = new StringBuffer();
        while(pluginsPath.hasMoreElements()) {
            Object path = pluginsPath.nextElement();
            buffer.append(path.toString());
            if(pluginsPath.hasMoreElements())
                buffer.append(File.pathSeparator);
        }
        return buffer.toString();
    }
    
    public synchronized void updateModel(String items) {
        if(items == null)
            return;
        ListModel m = l.getModel();
        if(m != null) {
            m.removeListDataListener(this);
        }
        
        model = createModel(items);
        model.addListDataListener(this);
        l.setModel(model);
    }
    
    public static DefaultListModel createModel(String items) {
        StringTokenizer tk = new StringTokenizer(items,File.pathSeparator);
        DefaultListModel model = new DefaultListModel();
        while(tk.hasMoreTokens()) {
            String path = tk.nextToken();
            model.addElement(path);
        }
        return model;
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int[] indices = l.getSelectedIndices();
        if(indices.length != 1) {
            up.setEnabled(false);
            down.setEnabled(false);
            return;
        }
        int single = l.getSelectedIndex();
        up.setEnabled(true);
        down.setEnabled(true);
        
        if(model.getSize() > 0)
            remove.setEnabled(true);
        
        if(single == 0) {
            up.setEnabled(false);
            if(model.getSize() == 1)
                down.setEnabled(false);
        }
        
        if(single == model.getSize() -1)
            down.setEnabled(false);
    }
    
    public void intervalAdded(ListDataEvent arg0) {
        if(lstnr == null)
            return;
        lstnr.intervalAdded(arg0);
    }
    
    public void intervalRemoved(ListDataEvent arg0) {
        if(lstnr == null)
            return;
        lstnr.intervalRemoved(arg0);
    }
    
    public void contentsChanged(ListDataEvent arg0) {
        if(lstnr == null)
            return;
        lstnr.contentsChanged(arg0);
    }
    
}
