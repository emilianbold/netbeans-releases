/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Class for persisting icons
 * @author Milan Kubec
 */
public class ExtIcon implements Externalizable {
    
    private static final long serialVersionUID = -8800765296866762961L;
    
    private Image image;
    private int width;
    private int height;
    
    public ExtIcon() {
    }
        
    public void setIcon(Icon icn) {
        width = icn.getIconWidth();
        height = icn.getIconHeight();
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        icn.paintIcon(new JPanel(), image.getGraphics(), 0, 0);
    }
    
    public Icon getIcon() {
        return new ImageIcon(image);
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        int[] pixels = new int[width * height];
        if (image != null) {
            try {
                PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
                pg.grabPixels();
                if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                    throw new IOException("Cannot load image data");
                }
            } catch (InterruptedException e) {
                throw new IOException("Loading image interrupted");
            }
        }
        out.writeInt(width);
        out.writeInt(height);
        out.writeObject(pixels);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        width = in.readInt();
        height = in.readInt();
        int[] pixels = (int[]) (in.readObject());
        if (pixels != null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ColorModel cm = ColorModel.getRGBdefault();
            image = toolkit.createImage(new MemoryImageSource(width, height, cm, pixels, 0, width));
        }
    }
    
}
