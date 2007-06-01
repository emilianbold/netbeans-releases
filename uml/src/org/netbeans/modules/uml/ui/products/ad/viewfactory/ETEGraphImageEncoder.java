/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.swing.drawingarea.SaveAsGraphicKind;
import com.tomsawyer.editor.TSEGraphImageEncoder;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.TSTransform;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import org.openide.ErrorManager;

/**
 * @author KevinM
 * @author Sheryl
 *
 *	This class extends the TSS image Encoder so we can save the transform.
 * You can only use the transform only after the image has been saved,
 */
public class ETEGraphImageEncoder extends TSEGraphImageEncoder
{
    protected TSTransform encoderTransform = null;
    protected String fileName;
    protected int kind;
    private float QUALITY = 75;
    
    public ETEGraphImageEncoder(TSEGraphWindow graphWindow)
    {
        super(graphWindow);
    }
    
    public ETEGraphImageEncoder(TSEGraphWindow graphWindow, String sFilename, int saveAsGraphicKind)
    {
        super(graphWindow);
        
        fileName = sFilename;
        kind = saveAsGraphicKind;
    }
    
        /*
         * Converts the graph window into an image output stream.
         */
    public boolean save(double scale)
    {
        boolean rc = false;
        try
        {
            TSEObjectUI currentUI = getGraphWindow().getGraph().getUI();
            ETImageExportGraphUI ui = new ETImageExportGraphUI();
            getGraphWindow().getGraph().setUI(ui);
            
            switch (kind)
            {
            case SaveAsGraphicKind.SAFK_JPG :
                FileImageOutputStream fio = new FileImageOutputStream(new File(fileName));
                write("jpg", fio, false,
                        TSEGraphWindow.CUSTOM_SIZE, false, false, QUALITY,
                        (int)(getGraphWindow().getGraph().getFrameBounds().getWidth()*scale),
                        (int)(getGraphWindow().getGraph().getFrameBounds().getHeight()*scale));
                rc = true;
                break;
            case SaveAsGraphicKind.SAFK_PNG :
                writePNGFormat(new FileOutputStream(fileName), false,
                        TSEGraphWindow.CUSTOM_SIZE, false, false,
                        (int)(getGraphWindow().getGraph().getFrameBounds().getWidth()*scale),
                        (int)(getGraphWindow().getGraph().getFrameBounds().getHeight()*scale));
                rc = true;
                break;
            case SaveAsGraphicKind.SAFK_SVG :
                writeSVGFormat(new FileOutputStream(fileName));
                rc = true;
                break;
            }
            encoderTransform = ui.getImageTransform();
            getGraphWindow().getGraph().setUI(currentUI);
        }
        catch (Exception e)
        {
            Log.stackTrace(e);
        }
        return rc;
    }
    
        /*
         * Only valid after a call to save(), it can be used to locate objects on the graphic.
         */
    public TSTransform getEncoderTransform()
    {
        return encoderTransform;
    }
    
    
    // override TSEGraphImageEncoder.writeJPEGFormat() to use ImageIO API 
    public void writeJPEGFormat(OutputStream fo)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        writeJPEGFormat(fo, false, TSEGraphWindow.ACTUAL_SIZE, false, false, 100, 0, 0);
    }
    
    public void writeJPEGFormat( OutputStream fo,
            boolean visibleAreaOnly,
            int zoomType,
            boolean drawGrid,
            boolean selectedOnly,
            float quality,
            int width,
            int height)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        write("jpg", fo, visibleAreaOnly, zoomType, drawGrid, selectedOnly, 
                quality, 
                (int)getGraphWindow().getGraph().getFrameBounds().getWidth(), 
                (int)getGraphWindow().getGraph().getFrameBounds().getHeight());
    }
    
    
    public void writePNGFormat(OutputStream fo)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        writePNGFormat(fo, false, TSEGraphWindow.ACTUAL_SIZE, false, false, 
                (int)getGraphWindow().getGraph().getFrameBounds().getWidth(), 
                (int)getGraphWindow().getGraph().getFrameBounds().getHeight());
    }
    
    
    public void writePNGFormat(  OutputStream fo,
            boolean visibleAreaOnly,
            int zoomType,
            boolean drawGrid,
            boolean selectedOnly,
            int width,
            int height)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        write("png", fo, visibleAreaOnly, zoomType, drawGrid, selectedOnly, 0, width, height);
    }
    
    
    public void write(String format,
            Object fo,
            boolean visibleAreaOnly,
            int zoomType,
            boolean drawGrid,
            boolean selectedOnly,
            float quality,
            int width,
            int height)
    {
        
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (visibleAreaOnly)
        {
            Image visible = getGraphWindow().getVisibleGraphImage(drawGrid, selectedOnly);
            if (visible instanceof BufferedImage)
                bufferedImage = (BufferedImage)visible;
        }
        else
        {
            if (zoomType == TSEGraphWindow.CUSTOM_SIZE)
                getGraphWindow().createEntireGraphImage(bufferedImage, zoomType,
                        drawGrid, selectedOnly, width, height);
            else
            {
                int w = (int)getGraphWindow().getGraph().getFrameBounds().getWidth();
                int h = (int)getGraphWindow().getGraph().getFrameBounds().getHeight();
                if (zoomType == TSEGraphWindow.CURRENT_ZOOM_LEVEL)
                {
                    w = (int)(width * getGraphWindow().getZoomLevel());
                    h = (int)(height * getGraphWindow().getZoomLevel());
                }
                else if (zoomType == TSEGraphWindow.FIT_IN_WINDOW)
                {
                    double d1 = getGraphWindow().getWidth()/getGraphWindow().getGraph().getFrameBounds().getWidth();
                    double d2 = getGraphWindow().getHeight()/getGraphWindow().getGraph().getFrameBounds().getHeight();
                    double ratio = Math.max(d1, d2);
                    w = (int)(w * ratio);
                    h = (int)(h * ratio);
                }
                
                bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                getGraphWindow().createEntireGraphImage(bufferedImage, zoomType,
                        drawGrid, selectedOnly, w, h);
            }
        }
        try
        {
            if ("jpg".equals(format) && (fo instanceof ImageOutputStream))
            {
                Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = (ImageWriter)iter.next();
                
                ImageWriteParam iwp = writer.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwp.setCompressionQuality(quality/100);
                writer.setOutput(fo);
                IIOImage image = new IIOImage(bufferedImage, null, null);
                writer.write(null, image, iwp);
                
                ((ImageOutputStream)fo).flush();
                ((ImageOutputStream)fo).close();
                
                writer.dispose();
            }
            else
            {
                if (fo instanceof OutputStream)
                {
                    ImageIO.write(bufferedImage, format, (OutputStream)fo);
                    ((OutputStream)fo).flush();
                    ((OutputStream)fo).close();
                }
            }
        }
        catch (IOException ioe)
        {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
}
