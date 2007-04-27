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
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/**
 * Class for persisting icons
 * @author Milan Kubec
 */
public class ExtIcon  {
    
    Image image;
    
    public ExtIcon() {
    }
    
    public ExtIcon(byte[] content) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ColorModel cm = ColorModel.getRGBdefault();
        byte w = content[0];
        byte h = content[1];
        image = toolkit.createImage(new MemoryImageSource(w, h, cm, content, 2, w));
    }
    
    public void setIcon(Icon icn) {
        image = icn != null ? Utilities.icon2Image(icn) : null;
    }
    
    public Icon getIcon() {
        return image != null ? new ImageIcon(image) : null;
    }
    
    
    public byte[] getBytes() throws IOException {
        if (image == null) {
            return null;
        }
        Icon icn = getIcon();
        byte h = (byte)icn.getIconHeight();
        byte w = (byte)icn.getIconWidth();
        PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, true);
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
            byte[] data = (byte[])obj;
            byte[] toRet = new byte[data.length + 2];
            toRet[0] = w;
            toRet[1] = h;
            for (int i = 0; i < data.length; i++) {
                toRet[i + 2] = data[i];
            }
            return toRet;
        } else {
            return intToByteArray((int[])obj, w, h);
        }
    }
    
    public static byte[] intToByteArray(int[] value, byte w, byte h) {
        byte[] b = new byte[value.length * 4 + 2];
        b[0] = w;
        b[1] = h;
        for (int j = 2; j < b.length; j = j + 4) {
            int val = value[(j - 2) / 4];
            b[j] = (byte)(val >>> 24);
            b[j + 1] = (byte)(val >> 16 & 0xff);
            b[j + 2] = (byte)(val >> 8 & 0xff);
            b[j + 3] = (byte)(val & 0xff);
        }
        return b;
    }
}
