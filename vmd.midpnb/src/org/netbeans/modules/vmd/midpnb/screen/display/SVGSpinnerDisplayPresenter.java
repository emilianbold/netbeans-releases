/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.screen.display;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;

/**
 *
 * @author akorostelev
 */
public class SVGSpinnerDisplayPresenter extends SVGComponentDisplayPresenter {

    // fix issue with incorrect BBox returned by persejus for spinner.
    // move bbox to the left and increase width by SELECTION_SHAPE_EXTENSION
    private static final int SELECTION_SHAPE_EXTENSION = 3;

    private Rectangle mySelectionRect = null;
    private Point myLocationPoint = null;

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        mySelectionRect = null;
        myLocationPoint = null;
    }

    @Override
    public Point getLocation() {
        if (myLocationPoint == null){
            Point location = super.getLocation();
            if (location != null){
                myLocationPoint = location;
                myLocationPoint.setLocation(
                        location.getX() - SELECTION_SHAPE_EXTENSION,
                        location.getY());
            }
        }
        return myLocationPoint;
    }

    @Override
    public Shape getSelectionShape() {

        if (mySelectionRect == null) {
            Shape shape = super.getSelectionShape();
            if (shape != null) {
                mySelectionRect = shape.getBounds();
                mySelectionRect.setSize(
                        (int) mySelectionRect.getWidth() + SELECTION_SHAPE_EXTENSION,
                        (int) mySelectionRect.getHeight());
            }
        }
        return mySelectionRect;
    }



}
