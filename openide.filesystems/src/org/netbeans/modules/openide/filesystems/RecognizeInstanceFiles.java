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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.filesystems;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.openide.util.NamedServicesProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.Service;

/** Interface for core/startup to provide lookup overt system filesystem.
 *
 * @author Jaroslav Tulach
 */
@Service(value=NamedServicesProvider.class, position=500)
public final class RecognizeInstanceFiles extends NamedServicesProvider {
    private static final Logger LOG = Logger.getLogger(RecognizeInstanceFiles.class.getName());
    
    
    public Lookup create(String path) {
        return new OverFiles(path);
    }        
    
    
    private static final class OverFiles extends ProxyLookup 
    implements FileChangeListener {
        private final String path;
        private final FileChangeListener weakL;
        private final AbstractLookup.Content content;
        private final AbstractLookup lkp;
        
        public OverFiles(String path) {
            this(path, new ArrayList<FOItem>(), new AbstractLookup.Content());
        }

        private OverFiles(String path, List<FOItem> items, AbstractLookup.Content cnt) {
            this(path, items, new AbstractLookup(cnt), cnt);
        }
        
        private OverFiles(String path, List<FOItem> items, AbstractLookup lkp, AbstractLookup.Content cnt) {
            super(computeDelegates(path, items, lkp));
            this.path = path;
            this.lkp = lkp;
            this.content = cnt;
            this.content.setPairs(order(items));
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            this.weakL = FileUtil.weakFileChangeListener(this, fs);
            fs.addFileChangeListener(weakL);
        }
        
        private static List<FOItem> order(List<FOItem> items) {
            Map<FileObject,FOItem> m = new LinkedHashMap<FileObject,FOItem>();
            for (FOItem item : items) {
                m.put(item.fo, item);
            }
            List<FileObject> files = FileUtil.getOrder(m.keySet(), true);
            List<FOItem> r = new ArrayList<FOItem>(files.size());
            for (FileObject f : files) {
                r.add(m.get(f));
            }
            return r;
        }
        
        private void refresh(FileEvent ev) {
            if (!(ev.getFile().getPath() + "/").startsWith(path)) {
                return;
            }
            List<FOItem> items = new ArrayList<FOItem>();
            Lookup[] delegates = computeDelegates(path, items, lkp);
            this.content.setPairs(order(items));
            this.setLookups(delegates);
        }
        
        private static Lookup[] computeDelegates(String p, List<FOItem> items, Lookup lkp) {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(p);
            List<Lookup> delegates = new LinkedList<Lookup>();
            delegates.add(lkp);
            if (fo != null) {
                for (FileObject f : fo.getChildren()) {
                    if (f.isFolder()) {
                        delegates.add(new OverFiles(f.getPath()));
                    } else {
                        items.add(new FOItem(f));
                    }
                }

            }
            delegates.add(
                Lookups.metaInfServices(Thread.currentThread().getContextClassLoader(), "META-INF/namedservices/" + p) // NOI18N
            );
            return delegates.toArray(new Lookup[0]);
        }
    
        public void fileFolderCreated(FileEvent fe) {
            refresh(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            refresh(fe);
        }

        public void fileChanged(FileEvent fe) {
            refresh(fe);
        }

        public void fileDeleted(FileEvent fe) {
            refresh(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            refresh(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            refresh(fe);
        }
    } // end of OverFiles
    
    
    private static final class FOItem extends AbstractLookup.Pair<Object> {
        private static Reference<Object> EMPTY = new WeakReference<Object>(null);
        private FileObject fo;
        private Reference<Object> ref = EMPTY;
        
        public FOItem(FileObject fo) {
            this.fo = fo;
        }

        protected boolean instanceOf(Class<?> c) {
            Object r = ref.get();
            if (r != null) {
                return c.isInstance(r);
            } else {
                return c.isAssignableFrom(getType());
            }
        }

        protected boolean creatorOf(Object obj) {
            return ref.get() == obj;
        }

        public synchronized Object getInstance() {
            Object r = ref.get();
            if (r == null) {
                r = fo.getAttribute("instanceCreate");
                if (r == null) {
                    try {
                        Class<?> type = getType();
                        if (SharedClassObject.class.isAssignableFrom(type)) {
                            r = SharedClassObject.findObject(type.asSubclass(SharedClassObject.class), true);
                        } else {
                            r = type.newInstance();
                        }
                    } catch (InstantiationException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (r != null) {
                    ref = new WeakReference<Object>(r);
                }
            }
            return r;
        }

        public Class<? extends Object> getType() {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = FOItem.class.getClassLoader();
            }
            try {
                return Class.forName(getClassName(fo), false, l);
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.INFO, ex.getMessage(), ex);
                return Object.class;
            }
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

            hash = 11 * hash + (this.fo != null ? this.fo.hashCode()
                                                : 0);
            return hash;
        }

    } // end of FOItem
    
}
