package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import java.lang.reflect.InvocationTargetException;

public class Application_041 extends TestFrame {

    JTree tree;
    DefaultTreeModel model;
    DefaultMutableTreeNode root;    
    long time;
    public Application_041() {
	super("Application_041");

        JButton start = new JButton("Start");
        start.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    start();
                }
            });

	getContentPane().setLayout(new BorderLayout());
        getContentPane().add(start, BorderLayout.SOUTH);

        time = System.currentTimeMillis() % 100;
        if ((org.netbeans.jemmy.JemmyProperties.getCurrentDispatchingModel() & 
             org.netbeans.jemmy.JemmyProperties.ROBOT_MODEL_MASK) != 0) {
            time = time * 10;
        }
        getContentPane().add(new JLabel(Long.toString(time)), BorderLayout.NORTH);

        root = new DefaultMutableTreeNode("Root");

        model = new DefaultTreeModel(root);

        tree = new JTree(root);
        tree.setModel(model);

	getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

	setSize(300, 300);
    }

    private void start() {
        new Thread(new Runnable() {
                public void run() {
                    TreePath path = new TreePath(new Object[] {root});
                    for(int i = 0; i < 30; i++) {
                        final int index = i;
                        try {Thread.sleep(time * 2);} catch(Exception e) {}
                        if(!tree.isExpanded(path)) {
                            tree.expandPath(path);
                        }
                        try {
                            EventQueue.invokeAndWait(new Runnable() {
                                    public void run() {
                                        model.insertNodeInto(new DefaultMutableTreeNode("node" + index), root, 0);
                                    }
                                });
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        } catch(InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
    }

    public static void main(String[] argv) {
	(new Application_041()).show();
    }

}
