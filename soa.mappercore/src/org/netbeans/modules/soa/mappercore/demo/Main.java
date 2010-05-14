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

package org.netbeans.modules.soa.mappercore.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.Mapper;

/**
 *
 * @author anjeleevich
 */
public class Main extends JFrame implements ActionListener {
    
    DemoMapperModel model;
    
    Mapper mapper;
    
    JButton remove = new JButton("Remove");
    JButton add = new JButton("Add Child");
    JTextField name = new JTextField(10);
    JButton expand = new JButton("Expand All NEG");
    JButton collapse = new JButton("Collapse All");
    JButton hide = new JButton("Hide Others");
    
    /** Creates a new instance of Main */
    public Main() {
        model = new DemoMapperModel();
        
        add.addActionListener(this);
        remove.addActionListener(this);
        expand.addActionListener(this);
        collapse.addActionListener(this);
        hide.addActionListener(this);
        
        JPanel toolBar = new JPanel();
        toolBar.setBorder(new LineBorder(Color.GRAY));
        toolBar.add(name);
        toolBar.add(add);
        toolBar.add(remove);
        toolBar.add(expand);
        toolBar.add(collapse);
        toolBar.add(hide);
        
        mapper = new Mapper(model);
        
        getContentPane().add(mapper);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("swing.aatext", "true");
        
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ex) {}
//        
        new Main().setVisible(true);
    }
    

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == add) {
            String name = this.name.getText().trim();
            if (name.equals("")) return;
            
            TreePath treePath = mapper.getSelectionModel().getSelectedPath();
            if (treePath == null) return;
                
            model.addChild(treePath, name);
        } else if (e.getSource() == remove) {
            TreePath treePath = mapper.getSelectionModel().getSelectedPath();
            if (treePath == null) return;
            if (treePath.getParentPath() == null) return;
            
            model.remove(treePath);
        } else if (e.getSource() == expand) {
            mapper.expandNonEmptyGraphs();
        } else if (e.getSource() == collapse) {
            mapper.collapseAll(2);
        } else if (e.getSource() == hide) {
            mapper.hideOtherPathes(2);
        }
    }
}
