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
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
//import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    private File base;

    public BackgroundImageUrlDialog(File base) {
        this.base = base;
    }
    private String imageUrl = null;
    private FileFilter imgFilter = new ImageFilter();

    // XXX Get URL also not just local file
    public boolean show(Component parent) {
        boolean retValue = false;
        JFileChooser fileChooser = Utilities.getJFileChooser();
        try {
            //try to find a web module for the edited css
            
//            WebModule webModule = WebModule.getWebModule(base);
//            FileObject webModuleDocumentBase = webModule == null ? null : webModule.getDocumentBase();
            FileObject webModuleDocumentBase = null;
            
            //identify a starting directory for the file chooser
            FileObject currDir = FileUtil.toFileObject(base);

            if (currDir != null) {
                fileChooser.setCurrentDirectory(FileUtil.toFile(currDir));
            }

            fileChooser.addChoosableFileFilter(new ImageFilter());
            if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                File imageFile = fileChooser.getSelectedFile();
                if(!imageFile.exists()) {
                    //non existing file
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(this.getClass(), "MSG_non_existing_file_selected", imageFile.getAbsolutePath()), 
                            NotifyDescriptor.ERROR_MESSAGE));
                    return false;
                }
                FileObject imageFO = FileUtil.toFileObject(imageFile);
                if (imageFO == null) {
                    //should not happen with Master FS
                    //assert imageFile != null;
                    LOGGER.log(Level.WARNING, null, new IllegalStateException("Cannot find FileObject for file " + imageFile.toURL()));
                    return false;
                }
                FileObject imageFolder = imageFO.getParent();

                //count relative path to the base dir
                //check if we do not overlap the web module root is thre is any
                //if we are out of the web module, copy the file to the css file
                //directory

                //assert webModuleDocumentBase is parent/ancestor of base
                
                String path = null;
                FileObject dir = currDir;
                int level = 0;
                do {
                    level++;
                    
                    dir = dir.getParent();
                    
                    if(dir == null) {
                        break;
                    }
                    
                    path = FileUtil.getRelativePath(dir, imageFolder);
                    
                    if(dir.equals(webModuleDocumentBase)) {
                        //we reached the web module document base boundary
                        //use relative path to this point
                        if(path == null) {
                            //the file is out of webmodule, copy it inside
                            FileUtil.copyFile(imageFO, currDir, imageFO.getName(), imageFO.getExt());
                            imageUrl = imageFO.getNameExt();
                            return true;
                        } //else just continue trying to find the relative path
                    }
                } while(path == null);
                
                if(path == null) {
                    //shoutldn't happen - a common path should always exist !?!?!
                    return false;
                }

                StringBuilder pathBuilder = new StringBuilder();
                pathBuilder.append(makePrefix(level));
                pathBuilder.append(path);
                if(path.length() > 0) {
                    pathBuilder.append('/');
                }
                pathBuilder.append(imageFO.getNameExt());
                imageUrl = encodeURL(pathBuilder.toString());
                
                retValue = true;

            }
        } catch (IOException exc) {
            LOGGER.log(Level.WARNING, null, exc);
        }
        return retValue;
    }

    private String makePrefix(int level) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < level; i++) {
            sb.append("../");
        }
        return sb.toString();
    }
    
    private String encodeURL(String imageUrl) {
        StringBuffer sb = new StringBuffer();
        int len = imageUrl.length();
        for (int i = 0; i < len; i++) {
            char chr = imageUrl.charAt(i);
            if (chr == ' ') {
                sb.append("%20");
            } else {
                sb.append(chr);
            }
        }
        return sb.toString();
    }

    public String getImageUrl() {
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

            if (i > 0 && i < s.length() - 1) {
                extension = s.substring(i + 1).toLowerCase();
            }

            if (extension != null) {
                if (extension.toLowerCase().equals("gif") || //NOI18N
                        extension.toLowerCase().equals("jpg") || //NOI18N
                        extension.toLowerCase().equals("png")) { //NOI18N
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
