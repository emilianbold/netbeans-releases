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
 * Software is Sun Microsystems, Inc. 
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.settings;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import org.netbeans.modules.openide.util.NamedServicesProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Use FolderLookup to find out intances of named services.
 *
 * @author Jaroslav Tulach
 */
public final class RecognizeInstanceObjects extends NamedServicesProvider {
    private static final Logger LOG = Logger.getLogger(RecognizeInstanceObjects.class.getName());
    
    
    public Lookup create(String path) {
        return new OverObjects(path);
    }        
    
    
    private static final class OverObjects extends ProxyLookup 
    implements LookupListener {
        private static Lookup.Result<ClassLoader> CL = Lookup.getDefault().lookupResult(ClassLoader.class);
        
        private final String path;
        
        public OverObjects(String path) {
            super(delegates(path));
            this.path = path;
            CL.addLookupListener(WeakListeners.create(LookupListener.class, this, CL));
        }
        
        @SuppressWarnings("deprecation")
        private static Lookup[] delegates(String path) {
            Collection<? extends ClassLoader> allCL = CL.allInstances();
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                allCL = Collections.singleton(ccl);
            } else {
                if (allCL.isEmpty()) {
                    allCL = Collections.singleton(RecognizeInstanceObjects.class.getClassLoader());
                }
            }
            try {
                FileObject fo = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), path);
                
                String s;
                if (path.endsWith("/")) { // NOI18N
                    s = path.substring(0, path.length() - 1);
                } else {
                    s = path;
                }
                
                org.openide.loaders.FolderLookup l;
                l = new org.openide.loaders.FolderLookup(DataFolder.findFolder(fo), s);
                return new Lookup[] { l.getLookup(), Lookups.metaInfServices(allCL.iterator().next(), "META-INF/namedservices/" + path) }; // NOI18N
            } catch (IOException ex) {
                return new Lookup[] { Lookups.metaInfServices(allCL.iterator().next(), "META-INF/namedservices/" + path) }; // NOI18N
            }
            
        }
    
        public void resultChanged(LookupEvent ev) {
            setLookups(delegates(path));
        }
    } // end of OverObjects
}
