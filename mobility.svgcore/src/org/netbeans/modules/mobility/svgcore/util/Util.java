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
/*
 * Util.java
 *
 * Created on June 29, 2006, 9:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;
import java.util.zip.GZIPInputStream;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableImage;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.mobility.svgcore.options.SvgcoreSettings;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Utility class for derived SVG support components. It provides external editor lunching facility and SVG image loading
 * @author suchys
 */
public class Util {  
    //public static final String UNIQUE_NODE_ID = "unid";
    
    //**
    // * Lunches external editor set using options. If the editor is not set or is not set correctly, 
    // * <br> no Exception is thrown but warning dialog is displayed.
    // * @param fo SVG file to be lunched in external editor
    // 
    public static void launchExternalEditor(final FileObject fo){
        assert fo != null : "File object is null";
        final InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(Util.class, "LBL_EditedSvgFile", fo.getName()), false); //NOI18N
        ExecutionEngine.getDefault().execute(NbBundle.getMessage(Util.class, "LBL_SvgEditor"), new Runnable() { //NOI18N
            public void run() {
                String path = SvgcoreSettings.getDefault().getExternalEditorPath();
                if (path == null || path.length() == 0 || !new File(path).exists()){
                    if (path == null || path.length() == 0){
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(Util.class, "ERR_SvgEditorNotSet", path), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
                    } else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(Util.class, "ERR_SvgEditor", path), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
                    }
                    io.closeInputOutput();
                    return;
                }
                io.getOut().println(NbBundle.getMessage(Util.class, "LBL_LaunchingFile", FileUtil.toFile(fo).toString())); //NOI18N
                NbProcessDescriptor descriptor = new NbProcessDescriptor(
                        path, '\"' + FileUtil.toFile(fo).getAbsoluteFile().toString() + '\"'); //NOI18N
                try {
                    Process p = descriptor.exec();
                    p.waitFor();                
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                } catch (InterruptedException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } finally {
                    if (io != null){
                        io.closeInputOutput();
                    }
                }               
            }
        }, io);
    }

    /**
     * Loads SVG image from the FileObject
     * @param fo from which to create SVG image
     * @param showProgress if progress should be shown during loading
     * @return SVGImage created SVG image
     * @throws IOException if loading failed
     */
    
    public static SVGImage createSVGImage (FileObject fo, boolean showProgress) throws IOException {
        assert fo != null : "File object is null";
        if (!showProgress){
            return (SVGImage) loadImage(fo.getURL().toString());
        } else {
            return loadImageWithProgress(fo);
        }
    }

    /*
    public static SVGImage createSVGImage (InputStream in) throws IOException {
        return (SVGImage) ScalableImage.createImage(in, null);
    }*/
    
    /**
     * Loads SVG image from the FileObject in other thread
     * @param fo from which to create SVG image
     * @param loadedListener where the events should be dispached
     */
/*    
    public static void createSVGImageAsync (final FileObject fo, final boolean showProgress, final SVGImageLoadedListener loadedListener ){
        assert fo != null : "File object is null";
        assert loadedListener != null : "Listener is null";
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                SVGImage image = null;
                Exception e = null;
                try {
                    if (!showProgress){
                        image = loadImage(fo.getURL().toString());
                    } else {
                        image = loadImageWithProgress(fo);
                    }
                } catch (FileStateInvalidException ex) {
                    e = ex;
                } catch (IOException ex) {
                    e = ex;
                } finally {
                    loadedListener.svgImageLoaded(image, e);
                }
            }
        });
    }
*/    
    
    private static SVGImage loadImage(String url) throws IOException {
        return (SVGImage) ScalableImage.createImage(url, null);
    }
    
    
    
    private static SVGImage loadImageWithProgress(FileObject fo) throws IOException {
        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Util.class, "MSG_Loading", fo.getNameExt()));
        handle.start(200);
        if ("svg".equals(fo.getExt().toLowerCase())){  //NOI18N
            ProgressInputStream pis = null;
            try {
                pis = new ProgressInputStream(fo.getInputStream(), fo.getSize(), handle);
                return (SVGImage) ScalableImage.createImage(pis, null);
            } finally {
                handle.finish();
                try {
                    if (pis != null)
                        pis.close();
                } catch (IOException ioEx){
                    //discard
                }
            }
        } else {
            ProgressInputStream pis = null;
            try {
                pis = new ProgressInputStream(
                        new GZIPInputStream(fo.getInputStream()), fo.getSize(), handle);
                return (SVGImage) ScalableImage.createImage(pis, null);
            } finally {
                handle.finish();
                try {
                    if (pis != null)
                        pis.close();
                } catch (IOException ioEx){
                    // discard
                }
            }
        }        
    } 
    

            
    public static interface SVGImageLoadedListener extends EventListener {
        ///**
        // * Notify about loaded SVG image or report Exception if loading failed
        // * @param image created SVG image or null if loading failed
        // * @param e containing @see java.lang.Exception if loading failed or null if loading was successful
        // 
        public void svgImageLoaded(SVGImage image, Exception e);
    }
    
            
    private static class ProgressInputStream extends BufferedInputStream {
        private ProgressHandle handle;
        private long expectedSize;
        private long alreadyRead;
        
        // **
        // * Creates a <code>BufferedInputStream</code>
        // * and saves its  argument, the input stream
        // * <code>in</code>, for later use. An internal
        // * buffer array is created and  stored in <code>buf</code>.
        // *
        // * @param   in   the underlying input stream.
        // 
        public ProgressInputStream(InputStream in, long expectedSize, ProgressHandle handle) {
            super(in);
            this.handle = handle;
            this.expectedSize = expectedSize;
        }
     
        public synchronized int read() throws IOException {
            int i = super.read();
            alreadyRead += i;
            updateProgress();
            return i;
        }

        public synchronized int read(byte b[], int off, int len) throws IOException {
            int i = super.read(b, off, len);
            alreadyRead += i;
            updateProgress();
            return i;
        }
        
        private void updateProgress() throws IOException {
            double current = ((double)alreadyRead / (double)expectedSize) * 100.0;
            if (current > (double)expectedSize){
                current = (double)expectedSize;
            }
            handle.progress((int)current);  
        }
    }
  
            
    /*
    public static Document createXMLDOM(DataObject doj) throws IOException, SAXException {
        InputSource  in = DataObjectAdapters.inputSource(doj);
        Document xmlDoc = XMLUtil.parse( in, false, true, null, null);
        return xmlDoc;
    }
     **/
    
    /**
     * Used for attributation for Perseus DOM
     * @param n ElementNode for root of document
     */
/*    
    public static void attributeTree(final ModelNode n) {
        System.out.println("SVG DOM");
        attributeTree(n, 1);//, new HashMap());
    }
    
    private static void attributeTree(final ModelNode n, int depth) {
        int order = 1;
        ModelNode child = (ModelNode) n.getFirstChildNode();
        if (child == null)
            return;
        
        while (child != null) {
            if (child instanceof ElementNode){
                String id = depth + "_" + order++;
                ((ElementNode)child).setAttribute(UNIQUE_NODE_ID, id);
                System.out.println(((ElementNode)child).getLocalName() + " " + id);
            }
            attributeTree(child, depth+1);            
            child = (ModelNode) child.getNextSiblingNode();
        }
    }        

    public static void attributeTree(final org.w3c.dom.Node elem) {
        System.out.println("W3C DOM");
        attributeTree(elem, 1);//, new HashMap());        
    }
    
    private static void attributeTree(final org.w3c.dom.Node elem, int depth) {
        int order = 1;
        org.w3c.dom.Node child = elem.getFirstChild();
        Document d;
        while (child != null) {
            if (child.getNodeType() == child.ELEMENT_NODE){
                String id = depth + "_" + order++;
                child.setUserData(UNIQUE_NODE_ID, id, null);
                System.out.println(child.getLocalName() + " " + id);
            }
            attributeTree(child, depth+1);            
            child = child.getNextSibling();
        }
    }        
*/
    /*
    //TODO remove??
    public static Hashtable SVG_ELEMENTS_ATTRIBUTES = new Hashtable();

    {
        SVG_ELEMENTS_ATTRIBUTES.put("svg", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("rect", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("circle", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("ellipse", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("line", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("path", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("g", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("image", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("text", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("a", defaults); //NOI18N
        SVG_ELEMENTS_ATTRIBUTES.put("use", defaults); //NOI18N
    }

    private static String[] defaults
         = {"fill", //NOI18N
            "stroke", //NOI18N
            "display", //NOI18N
            "stroke-width", //NOI18N
            "visibility", //NOI18N
            "stroke-linecap", //NOI18N
            "stroke-linejoin", //NOI18N
            "stroke-miterlimit", //NOI18N
            "stroke-dashoffset", //NOI18N
            "color", //NOI18N
            "fill-rule"}; //NOI18N
     */
    
    //TODO make more robust
    public static String getPropertyValue(String text, String propName) {
        if (text != null) {
            int index = text.indexOf(propName);
            if (index != -1) {
                index += propName.length();
                int from = text.indexOf('"', index);
                if (from != -1) {
                    int to = text.indexOf('"', ++from);
                    if (to != -1) {
                        return text.substring(from, to);
                    }
                }
            }
        }
        return null;
    }    
}
