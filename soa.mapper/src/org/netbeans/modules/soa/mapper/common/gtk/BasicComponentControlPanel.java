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

package org.netbeans.modules.soa.mapper.common.gtk;

import javax.swing.JPanel;

/**
 * <p>
 *
 * Title: BasicComponentControlPanel</p> <p>
 *
 * Description: </p> <p>
 *
 * @author    Charles Zhu
 * @author    Henry Tung
 * @created   December 3, 2002
 */

public class BasicComponentControlPanel
     extends JPanel
     implements IJComponentControlComponent {

    /**
     * The original canvas node of the Component Controller
     */
    protected ICanvasNode mSourceNode;

    /**
     * The view manager
     */
    //protected SbynIViewManagerInternal mViewManager;

    /**
     * The canvas.
     *
     * @todo   need to change for multiple canvas's
     */
    protected ICanvas mCanvas;

    /**
     * Default constructor
     */
    public BasicComponentControlPanel() {
        super();

    }

    /**
     * Initializes the ComponentControl panel
     *
     * @param node - the canvas node
     * @param viewManager - the view manager
     * @param canvas  - the canvas
     */
    //public void initialize(ICanvasNode node, SbynIViewManagerInternal viewManager, ICanvas canvas) {
    public void initialize(ICanvasNode node, ICanvas canvas) {
        mSourceNode = node;
        //mViewManager = viewManager;
        mCanvas = canvas;
    }

    /**
     * set the visiblity of the panel and its associated node
     *
     * @param val - boolean true or false
     */
    public void setVisible(boolean val) {
        super.setVisible(val);
        if (mSourceNode.getControlNode() != null) {
            mSourceNode.getControlNode().setVisible(val);
        }
        if (mSourceNode.getComponentNode() != null) {
            mSourceNode.getComponentNode().setVisible(val);
        }
        mSourceNode.setVisible(!val);
    }

    /**
     * Gets the ui object
     *
     * @return   the ComponentControl panel
     */
    public Object getUIObject() {
        return this;
    }

}
