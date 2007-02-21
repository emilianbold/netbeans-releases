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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Container widget that paints a gradient background behind its children.
 * @author rdara
 */
public class CasaGradientWidget extends Widget {

    private GradientRectangleDrawer.CustomPainter mCustomPainter;
    CasaGradientInterface mCasaGradientProvider;
    
    
    public CasaGradientWidget(Scene scene, GradientRectangleDrawer.CustomPainter customPainter, CasaGradientInterface casaGradient) {
        super(scene);
        mCustomPainter = customPainter;
        mCasaGradientProvider = casaGradient;
    }

    protected void paintWidget () {
        super.paintWidget();

        GradientRectangleDrawer.paintGradientBackground(
                getGraphics(), 
                mCasaGradientProvider.getRectangleToBePainted(), 
                true, 
                true, 
                mCasaGradientProvider.getGradientColorSceheme(),
                mCasaGradientProvider.isBorderShown(), 
                mCustomPainter);
    }
}
