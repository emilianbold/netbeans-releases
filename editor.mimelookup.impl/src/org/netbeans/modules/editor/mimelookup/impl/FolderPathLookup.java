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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author vita
 */
public final class FolderPathLookup extends AbstractLookup {
    
    private static final Logger LOG = Logger.getLogger(FolderPathLookup.class.getName());
    
    private final InstanceContent content;
    
    private final CompoundFolderChildren children;
    private final PCL listener = new PCL();

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
        List<FOItem> instanceFiles = new ArrayList<FOItem>();
        for (FileObject fo : children.getChildren()) {
            if (!fo.isValid()) {
                // Can happen after modules are disabled. Ignore it.
                continue;
            }
            instanceFiles.add(new FOItem(fo));
        }
        content.setPairs(instanceFiles);
    }
    
    private class PCL implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
    } // End of PCL class
    

    /**
     * Item for a single FileObject.
     */
    private static final class FOItem extends AbstractLookup.Pair<Object> {

        static final long serialVersionUID = 10L;
        
        private final FileObject fo;
        
        /** reference to created object */
        private transient Reference<Object> ref;

        /** Constructs new item. */
        public FOItem (FileObject fo) {
            this.fo = fo;
        }
        
        @Override
        protected boolean instanceOf(Class<?> c) {
            Reference<Object> refL = ref;
            Object inst = (refL != null) ? refL.get() : null;
            if (inst != null) {
                return c.isInstance(inst);
            } else {
                String instanceOf = (String) fo.getAttribute("instanceOf");
                if (instanceOf != null) {
                    for (String xface : instanceOf.split(",")) {
                        try {
                            if (c.isAssignableFrom(Class.forName(xface, false, loader()))) {
                                return true;
                            }
                        } catch (ClassNotFoundException x) {
                            // Not necessarily a problem, e.g. from org-netbeans-lib-commons_net-antlibrary.instance
                            LOG.log(Level.FINE, "could not load " + xface + " for " + fo.getPath(), x);
                        }
                    }
                    return false;
                } else {
                    return c.isAssignableFrom(getType());
                }
            }
        }

        protected boolean creatorOf(Object obj) {
            Reference<Object> refL = ref;
            return (refL != null) ? refL.get() == obj : false;
        }

        public synchronized Object getInstance() {
            Reference<Object> refL = ref;
            Object inst = (refL != null) ? refL.get() : null;
            if (inst == null) {
                inst = createInstanceFor(fo, Object.class);
                if (inst != null) {
                    ref = new WeakReference<Object>(inst);
                }
            }
            return inst;
        }

        public Class<? extends Object> getType() {
            Class<? extends Object> type = findTypeFor(fo);
            return type != null ? type : Void.class;
        }

        public String getId() {
            String s = fo.getPath();
            if (s.endsWith(".instance")) { // NOI18N
                s = s.substring(0, s.length() - ".instance".length());
            }
            return s;
        }

        public String getDisplayName() {
            String n = fo.getName();
            try {
                n = fo.getFileSystem().getStatus().annotateName(n, Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
            return n;
        }
        
        public @Override boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final FOItem other = (FOItem) obj;

            if (this.fo != other.fo &&
                (this.fo == null || !this.fo.equals(other.fo)))
                return false;
            return true;
        }

        public @Override int hashCode() {
            int hash = 3;
            hash = 11 * hash + (this.fo != null ? this.fo.hashCode() : 0);
            return hash;
        }

        private static ClassLoader loader() {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = FOItem.class.getClassLoader();
            }
            return l;
        }

        static <T> T createInstanceFor(FileObject f, Class<T> resultType) {
            Object inst = f.getAttribute("instanceCreate");
            if (inst == null) {
                try {
                    Class<?> type = findTypeFor(f);
                    if (type == null) {
                        return null;
                    }
                    if (SharedClassObject.class.isAssignableFrom(type)) {
                        inst = SharedClassObject.findObject(type.asSubclass(SharedClassObject.class), true);
                    } else {
                        inst = type.newInstance();
                    }
                } catch (InstantiationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return resultType.isInstance(inst) ? resultType.cast(inst) : null;
        }

        private static Class<? extends Object> findTypeFor(FileObject f) {
            String clazz = getClassName(f);
            if (clazz == null) {
                return null;
            }
            try {
                return Class.forName(clazz, false, loader());
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.FINE, ex.getMessage(), ex);
                return null;
            }
        }
        /** get class name from specified file object*/
        private static String getClassName(FileObject fo) {
            // first of all try "instanceClass" property of the primary file
            Object attr = fo.getAttribute ("instanceClass");
            if (attr instanceof String) {
                return Utilities.translate((String) attr);
            } else if (attr != null) {
                LOG.warning(
                    "instanceClass was a " + attr.getClass().getName()); // NOI18N
            }

            attr = fo.getAttribute("instanceCreate");
            if (attr != null) {
                return attr.getClass().getName();
            } else {
                Enumeration<String> attributes = fo.getAttributes();
                while (attributes.hasMoreElements()) {
                    if (attributes.nextElement().equals("instanceCreate")) {
                        // It was specified, just unloadable (usually a methodvalue).
                        return null;
                    }
                }
            }

            // otherwise extract the name from the filename
            String name = fo.getName ();

            int first = name.indexOf('[') + 1;
            if (first != 0) {
                LOG.log(Level.WARNING, "Cannot understand {0}", fo);
            }

            int last = name.indexOf (']');
            if (last < 0) {
                last = name.length ();
            }

            // take only a part of the string
            if (first < last) {
                name = name.substring (first, last);
            }

            name = name.replace ('-', '.');
            name = Utilities.translate(name);

            //System.out.println ("Original: " + getPrimaryFile ().getName () + " new one: " + name); // NOI18N
            return name;
        }

    }
        
}
