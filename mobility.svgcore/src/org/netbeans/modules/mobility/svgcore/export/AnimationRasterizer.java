/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Color;
import java.awt.Dimension;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

import javax.imageio.ImageIO;

import java.io.File;
import java.util.MissingResourceException;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * 
 */
public class AnimationRasterizer {
    
    /**
     * @param args 
     */
    public static void rasterize(String svgURL, int width, int height, float startTime, float endTime, int numberOfSteps,  File outputFile) throws Exception {
        // Load SVG image into memory
        SVGImage svgImage = (SVGImage) SVGImage.createImage(svgURL, null);
        rasterize(svgImage, width, height, startTime, endTime, numberOfSteps, outputFile);
    }
    
    public static void rasterize(SVGImage svgImage, int width, int height, float startTime, float endTime, int numberOfSteps,  File outputFile) throws Exception {

        // Create an offscreen buffer of the right size.
        BufferedImage buffer = 
            new BufferedImage(width * numberOfSteps, height, BufferedImage.TYPE_INT_ARGB);

        // Scale the SVG image to the desired size.
        svgImage.setViewportWidth(width);
        svgImage.setViewportHeight(height);

        // Create graphics to draw the image into.
        Graphics g = buffer.createGraphics();
        g.setColor(Color.WHITE);
        
        ScalableGraphics sg = ScalableGraphics.createInstance();

        float currentTime = startTime;
        float stepLenght = (endTime - startTime) / numberOfSteps;
        SVGSVGElement element = 
                (SVGSVGElement) svgImage.getDocument().getDocumentElement();
        element.setCurrentTime(currentTime);
        
        // Render now for each rendering step.  
        for (int i = 0; i < numberOfSteps; i++) {
            sg.bindTarget(g);
            sg.render(i * width, 0, svgImage);
            sg.releaseTarget();
            currentTime += stepLenght;
            element.setCurrentTime(currentTime);
        }

        // Now, save the image to a PNG file.
        ImageIO.write(buffer, "png", outputFile); //NOI18N
    }
}
