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
package framework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.junit.NbTestCase;

/**
 * @author David Kaspar
 */
public class VisualTestCase extends NbTestCase {
    
    public VisualTestCase(String testName) {
        super(testName);
    }
    
    public void assertScene (Scene scene, Color clearColor, Shape... clearShapes) {
        assert scene != null  &&  clearColor != null;
        // show frame
        JFrame frame = new JFrame ();
        frame.getContentPane().setLayout (new BorderLayout ());
        frame.getContentPane().add(scene.createView(), BorderLayout.CENTER);
        frame.setSize(400, 300);
        frame.setVisible(true);
        int countdown = 10;
        for (;;) {
            if (frame.isShowing()  &&  scene.isValidated())
                break;
            if (-- countdown < 0) {
                frame.setVisible(false);
                return;
            }
            try {
                Thread.sleep (1000);
            } catch (InterruptedException e) {
            }
        }
        
        // take a snapshot
        Dimension viewSize = scene.getView().getSize();
        assert viewSize.width >= 0  &&  viewSize.height >= 0;
        BufferedImage snapshot = new BufferedImage (viewSize.width, viewSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D snapshotGraphics = snapshot.createGraphics();
        scene.paint(snapshotGraphics);
        snapshotGraphics.dispose ();
        
        // clear regions
        BufferedImage clean = new BufferedImage (viewSize.width, viewSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D cleanGraphics = clean.createGraphics();
        cleanGraphics.drawImage(snapshot, 0, 0, null);
        cleanGraphics.setColor(clearColor);
        for (Shape shape : clearShapes)
            cleanGraphics.fill (shape);
        cleanGraphics.dispose();
        
        // test cleanness
        boolean isClean = true;
        int cleanRGB = clearColor.getRGB();
        for (int y = 0; y < viewSize.height; y ++)
            for (int x = 0; x < viewSize.width; x ++)
                if (clean.getRGB(x, y) != cleanRGB)
                    isClean = false;
        
        // hide frame
        frame.setVisible(false);
        
        if (! isClean) {
            try {
                ImageIO.write (snapshot, "png", new File (getWorkDir(), "snapshot.png")); // NOI18N
                ImageIO.write (clean, "png", new File (getWorkDir(), "clean-snapshot.png")); // NOI18N
            } catch (IOException e) {
            }
            assertTrue("The scene snapshot is not clean.", isClean);
        }
    }

}
