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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import threaddemo.apps.index.IndexApp;
import threaddemo.model.*;
import threaddemo.views.PhadhailViews;

// XXX parallel apps - some read only, some with write

/**
 * Demonstrate various models and views for big data sets.
 * @author Jesse Glick
 */
public final class Main extends JFrame {
    
    private static JFrame mainFrame;
    
    public static void main(String[] args) {
        File root;
        if (args.length == 1) {
            root = new File(args[0]);
        } else {
            root = File.listRoots()[0];
        }
        mainFrame = new Main(root);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLocation(0, 0);
        mainFrame.pack();
        mainFrame.show();
    }
    
    private final File root;
    private final JRadioButton synchButton, lockedButton, eventHybridLockedButton, spunButton, swungButton, nodeButton, lookNodeButton, lookButton, rawButton;
    
    private Main(File root) {
        super("Thread Demo [" + root.getAbsolutePath() + "]");
        this.root = root;
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel modelPanel = new JPanel();
        ButtonGroup modelGroup = new ButtonGroup();
        synchButton = new JRadioButton("Synchronous", false);
        lockedButton = new JRadioButton("Locked", false);
        eventHybridLockedButton = new JRadioButton("Event-Hybrid-Locked", true);
        spunButton = new JRadioButton("Spun", false);
        swungButton = new JRadioButton("Swung", false);
        modelGroup.add(synchButton);
        modelGroup.add(lockedButton);
        modelGroup.add(eventHybridLockedButton);
        modelGroup.add(spunButton);
        modelGroup.add(swungButton);
        modelPanel.add(synchButton);
        modelPanel.add(lockedButton);
        modelPanel.add(eventHybridLockedButton);
        modelPanel.add(spunButton);
        modelPanel.add(swungButton);
        getContentPane().add(modelPanel);
        JPanel viewPanel = new JPanel();
        ButtonGroup viewGroup = new ButtonGroup();
        nodeButton = new JRadioButton("Node", false);
        lookNodeButton = new JRadioButton("Look Node", true);
        lookButton = new JRadioButton("Look", false);
        rawButton = new JRadioButton("Raw", false);
        viewGroup.add(nodeButton);
        viewGroup.add(lookNodeButton);
        viewGroup.add(lookButton);
        viewGroup.add(rawButton);
        viewPanel.add(rawButton);
        viewPanel.add(nodeButton);
        viewPanel.add(lookNodeButton);
        viewPanel.add(lookButton);
        getContentPane().add(viewPanel);
        JButton showB = new JButton("Show");
        showB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                showView();
            }
        });
        JPanel bPanel = new JPanel();
        bPanel.add(showB);
        getContentPane().add(bPanel);
        getContentPane().add(new Monitor());
        getRootPane().setDefaultButton(showB);
    }
    
    private void showView() {
        // Clear caches first!
        System.gc();
        System.runFinalization();
        System.gc();
        final Phadhail model;
        final String modelType;
        if (synchButton.isSelected()) {
            model = Phadhails.synchronous(root);
            modelType = "Synchronous";
        } else if (lockedButton.isSelected()) {
            model = Phadhails.locked(root);
            modelType = "Locked";
        } else if (eventHybridLockedButton.isSelected()) {
            model = Phadhails.eventHybridLocked(root);
            modelType = "Event-Hybrid-Locked";
        } else if (spunButton.isSelected()) {
            model = Phadhails.spun(root);
            modelType = "Spun";
        } else {
            assert swungButton.isSelected();
            model = Phadhails.swung(root);
            modelType = "Swung";
        }
        Component view;
        final String viewType;
        if (nodeButton.isSelected()) {
            view = PhadhailViews.nodeView(model);
            viewType = "Node";
        } else if (lookNodeButton.isSelected()) {
            view = PhadhailViews.lookNodeView(model);
            viewType = "Look Node";
        } else if (lookButton.isSelected()) {
            view = PhadhailViews.lookView(model);
            viewType = "Look";
        } else {
            assert rawButton.isSelected();
            view = PhadhailViews.rawView(model);
            viewType = "Raw";
        }
        final JFrame frame = new JFrame();
        // For the benefit of Swung model which will produce the root path asynch:
        final PhadhailListener l = new PhadhailListener() {
            public void nameChanged(PhadhailNameEvent ev) {
                frame.setTitle(modelType + " " + viewType + ": " + model.getPath());
            }
            public void childrenChanged(PhadhailEvent ev) {}
        };
        l.nameChanged(null);
        model.addPhadhailListener(l);
        frame.getContentPane().add(view);
        JButton indexB = new JButton("View Index");
        indexB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                viewIndex(model);
            }
        });
        JPanel bPanel = new JPanel();
        bPanel.add(indexB);
        frame.getContentPane().add(bPanel, BorderLayout.SOUTH);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                model.removePhadhailListener(l);
                frame.removeWindowListener(this);
                // Just to make sure the view is collected:
                frame.getContentPane().removeAll();
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 500);
        frame.setLocation(mainFrame.getX() + mainFrame.getWidth(), 0);
        frame.show();
    }
    
    private void viewIndex(Phadhail model) {
        new IndexApp(model).setVisible(true);
    }
    
}
