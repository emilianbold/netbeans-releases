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

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComponent;

/**
 * @author    Henry Tung
 * @created   December 3, 2002
 */

public interface ICanvasComponentNode
     extends ICanvasNode {

    /**
     * Initialize the componentNode
     *
     * @param swComponent - the component
     * @param initSize - initial size
     */
    void initialize(JComponent swComponent, Dimension initSize);

    /**
     * Gets the jComponent attribute of the ICanvasComponentNode object
     *
     * @return   The jComponent value
     */
    JComponent getJComponent();

    /**
     * Description of the Method
     *
     * @param location  Description of the Parameter
     */
    void setupINPort(Point location);

    /**
     * Description of the Method
     *
     * @param location  Description of the Parameter
     */
    void setupOUTPort(Point location);

    /**
     * Description of the Method
     *
     * @param node        Description of the Parameter
     * @param location    Description of the Parameter
     * @param coord       Description of the Parameter
     * @param isLeftSide  Description of the Parameter
     */
    void createNewPort(ICanvasNode node, Point location,
        int coord, boolean isLeftSide);
}
