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


/**
 * <p>
 *
 * Title: </p> <p>
 *
 * Description: </p> <p>
 *
 * Copyright: Copyright (c) 2002</p> <p>
 *
 * Company: </p>
 *
 * @author    unascribed
 * @created   December 3, 2002
 * @version   1.0
 */

public interface ICanvasControlNode
     extends ICanvasNode {

    /**
     * Description of the Method
     *
     * @param swComponent  Description of the Parameter
     * @param initSize     Description of the Parameter
     */
    void initialize(IJComponentControlComponent swComponent
                    , Dimension initSize);

    /**
     * Gets the componentControlComponent attribute of the
     * ICanvasControlNode object
     *
     * @return   The componentControlComponent value
     */
    IJComponentControlComponent getComponentControlComponent();

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
