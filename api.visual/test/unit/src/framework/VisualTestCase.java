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

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.junit.NbTestCase;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Visual test case allows to create visual test - usually by creating a snapshot of a scene rendered into an image.
 * Then the image can been "cleaned" by a specific colors at particular regions.
 * Then the image can be tested if colors that remains there.
 *
 * @author David Kaspar
 */
public class VisualTestCase extends NbTestCase {

    /**
     * Creates a new visual test case.
     * @param testName the test name
     */
    public VisualTestCase(String testName) {
        super(testName);
    }

    /**
     * Shows a frame for a scene and wait until it shown on a screen. View size is [400,300].
     * @param scene the scene
     * @return the frame; null if the frame is not shown in 10 seconds
     */
    public JFrame showFrame (Scene scene) {
        return showFrame (scene, 400, 300);
    }

    /**
     * Shows a frame of for a scene and wait until it shown on a screen.
     * @param scene the scene
     * @param width the view width
     * @param height the view height
     * @return the frame; null if the frame is not shown in 10 seconds
     */
    public JFrame showFrame (Scene scene, int width, int height) {
        assert scene != null;

        JFrame frame = new JFrame ();
        frame.getContentPane().setLayout (new BorderLayout ());
        JComponent view = scene.createView ();
        view.setPreferredSize (new Dimension (width, height));
        frame.getContentPane().add(view, BorderLayout.CENTER);
        frame.pack ();
        frame.setVisible(true);
        int countdown = 10;
        for (;;) {
            if (frame.isShowing()  &&  scene.isValidated())
                break;
            if (-- countdown < 0) {
                frame.setVisible(false);
                frame.dispose ();
                return null;
            }
            try {
                Thread.sleep (1000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
        return frame;
    }

    /**
     * Takes a snapshot of a scene. This method assumes that the scene view is already shown on the screen
     * and therefore the scene is initialized correctly.
     * @param scene the scene
     * @return the snapshot
     */
    public BufferedImage takeSnapshot (Scene scene) {
        assert scene != null;
        Dimension viewSize = scene.getView().getSize();
        assert viewSize.width >= 0  &&  viewSize.height >= 0;
        return takeSnapshot (scene, viewSize.width, viewSize.height);
    }

    /**
     * Takes a snapshot of a scene. This method assumes that the scene view is already shown on the screen
     * and therefore the scene is initialized correctly.
     * @param scene the scene
     * @param width the snapshot width
     * @param height the snapshot height
     * @return the snapshot
     */
    public BufferedImage takeSnapshot (Scene scene, int width, int height) {
        assert scene != null;
        BufferedImage snapshot = new BufferedImage (width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D snapshotGraphics = snapshot.createGraphics();
        scene.paint(snapshotGraphics);
        snapshotGraphics.dispose ();
        return snapshot;
    }

    /**
     * Takes a one-time snapshot. Similar to takeSnapshot method but it automatically creates and disposes a frame for it.
     * @param scene the scene
     * @return the snapshot
     */
    public BufferedImage takeOneTimeSnapshot (Scene scene) {
        JFrame frame = showFrame (scene);
        BufferedImage snapshot = takeSnapshot (scene);
        assert scene != null;
        frame.setVisible(false);
        frame.dispose ();
        return snapshot;
    }

    /**
     * Takes a one-time snapshot. Similar to takeSnapshot method but it automatically creates and disposes a frame for it.
     * @param scene the scene
     * @param width the scene width
     * @param height the scene height
     * @return the snapshot
     */
    public BufferedImage takeOneTimeSnapshot (Scene scene, int width, int height) {
        JFrame frame = showFrame (scene, width, height);
        BufferedImage snapshot = takeSnapshot (scene, width, height);
        assert scene != null;
        frame.setVisible(false);
        frame.dispose ();
        return snapshot;
    }

    /**
     * Creates a new image which is a copy of a specified image and contains "clean" regions.
     * @param image the image to be copied
     * @param clearColor the color by which the regions are cleaned.
     * @param clearShapes the clean regions
     * @return the clean image
     */
    public BufferedImage clearRegions (BufferedImage image, Color clearColor, Shape... clearShapes) {
        BufferedImage clean = new BufferedImage (image.getWidth (), image.getHeight (), BufferedImage.TYPE_INT_RGB);
        Graphics2D cleanGraphics = clean.createGraphics();
        cleanGraphics.drawImage(image, 0, 0, null);
        cleanGraphics.setColor(clearColor);
        for (Shape shape : clearShapes)
            cleanGraphics.fill (shape);
        cleanGraphics.dispose();
        return clean;
    }

    /**
     * Checks whether an image contains only specified colors.
     * @param image the image
     * @param colors the colors
     * @return true if the image contains specified colors only; false otherwise
     */
    public boolean testCleaness (BufferedImage image, Color... colors) {
        int[] rgbs = new int[colors.length];
        for (int a = 0; a < colors.length; a ++)
            rgbs[a] = colors[a].getRGB ();
        int height = image.getHeight ();
        int width =  image.getWidth ();
        for (int y = 0; y < height; y ++)
            for (int x = 0; x < width; x ++) {
                boolean isClean = false;
                for (int rgb : rgbs)
                    if (image.getRGB(x, y) == rgb) {
                        isClean = true;
                        break;
                    }
                if (! isClean)
                    return false;
            }
        return true;
    }

    /**
     * Saves an image into the working directory of the test.
     * @param image the image to save
     * @param imageID the image id used as an image file name
     */
    public void saveImage (BufferedImage image, String imageID) {
        if (image == null)
            return;
        try {
            ImageIO.write (image, "png", new File (getWorkDir(), imageID + ".png")); // NOI18N
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    /**
     * Asserts cleaness and optionally saves snapshots.
     * @param isClean if true, nothing happens; if false, the assertion fails
     * @param snapshot the snapshot image
     * @param clean the clean snapshot image
     */
    public void assertCleaness (boolean isClean, BufferedImage snapshot, BufferedImage clean) {
        if (! isClean) {
            saveImage (snapshot, "snapshot");
            saveImage (clean, "clean-snapshot");
            assertTrue("The scene snapshot is not clean.", isClean);
        }
    }

    /**
     * Asserts a scene by taking its snapshots, cleaning specified regions with clear color and asserting that the clear color remains in the snapshot.
     * @param scene the scene
     * @param clearColor the clear color
     * @param clearShapes the clear regions
     */
    public void assertScene (Scene scene, Color clearColor, Shape... clearShapes) {
        BufferedImage snapshot = takeOneTimeSnapshot (scene);
        BufferedImage clean = clearRegions (snapshot, clearColor, clearShapes);
        assertCleaness (testCleaness (clean, clearColor), snapshot, clean);
    }

}
