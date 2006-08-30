/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.widget;

import org.netbeans.api.visual.widget.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author David Kaspar
 */
public final class SatelliteComponent extends JComponent implements MouseListener, MouseMotionListener, Scene.SceneListener {

    private Scene scene;

    public SatelliteComponent (Scene scene) {
        this.scene = scene;
        setDoubleBuffered (true);
        setPreferredSize (new Dimension (128, 128));
        addMouseListener (this);
        addMouseMotionListener (this);
    }

    public void addNotify () {
        super.addNotify ();
        scene.addSceneListener (this);
        repaint ();
    }

    public void removeNotify () {
        scene.removeSceneListener (this);
        super.removeNotify ();
    }

    public void paint (Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        super.paint (g);
        Rectangle bounds = scene.getBounds ();
        Dimension size = getSize ();

        double sx = bounds.width > 0 ? (double) size.width / bounds.width : 0.0;
        double sy = bounds.width > 0 ? (double) size.height / bounds.height : 0.0;
        double scale = Math.min (sx, sy);

        int vw = (int) (scale * bounds.width);
        int vh = (int) (scale * bounds.height);
        int vx = (size.width - vw) / 2;
        int vy = (size.height - vh) / 2;

        AffineTransform previousTransform = gr.getTransform ();
        gr.translate (vx, vy);
        gr.scale (scale, scale);

        scene.paint (gr);
        gr.setTransform (previousTransform);

        JComponent component = scene.getComponent ();
        double zoomFactor = scene.getZoomFactor ();
        Rectangle viewRectangle = component != null ? component.getVisibleRect () : null;
        if (viewRectangle != null) {
            Rectangle window = new Rectangle (
                (int) ((double) viewRectangle.x * scale / zoomFactor),
                (int) ((double) viewRectangle.y * scale / zoomFactor),
                (int) ((double) viewRectangle.width * scale / zoomFactor),
                (int) ((double) viewRectangle.height * scale / zoomFactor)
            );
            window.translate (vx, vy);
//            Area area = new Area (new Rectangle (vx, vy, vw, vh));
//            area.subtract (new Area (window));
            gr.setColor (new Color (200, 200, 200, 128));
            gr.fill (window);
            gr.setColor (Color.BLACK);
            gr.drawRect (window.x, window.y, window.width - 1, window.height - 1);
        }
    }

    public void mouseClicked (MouseEvent e) {
    }

    public void mousePressed (MouseEvent e) {
        moveVisibleRect (e.getPoint ());
    }

    public void mouseReleased (MouseEvent e) {
        moveVisibleRect (e.getPoint ());
    }

    public void mouseEntered (MouseEvent e) {
    }

    public void mouseExited (MouseEvent e) {
    }

    public void mouseDragged (MouseEvent e) {
        moveVisibleRect (e.getPoint ());
    }

    public void mouseMoved (MouseEvent e) {
    }

    private void moveVisibleRect (Point center) {
        JComponent component = scene.getComponent ();
        if (component == null)
            return;
        double zoomFactor = scene.getZoomFactor ();
        Rectangle bounds = scene.getBounds ();
        Dimension size = getSize ();

        double sx = bounds.width > 0 ? (double) size.width / bounds.width : 0.0;
        double sy = bounds.width > 0 ? (double) size.height / bounds.height : 0.0;
        double scale = Math.min (sx, sy);

        int vw = (int) (scale * bounds.width);
        int vh = (int) (scale * bounds.height);
        int vx = (size.width - vw) / 2;
        int vy = (size.height - vh) / 2;

        int cx = (int) ((double) (center.x - vx) / scale * zoomFactor);
        int cy = (int) ((double) (center.y - vy) / scale * zoomFactor);

        Rectangle visibleRect = component.getVisibleRect ();
        visibleRect.x = cx - visibleRect.width / 2;
        visibleRect.y = cy - visibleRect.height / 2;
        component.scrollRectToVisible (visibleRect);
    }

    public void sceneRepaint () {
        repaint ();
    }

    public void sceneValidating () {
    }

    public void sceneValidated () {
    }
}
