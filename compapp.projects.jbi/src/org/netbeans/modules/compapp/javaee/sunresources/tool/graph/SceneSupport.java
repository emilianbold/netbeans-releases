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

package org.netbeans.modules.compapp.javaee.sunresources.tool.graph;

import org.netbeans.api.visual.widget.Scene;

import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.ApplicationArchive;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.Archive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.File;

/**
 * @author echou
 */
public class SceneSupport {

    public static void show (final Scene scene, int width, int height, 
            final Archive archive, final File targetArchive) {
        JComponent sceneView = scene.getView();
        if (sceneView == null)
            sceneView = scene.createView ();
        
        //int width=800,height=600;
        JFrame frame = new JFrame ();//new JDialog (), true);
        JButton saveButton = new JButton("Save"); // NOI18N
        saveButton.setPreferredSize(new Dimension(80, 20));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (archive.getJAXBHandler() != null) {
                        archive.getJAXBHandler().saveXML();
                    }
                    //archive.zipToFile(targetArchive);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
        });
        
        
        JPanel bar = new JPanel();
        JScrollPane panel2 = new JScrollPane (bar);
        bar.add(saveButton);
        
        addResourceButton(bar, archive);
        
        frame.add (panel2, BorderLayout.NORTH);
        
        JScrollPane panel = new JScrollPane (sceneView);
        panel.getHorizontalScrollBar ().setUnitIncrement (32);
        panel.getHorizontalScrollBar ().setBlockIncrement (256);
        panel.getVerticalScrollBar ().setUnitIncrement (32);
        panel.getVerticalScrollBar ().setBlockIncrement (256);
        frame.add (panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
        frame.setVisible (true);
    }

    /**
     * @param bar
     */
    private static void addResourceButton(JPanel bar, Archive archive) {
        if (archive instanceof ApplicationArchive) {
            final ApplicationArchive appArchive = (ApplicationArchive) archive;
            JButton resourceButton = new JButton("Generate Resource"); // NOI18N
            resourceButton.setPreferredSize(new Dimension(160, 20));
            resourceButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        //appArchive.getResourcesDD().saveXML();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } 
            });
            bar.add(resourceButton);
        }
    }

}
