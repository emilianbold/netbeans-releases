package org.netbeans.modules.soa.mappercore;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author anjeleevich
 */
public class MapperStyle {
    public static final Color VERTEX_ITEM_TEXT_COLOR = Color.BLACK;
    public static final Color FUNCTION_RESULT_TEXT_COLOR = Color.GRAY;
    
    public static final Color PIN_BACKGROUND_COLOR = new Color(0x5668CA);
    public static final Color PIN_FOREGROUND_COLOR = Color.WHITE;
    
    public static final Color SELECTION_COLOR = new Color(0x5D985C);
    public static final Stroke SELECTION_STROKE = new BasicStroke(2);

    public static final Stroke FOCUS_STROKE = new BasicStroke(2, 
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, 
            new float[] { 6, 4 }, 0);
    
    public static final Color LINK_COLOR_SELECTED_NODE = new Color(0xE68B2C);
    public static final Color LINK_TCOLOR_SELECTED_NODE = new Color(0x00E68B2C, true);
    
    public static final Color LINK_COLOR_UNSELECTED_NODE = new Color(0x999999);
    public static final Color LINK_TCOLOR_UNSELECTED_NODE = new Color(0x00999999, true);
    
    public static final Stroke LINK_STROKE = new BasicStroke(1);
    
    public static final Color VERTEX_BACKGROUND_COLOR = Color.WHITE;
    public static final Color VERTEX_BORDER_COLOR = new Color(0xA7A2A7);

    public static final Color ICON_COLOR = new Color(0x5668CA);

    public static final BufferedImage GRADIENT_TEXTURE;

    static {
        double[] percents = { 0, 0.0843, 0.1798, 0.7416, 0.9045, 1.0674 };
        int[] rgbColors = { 0xA9CDE8, 0xDDEBF6, 0xFFFFFF, 0xDCE3EF, 0xE7F1F9, 
                0xDCE3EF };
        
        final int height = 100;
        
        BufferedImage image = new BufferedImage(1, height, 
                BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 1, height);
        
        for (int i = 1; i < percents.length; i++) {
            float y0 = (float) (percents[i - 1] * height - 0.5);
            float y1 = (float) (percents[i] * height + 0.5);
            
            rect.y = y0;
            rect.height = y1 - y0;
            
            Paint p = new GradientPaint(
                    0, y0, new Color(rgbColors[i - 1]), 
                    0, y1, new Color(rgbColors[i]));
            
            g2.setPaint(p);
            g2.fill(rect);
        }
        
        GRADIENT_TEXTURE = image;
    }     
}
