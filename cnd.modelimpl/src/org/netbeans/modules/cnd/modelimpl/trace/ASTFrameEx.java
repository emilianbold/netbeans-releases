/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import antlr.*;
import antlr.debug.misc.*;
import antlr.collections.*;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPParser;

/**
 * @author Vladimir Kvasihn
 */
public class ASTFrameEx extends JFrame {

    JTree tree;
    JTextArea text;

    class MyTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent event) {
            TreePath path = event.getPath();
//            System.out.println("Selected: " + path.getLastPathComponent());
//            Object elements[] = path.getPath();
//            for (int i = 0; i < elements.length; i++) {
//                System.out.print("->" + elements[i]);
//            }
            AST ast = (AST) path.getLastPathComponent();
            displayText("name:\t" + ast.getText()); // NOI18N
            appendText("\ntype:\t" + TraceUtils.getTokenTypeName(ast)); // NOI18N
            appendText("\npos:\t" + ast.getLine() + ':' + ast.getColumn()); // NOI18N
        }
    }

    
    private void displayText(String s) {
        text.setText(s);
    }

    private void appendText(String s) {
        text.setText(text.getText() + s);
    }
    
    public ASTFrameEx(String lab, AST r) {

        super(lab);

        JTreeASTModel model = new JTreeASTModel(r);
        tree = new JTree(model);
        tree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N

        TreeSelectionListener listener = new MyTreeSelectionListener();
        tree.addTreeSelectionListener(listener);

        JScrollPane treeScroller = new JScrollPane(tree);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        text = new JTextArea() {
            public Insets getInsets() {
                return new Insets(6, 6,  6,  6);
            }
        };
        text.setEditable(false);
        text.setTabSize(4);
        JScrollPane textScroller = new JScrollPane(text);
        
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitter.setLeftComponent(treeScroller);
        splitter.setRightComponent(textScroller);
        splitter.setDividerSize(2);
        splitter.setResizeWeight(0.6);
        
        content.add(splitter, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Frame f = (Frame)e.getSource();
                f.setVisible(false);
                f.dispose();
                // System.exit(0);
            }
        });
        setSize(320, 480);
    }
    
}
