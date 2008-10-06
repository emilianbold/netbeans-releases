/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableImage;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
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

    private static final int MAX_WORKUNITS = 300;

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
        handle.start(MAX_WORKUNITS);
        if ( SVGDataObject.EXT_SVG.equals(fo.getExt().toLowerCase())){  //NOI18N
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
        private int lastProgress;
        
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
            this.lastProgress = 0;
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
            if (current >= MAX_WORKUNITS) {
                current = 295;
            }
            if (lastProgress < (int)current){
                lastProgress = (int)current;
            }
            handle.progress(lastProgress);  
        }
    }
    
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
