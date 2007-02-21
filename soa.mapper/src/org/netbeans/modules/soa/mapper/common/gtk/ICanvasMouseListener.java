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

public interface ICanvasMouseListener {

    /**
     * Handles mouse press
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doMouseDown(ICanvasMouseData data);

    /**
     * Handles  mouse release
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doMouseUp(ICanvasMouseData data);

    /**
     * Handls default mouse move
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doMouseMove(ICanvasMouseData data);

    /**
     * Handls default mouse click
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doMouseClick(ICanvasMouseData data);

    /**
     * Handles default mouse double click
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doMouseDblClick(ICanvasMouseData data);
}