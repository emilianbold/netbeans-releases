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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;
import javax.swing.text.BadLocationException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * Helper for rasterizing SVG images/animations into bitmap images
 * 
 * @author breh
 */
public class AnimationRasterizer {
    public static final String [] COMPRESSION_LEVEL_NAMES     = new String [] { "Minimum", "Low", "Medium", "High", "Very high", "Maximum" };
    public static final int []    COMPRESSION_LEVELS          = new int [] { 10, 30, 40, 60, 80, 99 };
    public static final int       DEFAULT_COMPRESSION_QUALITY = 3;
    
    public enum ImageType {
        JPEG("JPG", "JPEG", true, false, ".jpg"),
        PNG8("PNG", "PNG-8", false, true, ".png"),
        PNG24("PNG", "PNG-24", false, true, ".png");

        private final String  m_name;
        private final String  m_displayName;
        private final boolean m_supportsCompression;
        private final boolean m_supportsTransparency;
        private final String  m_extension;
        
        ImageType(String name, String displayName,boolean supportsCompression,
                boolean supportsTransparency, String extension) {
            m_name = name;
            m_displayName = displayName;
            m_supportsCompression = supportsCompression;
            m_supportsTransparency = supportsTransparency;
            m_extension            = extension;
        }
        
        public boolean supportsCompression() {
            return m_supportsCompression;
        }

        public boolean supportsTransparency() {
            return m_supportsTransparency;
        }
        
        public String getName() {
            return m_name;
        }
        
        public String toString() {
            return m_displayName;
        }
        
        public String getFileName( String filename) {
            String str = filename.toLowerCase();
            String ext = "." + SVGDataObject.EXT_SVG;
            if ( !str.endsWith(ext)) {
                ext = "." + SVGDataObject.EXT_SVGZ;
                if ( !str.endsWith(ext)){
                    ext = "";
                }
            }
            filename = filename.substring(0, filename.length() - ext.length()) + m_extension;
            return filename;
        }
    }    

    public static interface Params {
        SVGImage getSVGImage() throws IOException, BadLocationException;
        int getImageWidth();
        int getImageHeight();
        float getStartTime();
        float getEndTime();
        float getFramesPerSecond();
        boolean isForAllConfigurations();
        double getRatio();
        float getCompressionQuality();
        boolean isProgressive();
        boolean isInSingleImage();
        boolean isTransparent();
        AnimationRasterizer.ImageType getImageType();
        void setImageWidth(int w);
        void setImageHeight(int h);
        int getNumberFrames();
        J2MEProject getProject();
        String getElementId();
    }
    
    public static class PreviewInfo {
        final BufferedImage m_image;
        final String        m_imageFormat;
        final int           m_imageSize;
        
        private PreviewInfo(BufferedImage image, String format, int size) {
            m_image       = image;
            m_imageFormat = format;
            m_imageSize   = size;
        }
    }
      
    public static String createFileNameRoot( SVGDataObject dObj, Params params, ProjectConfiguration projectCfg, boolean fullPath) {
        FileObject fo           = dObj.getPrimaryFile();
        String     filenameRoot;
        
        if ( fullPath) {
            filenameRoot = fo.getPath();
            String ext   = fo.getExt();
            //remove the extension including the dot
            if ( ext != null && ext.length() > 0) {
                filenameRoot = filenameRoot.substring(0, filenameRoot.length() - ext.length() - 1);
            }
        } else {
            filenameRoot= fo.getName();
        }
        
        if (params.isForAllConfigurations()) {
            if (projectCfg != null) {
                if (projectCfg != params.getProject().getConfigurationHelper().getDefaultConfiguration ()){
                    filenameRoot += "_" + projectCfg.getDisplayName();
                }
            } else {
                filenameRoot += "_{configuration-name-here}";
            }
        } else {
            filenameRoot += getActiveConfigurationName(fo);
        }
        
        if ( params.getElementId() != null) {
            filenameRoot += "_" + params.getElementId();
        }
        
        return filenameRoot;
    } 

    public static String createFileName( String filenameRoot, Params params, int frameIndex, int frameNum) {
        if (!params.isInSingleImage()) {
            assert frameIndex >= 0;
            assert frameNum >= 0;
            filenameRoot += "_" + frameIndex + "_" + frameNum;
        }
        filenameRoot = params.getImageType().getFileName(filenameRoot);
        return filenameRoot;
    }
    
    static void export(final SVGDataObject dObj, final Params params) throws MissingResourceException {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_AnimationExportProgress"));
        try {   
            final FileObject fo        = dObj.getPrimaryFile();
            final File       directory = FileUtil.toFile(fo.getParent());
            
            if (!params.isForAllConfigurations()){
                handle.start(1);
                
                String   filenameRoot = createFileNameRoot(dObj, params, null, false);                
                rasterizeImage( params.getSVGImage(), directory, filenameRoot, params);                
                handle.progress(1);
            } else {
                Collection<ProjectConfiguration> configurations = params.getProject().getConfigurationHelper().getConfigurations();
                handle.start(configurations.size());
                
                int stepsDone = 0;

                int imageWidth  = params.getImageWidth();
                int imageHeigth = params.getImageHeight();
                for (ProjectConfiguration configuration: configurations) {
                    Dimension activeDimenson = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, null);
                    double    ratioWidth   = (double)imageWidth / activeDimenson.getWidth();
                    double    ratioHeight  = (double)imageHeigth / activeDimenson.getHeight(); 
                    assert configuration != null;
                    String    filenameRoot = createFileNameRoot(dObj, params, configuration, false);
                    Dimension dim          = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, configuration.getDisplayName());
                    
                    params.setImageWidth((int)((double)dim.getWidth() * ratioWidth));
                    params.setImageHeight((int)((double)dim.getHeight() * ratioHeight));
                    
                    rasterizeImage( params.getSVGImage(), directory, filenameRoot, params);
                    handle.progress(++stepsDone);
                }
             }
            fo.getParent().refresh(false);
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            handle.finish();
        }
    }

    static void exportElement(final FileObject fo, final J2MEProject project, final SVGImage svgImage, String id, Params params) throws MissingResourceException {
        //TODO Implement
        /*
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_AnimationExportProgress"));
        try {                    
            id = id != null ? "_" + id + "_": "";
            
            final File directory = FileUtil.toFile(fo.getParent());
            
            if (!params.isForAllConfigurations()){
                handle.start(1);
                //File output = new File(FileUtil.toFile(fo.getParent()), fo.getName()  + id + getActiveConfigurationName(fo) + getSuffixForImageType(imageType));
                String filenameRoot = fo.getName()  + id + getActiveConfigurationName(fo);
                AnimationRasterizer.rasterize( svgImage, directory, filenameRoot, params);
                handle.progress(1);
            } else {
               
                Collection<ProjectConfiguration> configurations = project.getConfigurationHelper().getConfigurations();
                handle.start(configurations.size());
                int stepsDone = 0;

                int imageWidth = params.getImageWidth();
                int imageHeigth = params.getImageHeight();
                
                  for (ProjectConfiguration configuration: configurations) {
                    String name;
                    if (configuration != project.getConfigurationHelper().getDefaultConfiguration ()){
                        name = "_" + configuration.getDisplayName();
                    } else {
                        name = "";
                    }
                    Dimension activeDimenson = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, null);
                    double ratioWidth = (double)imageWidth / activeDimenson.getWidth();
                    double ratioHeight = (double)imageHeigth / activeDimenson.getHeight();
                    
                    //File output = new File(FileUtil.toFile(fo.getParent()), fo.getName() + id + name + getSuffixForImageType(imageType));
                    String filenameRoot =  fo.getName() + id + name;
                    Dimension dim = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, configuration.getDisplayName());
                    params.setImageWidth((int)((double)dim.getWidth() * ratioWidth));
                    params.setImageHeight((int)((double)dim.getHeight() * ratioHeight));
                    AnimationRasterizer.rasterize( fo.getURL().toString(), directory, filenameRoot, params);
                    handle.progress(++stepsDone);
                }
                 
            }
            fo.getParent().refresh(false);
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            handle.finish();
        }
         * */
    }    
    
    /**
     * @param args 
     */
    /*
    private static void rasterize(String svgURL,File directory, String filenameRoot, Params params) throws IOException {
        // Load SVG image into memory
        SVGImage svgImage = (SVGImage) SVGImage.createImage(svgURL, null);
        rasterize(svgImage,directory,filenameRoot, params);
    }
  */  
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    private static BufferedImage adjustImage(BufferedImage img, Params params) {
/*        if ( !params.isTransparent()) {
            // remove all (semi) transparent pixels
            int w = img.getWidth();
            int h = img.getHeight();

            BufferedImage background = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB);            
            Graphics g = background.createGraphics();
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, w, h);
            g.drawImage(img, 0, 0, null);
            g.dispose();
            img = background;            
        } */

        if (ImageType.PNG8.equals(params.getImageType())) {
            img = convertTo256Color(img, params);
        } 
        return img;
    }
    
    private static final int PATTERN_SIZE = 10;
    private static final Color PATTERN_COLOR1 = new Color(192, 192, 192);
    private static final Color PATTERN_COLOR2 = new Color(220, 220, 220);
    
    public static PreviewInfo previewFrame( SVGImage svgImage, Params params, int frameIndex, float time) throws IOException {
        BufferedImage img;
        
        if (frameIndex != -1 &&
            ImageType.PNG8.equals(params.getImageType()) &&
            params.isInSingleImage()) {
            img = rasterizeFramesInSingleImage( svgImage, params);
            int           w    = params.getImageWidth();
            int           h    = params.getImageHeight();
            BufferedImage temp = createBuffer(w, h, params.isTransparent());            
            Graphics g = temp.createGraphics();
            int      x = frameIndex * w;
            g.drawImage( img, 0, 0, w - 1, h - 1, x, 0, x + w - 1, h - 1, null);
            g.dispose();
            img = temp;
        } else {
            img = rasterizeFrame(null, 0, svgImage, params, time);
            img = adjustImage(img, params);
        }

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        encodeImage(img, buff, params);
        
        if ( ImageType.JPEG.equals(params.getImageType())) {
            ByteArrayInputStream in = new ByteArrayInputStream(buff.toByteArray());
            img = decodeImage(in, params);
        } else {
            if (params.isTransparent()) {
                int w = img.getWidth();
                int h = img.getHeight();
                
                //draw checquered pattern to distinguish background
                BufferedImage background = createBuffer(w, h, false);            
                Graphics g = background.createGraphics();
                g.setColor(PATTERN_COLOR1);
                g.fillRect(0, 0, w, h);
                g.setColor(PATTERN_COLOR2);
                int start = 0;
                for ( int y = 0; y < h; y += PATTERN_SIZE) {
                    for (int x = start; x < w; x += 2 * PATTERN_SIZE) {
                        g.fillRect( x, y, PATTERN_SIZE, PATTERN_SIZE);
                    }
                    start ^= PATTERN_SIZE;
                }
                g.drawImage(img, 0, 0, null);
                g.dispose();
                img = background;
            }
        }
        //writeImageToFile(img, new File( "c:\\test.jpg"), params);
        return new PreviewInfo(  img, params.getImageType().toString(), buff.size());
    }
    
    private static BufferedImage createBuffer(int w, int h, boolean isTransparent) {
        return new BufferedImage(w, h, isTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);        
    }

    public static BufferedImage rasterizeFrame( BufferedImage buffer, int xOffset,
            SVGImage svgImage, Params params, float time) {
        int w = params.getImageWidth();
        int h = params.getImageHeight();
        
        // Create an offscreen buffer of the right size. 
        if (buffer == null) {
            buffer = createBuffer( w, h, params.isTransparent());
            xOffset = 0;
        }
        
        // Create graphics to draw the image into.
        Graphics g = buffer.createGraphics();
        if (!params.isTransparent()) {
            g.setColor(BACKGROUND_COLOR);            
            g.fillRect( xOffset, 0, w, h);
        }
        
        synchronized(svgImage) {
            // Scale the SVG image to the desired size.
            float ratio = (float) w / svgImage.getViewportWidth();
            if ( svgImage.getViewportHeight() * ratio > h) {
                ratio = (float) h / svgImage.getViewportHeight();
            }
            int _w = Math.round( (float) (svgImage.getViewportWidth() * ratio));
            int _h = Math.round( (float) (svgImage.getViewportHeight() * ratio));
            svgImage.setViewportWidth(_w);
            svgImage.setViewportHeight(_h);
            assert _w <= w;
            assert _h <= h;
            
            // create instance of scalable graphics
            ScalableGraphics sg = ScalableGraphics.createInstance();

            SVGSVGElement element = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
            element.setCurrentTime(time);

            sg.bindTarget(g);
            sg.render(xOffset + (w-_w)/2, (h-_h)/2, svgImage);
            sg.releaseTarget();
        }
        
        return buffer;
    }

    private static BufferedImage rasterizeFramesInSingleImage(SVGImage svgImage, Params params) {
        int frameNum = params.getNumberFrames();
        int w        = params.getImageWidth();
        int h        = params.getImageHeight();
        BufferedImage img = createBuffer(w * frameNum, h, params.isTransparent());
        for (int i = 0; i < frameNum; i++) {
            rasterizeFrame( img, i * w, svgImage, params, params.getStartTime() + (i / params.getFramesPerSecond()));
        }
        img = adjustImage(img, params);
        return img;
    }
    
    public static void rasterizeImage(SVGImage svgImage, File directory, String filenameRoot, Params params) throws IOException {        
        int frameNum = params.getNumberFrames();
        
        if (params.isInSingleImage()) {
            BufferedImage img = rasterizeFramesInSingleImage( svgImage, params);
            writeImageToFile( img, new File( directory, createFileName(filenameRoot, params, -1, -1)), params);
        } else {
            for (int i = 0; i < frameNum; i++) {
                BufferedImage img = rasterizeFrame( null, 0, svgImage, params, 
                        params.getStartTime() + (i / params.getFramesPerSecond()));
                img = adjustImage(img, params);
                writeImageToFile( img, new File( directory, createFileName(filenameRoot, params, i, frameNum) ), params);
            }
        }
    }
    
    private static int getEncodedSize(BufferedImage img, Params params) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        encodeImage(img, bOut, params);
        return bOut.size();
    }
    
    private static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
    public static int calculateAnimationSize(SVGImage svgImage, Params params) throws IOException, InterruptedException {        
        int size;
        int frameNum = params.getNumberFrames();
        
        if (params.isInSingleImage()) {
            int w        = params.getImageWidth();
            int h        = params.getImageHeight();
            BufferedImage img = createBuffer(w * frameNum, h, params.isTransparent());
            for (int i = 0; i < frameNum; i++) {
                rasterizeFrame( img, i * w, svgImage, params, params.getStartTime() + (i / params.getFramesPerSecond()));
            }
            checkInterrupted();
            img = adjustImage(img, params);
            checkInterrupted();
            size = getEncodedSize(img, params);
        } else {
            size = 0;
            for (int i = 0; i < frameNum; i++) {
                BufferedImage img = rasterizeFrame( null, 0, svgImage, params, 
                        params.getStartTime() + (i / params.getFramesPerSecond()));
                checkInterrupted();                
                img = adjustImage(img, params);
                checkInterrupted();
                size += getEncodedSize(img, params);
                checkInterrupted();
            }
        }
        return size;
    }
    
    
    
 /*   
    public static void _rasterize(SVGImage svgImage, File directory, String filenameRoot, Params params) throws IOException {
        // Scale the SVG image to the desired size.
        svgImage.setViewportWidth(params.getImageWidth());
        svgImage.setViewportHeight(params.getImageHeight());

        // create instance of scalable graphics
        ScalableGraphics sg = ScalableGraphics.createInstance();

        float currentTime = params.getStartTime();
        float stepLenght = (params.getEndTime() - params.getStartTime()) / params.getNumberFrames();
        SVGSVGElement element = 
                (SVGSVGElement) svgImage.getDocument().getDocumentElement();
        element.setCurrentTime(currentTime);
        
        if (params.isInSingleImage()) {
            // Create an offscreen buffer of the right size.        
            BufferedImage buffer = new BufferedImage(params.getImageWidth() params.getImageWidth() * params.getNumberFrames(),
                    params.getImageHeight(), BufferedImage.TYPE_INT_ARGB);            
            //BufferedImage buffer = new BufferedImage(width * numberOfSteps, height, BufferedImage.TYPE_BYTE_INDEXED);            
            // Create graphics to draw the image into.
            Graphics g = buffer.createGraphics();
            g.setColor(Color.WHITE);            
            // Render now for each rendering step.
            for (int i = 0; i < params.getNumberFrames(); i++) {
                sg.bindTarget(g);
                sg.render(i * params.getImageWidth(), 0, svgImage);
                sg.releaseTarget();
                currentTime += stepLenght;
                element.setCurrentTime(currentTime);
            }

            //System.out.println("Number of bands: " + buffer.getSampleModel().getNumBands());
            //System.out.println("Color model: " + buffer.getColorModel());
//*            
            {    
                System.out.println("Encoding PNG with Batik #1...");
                File outputFile = new File(directory, filenameRoot + "1" + getSuffixForImageType(imageType));        
                PNGEncodeParam.RGB params = new PNGEncodeParam.RGB();
                //params.setBitDepth(8);
                FileOutputStream fout = new FileOutputStream(outputFile);
                PNGImageEncoder pngEncoder = new PNGImageEncoder(fout, params);
                pngEncoder.encode(buffer);
                fout.close();
                System.out.println("PNG encoded.");
            }

            {    
                System.out.println("Encoding PNG with Batik #2...");
                File outputFile = new File(directory, filenameRoot + "2" + getSuffixForImageType(imageType));        
                PNGEncodeParam params = new PNGEncodeParam.RGB();
                params.setBitDepth(16);
                FileOutputStream fout = new FileOutputStream(outputFile);
                PNGImageEncoder pngEncoder = new PNGImageEncoder(fout, params);
                pngEncoder.encode(buffer);
                fout.close();
                System.out.println("PNG encoded.");
            }

            {    
                System.out.println("Encoding PNG with Batik #3...");
                File outputFile = new File(directory, filenameRoot + "3" + getSuffixForImageType(imageType));        
                PNGEncodeParam params = new PNGEncodeParam.Gray();
                FileOutputStream fout = new FileOutputStream(outputFile);
                PNGImageEncoder pngEncoder = new PNGImageEncoder(fout, params);
                pngEncoder.encode(buffer);
                fout.close();
                System.out.println("PNG encoded.");
            }
  //       
            System.out.print("Writing PNG using JDK (DirectColorModel ...");
            File outputFile = new File(directory,filenameRoot + "_jdk0" +  getSuffixForImageType(params.getImageType()));        
            writeImageToFile(buffer, outputFile, params);
            System.out.println("size=" + outputFile.length());
///*
            System.out.print("Writing PNG using Batik (DirectColorModel ...");
            outputFile = new File(directory, filenameRoot + "_batik0" +  getSuffixForImageType(imageType));        
            FileOutputStream fout = new FileOutputStream(outputFile);
            PNGEncodeParam params = PNGEncodeParam.getDefaultEncodeParam(buffer);
            PNGImageEncoder pngEncoder = new PNGImageEncoder(fout, params);
            pngEncoder.encode(buffer);
            fout.close();
            System.out.println("size=" + outputFile.length());

            System.out.print("Writing PNG using JDK (remapped IndexedColorModel ...");
            BufferedImage buffer2 = convertModel1(buffer);
            outputFile = new File(directory, filenameRoot + "_jdk2" + getSuffixForImageType(imageType));                    
            writeImageToFile(buffer2, outputFile, imageType, compressionQuality, progressive);
            System.out.println("size=" + outputFile.length());

            System.out.print("Writing PNG using Batik (remapped IndexedColorModel ...");
            outputFile = new File(directory, filenameRoot + "_batik2" +  getSuffixForImageType(imageType));        
            fout = new FileOutputStream(outputFile);
            params = PNGEncodeParam.getDefaultEncodeParam(buffer2);
            pngEncoder = new PNGImageEncoder(fout, params);
            pngEncoder.encode(buffer2);
            fout.close();
            System.out.println("size=" + outputFile.length());
            
            System.out.print("Writing PNG using JDK (IndexedColorModel ...");
            BufferedImage buffer1 = convertModel(buffer);
            outputFile = new File(directory, filenameRoot + "_jdk1" + getSuffixForImageType(imageType));                    
            writeImageToFile(buffer1, outputFile, imageType, compressionQuality, progressive);
            System.out.println("size=" + outputFile.length());

            System.out.print("Writing PNG using Batik (IndexedColorModel ...");
            outputFile = new File(directory, filenameRoot + "_batik1" +  getSuffixForImageType(imageType));        
            fout = new FileOutputStream(outputFile);
            params = PNGEncodeParam.getDefaultEncodeParam(buffer1);
            pngEncoder = new PNGImageEncoder(fout, params);
            pngEncoder.encode(buffer1);
            fout.close();
            System.out.println("size=" + outputFile.length());
//            
        } else {                       

            for (int i = 0; i < params.getNumberFrames(); i++) {                                
                // Create an offscreen buffer of the right size.        
                BufferedImage buffer = new BufferedImage(params.getImageWidth(), params.getImageHeight(), BufferedImage.TYPE_INT_ARGB);
                // Create graphics to draw the image into.
                Graphics g = buffer.createGraphics();
                g.setColor(Color.WHITE);                        
                sg.bindTarget(g);
                sg.render(0, 0, svgImage);
                sg.releaseTarget();
                currentTime += stepLenght;
                element.setCurrentTime(currentTime);
                final int fileIndex = i + 1;
                File outputFile = new File(directory,filenameRoot + "_" + fileIndex + getSuffixForImageType(params.getImageType()));                
                writeImageToFile(buffer, outputFile, params);
            }            
        }                
    }

            
    private static BufferedImage convertModel( BufferedImage image) {
        BufferedImage img = new BufferedImage( image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        Graphics g = img.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return img;
    }
*/    
    private static BufferedImage convertTo256Color(BufferedImage image, Params params) {
        int w = image.getWidth(),
            h = image.getHeight();
        int [][] pixels = new int[w][h];
        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                pixels[x][y] = image.getRGB(x, y);
            }
        }
        boolean isTransparent = params.isTransparent();
        
        int [] palette = ShrinkPalette.quantizeImage(pixels, isTransparent ? 255 : 256);

        byte [] red   = new byte[256];
        byte [] green = new byte[265];
        byte [] blue  = new byte[256];
        
        for (int i = 0; i < palette.length; i++) {
            int rgb = palette[i];
        //a = (rgb & 0xFF000000) >> 24;
            red[i] = (byte)((rgb & 0xFF0000) >> 16);
            green[i] = (byte)((rgb & 0xFF00) >> 8);
            blue[i] = (byte)((rgb & 0xFF)); 
        }
        int transparentColor = isTransparent ? 255 : -1;
        
        IndexColorModel colorModel = new IndexColorModel( 8, 256, red, green, blue, transparentColor);
        BufferedImage bufferedImage = new BufferedImage(colorModel, 
					colorModel.createCompatibleWritableRaster(w, h), 
					false, null);        
        WritableRaster raster = bufferedImage.getRaster();
        int[] pixelArray = new int[1];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = image.getRGB(x, y);
                if ( (pixel >>> 24) > 127) {
                    pixel = pixels[x][y];
                } else {
                    pixel = transparentColor;
                }
                pixelArray[0] = pixel;
                raster.setPixel(x, y, pixelArray);
            }
        }
        return bufferedImage;        
        /*
        Quantizer quantizer = new Quantizer(256, false, false);
        return quantizer.quantizeImage(image);
         */ 
    }
            
    private static void writeImageToFile(BufferedImage image, File file, Params params) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        try {
            encodeImage(image, fout, params);
            fout.flush();
        } finally {
            fout.close();
        }
    }
    
    /*
    static {
        String [] formats = ImageIO.getWriterFormatNames();
        System.out.println("Image writters:");
        for ( String writer : formats) {
            System.out.println(writer);
        }
    }*/
    

    private static void encodeImage(BufferedImage image, OutputStream out, Params params) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(params.getImageType().getName());
        // these guys were found
        ImageWriter writer = writers.hasNext() ? writers.next() : null;
        
        ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();        
        if (imageWriteParam.canWriteProgressive()) {
            imageWriteParam.setProgressiveMode(params.isProgressive() ? ImageWriteParam.MODE_DEFAULT : ImageWriteParam.MODE_DISABLED);
        }
        
        if (imageWriteParam.canWriteCompressed()) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(params.getCompressionQuality());
        }
        
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image,null,null), imageWriteParam);
        writer.dispose();
        ios.close();
    }
    
    private static BufferedImage decodeImage(InputStream in, Params params) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(params.getImageType().getName());
        // these guys were found
        ImageReader reader = readers.hasNext() ? readers.next() : null;
        
        ImageInputStream iis = ImageIO.createImageInputStream(in);
        reader.setInput(iis);
        BufferedImage img = reader.read(0);
        reader.dispose();
        iis.close();
        return img;
    }
    
    private static String getActiveConfigurationName( FileObject primaryFile ){
        Project p = FileOwnerQuery.getOwner(primaryFile);
        if (p == null || !(p instanceof J2MEProject)){
            return "";
        }
        return getDefaultName((J2MEProject) p);
    }
    
    private static String getDefaultName(J2MEProject project){
        AntProjectHelper helper = (AntProjectHelper) project.getLookup().lookup(AntProjectHelper.class);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ProjectConfigurationsHelper confs = project.getConfigurationHelper();
        
        return confs.getActiveConfiguration () != confs.getDefaultConfiguration () ? "_" + confs.getActiveConfiguration ().getDisplayName () : "";
    }
}
