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
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wizard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public class SamlCallbackCreator {

   public static final String HOK = "hok";
   public static final String SV = "sv";
   
   public static final String SAML11 = "11";
   public static final String SAML20 = "20";
   
    public SamlCallbackCreator() { }
     
    public DataObject generateSamlCBHandler(FileObject targetFolder, String targetName, String type, String version) {
        try {
            DataFolder folder = (DataFolder) DataObject.find(targetFolder);
            FileObject fo = null;
            fo = Repository.getDefault().getDefaultFileSystem().findResource("Templates/WebServices/Saml" + version + type + ".java"); // NOI18N
            if (fo != null) {
                DataObject template = DataObject.find(fo);
                DataObject obj = template.createFromTemplate(folder, targetName);            
                return obj;
            }
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
        
        return null;
    }
    
}
