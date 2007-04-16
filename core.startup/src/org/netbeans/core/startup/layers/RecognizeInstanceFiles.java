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

package org.netbeans.core.startup.layers;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Interface for core/startup to provide lookup overt system filesystem.
 *
 * @author Jaroslav Tulach
 */
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
            try {
                items = Utilities.topologicalSort(items, new OrderAttribsMap(items));
            } catch (TopologicalSortException ex) {
                @SuppressWarnings("unchecked")
                List<FOItem> l = (List<FOItem>)ex.partialSort();
                items = l;
            }
            this.content.setPairs(items);
            
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            this.weakL = FileUtil.weakFileChangeListener(this, fs);
            fs.addFileChangeListener(weakL);
        }
        
        private void refresh() {
            List<FOItem> items = new ArrayList<FOItem>();
            Lookup[] delegates = computeDelegates(path, items, lkp);
        
            try {
                items = Utilities.topologicalSort(items, new OrderAttribsMap(items));
            } catch (TopologicalSortException ex) {
                @SuppressWarnings("unchecked")
                List<FOItem> l = (List<FOItem>)ex.partialSort();
                items = l;
            }
            
            this.content.setPairs(items);
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
            refresh();
        }

        public void fileDataCreated(FileEvent fe) {
            refresh();
        }

        public void fileChanged(FileEvent fe) {
            refresh();
        }

        public void fileDeleted(FileEvent fe) {
            refresh();
        }

        public void fileRenamed(FileRenameEvent fe) {
            refresh();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            refresh();
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

        public boolean equals(Object obj) {
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

        public int hashCode() {
            int hash = 3;

            hash = 11 * hash + (this.fo != null ? this.fo.hashCode()
                                                : 0);
            return hash;
        }

    } // end of FOItem
    
    private static final class OrderAttribsMap implements Map<FOItem,List<FOItem>> {
        private final List<FOItem> all;

        public OrderAttribsMap(List<FOItem> all) {
            this.all = all;
        }
        
        public int size() {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public List<FOItem> get(Object key) {
            if (key instanceof FOItem) {
                FOItem fi = (FOItem)key;
                FileObject folder = fi.fo.getParent();
                if (folder == null) {
                    return null;
                }
                Set<String> afterMeNames = new HashSet<String>();
                for (Enumeration<String> it = folder.getAttributes(); it.hasMoreElements();) {
                    String attr = it.nextElement();
                    int slash = attr.indexOf('/');
                    if (slash == -1) {
                        continue;
                    }
                    if (
                        fi.fo.getNameExt().equals(attr.substring(0, slash)) &&
                        Boolean.TRUE.equals(folder.getAttribute(attr))
                    ) {
                        afterMeNames.add(attr.substring(slash + 1));
                    }
                }
                if (afterMeNames.isEmpty()) {
                    return null;
                }
                
                List<FOItem> afterMe = new ArrayList<FOItem>();
                for (FOItem foItem : all) {
                    if (afterMeNames.contains(foItem.fo.getNameExt())) {
                        afterMe.add(foItem);
                    }
                }
                return afterMe;
            }
            return null;
        }

        public List<FOItem> put(FOItem key,
                                List<FOItem> value) {
            throw new UnsupportedOperationException();
        }

        public List<FOItem> remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map<? extends FOItem, ? extends List<FOItem>> t) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public Set<FOItem> keySet() {
            throw new UnsupportedOperationException();
        }

        public Collection<List<FOItem>> values() {
            throw new UnsupportedOperationException();
        }

        public Set<Entry<FOItem, List<FOItem>>> entrySet() {
            throw new UnsupportedOperationException();
        }
        
    }
}
