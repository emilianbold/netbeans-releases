/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.util;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import java.awt.image.BufferedImage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

/** this class allows to encode BufferedImage into 24bit true color PNG image format with maximum compression
 * @author Adam Sotona
 * @version 1.0
 */
public class PNGEncoder extends Object
{
    OutputStream out;
    CRC32 crc;
/** public constructor of PNGEncoder class
 * @param out output stream for PNG image format to write into
 */    
    public PNGEncoder(OutputStream out) {
        crc=new CRC32();
        this.out = out;
    }
    void write(int i) throws IOException {
        byte b[]={(byte)((i>>24)&0xff),(byte)((i>>16)&0xff),(byte)((i>>8)&0xff),(byte)(i&0xff)};
        write(b);
    }
    void write(byte b[]) throws IOException {
        out.write(b);
        crc.update(b);
    }
/** main encoding method (stays blocked till encoding is finished)
 * @param image BufferedImage to encode
 * @throws IOException IOException
 */    
    public void encode(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        final byte id[] = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13};
        write(id);
        crc.reset();
        write("IHDR".getBytes());
        width = image.getWidth(null);
        height = image.getHeight(null);
        write(width);
        write(height);
        final byte head[]={8, 2, 0, 0, 0};
        write(head);
        write((int) crc.getValue());
        ByteArrayOutputStream compressed = new ByteArrayOutputStream(65536);
        BufferedOutputStream bos = new BufferedOutputStream( new DeflaterOutputStream(compressed, new Deflater(9)));
        int pixel;
        for (int y=0;y<height;y++) {
            bos.write(0);
            for (int x=0;x<width;x++) {
                pixel=image.getRGB(x,y);
                bos.write((byte)((pixel >> 16) & 0xff));
                bos.write((byte)((pixel >> 8) & 0xff));
                bos.write((byte)(pixel & 0xff));
            }
        }
        bos.close();
        write(compressed.size());
        crc.reset();
        write("IDAT".getBytes());
        write(compressed.toByteArray());
        write((int) crc.getValue()); 
        write(0);
        crc.reset();
        write("IEND".getBytes());
        write((int) crc.getValue()); 
        out.close();
    }

/** static method performing whole screen capture into PNG image format file with given fileName
 * @param fileName file name for screen capture PNG image file
 */    
    public static void captureScreen(Rectangle rect, String fileName) {
        try {
            BufferedImage capture=new Robot().createScreenCapture(rect);
            BufferedOutputStream file=new BufferedOutputStream(new FileOutputStream(fileName));
            PNGEncoder encoder=new PNGEncoder(file);
            encoder.encode(capture);
        } catch (AWTException awte) {
            awte.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void captureScreen(Component comp, String fileName) {
	captureScreen(new Rectangle(comp.getLocationOnScreen(),
				    comp.getSize()),
		      fileName);
    }

    public static void captureScreen(String fileName) {
	captureScreen(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()), fileName);
    }

}
