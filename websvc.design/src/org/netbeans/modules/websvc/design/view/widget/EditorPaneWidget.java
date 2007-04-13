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


package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JEditorPane;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Ajit
 */
public class EditorPaneWidget extends Widget {
    
    private JEditorPane pane;
    private boolean componentAdded;
    private float origoinalFontSize = 0;
    private ComponentSceneListener validateListener;
    private ComponentComponentListener componentListener;
    
    /** Creates a new instance of EditorPaneWidget 
     * @param scene 
     * @param text 
     */
    public EditorPaneWidget(Scene scene, String text) {
        super(scene);
        pane = new JEditorPane("text/java",text);
        pane.setVisible(false);
        componentAdded = false;
        origoinalFontSize = pane.getFont().getSize2D();
        componentListener = new ComponentComponentListener ();
    }

    public String getText() {
        return pane.getText();
    }

    protected void notifyAdded() {
        pane.setVisible(true);
        if(validateListener==null) {
            validateListener = new ComponentSceneListener ();
            getScene ().addSceneListener (validateListener);
        }
    }

    protected void notifyRemoved() {
        pane.setVisible(false);
        if(validateListener==null) {
            getScene ().removeSceneListener (validateListener);
            validateListener = null;
        }
    }

    /**
     * Calculates a client area from the preferred size of the component.
     * @return the calculated client area
     */
    protected final Rectangle calculateClientArea () {
        return new Rectangle (pane.getPreferredSize ());
    }

    /**
     * Paints the component widget.
     */
    protected final void paintWidget () {
        if(!componentAdded) {
            getScene().getView().add(pane);
            componentAdded = true;
        }
        pane.setBounds (getScene().convertSceneToView (convertLocalToScene (getClientArea())));
        pane.setFont(pane.getFont().deriveFont((float)getScene().getZoomFactor()*origoinalFontSize));
        pane.repaint();
    }

    private final class ComponentSceneListener implements Scene.SceneListener {

        public void sceneRepaint () {
        }

        public void sceneValidating () {
            if(componentAdded) {
                getScene().getView().remove(pane);
                componentAdded = false;
                pane.removeComponentListener (componentListener);
            }
        }

        public void sceneValidated () {
            if(componentAdded) {
                pane.addComponentListener (componentListener);
            }
        }
    }

    private final class ComponentComponentListener implements ComponentListener {

        public void componentResized (ComponentEvent e) {
            revalidate ();
        }

        public void componentMoved (ComponentEvent e) {
            revalidate ();
        }

        public void componentShown (ComponentEvent e) {
        }

        public void componentHidden (ComponentEvent e) {
        }

    }
}
