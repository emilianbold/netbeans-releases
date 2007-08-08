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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Ajit
 */
public class EditorPaneWidget extends Widget {
    
    private JEditorPane editorPane;
    private JScrollPane scrollPane;
    private boolean componentAdded;
    private float origoinalFontSize = 0;
    private ComponentSceneListener validateListener;
    private ComponentComponentListener componentListener;
    
    /** Creates a new instance of EditorPaneWidget 
     * @param scene 
     * @param text 
     * @param contentType 
     */
    public EditorPaneWidget(Scene scene, String text, String contentType) {
        super(scene);
        editorPane = new JEditorPane(contentType,text);
        scrollPane = new JScrollPane(editorPane);
        editorPane.setVisible(false);
        scrollPane.setVisible(false);
        componentAdded = false;
        origoinalFontSize = editorPane.getFont().getSize2D();
        editorPane.setPreferredSize(new Dimension(0,(int)origoinalFontSize*6));
        componentListener = new ComponentComponentListener ();
    }

    /**
     * 
     * @param flag 
     */
    public void setEditable(boolean flag) {
        editorPane.setEditable(flag);
    }

    public boolean isEditable() {
        return editorPane.isEditable();
    }

    public String getText() {
        return editorPane.getText();
    }

    protected void notifyAdded() {
        editorPane.setVisible(true);
        scrollPane.setVisible(true);
        if(validateListener==null) {
            validateListener = new ComponentSceneListener ();
            getScene ().addSceneListener (validateListener);
        }
    }

    protected void notifyRemoved() {
        editorPane.setVisible(false);
        scrollPane.setVisible(false);
        if(validateListener!=null) {
            getScene ().removeSceneListener (validateListener);
            validateListener = null;
        }
    }

    /**
     * Calculates a client area from the preferred size of the component.
     * @return the calculated client area
     */
    protected final Rectangle calculateClientArea () {
        return new Rectangle (editorPane.getPreferredSize ());
    }

    /**
     * Paints the component widget.
     */
    protected final void paintWidget () {
        if(!componentAdded) {
            getScene().getView().add(scrollPane);
            componentAdded = true;
        }
        scrollPane.setBounds (getScene().convertSceneToView (convertLocalToScene (getClientArea())));
        editorPane.setFont(editorPane.getFont().deriveFont((float)getScene().getZoomFactor()*origoinalFontSize));
        editorPane.repaint();
    }

    private final class ComponentSceneListener implements Scene.SceneListener {

        public void sceneRepaint () {
        }

        public void sceneValidating () {
            if(componentAdded) {
                getScene().getView().remove(scrollPane);
                componentAdded = false;
                scrollPane.removeComponentListener (componentListener);
            }
        }

        public void sceneValidated () {
            if(componentAdded) {
                scrollPane.addComponentListener (componentListener);
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
