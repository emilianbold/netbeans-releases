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
package org.netbeans.modules.visual.widget;

import org.netbeans.api.visual.widget.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;

/**
 * @author David Kaspar
 */
public final class SatelliteComponent extends JComponent implements MouseListener, MouseMotionListener, Scene.SceneListener, ComponentListener {

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
        JComponent viewComponent = scene.getView ();
        if (viewComponent == null)
            viewComponent = scene.createView ();
        viewComponent.addComponentListener (this);
        repaint ();
    }

    public void removeNotify () {
        scene.getView ().removeComponentListener (this);
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

        JComponent component = scene.getView ();
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
        JComponent component = scene.getView ();
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

    public void componentResized (ComponentEvent e) {
        repaint ();
    }

    public void componentMoved (ComponentEvent e) {
        repaint ();
    }

    public void componentShown (ComponentEvent e) {
    }

    public void componentHidden (ComponentEvent e) {
    }
}
