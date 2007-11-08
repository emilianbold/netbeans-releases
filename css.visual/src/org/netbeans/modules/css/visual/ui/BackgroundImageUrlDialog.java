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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * BackgroundImageUrlDialog.java
 * Created on November 5, 2004, 5:08 PM
 */

package org.netbeans.modules.css.visual.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.Utilities;
//import org.netbeans.modules.css.visual.model.CssMetaModel;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Dialog to select a Image URL
 * @author  Winston Prakash
 * @version 1.0
 */
public class BackgroundImageUrlDialog { //extends URLPanel{
    
    private static final Logger LOGGER = Logger.getLogger(BackgroundImageUrlDialog.class.getName());
    
    private FileObject base;
    
    public BackgroundImageUrlDialog(FileObject base) {
        this.base = base;
    }
    
    private String imageUrl = null;
    private FileFilter imgFilter = new ImageFilter() ;
    
    // XXX Get URL also not just local file
    public boolean show(Component parent){
        boolean retValue = false;
        JFileChooser fileChooser = Utilities.getJFileChooser();
        File currDir = null;
        try{
            currDir = FileUtil.toFile(base).getParentFile();
            if (currDir == null) currDir = new File(System.getProperty("user.home")); //NOI18N
            if (currDir != null ) fileChooser.setCurrentDirectory(currDir);
            fileChooser.addChoosableFileFilter(new ImageFilter()) ;
            if ( fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                File imageFile = fileChooser.getSelectedFile();
                FileObject imageFO = FileUtil.toFileObject(imageFile);
                if(imageFO == null) {
                    //should not happen with Master FS
                    //assert imageFile != null;
                    LOGGER.log(Level.WARNING, null, new IllegalStateException("Cannot find FileObject for file " + imageFile.toURL()));
                    return false;
                }
                
                FileObject currDirFO = FileUtil.toFileObject(currDir);
                if(currDirFO == null) {
                    //should not happen with Master FS
                    //assert imageFile != null;
                    LOGGER.log(Level.WARNING, null, new IllegalStateException("Cannot find FileObject for file " + currDir.toURL()));
                    return false;
                }
                
                FileObject newImageFO = imageFO.copy(currDirFO, imageFO.getName(), imageFO.getExt());
                imageUrl = newImageFO.getNameExt();
                StringBuffer sb = new StringBuffer();
                int len = imageUrl.length();
                for (int i = 0; i < len; i++) {
                    char chr =  imageUrl.charAt(i);
                    if (chr == ' '){
                        sb.append("%20");
                    }else{
                        sb.append(chr);
                    }
                }
                imageUrl = sb.toString();
                retValue = true;
                
            }
        } catch (IOException exc){
            LOGGER.log(Level.WARNING, null, exc);
        }
        return retValue;
    }
    
    public String getImageUrl(){
        return imageUrl;
    }
    
    public static class ImageFilter extends FileFilter {
        //Accept all directories and image files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            
            if (i > 0 &&  i < s.length() - 1) {
                extension = s.substring(i+1).toLowerCase();
            }
            
            if (extension != null) {
                if (extension.toLowerCase().equals("gif") || //NOI18N
                        extension.toLowerCase().equals("jpg") || //NOI18N
                        extension.toLowerCase().equals("png") ) { //NOI18N
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
        
        //The description of this filter
        public String getDescription() {
            return NbBundle.getMessage(BackgroundImageUrlDialog.class, "IMAGE_FILTER"); //NOI18N
        }
    }
    
}