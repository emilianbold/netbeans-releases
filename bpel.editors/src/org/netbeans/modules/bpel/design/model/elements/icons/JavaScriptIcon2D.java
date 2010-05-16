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
package org.netbeans.modules.bpel.design.model.elements.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

public class JavaScriptIcon2D extends Icon2D {
    
  public void paint(Graphics2D g2) {
        g2.setStroke(STROKE);
        g2.setPaint(COLOR);
        g2.draw(SHAPE);

        // change update constants below to update "JS" label position
        final double jsCenterX = 7;
        final double jsCenterY = 4;

        g2.translate(jsCenterX, jsCenterY);

        g2.setPaint(Color.WHITE);
        g2.setStroke(JS_STROKE);
        g2.draw(JS_SHAPE);

        g2.setPaint(JS_COLOR);
        g2.fill(JS_SHAPE);
        
        g2.translate(-jsCenterX, -jsCenterY);

        g2.setStroke(STROKE);
        g2.setPaint(COLOR);
    }

    public static final Icon2D INSTANCE = new JavaScriptIcon2D();
    private static final Shape SHAPE;

    private static final Shape JS_SHAPE;
    private static final Stroke JS_STROKE = new BasicStroke(2);
    private static final Paint JS_COLOR = new Color(0x586397);
    
    static {
        GeneralPath gp = new GeneralPath(new RoundRectangle2D
                .Float(-7, -4, 14, 8, 2, 2));
        gp.moveTo(-3, -1);
        gp.lineTo(3, -1);
        gp.moveTo(-3, 1);
        gp.lineTo(3, 1);

        SHAPE = gp;

        // "JS" shape. Width=10px. Height=6px. Centered.
        GeneralPath jsPath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        jsPath.moveTo(-2.28947f, -2.9050968f);
        jsPath.lineTo(-2.28947f, 0.8383128f);
        jsPath.quadTo(-2.28947f, 1.5448154f, -2.4581752f, 1.7662566f);
        jsPath.quadTo(-2.6268804f, 1.9876977f, -3.054267f, 1.9876977f);
        jsPath.quadTo(-3.4591594f, 1.9876977f, -3.6616056f, 1.6818981f);
        jsPath.quadTo(-3.7965696f, 1.4710017f, -3.8190637f, 0.97539544f);
        jsPath.lineTo(-5.0f, 1.1019332f);
        jsPath.quadTo(-4.988753f, 2.029877f, -4.499508f, 2.509666f);
        jsPath.quadTo(-4.010263f, 2.9894552f, -3.088008f, 2.9894552f);
        jsPath.quadTo(-2.311964f, 2.9894552f, -1.833966f, 2.6836555f);
        jsPath.quadTo(-1.355968f, 2.3778558f, -1.1760157f, 1.8717047f);
        jsPath.quadTo(-1.0410516f, 1.4815465f, -1.0410516f, 0.7644991f);
        jsPath.lineTo(-1.0410516f, -2.9050968f);
        jsPath.closePath();
        jsPath.moveTo(2.4469283f, -3.0f);
        jsPath.quadTo(1.7383664f, -3.0f, 1.2322508f, -2.7996485f);
        jsPath.quadTo(0.72613525f, -2.599297f, 0.46183044f, -2.2144113f);
        jsPath.quadTo(0.19752565f, -1.8295255f, 0.19752565f, -1.3866433f);
        jsPath.quadTo(0.19752565f, -0.7012302f, 0.7598763f, -0.21616872f);
        jsPath.quadTo(1.1647687f, 0.12126538f, 2.1769998f, 0.3532513f);
        jsPath.quadTo(2.9530437f, 0.5325132f, 3.166737f, 0.6063269f);
        jsPath.quadTo(3.4929004f, 0.71177506f, 3.622241f, 0.8594025f);
        jsPath.quadTo(3.7515817f, 1.0070299f, 3.7515817f, 1.2073814f);
        jsPath.quadTo(3.7515817f, 1.5342706f, 3.4422889f, 1.771529f);
        jsPath.quadTo(3.1329958f, 2.0087874f, 2.5369043f, 2.0087874f);
        jsPath.quadTo(1.9633067f, 2.0087874f, 1.6315198f, 1.7398945f);
        jsPath.quadTo(1.2997329f, 1.4710017f, 1.1872628f, 0.9015817f);
        jsPath.lineTo(-0.027414594f, 1.0070299f);
        jsPath.quadTo(0.09630255f, 1.977153f, 0.72051173f, 2.4885764f);
        jsPath.quadTo(1.344721f, 3.0f, 2.5256572f, 3.0f);
        jsPath.quadTo(3.3241951f, 3.0f, 3.8640518f, 2.7891037f);
        jsPath.quadTo(4.4039083f, 2.5782075f, 4.7019544f, 2.1405976f);
        jsPath.quadTo(5.0f, 1.7029877f, 5.0f, 1.2073814f);
        jsPath.quadTo(5.0f, 0.65905094f, 4.752566f, 0.28471002f);
        jsPath.quadTo(4.5051312f, -0.08963093f, 4.066498f, -0.30579966f);
        jsPath.quadTo(3.6278644f, -0.52196836f, 2.7168565f, -0.72231984f);
        jsPath.quadTo(1.8058485f, -0.9226714f, 1.5696611f, -1.112478f);
        jsPath.quadTo(1.389709f, -1.2601055f, 1.389709f, -1.460457f);
        jsPath.quadTo(1.389709f, -1.6924429f, 1.5809082f, -1.8189807f);
        jsPath.quadTo(1.8958246f, -2.029877f, 2.435681f, -2.029877f);
        jsPath.quadTo(2.9642906f, -2.029877f, 3.2285955f, -1.8347979f);
        jsPath.quadTo(3.4929004f, -1.6397188f, 3.5716295f, -1.1968366f);
        jsPath.lineTo(4.820048f, -1.2495606f);
        jsPath.quadTo(4.786307f, -2.0404217f, 4.2014623f, -2.520211f);
        jsPath.quadTo(3.6166174f, -3.0f, 2.4469283f, -3.0f);
        jsPath.closePath();

        JS_SHAPE = jsPath;
    }
}
