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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import java.awt.Point;

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.soa.mapper.common.gtk.ICanvas;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseData;

/**
 * <p>
 *
 * Title: </p> BasicCanvasMouseData<p>
 *
 * Description: </p> BasicCanvasMouseData provides basic implemeation of
 * ICanvasMouseData. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    unascribed
 * @created   December 4, 2002
 * @version   1.0
 */
public class BasicCanvasMouseData
     implements ICanvasMouseData {
    /**
     * The position of the canvas object
     */
    public static final int CANVAS = 0;

    /**
     * The position of the modifier
     */
    public static final int MODIFIER = 1;

    /**
     * The position of the document location object(Point)
     */
    public static final int DOCUMENT_LOCATION = 2;

    /**
     * The position of the view location object(Point)
     */
    public static final int VIEW_LOCATION = 3;

    /**
     * Mouse modifier
     */
    protected int mModifier = 0;

    /**
     * The document location
     */
    protected Point mDocLocation = null;

    /**
     * The view location
     */
    protected Point mViewLocation = null;

    /**
     * Event data collection
     */
    protected ArrayList mEventDataCollection = new ArrayList();

    /**
     * The canvas
     */
    protected BasicCanvasView mCanvas;

    /**
     * Instaniate an BasicCanvasMouseData with all the configuration data.
     *
     * @param canvas        - the canvas
     * @param modifier      - the modifier
     * @param docLocation   - the document location
     * @param viewLocation  - the view location
     */
    public BasicCanvasMouseData(
        BasicCanvasView canvas,
        int modifier,
        Point docLocation,
        Point viewLocation) {
        mCanvas = canvas;
        mModifier = modifier;
        mDocLocation = docLocation;

        mViewLocation = viewLocation;

        //JL: why do we need this collection?
        //JL: shouldn't they be retrieved with member methods
        mEventDataCollection.add(
            CANVAS,
            mCanvas);
        mEventDataCollection.add(
            MODIFIER,
            new Integer(mModifier));
        mEventDataCollection.add(
            DOCUMENT_LOCATION,
            mDocLocation);
        mEventDataCollection.add(
            VIEW_LOCATION,
            mViewLocation);
    }

    /**
     * Retrieves the canvas
     *
     * @return   The canvas value
     */
    public ICanvas getCanvas() {
        return mCanvas;
    }

    /**
     * Retrieves the data at specified index
     *
     * @param index  - index of the indiviudal data object
     * @return       The data value
     */
    public Object getData(int index) {
        return mEventDataCollection.get(index);
    }

    /**
     * Retrieves the data collection
     *
     * @return   - collection
     */
    public Collection getDataCollection() {
        return mEventDataCollection;
    }

    /**
     * Retrieves doc location
     *
     * @return   The docLocation value
     */
    public Point getDocLocation() {
        return mDocLocation;
    }

    /**
     * Retrieves doc location
     *
     * @return   The modelLocation value
     */
    public Point getModelLocation() {
        return mDocLocation;
    }

    /**
     * Retrieves the modifier
     *
     * @return   The modifier value
     */
    public int getModifier() {
        return mModifier;
    }

    /**
     * Retrieves the modifier
     *
     * @return   The mouseModifier value
     */
    public int getMouseModifier() {
        return mModifier;
    }

    /**
     * Retrieve the view locaiton
     *
     * @return   The viewLocation value
     */
    public Point getViewLocation() {
        return mViewLocation;
    }

    /**
     * Returns a string describing the object.
     *
     * @return   a String
     */
    public String toString() {
        return "doc location: " + mDocLocation + ", view location: "
            + mViewLocation;
    }
}
