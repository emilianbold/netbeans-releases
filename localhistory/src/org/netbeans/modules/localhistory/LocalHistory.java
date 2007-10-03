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
package org.netbeans.modules.localhistory;

import java.io.File;
import org.netbeans.modules.localhistory.store.LocalHistoryStore;
import org.netbeans.modules.localhistory.store.LocalHistoryStoreFactory;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;

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
    
    public final static Object EVENT_FILE_CREATED = new Object();
    
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
        if(Diagnostics.ON) {
            Diagnostics.println(".isManaged() " + file);
        }
        return true;
    }

    private ListenersSupport listenerSupport = new ListenersSupport(this);
    
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }

    void fireFileEvent(Object id, File file) {
        listenerSupport.fireVersioningEvent(id, new Object[]{file});
    }    
}
