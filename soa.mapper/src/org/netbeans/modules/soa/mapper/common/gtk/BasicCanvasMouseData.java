/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Point;


/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public class BasicCanvasMouseData implements ICanvasMouseData {

    /**
     * Description of the Field
     */
    protected ICanvas mCanvas;
    /**
     * Description of the Field
     */
    protected Point mModelLocation;
    /**
     * Description of the Field
     */
    protected Point mViewLocation;
    /**
     * Description of the Field
     */
    protected int mModifier;

    /**
     * Constructor for the BasicCanvasMouseData object
     *
     * @param canvas    Description of the Parameter
     * @param modifier  Description of the Parameter
     * @param modelLoc  Description of the Parameter
     * @param viewLoc   Description of the Parameter
     */
    public BasicCanvasMouseData(ICanvas canvas, int modifier, Point modelLoc
                                , Point viewLoc) {
        mCanvas = canvas;
        mModelLocation = modelLoc;
        mViewLocation = viewLoc;
        mModifier = modifier;
    }

    /**
     * Gets the canvas attribute of the BasicCanvasMouseData object
     *
     * @return   The canvas value
     */
    public ICanvas getCanvas() {
        return mCanvas;
    }

    /**
     * Gets the modelLocation attribute of the BasicCanvasMouseData
     * object
     *
     * @return   The modelLocation value
     */
    public Point getModelLocation() {
        return mModelLocation;
    }

    /**
     * Gets the viewLocation attribute of the BasicCanvasMouseData
     * object
     *
     * @return   The viewLocation value
     */
    public Point getViewLocation() {
        return mViewLocation;
    }

    /**
     * Gets the mouseModifier attribute of the BasicCanvasMouseData
     * object
     *
     * @return   The mouseModifier value
     */
    public int getMouseModifier() {
        return mModifier;
    }
}
