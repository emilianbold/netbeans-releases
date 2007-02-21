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

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasNodeLabel {

    /**
     * Sets the label
     *
     * @param text - the text
     */
    void updateText(String text);

    /**
     * get the label
     *
     * @return Stromg
     */
    String getText();

    /**
     * sets the ui
     *
     * @param ui - the ui object
     */
    void setUIComponent(Object ui);

    /**
     * Gets the uIComponent attribute of the ICanvasNodeLabel object
     *
     * @return   The uIComponent value
     */
    Object getUIComponent();

    /**
     * Adds a feature to the CanvasNodeLabelChangeListener attribute of
     * the ICanvasNodeLabel object
     *
     * @param listener  The feature to be added to the
     *      CanvasNodeLabelChangeListener attribute
     */
    void addCanvasNodeLabelChangeListener(
            ICanvasNodeLabelChangeListener listener);

    /**
     * Description of the Method
     *
     * @param listener  Description of the Parameter
     */
    void removeCanvasNodeLabelChangeListener(
            ICanvasNodeLabelChangeListener listener);
}
