/*
 * DiagramFont.java
 *
 * Created on 8 Сентябрь 2006 г., 10:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
    

    private static final float DIAGRAM_FONT_SIZE = 11f;
    
    private static Font font = new JLabel().getFont().deriveFont(DIAGRAM_FONT_SIZE);
    
    private static final float zoomCorrection = new JLabel().getFont().getSize2D() /
            DIAGRAM_FONT_SIZE;
    
    private static final Set<Locale> multibyteLocales = new HashSet<Locale>();
    
    static {
        multibyteLocales.add(Locale.JAPANESE);
        multibyteLocales.add(Locale.KOREAN);
        multibyteLocales.add(Locale.CHINESE);
       
        //BAD HACK. I dont know how to determine
        if (multibyteLocales.contains(Locale.getDefault())){
            System.out.println("changin font");
            font = new Font("sansserif", 
                    font.getStyle(),
                    font.getSize());
        }
        
        
    }
    
    public static Font getFont(){
        return font;
    }
    
    public static float getZoomCorrection(){
        return zoomCorrection;
    }
}
