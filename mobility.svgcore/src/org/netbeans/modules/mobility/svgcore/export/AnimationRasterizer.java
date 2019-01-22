/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;
import javax.imageio.IIOException;
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
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * Helper for rasterizing SVG images/animations into bitmap images
 * 
 * 
 */
public class AnimationRasterizer {
    private static final int   PATTERN_SIZE     = 10;
    private static final Color PATTERN_COLOR1   = new Color(192, 192, 192);
    private static final Color PATTERN_COLOR2   = new Color(220, 220, 220);
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    public enum CompressionLevel {
        MAXIMUM( getMessage("LBL_CompressionMaximum"), 10), //NOI18N
        VERY_HIGH(getMessage("LBL_CompressionVeryHigh"), 30), //NOI18N
        HIGH(getMessage("LBL_CompressionHigh"), 40), //NOI18N
        MEDIUM(getMessage("LBL_CompressionMedium"), 60), //NOI18N
        LOW(getMessage("LBL_CompressionLow"), 80), //NOI18N
        MINIMUM(getMessage("LBL_CompressionMinimum"), 99); //NOI18N

        private final String  m_name;
        private final int     m_rate;
        
        CompressionLevel(String name, int rate) {
            m_name = name;
            m_rate = rate;
        }
        
        public String toString() {
            return m_name;
        }
        
        public int getRate() {
            return m_rate;
        }
        
        public static CompressionLevel getLevel( int rate) {
            assert rate >= 0 && rate <= 99;
            for (CompressionLevel level : CompressionLevel.values()) {
                if ( rate <= level.getRate()) {
                    return level;
                }
            }
            assert false : "Could not find compression level for rate: " + rate; //NOI18N
            return MINIMUM;
        }
    }    

    private interface ColorReducer {
        BufferedImage reduceColors(BufferedImage image, Params params);
    }
    
    public enum ColorReductionMethod implements ColorReducer {
        QUANTIZE( getMessage("LBL_ColorReductionColorQuantization")) { //NOI18N
            public BufferedImage reduceColors(BufferedImage image, Params params) {
                return reduceColorsQuantize(image, params);
            }
        }, 
        MEDIAN_CUT(getMessage("LBL_ColorReductionMedianCut")){ //NOI18N
            public BufferedImage reduceColors(BufferedImage image, Params params) {
                return reduceColorsMedianCut(image, params);
            }
        };
        
        private final String m_displayName;
        
        ColorReductionMethod(String displayName) {
            m_displayName = displayName;
        }
        public String toString() {
            return m_displayName;
        }
    }
    
    public enum ImageType {
        JPEG("JPG", "JPEG", true, false, false, ".jpg"), //NOI18N
        PNG8("PNG", "PNG-8", false, true, true, ".png"), //NOI18N
        PNG24("PNG", "PNG-24", false, true, false, ".png"); //NOI18N

        private final String  m_name;
        private final String  m_displayName;
        private final boolean m_supportsCompression;
        private final boolean m_supportsTransparency;
        private final String  m_extension;
        private final boolean m_colorReduction;
        
        ImageType(String name, String displayName,boolean supportsCompression,
                boolean supportsTransparency, boolean colorReduction, String extension) {
            m_name                 = name;
            m_displayName          = displayName;
            m_supportsCompression  = supportsCompression;
            m_supportsTransparency = supportsTransparency;
            m_colorReduction       = colorReduction;
            m_extension            = extension;
        }
        
        public boolean supportsCompression() {
            return m_supportsCompression;
        }

        public boolean supportsTransparency() {
            return m_supportsTransparency;
        }
        
        public boolean needsColorReduction() {
            return m_colorReduction;
        }
        
        public String getName() {
            return m_name;
        }
        
        public String toString() {
            return m_displayName;
        }
        
        public String getFileName( String filename) {
            String str = filename.toLowerCase();
            String ext = "." + SVGDataObject.EXT_SVG; //NOI18N
            if ( !str.endsWith(ext)) {
                ext = "." + SVGDataObject.EXT_SVGZ; //NOI18N
                if ( !str.endsWith(ext)){
                    ext = "";  //NOI18N
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
        ColorReductionMethod getColorReductionMethod();
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
                    filenameRoot += "_" + projectCfg.getDisplayName(); //NOI18N
                }
            } else {
                filenameRoot += "_{configuration-name-here}"; //NOI18N
            }
        } else {
            filenameRoot += getActiveConfigurationName(fo);
        }
        
        if ( params.getElementId() != null) {
            filenameRoot += "_" + params.getElementId(); //NOI18N
        }
        
        return filenameRoot;
    } 

    public static String createFileName( String filenameRoot, Params params, int frameIndex, int frameNum) {
        if (!params.isInSingleImage()) {
            assert frameIndex >= 0;
            assert frameNum >= 0;
            filenameRoot += "_" + frameIndex + "_" + frameNum; //NOI18N
        }
        filenameRoot = params.getImageType().getFileName(filenameRoot);
        return filenameRoot;
    }
    
    public static void export(final SVGDataObject dObj, final Params params) throws MissingResourceException, IOException, BadLocationException {
        FileObject fo = dObj.getPrimaryFile();
        export(dObj, params, fo.getParent());
    }
        
    public static FileObject export(final SVGDataObject dObj, final Params params, final FileObject directory) throws MissingResourceException, IOException, BadLocationException {
        final ProgressHandle handle = ProgressHandleFactory.createHandle( getMessage("TITLE_AnimationExportProgress")); //NOI18N
        FileObject file = null;
        try {               
            if (!params.isForAllConfigurations()){
                handle.start(1);
                
                String   filenameRoot = createFileNameRoot(dObj, params, null, false);                
                file = rasterizeImage( params.getSVGImage(), directory, filenameRoot, params);                
                handle.progress(1);
            } else {
                FileObject fo = dObj.getPrimaryFile();                
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
                    
                    params.setImageWidth((int)(dim.getWidth() * ratioWidth));
                    params.setImageHeight((int)(dim.getHeight() * ratioHeight));
                    
                    rasterizeImage( params.getSVGImage(), directory, filenameRoot, params);
                    handle.progress(++stepsDone);
                }
             }
             directory.refresh(false);          
        } finally {
            handle.finish();
        }
        return file;
    }
    
    private static BufferedImage adjustImage(BufferedImage img, Params params) {
        if (params.getImageType().needsColorReduction()) {
            img = params.getColorReductionMethod().reduceColors(img, params);
        }
        return img;
    }
        
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
        return new PreviewInfo(  img, params.getImageType().toString(), buff.size());
    }
    
    private static BufferedImage createBuffer(int w, int h, boolean isTransparent) throws IIOException {
        try {
            return new BufferedImage(w, h, isTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);        
        } catch( Throwable e) {
            System.err.println("Not enough memory");
            throw new IIOException("Not enough memory", e);
        }
    }

    public static BufferedImage rasterizeFrame( BufferedImage buffer, int xOffset,
            SVGImage svgImage, Params params, float time) throws IIOException {
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
            int _w = Math.round( (svgImage.getViewportWidth() * ratio));
            int _h = Math.round( (svgImage.getViewportHeight() * ratio));
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

    private static BufferedImage rasterizeFramesInSingleImage(SVGImage svgImage, Params params) throws IIOException {
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
    
    public static FileObject rasterizeImage(SVGImage svgImage, FileObject dir, String filenameRoot, Params params) throws IOException {        
        FileObject file = null;
        int frameNum = params.getNumberFrames();
        
        if (params.isInSingleImage()) {
            BufferedImage img = rasterizeFramesInSingleImage( svgImage, params);
            String name = createFileName(filenameRoot, params, -1, -1);
            FileObject fo  = dir.getFileObject(name);
            if (fo != null){
                fo.delete();
            }
            fo = dir.createData(name);
            
            writeImageToFile( img, fo, params);
            file = fo;
        } else {
            for (int i = 0; i < frameNum; i++) {
                BufferedImage img = rasterizeFrame( null, 0, svgImage, params, 
                        params.getStartTime() + (i / params.getFramesPerSecond()));
                img = adjustImage(img, params);
                FileObject fo = dir.createData(createFileName(filenameRoot, params, i, frameNum));
                writeImageToFile( img, fo, params);
            }
        }
        return file;
    }
    
    static final String getMessage( String stringID) {
        return NbBundle.getMessage(AnimationRasterizer.class, stringID);
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
    
    interface ProgressUpdater {
        void updateProgress( String text);
    }
    
    static int calculateAnimationSize(SVGImage svgImage, Params params, ProgressUpdater updater) throws IOException, InterruptedException {        
        int size;
        int frameNum = params.getNumberFrames();
        
        if (params.isInSingleImage()) {
            int w        = params.getImageWidth();
            int h        = params.getImageHeight();
            BufferedImage img = createBuffer(w * frameNum, h, params.isTransparent());
            for (int i = 0; i < frameNum; i++) {
                if (updater != null) {
                    updater.updateProgress( NbBundle.getMessage(AnimationRasterizer.class, "LBL_PreviewRasterizing", String.valueOf(i), String.valueOf(frameNum))); //NOI18N
                }
                rasterizeFrame( img, i * w, svgImage, params, params.getStartTime() + (i / params.getFramesPerSecond()));
            }
            checkInterrupted();
            if (updater != null) {
                updater.updateProgress(getMessage("LBL_PreviewEncoding")); //NOI18N
            }
            img = adjustImage(img, params);
            checkInterrupted();
            size = getEncodedSize(img, params);
            if (updater != null) {
                updater.updateProgress(getSizeText(size));
            }
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
    
    public static String getSizeText( int size) {
        if ( size < 1024) {
            return size + " Bytes"; //NOI18N
        } else if ( size < 1024 * 1024) {
            return (Math.round(size / 102.4) / 10.0) + " KBytes"; //NOI18N
        } else {
            return (Math.round(size / (102.4 * 1024)) / 10.0) + " MBytes"; //NOI18N
        }
    }
    
    private static BufferedImage reduceColorsMedianCut(BufferedImage image, Params params) {
        int w = image.getWidth(),
            h = image.getHeight();
        int [] pixels = new int[w * h];
        
        int i = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int rgb = image.getRGB(x, y);
                if ( (rgb >>> 24) > 127) {
                    pixels[i++] = rgb;
                } else {
                    pixels[i++] = pixels[0];
                }
            }
        }
        Quantizer quantizer = new Quantizer(pixels, w, h);
        return quantizer.toImage();
    }
    
    private static BufferedImage reduceColorsQuantize(BufferedImage image, Params params) {
        int w = image.getWidth(),
            h = image.getHeight();
        int [][] pixels = new int[w][h];
        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int rgb = image.getRGB(x, y);
                if ( (rgb >>> 24) > 127) {
                    pixels[x][y] = rgb;
                } else {
                    pixels[x][y] = pixels[0][0];
                }
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
            
    private static void writeImageToFile(BufferedImage image, FileObject file, Params params) throws IOException {
        OutputStream fout = file.getOutputStream();
        try {
            encodeImage(image, fout, params);
            fout.flush();
        } finally {
            fout.close();
        }
    }

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
            return ""; //NOI18N
        }
        return getDefaultName((J2MEProject) p);
    }
    
    private static String getDefaultName(J2MEProject project){
        AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ProjectConfigurationsHelper confs = project.getConfigurationHelper();
        
        return confs.getActiveConfiguration () != confs.getDefaultConfiguration () ? "_" + confs.getActiveConfiguration ().getDisplayName () : ""; //NOI18N
    }
}
