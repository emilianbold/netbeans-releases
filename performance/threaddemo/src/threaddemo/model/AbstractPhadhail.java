/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.model;

import java.io.*;
import java.lang.ref.*;
import java.util.*;
import org.openide.util.Mutex;

/**
 * A convenience skeleton for making a phadhail based on files.
 * Supplies the actual file operations, and handles caching and
 * event firing and so on. Subclasses supply specialized threading
 * behaviors (this class is not thread-safe, except for adding and
 * removing listeners).
 * @author Jesse Glick
 */
public abstract class AbstractPhadhail implements Phadhail {
    
    private static final Map instances = new WeakHashMap(); // Map<Factory,Map<File,Reference<AbstractPhadhail>>>
    
    protected interface Factory {
        AbstractPhadhail create(File f);
    }
    
    private static Map instancesForFactory(Factory y) { // Map<File,Reference<AbstractPhadhail>>
        assert Thread.holdsLock(AbstractPhadhail.class);
        Map instances2 = (Map)instances.get(y);
        if (instances2 == null) {
            instances2 = new WeakHashMap();
            instances.put(y, instances2);
        }
        return instances2;
    }
    
    /** factory */
    protected static synchronized AbstractPhadhail forFile(File f, Factory y) {
        Map instances2 = instancesForFactory(y);
        Reference r = (Reference)instances2.get(f);
        AbstractPhadhail ph = (r != null) ? (AbstractPhadhail)r.get() : null;
        if (ph == null) {
            // XXX could also yield lock while calling create, but don't bother
            ph = y.create(f);
            instances2.put(f, new WeakReference(ph));
        }
        return ph;
    }
    
    private File f;
    private List listeners = null; // List<PhadhailListener>
    private Reference kids; // Reference<List<Phadhail>>
    private static boolean firing = false;
    
    protected AbstractPhadhail(File f) {
        this.f = f;
    }

    /** factory to create new instances of this class; should be a constant */
    protected abstract Factory factory();
    
    public List getChildren() {
        assert mutex().canRead();
        List phs = null; // List<Phadhail>
        if (kids != null) {
            phs = (List)kids.get();
        }
        if (phs == null) {
            // Need to (re)calculate the children.
            File[] fs = f.listFiles();
            if (fs != null) {
                Arrays.sort(fs);
                phs = new ChildrenList(fs);
            } else {
                phs =  Collections.EMPTY_LIST;
            }
            kids = new WeakReference(phs);
        }
        return phs;
    }
    
    private final class ChildrenList extends AbstractList {
        private final File[] files;
        private final Phadhail[] kids;
        public ChildrenList(File[] files) {
            this.files = files;
            kids = new Phadhail[files.length];
        }
        // These methods need not be called with the read mutex held
        // (see Phadhail.getChildren Javadoc).
        public Object get(int i) {
            Phadhail ph = kids[i];
            if (ph == null) {
                ph = forFile(files[i], factory());
            }
            return ph;
        }
        public int size() {
            return files.length;
        }
    }
    
    public String getName() {
        assert mutex().canRead();
        return f.getName();
    }
    
    public String getPath() {
        assert mutex().canRead();
        return f.getAbsolutePath();
    }
    
    public boolean hasChildren() {
        assert mutex().canRead();
        return f.isDirectory();
    }
    
    /**
     * add/removePhadhailListener must be called serially
     */
    private static final Object LISTENER_LOCK = new String("LP.LL");
    
    public final void addPhadhailListener(PhadhailListener l) {
        synchronized (LISTENER_LOCK) {
            if (listeners == null) {
                listeners = new ArrayList();
            }
            listeners.add(l);
        }
    }
    
    public final void removePhadhailListener(PhadhailListener l) {
        synchronized (LISTENER_LOCK) {
            if (listeners != null) {
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    listeners = null;
                }
            }
        }
    }
    
    private final PhadhailListener[] listeners() {
        synchronized (LISTENER_LOCK) {
            if (listeners != null) {
                return (PhadhailListener[])listeners.toArray(new PhadhailListener[listeners.size()]);
            } else {
                return null;
            }
        }
    }
    
    protected final void fireChildrenChanged() {
        final PhadhailListener[] l = listeners();
        if (l != null) {
            mutex().readAccess(new Mutex.Action() {
                public Object run() {
                    firing = true;
                    try {
                        PhadhailEvent ev = PhadhailEvent.create(AbstractPhadhail.this);
                        for (int i = 0; i < l.length; i++) {
                            l[i].childrenChanged(ev);
                        }
                    } finally {
                        firing = false;
                    }
                    return null;
                }
            });
        }
    }
    
    protected final void fireNameChanged(final String oldName, final String newName) {
        final PhadhailListener[] l = listeners();
        if (l != null) {
            mutex().readAccess(new Mutex.Action() {
                public Object run() {
                    firing = true;
                    try {
                        PhadhailNameEvent ev = PhadhailNameEvent.create(AbstractPhadhail.this, oldName, newName);
                        for (int i = 0; i < l.length; i++) {
                            l[i].nameChanged(ev);
                        }
                    } finally {
                        firing = false;
                    }
                    return null;
                }
            });
        }
    }
    
    public void rename(String nue) throws IOException {
        assert mutex().canWrite();
        assert !firing : "Mutation within listener callback";
        String oldName = getName();
        if (oldName.equals(nue)) {
            return;
        }
        File newFile = new File(f.getParentFile(), nue);
        if (!f.renameTo(newFile)) {
            throw new IOException("Renaming " + f + " to " + nue);
        }
        File oldFile = f;
        f = newFile;
        synchronized (AbstractPhadhail.class) {
            Map instances2 = instancesForFactory(factory());
            instances2.remove(oldFile);
            instances2.put(newFile, new WeakReference(this));
        }
        fireNameChanged(oldName, nue);
        if (hasChildren()) {
            // Fire changes in path of children too.
            List recChildren = new ArrayList(100); // List<AbstractPhadhail>
            String prefix = oldFile.getAbsolutePath() + File.separatorChar;
            synchronized (AbstractPhadhail.class) {
                Iterator it = instancesForFactory(factory()).values().iterator();
                while (it.hasNext()) {
                    AbstractPhadhail ph = (AbstractPhadhail)((Reference)it.next()).get();
                    if (ph != null && ph != this && ph.getPath().startsWith(prefix)) {
                        recChildren.add(ph);
                    }
                }
            }
            // Do the notification after traversing the instances map, since
            // we cannot mutate the map while an iterator is active.
            Iterator it = recChildren.iterator();
            while (it.hasNext()) {
                ((AbstractPhadhail)it.next()).parentRenamed(oldFile, newFile);
            }
        }
    }
    
    /**
     * Called when some parent dir has been renamed, and our name
     * needs to change as well.
     */
    private void parentRenamed(File oldParent, File newParent) {
        String prefix = newParent.getAbsolutePath();
        String suffix = f.getAbsolutePath().substring(oldParent.getAbsolutePath().length());
        File oldFile = f;
        f = new File(prefix + suffix);
        synchronized (AbstractPhadhail.class) {
            Map instances2 = instancesForFactory(factory());
            instances2.remove(oldFile);
            instances2.put(f, new WeakReference(this));
        }
        fireNameChanged(null, null);
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        assert mutex().canWrite();
        assert !firing : "Mutation within listener callback";
        File child = new File(f, name);
        if (!child.mkdir()) {
            throw new IOException("Creating dir " + child);
        }
        fireChildrenChanged();
        return forFile(child, factory());
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        assert mutex().canWrite();
        assert !firing : "Mutation within listener callback";
        File child = new File(f, name);
        if (!child.createNewFile()) {
            throw new IOException("Creating file " + child);
        }
        fireChildrenChanged();
        return forFile(child, factory());
    }
    
    public void delete() throws IOException {
        assert mutex().canWrite();
        assert !firing : "Mutation within listener callback";
        if (!f.delete()) {
            throw new IOException("Deleting file " + f);
        }
        forFile(f.getParentFile(), factory()).fireChildrenChanged();
    }
    
    public InputStream getInputStream() throws IOException {
        assert mutex().canRead();
        return new FileInputStream(f);
    }
    
    public OutputStream getOutputStream() throws IOException {
        // Yes, read access - for the sake of the demo, currently Phadhail.getOutputStream
        // is not considered a mutator method (fires no changes); this would be different
        // if PhadhailListener included a content change event.
        // That would be trickier because then you would need to acquire the write mutex
        // when opening the stream but release it when closing the stream (*not* when
        // returning it to the caller).
        assert mutex().canRead();
        return new FileOutputStream(f);
    }
    
    public String toString() {
        String clazz = getClass().getName();
        int i = clazz.lastIndexOf('.');
        return clazz.substring(i + 1) + "<" + f + ">";
    }

    public abstract Mutex mutex();
    
}
