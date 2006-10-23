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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.openide.util.Utilities;

/**
 * Class for persisting icons
 * @author Milan Kubec
 */
public class ExtIcon  {
    
    private Image image;
    
    public ExtIcon() {
    }
    
    public ExtIcon(byte[] content) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ColorModel cm = ColorModel.getRGBdefault();
        image = toolkit.createImage(new MemoryImageSource(16, 16, cm, content, 0, 16 * 4));
    }
        
    public void setIcon(Icon icn) {
        image = Utilities.icon2Image(icn);
    }
    
    public Icon getIcon() {
        return new ImageIcon(image);
    }
    
    
    public byte[] getBytes() throws IOException {
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 16, 16, false);
        try {
            pg.grabPixels();
            if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                throw new IOException("Cannot load image data");
            }
        } catch (InterruptedException e) {
            throw new IOException("Loading image interrupted");
        }
        Object obj = pg.getPixels();
        if (obj instanceof byte[]) {
            return (byte[])obj;
        } else {
            return intToByteArray((int[])obj);
        }
    }
    
    public static byte[] intToByteArray(int[] value) {
        byte[] b = new byte[value.length * 4];
        for (int j = 0; j < b.length; j = j + 4) {
            int val = value[j / 4];
            b[j] = (byte)(val >>> 24);
            b[j + 1] = (byte)(val >> 16 & 0xff);
            b[j + 2] = (byte)(val >> 8 & 0xff);
            b[j + 3] = (byte)(val & 0xff);
        }
        return b;
    }
}
