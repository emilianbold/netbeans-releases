package org.netbeans.modules.soa.mappercore.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalTextFieldUI;

/**
 *
 * @author anjeleevich
 */
public class MetalTextFieldBorder implements Border {
    
    public void paintBorder(Component c, Graphics g, int x, int y, 
            int width, int height) 
    {
        Color oldColor = g.getColor();
        g.setColor(OUTER_COLOR);
        g.drawRect(x, y, width - 1, height - 1);
        
//        g.setColor(INNER_COLOR);
//        g.drawRect(x + 1, y + 1, width - 3, height - 3);
        
        g.setColor(oldColor);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(1, 4, 1, 4);
    }

    public boolean isBorderOpaque() {
        return false;
    }
    
    
    public static final void installIfItIsNeeded(JTextField textField) {
        if (textField.getUI() instanceof MetalTextFieldUI) {
            textField.setBorder(INSTANCE);
        }
    }

    private static final Color OUTER_COLOR = new Color(0x7A8A99);
    private static final Color INNER_COLOR = new Color(0xB8CFE5);
    
    public static final Border INSTANCE = new MetalTextFieldBorder();
}
