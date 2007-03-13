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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * BackgroundImageUrlDialog.java
 * Created on November 5, 2004, 5:08 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.Utilities;
import org.netbeans.modules.css.visual.model.CssMetaModel;
import java.awt.Component;
import java.io.File;
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
    
    private String imageUrl = null;
    private FileFilter imgFilter = new ImageFilter() ;
    
    // XXX Get URL also not just local file
    public boolean show(Component parent){
        boolean retValue = false;
        JFileChooser fileChooser = Utilities.getJFileChooser();
        File currDir = null;
        try{
            FileObject fo = CssMetaModel.getDataObject().getPrimaryFile();
            currDir = FileUtil.toFile(fo).getParentFile();
            if (currDir == null) currDir = new File(System.getProperty("user.home")); //NOI18N
            if (currDir != null ) fileChooser.setCurrentDirectory(currDir);
            fileChooser.addChoosableFileFilter(new ImageFilter()) ;
            if ( fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                File imageFile = fileChooser.getSelectedFile();
                File newImageFile = new File(currDir, imageFile.getName());
                Utilities.copyFile(imageFile, newImageFile);
                imageUrl = imageFile.getName();
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
        } catch (Exception exc){
            exc.printStackTrace();
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