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

package apichanges;

import framework.VisualTestCase;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.LayerWidget;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author David Kaspar
 */
public class OffscreenRenderingTest extends VisualTestCase {

    public OffscreenRenderingTest (String testName) {
        super (testName);
    }

    public void testOffscreenRendering () {
        Scene scene = new Scene ();

        LayerWidget layer = new LayerWidget (scene);
        layer.setPreferredBounds (new Rectangle (0, 0, 80, 80));
        scene.addChild (layer);

        LabelWidget widget = new LabelWidget (scene, "Hi");
        widget.setVerticalAlignment (LabelWidget.VerticalAlignment.CENTER);
        widget.setAlignment (LabelWidget.Alignment.CENTER);
        widget.setBorder (BorderFactory.createLineBorder ());
        widget.setPreferredLocation (new Point (20, 20));
        widget.setPreferredBounds (new Rectangle (0, 0, 40, 40));
        layer.addChild (widget);

        BufferedImage image = dumpSceneOffscreenRendering (scene);
        assertCleaness (testCleaness (image, Color.WHITE, Color.BLACK), image, null);

        assertScene (scene, Color.WHITE, new Rectangle (19, 19, 42, 42));
    }

    private BufferedImage dumpSceneOffscreenRendering (Scene scene) {
        // validate the scene with a off-screen graphics
        BufferedImage emptyImage = new BufferedImage (1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D emptyGraphics = emptyImage.createGraphics ();
        scene.validate (emptyGraphics);
        emptyGraphics.dispose ();

        // now the scene is calculated using the emptyGraphics, all widgets should be layout and scene has its size resolved
        // paint the scene with a off-screen graphics
        Rectangle viewBounds = scene.convertSceneToView (scene.getBounds ());
        BufferedImage image = new BufferedImage (viewBounds.width, viewBounds.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = image.createGraphics ();
        double zoomFactor = scene.getZoomFactor ();
        graphics.scale (zoomFactor, zoomFactor);
        scene.paint (graphics);
        graphics.dispose ();

        return image;
    }

}
