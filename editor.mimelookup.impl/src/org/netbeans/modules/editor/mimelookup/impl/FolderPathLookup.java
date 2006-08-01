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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author vita
 */
public final class FolderPathLookup extends AbstractLookup {
    
    private static final Logger LOG = Logger.getLogger(FolderPathLookup.class.getName());
    
    private InstanceContent content;
    
    private CompoundFolderChildren children;
    private PCL listener = new PCL();

    private final String LOCK = new String("InstanceProviderLookup.LOCK"); //NOI18N
    
    private final InstanceConvertor CONVERTOR = new InstanceConvertor();
    
    /** Creates a new instance of InstanceProviderLookup */
    public FolderPathLookup(String [] paths) {
        this(paths, new InstanceContent());
    }
    
    private FolderPathLookup(String [] paths, InstanceContent content) {
        super(content);
        
        this.content = content;
        
        this.children = new CompoundFolderChildren(paths, false);
        this.children.addPropertyChangeListener(listener);
        
        rebuild();
    }

    private void rebuild() {
        List files = children.getChildren();
        ArrayList instanceFiles = new ArrayList();
        
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            FileObject file = (FileObject) i.next();
            
            try {
                DataObject d = DataObject.find(file);
                InstanceCookie instanceCookie = (InstanceCookie) d.getCookie(InstanceCookie.class);
                if (instanceCookie != null) {
                    instanceFiles.add(file.getPath());
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't create DataObject", e); //NOI18N
            }
        }

//        System.out.println("Setting instanceFiles for FolderPathLookup@" + System.identityHashCode(this) + " {");
//        for (Iterator i = instanceFiles.iterator(); i.hasNext(); ) {
//            String filePath = (String) i.next();
//            System.out.println("    '" + filePath);
//        }
//        System.out.println("} End of Setting instanceFiles for FolderPathLookup@" + System.identityHashCode(this) + " -----------------------------");
        
        content.set(instanceFiles, CONVERTOR);
    }
    
    private class PCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
    } // End of PCL class
    
    private static final class InstanceConvertor implements InstanceContent.Convertor {
        private HashMap types = new HashMap();
        
        public Class type(Object filePath) {
            synchronized (types) {
                WeakReference ref = (WeakReference) types.get(filePath);
                Class type = ref == null ? null : (Class) ref.get();
                if (type == null) {
                    try {
                        type = getInstanceCookie(filePath).instanceClass();
                        types.put(filePath, new WeakReference(type));
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Can't determine instance class from '" + filePath + "'", e); //NOI18N
                        return DeadMarker.class; // Something nobody will ever find
                    }
                }
                
                return type;
            }
        }

        public String id(Object filePath) {
            return (String) filePath;
        }

        public String displayName(Object filePath) {
            try {
                return getInstanceCookie(filePath).instanceName();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't determine instance name from '" + filePath + "'", e); //NOI18N
                return DeadMarker.class.getName();
            }
        }

        public Object convert(Object filePath) {
            try {
                return getInstanceCookie(filePath).instanceCreate();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't create instance from '" + filePath + "'", e); //NOI18N
                return DeadMarker.THIS;
            }
        }
        
        private InstanceCookie getInstanceCookie(Object filePath) throws IOException {
            FileObject file = Repository.getDefault().getDefaultFileSystem().findResource((String) filePath);
            if (file == null) {
                // Should not occure
                throw new IOException("The file does not exist '" + filePath + "'"); //NOI18N
            }
            
            DataObject d = DataObject.find(file);
            InstanceCookie cookie = (InstanceCookie) d.getCookie(InstanceCookie.class);
            if (cookie != null) {
                return cookie;
            } else {
                // Should not occure
                throw new IOException("Can't find InstanceCookie for '" + filePath + "'"); //NOI18N
            }
        }
    } // End of InstanceConvertor class
    
    private static final class DeadMarker {
        public static final DeadMarker THIS = new DeadMarker();
    } // End of DeadMarker class
}
