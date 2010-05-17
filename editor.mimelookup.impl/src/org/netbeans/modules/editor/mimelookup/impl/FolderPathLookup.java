/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Exceptions;
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

//    private final InstanceConvertor CONVERTOR = new InstanceConvertor();
    
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
        List<ICItem> instanceFiles = new ArrayList<ICItem>();
        
        for (FileObject file : children.getChildren()) {
            if (!file.isValid()) {
                // Can happen after modules are disabled. Ignore it.
                continue;
            }
            
            try {
                DataObject d = DataObject.find(file);
                InstanceCookie instanceCookie = d.getCookie(InstanceCookie.class);
                if (instanceCookie != null) {
                    instanceFiles.add(new ICItem(d, instanceCookie));
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
        
        content.setPairs(instanceFiles);
    }
    
    private class PCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
    } // End of PCL class
    
    
    // XXX: this is basically a copy of FolderLookup.ICItem, see #104705
    
    /** Item that delegates to <code>InstanceCookie</code>. Item which 
     * the internal lookup data structure is made from. */
    private static final class ICItem extends AbstractLookup.Pair {
        static final long serialVersionUID = 10L;
        
        static final ThreadLocal<ICItem> DANGEROUS = new ThreadLocal<ICItem> ();

        /** error manager for ICItem */
        private static final Logger ERR = Logger.getLogger(ICItem.class.getName());

        /** when deserialized only primary file is stored */
        private FileObject fo;
        
        private transient InstanceCookie ic;
        /** source data object */
        private transient DataObject dataObject;
        /** reference to created object */
        private transient Reference<Object> ref;

        /** Constructs new item. */
        public ICItem (DataObject obj, InstanceCookie ic) {
            this.ic = ic;
            this.dataObject = obj;
            this.fo = obj.getPrimaryFile();
            
            if (ERR.isLoggable(Level.FINE)) ERR.fine("New ICItem: " + obj); // NOI18N
        }
        
        /** Initializes the item
         */
        public void init () {
            if (ic != null) return;

            ICItem prev = DANGEROUS.get ();
            try {
                DANGEROUS.set (this);
                if (dataObject == null) {
                    try {
                        dataObject = DataObject.find(fo);
                    } catch (DataObjectNotFoundException donfe) {
                        ic = new BrokenInstance("No DataObject for " + fo.getPath(), donfe); // NOI18N
                        return;
                    }
                }

                ic = dataObject.getCookie (InstanceCookie.class);
                if (ic == null) {
                    ic = new BrokenInstance("No cookie for " + fo.getPath(), null); // NOI18N
                }
            } finally {
                DANGEROUS.set (prev);
            }
        }
            
        /**
         * Fake instance cookie.
         * Used in case a file had an instance in a previous session but now does not
         * (or the data object could not even be created correctly).
         */
        private static final class BrokenInstance implements InstanceCookie.Of {
            private final String message;
            private final Exception ex;
            public BrokenInstance(String message, Exception ex) {
                this.message = message;
                this.ex = ex;
            }
            public String instanceName() {
                return "java.lang.Object"; // NOI18N
            }
            private ClassNotFoundException die() {
                if (ex != null) {
                    return new ClassNotFoundException(message, ex);
                } else {
                    return new ClassNotFoundException(message);
                }
            }
            public Class instanceClass() throws IOException, ClassNotFoundException {
                throw die();
            }
            public Object instanceCreate() throws IOException, ClassNotFoundException {
                throw die();
            }
            public boolean instanceOf(Class type) {
                return false;
            }
        }


        /** The class of the result item.
         * @return the class of the item
         */
        protected boolean instanceOf (Class clazz) {
            init ();
            
            if (ERR.isLoggable(Level.FINE)) ERR.fine("instanceOf: " + clazz.getName() + " obj: " + dataObject); // NOI18N
            
            if (ic instanceof InstanceCookie.Of) {
                // special handling for special cookies
                InstanceCookie.Of of = (InstanceCookie.Of)ic;
                boolean res = of.instanceOf (clazz);
                if (ERR.isLoggable(Level.FINE)) ERR.fine("  of: " + res); // NOI18N
                return res;
            }

            // handling of normal instance cookies
            try {
                @SuppressWarnings("unchecked")
                boolean res = clazz.isAssignableFrom (ic.instanceClass ());
                if (ERR.isLoggable(Level.FINE)) ERR.fine("  plain: " + res); // NOI18N
                return res;
            } catch (ClassNotFoundException ex) {
                exception(ex, fo);
            } catch (IOException ex) {
                exception(ex, fo);
            }
            return false;
        }

        /** The class of the result item.
         * @return the instance of the object or null if it cannot be created
         */
        public Object getInstance() {
            init ();
            
            try {
                Object obj = ic.instanceCreate();
                if (ERR.isLoggable(Level.FINE)) ERR.fine("  getInstance: " + obj + " for " + this.dataObject); // NOI18N
                ref = new WeakReference<Object> (obj);
                return obj;
            } catch (ClassNotFoundException ex) {
                exception(ex, fo);
            } catch (IOException ex) {
                exception(ex, fo);
            }
            return null;
        }

        /** Hash code is the <code>InstanceCookie</code>'s code. */
        public @Override int hashCode () {
            init ();
            
            return System.identityHashCode (ic);
        }

        /** Two items are equal if they point to the same cookie. */
        public @Override boolean equals (Object obj) {
            if (obj instanceof ICItem) {
                ICItem i = (ICItem)obj;
                i.init ();
                init ();
                return ic == i.ic;
            }
            return false;
        }

        /** An identity of the item.
         * @return string representing the item, that can be used for
         *   persistance purposes to locate the same item next time */
        public String getId() {
            init ();

            if (dataObject == null) {
                // Deser problems.
                return "<broken: " + fo.getPath() + ">"; // NOI18N
            }
            
            return dataObject.getName();
        }

        /** Display name is extracted from name of the objects node. */
        public String getDisplayName () {
            init ();
            
            if (dataObject == null) {
                // Deser problems.
                return "<broken: " + fo.getPath() + ">"; // NOI18N
            }
            
            return dataObject.getNodeDelegate ().getDisplayName ();
        }

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *  as obj.
         */
        protected boolean creatorOf(Object obj) {
            Reference w = ref;
            if (w != null && w.get () == obj) {
                return true;
            }
            if (this.dataObject instanceof InstanceDataObject) {
                try {
                    Method m = InstanceDataObject.class.getDeclaredMethod("creatorOf", Object.class); //NOI18N
                    return (Boolean) m.invoke(this.dataObject, obj);
                } catch (Exception e) {
                    // ignore
                }
            }
            return false;
        }

        /** The class of this item.
         * @return the correct class
         */
        public Class getType() {
            init ();
            
            try {
                return ic.instanceClass ();
            } catch (IOException ex) {
                // ok, no class available
            } catch (ClassNotFoundException ex) {
                // ok, no class available
            }
            return Object.class;
        }

        private static void exception(Exception e, FileObject fo) {
            Exceptions.attachMessage(e, "Bad file: " + fo); // NOI18N
            LOG.log(Level.WARNING, null, e);
        }
    } // End of ICItem class.
    
//    private static final class InstanceConvertor implements InstanceContent.Convertor<String,Object> {
//        private Map<String,Reference<Class<?>>> types = new HashMap<String,Reference<Class<?>>>();
//        
//        public Class<?> type(String filePath) {
//            synchronized (types) {
//                Reference<Class<?>> ref = types.get(filePath);
//                Class<?> type = ref == null ? null : (Class) ref.get();
//                if (type == null) {
//                    try {
//                        type = getInstanceCookie(filePath).instanceClass();
//                        types.put(filePath, new WeakReference<Class<?>>(type));
//                    } catch (Exception e) {
//                        LOG.log(Level.WARNING, "Can't determine instance class from '" + filePath + "'", e); //NOI18N
//                        return DeadMarker.class; // Something nobody will ever find
//                    }
//                }
//                
//                return type;
//            }
//        }
//
//        public String id(String filePath) {
//            return filePath;
//        }
//
//        public String displayName(String filePath) {
//            try {
//                return getInstanceCookie(filePath).instanceName();
//            } catch (Exception e) {
//                LOG.log(Level.WARNING, "Can't determine instance name from '" + filePath + "'", e); //NOI18N
//                return DeadMarker.class.getName();
//            }
//        }
//
//        public Object convert(String filePath) {
//            try {
//                return getInstanceCookie(filePath).instanceCreate();
//            } catch (Exception e) {
//                LOG.log(Level.WARNING, "Can't create instance from '" + filePath + "'", e); //NOI18N
//                return DeadMarker.THIS;
//            }
//        }
//        
//        private InstanceCookie getInstanceCookie(String filePath) throws IOException {
//            FileObject file = FileUtil.getConfigFile(filePath);
//            if (file == null) {
//                // Should not occure
//                throw new IOException("The file does not exist '" + filePath + "'"); //NOI18N
//            }
//            
//            DataObject d = DataObject.find(file);
//            InstanceCookie cookie = d.getCookie(InstanceCookie.class);
//            if (cookie != null) {
//                return cookie;
//            } else {
//                // Should not occure
//                throw new IOException("Can't find InstanceCookie for '" + filePath + "'"); //NOI18N
//            }
//        }
//    } // End of InstanceConvertor class
//    
//    private static final class DeadMarker {
//        public static final DeadMarker THIS = new DeadMarker();
//    } // End of DeadMarker class
}
