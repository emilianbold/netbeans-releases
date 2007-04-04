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
package org.netbeans.modules.bpel.design;

import java.awt.Font;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.JLabel;

/**
 *
 * @author Alexey
 */
public class DiagramFontUtil {
    

    private static final double DIAGRAM_FONT_SIZE = 11;
    
    public static Font getFont(){
        return font;
    }
    
    public static double getZoomCorrection(){
        return zoomCorrection;
    }

    private static Font font = new JLabel().getFont()
            .deriveFont((float) DIAGRAM_FONT_SIZE);
    
    private static final double zoomCorrection = new JLabel().getFont().getSize2D() /
            DIAGRAM_FONT_SIZE;
    
    private static final Set<Locale> multibyteLocales = new HashSet<Locale>();

    static {
        multibyteLocales.add(Locale.JAPANESE);
        multibyteLocales.add(Locale.KOREAN);
        multibyteLocales.add(Locale.CHINESE);
       
        //BAD HACK. I dont know how to determine
        if (multibyteLocales.contains(Locale.getDefault())){
//            System.out.println("changin font");
            font = new Font("sansserif", 
                    font.getStyle(),
                    font.getSize());
        }
    }
}
