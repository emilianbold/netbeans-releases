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

package org.netbeans.modules.soa.mapper.common.basicmapper.canvas;

import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperView;
import java.awt.Component;


/**
 * <p>
 *
 * Title: IMapperCanvasView </p> <p>
 *
 * Description: IMapperCanvasView provides the functionalities for a IMapperCanvas as
 * a mapper view. </p> <p>
 *
 * @author    Un Seng Leong
 * @created   December 31, 2002
 */

public interface IMapperCanvasView
     extends IBasicMapperView {

    /**
     * Return the mapper canvas view.
     *
     * @return   the mapper canvas view.
     */
    public ICanvasView getCanvas();

    /**
     * Return the visiual component of the canvas.
     *
     * @return the visiual component of the canvas.
     */
    public Component getCanvasComponent();
}
