/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import threaddemo.model.Phadhail;
import threaddemo.model.Phadhails;
import threaddemo.views.PhadhailViews;

// XXX memory usage meter
// XXX AWT blockage meter

/**
 * Demonstrate various models and views for big data sets.
 * Classpath: openide.jar:naming.jar:looks.jar:Spin.jar
 * naming.jar is from core/naming; looks.jar from openidex/looks in branch looks_jul_2002_private_b;
 * Spin.jar is from spin.sf.net version 1.1.
 * @author Jesse Glick
 */
public final class Main extends JFrame {
    
    public static void main(String[] args) {
        File root;
        if (args.length == 1) {
            root = new File(args[0]);
        } else {
            root = File.listRoots()[0];
        }
        JFrame frame = new Main(root);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();
    }
    
    private final File root;
    private final JRadioButton synchButton, lockedButton, spunButton, nodeButton, lookNodeButton, lookButton, rawButton;
    
    private Main(File root) {
        this.root = root;
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel modelPanel = new JPanel();
        ButtonGroup modelGroup = new ButtonGroup();
        synchButton = new JRadioButton("Synchronous", true);
        lockedButton = new JRadioButton("Locked", false);
        spunButton = new JRadioButton("Spun", false);
        modelGroup.add(synchButton);
        modelGroup.add(lockedButton);
        modelGroup.add(spunButton);
        modelPanel.add(synchButton);
        modelPanel.add(lockedButton);
        modelPanel.add(spunButton);
        getContentPane().add(modelPanel);
        JPanel viewPanel = new JPanel();
        ButtonGroup viewGroup = new ButtonGroup();
        nodeButton = new JRadioButton("Node", false);
        lookNodeButton = new JRadioButton("Look Node", false);
        lookButton = new JRadioButton("Look", false);
        rawButton = new JRadioButton("Raw", true);
        viewGroup.add(nodeButton);
        viewGroup.add(lookNodeButton);
        viewGroup.add(lookButton);
        viewGroup.add(rawButton);
        viewPanel.add(rawButton);
        viewPanel.add(nodeButton);
        viewPanel.add(lookNodeButton);
        viewPanel.add(lookButton);
        getContentPane().add(viewPanel);
        JButton b = new JButton("Show");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                showView();
            }
        });
        getContentPane().add(b);
    }
    
    private void showView() {
        Phadhail model;
        if (synchButton.isSelected()) {
            model = Phadhails.synchronous(root);
        } else if (lockedButton.isSelected()) {
            model = Phadhails.locked(root);
        } else if (spunButton.isSelected()) {
            model = Phadhails.spun(root);
        } else {
            throw new IllegalStateException();
        }
        Component view;
        if (nodeButton.isSelected()) {
            view = PhadhailViews.nodeView(model);
        } else if (lookNodeButton.isSelected()) {
            view = PhadhailViews.lookNodeView(model);
        } else if (lookButton.isSelected()) {
            view = PhadhailViews.lookView(model);
        } else if (rawButton.isSelected()) {
            view = PhadhailViews.rawView(model);
        } else {
            throw new IllegalStateException();
        }
        JFrame frame = new JFrame(model.getPath());
        frame.getContentPane().add(view);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 500);
        // Clear caches first!
        System.gc();
        frame.show();
    }
    
}
