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


//import com.stc.egate.gui.common.view.SbynIViewManagerInternal;

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
public interface IJComponentControlComponent {
    /**
     * Description of the Method
     *
     * @param canvasNode   Description of the Parameter
     * @param viewManager  Description of the Parameter
     * @param canvas       Description of the Parameter
     */
    //void initialize(ICanvasNode canvasNode, SbynIViewManagerInternal viewManager, ICanvas canvas);
    void initialize(ICanvasNode canvasNode, ICanvas canvas);

    /**
     * Gets the uIObject attribute of the IJComponentControlComponent
     * object
     *
     * @return   The uIObject value
     */
    Object getUIObject();
}
