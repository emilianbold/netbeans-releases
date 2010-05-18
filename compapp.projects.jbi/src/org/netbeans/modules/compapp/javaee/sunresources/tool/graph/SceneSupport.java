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
