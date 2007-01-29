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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.localhistory;
        
import java.io.File;
import java.util.regex.Pattern;
import org.netbeans.modules.localhistory.store.LocalHistoryStore;
import org.netbeans.modules.localhistory.store.LocalHistoryStoreFactory;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/** 
 * 
 * A singleton Local Hisotry manager class, center of the Local History module. 
 * Use {@link #getInstance()} to get access to Local History module functionality.
 * @author Tomas Stupka
 */  
public class LocalHistory {    
      
    private static LocalHistory instance;
    private VCSInterceptor vcsInterceptor;
    private VCSAnnotator vcsAnnotator;
    private LocalHistoryStore store;

    private static String userDir;    
    
    public static synchronized LocalHistory getInstance() {
        if(instance == null) {
            instance = new LocalHistory();  
        }
        return instance;
    }
    
    VCSInterceptor getVCSInterceptor() {
        if(vcsInterceptor == null) {
            vcsInterceptor = new LocalHistoryVCSInterceptor();
        }
        return vcsInterceptor;
    }    
    
    VCSAnnotator getVCSAnnotator() {
        if(vcsAnnotator == null) {
            vcsAnnotator = new LocalHistoryVCSAnnotator();
        } 
        return vcsAnnotator;
    }    
    
    public LocalHistoryStore getLocalHistoryStore() {
        if(store == null) {
            store = LocalHistoryStoreFactory.getInstance().createLocalHistoryStorage();
        }
        return store;
    }   
    
    private String getUserDir() {
        if(userDir == null) {
            userDir = System.getProperty("netbeans.user");                      // NOI18N
        }
        return userDir;
    }
    
    File isManagedByParent(File file) {
        File parent = file.getParentFile();
        while(parent != null) {
            
            if(parent.getAbsolutePath().equals(getUserDir())) {
                // ignore userdir
                return null;
            }                        
            
            file = parent;
            parent = file.getParentFile();       
        }
        return file;    
    }
    
    boolean isManaged(File file) {        
        return isEditable(file) && file.length() <= LocalHistorySettings.getMaxFileSize();
    }
    
    private boolean isEditable(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if(fo == null) {
            return false;
        }         
        DataObject data = null;
        try {
            data = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            return false;
        }
        if(data == null) {
            return false;
        }        
        return data.getCookie(EditCookie.class) != null;       
    }
}
