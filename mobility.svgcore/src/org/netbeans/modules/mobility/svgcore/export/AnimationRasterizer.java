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
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
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
    
    

    
    public static enum ImageType {JPEG, PNG};
    
    
    
    
    static void export(final FileObject fo, final J2MEProject project, final int imageWidth, final int imageHeigth,
                        final AnimationRasterizer.ImageType imageType, final boolean progressive, final float compressionQuality,
                        final float startTime, final float endTime, final int numberOfSteps, final boolean inSingleImage, final boolean forAllConfig) throws MissingResourceException {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_AnimationExportProgress"));
        try {   
            final File directory = FileUtil.toFile(fo.getParent());
            
            if (!forAllConfig){
                handle.start(1);
                                
                String filenameRoot = fo.getName()  + getActiveConfigurationName(fo);
                
                rasterize(
                        fo.getURL().toString(), 
                        imageWidth, 
                        imageHeigth, 
                        startTime,
                        endTime,
                        numberOfSteps, 
                        inSingleImage,
                        compressionQuality,
                        progressive,
                        imageType,
                        directory, 
                        filenameRoot);
                
                handle.progress(1);
            } else {
               
                Collection<ProjectConfiguration> configurations = project.getConfigurationHelper().getConfigurations();
                handle.start(configurations.size());
                int stepsDone = 0;

                for (ProjectConfiguration configuration: configurations) {
                    String name;
                    if (configuration != project.getConfigurationHelper().getDefaultConfiguration ()){
                        name = "_" + configuration.getDisplayName();
                    } else {
                        name = "";
                    }
                    Dimension activeDimenson = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, null);
                    double ratioWidth = (double)imageWidth / activeDimenson.getWidth();
                    double rationHeight = (double)imageHeigth / activeDimenson.getHeight();
                    
                    String filenameRoot = fo.getName()  + name;
                    Dimension dim = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, configuration.getDisplayName());
                    
                    rasterize(
                            fo.getURL().toString(), 
                            (int)((double)dim.getWidth() * ratioWidth), 
                            (int)((double)dim.getHeight() * rationHeight), 
                            startTime, 
                            endTime,
                            numberOfSteps, 
                            inSingleImage,
                            compressionQuality,
                            progressive,
                            imageType,
                            directory,
                            filenameRoot);
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

    static void exportElement(final FileObject fo, final J2MEProject project, final SVGImage svgImage, String id, final int imageWidth, final int imageHeigth,
                                final AnimationRasterizer.ImageType imageType, final boolean progressive, final float compressionQuality,
                                final float startTime, final float endTime, final int numberOfSteps, final boolean inSingleImage, final boolean forAllConfig) throws MissingResourceException {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_AnimationExportProgress"));
        try {                    
            id = id != null ? "_" + id + "_": "";
            
            final File directory = FileUtil.toFile(fo.getParent());
            
            if (!forAllConfig){
                handle.start(1);
                //File output = new File(FileUtil.toFile(fo.getParent()), fo.getName()  + id + getActiveConfigurationName(fo) + getSuffixForImageType(imageType));
                String filenameRoot = fo.getName()  + id + getActiveConfigurationName(fo);
                AnimationRasterizer.rasterize(
                        svgImage, 
                        imageWidth, 
                        imageHeigth, 
                        startTime, 
                        endTime,
                        numberOfSteps, 
                        inSingleImage,
                        compressionQuality,
                        progressive,
                        imageType,
                        directory,
                        filenameRoot);
                handle.progress(1);
            } else {
               
                Collection<ProjectConfiguration> configurations = project.getConfigurationHelper().getConfigurations();
                handle.start(configurations.size());
                int stepsDone = 0;

                  for (ProjectConfiguration configuration: configurations) {
                    String name;
                    if (configuration != project.getConfigurationHelper().getDefaultConfiguration ()){
                        name = "_" + configuration.getDisplayName();
                    } else {
                        name = "";
                    }
                    Dimension activeDimenson = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, null);
                    double ratioWidth = (double)imageWidth / activeDimenson.getWidth();
                    double rationHeight = (double)imageHeigth / activeDimenson.getHeight();
                    
                    //File output = new File(FileUtil.toFile(fo.getParent()), fo.getName() + id + name + getSuffixForImageType(imageType));
                    String filenameRoot =  fo.getName() + id + name;
                    Dimension dim = ScreenSizeHelper.getCurrentDeviceScreenSize(fo, configuration.getDisplayName());
                    AnimationRasterizer.rasterize(
                            fo.getURL().toString(), 
                            (int)((double)dim.getWidth() * ratioWidth), 
                            (int)((double)dim.getHeight() * rationHeight), 
                            startTime, 
                            endTime,
                            numberOfSteps,
                            inSingleImage,
                            compressionQuality,
                            progressive,
                            imageType,                            
                            directory,
                            filenameRoot);
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
    
    
    
    
    /**
     * @param args 
     */
    private static void rasterize(String svgURL, int width, int height, float startTime, float endTime, int numberOfSteps,
                                    boolean inSingleImage, float compressionQuality, boolean progressive, ImageType imageType,
                                    File directory, String filenameRoot) throws IOException {

        // Load SVG image into memory
        SVGImage svgImage = (SVGImage) SVGImage.createImage(svgURL, null);
        rasterize(svgImage, width, height, startTime, endTime, numberOfSteps, inSingleImage, compressionQuality, progressive, imageType,directory,filenameRoot);
    }
    
    public static void rasterize(SVGImage svgImage, int width, int height, float startTime, float endTime, int numberOfSteps,
                                boolean inSingleImage, float compressionQuality, boolean progressive, ImageType imageType, 
                                File directory, String filenameRoot) throws IOException {

        

        // Scale the SVG image to the desired size.
        svgImage.setViewportWidth(width);
        svgImage.setViewportHeight(height);

        // create instance of scalable graphics
        ScalableGraphics sg = ScalableGraphics.createInstance();

        float currentTime = startTime;
        float stepLenght = (endTime - startTime) / numberOfSteps;
        SVGSVGElement element = 
                (SVGSVGElement) svgImage.getDocument().getDocumentElement();
        element.setCurrentTime(currentTime);
        
        
        if (inSingleImage) {
            File outputFile = new File(directory, filenameRoot +  getSuffixForImageType(imageType));        
            // Create an offscreen buffer of the right size.        
            BufferedImage buffer = new BufferedImage(width * numberOfSteps, height, BufferedImage.TYPE_INT_ARGB);            
            // Create graphics to draw the image into.
            Graphics g = buffer.createGraphics();
            g.setColor(Color.WHITE);            
            // Render now for each rendering step.
            for (int i = 0; i < numberOfSteps; i++) {
                sg.bindTarget(g);
                sg.render(i * width, 0, svgImage);
                sg.releaseTarget();
                currentTime += stepLenght;
                element.setCurrentTime(currentTime);
            }
            writeImageToFile(buffer, outputFile, imageType, compressionQuality, progressive);

        } else {                       

            for (int i = 0; i < numberOfSteps; i++) {                                
                // Create an offscreen buffer of the right size.        
                BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                // Create graphics to draw the image into.
                Graphics g = buffer.createGraphics();
                g.setColor(Color.WHITE);                        
                sg.bindTarget(g);
                sg.render(0, 0, svgImage);
                sg.releaseTarget();
                currentTime += stepLenght;
                element.setCurrentTime(currentTime);
                final int fileIndex = i + 1;
                File outputFile = new File(directory, filenameRoot + "_" + fileIndex + getSuffixForImageType(imageType));                
                writeImageToFile(buffer, outputFile, imageType, compressionQuality, progressive);
            }            
        }
                
    }
    
    
    private static void writeImageToFile(BufferedImage image, File outputFile, ImageType imageType, float compressionQuality,  boolean progressive) throws IOException {
         // Now, save the image to a PNG file.
        //ImageIO.write(buffer, "png", outputFile); //NOI18N
        
        IIOImage iioImage = new IIOImage(image,null,null);                
        
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(imageType.name());
        // these guys were found
        ImageWriter writer = writers.hasNext() ? writers.next() : null;
        //System.out.println("Using imagewriter: "+writer);
        /*
        while (writers.hasNext()) {
            writer = writers.next();
            System.out.println(" + additional writter: "+writer);
        }
        System.out.println("writers done \n");
        */
        
        ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();        
        if (imageWriteParam.canWriteProgressive()) {
            //System.out.println("Supports progressive");
            imageWriteParam.setProgressiveMode(progressive ? ImageWriteParam.MODE_DEFAULT : ImageWriteParam.MODE_DISABLED);
        }
        
        
        if (imageWriteParam.canWriteCompressed()) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            //System.out.println("Supports compression quality");
            imageWriteParam.setCompressionQuality(compressionQuality);
        }
        
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);
        writer.write(null, iioImage, imageWriteParam);
        writer.dispose();
        ios.close();
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
    
    
    private static String getSuffixForImageType(ImageType imageType) {
        if (ImageType.JPEG.equals(imageType)) {
            return ".jpg"; // NOI18N
        } else {
            return ".png"; // NOI18N
        }
    }

    
}
