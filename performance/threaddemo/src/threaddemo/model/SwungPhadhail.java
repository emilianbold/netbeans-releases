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
import java.util.*;
import javax.swing.SwingUtilities;
import org.openide.util.Mutex;

/**
 * Phadhail model impl using a technique like SwingWorker.
 * Fairly complicated. Methods are broken down into several categories:
 * 1. Simple methods like delete() will just block on the work thread.
 * 2. Ditto hasChildren(), but the result is cached.
 * 3. name + path return a dummy initial value and later fire a change.
 * 4. Ditto children, but then the results must be wrapped too.
 * 5. create* also wraps results.
 * 6. Listeners are added asynch and their callbacks must be posted back to AWT too.
 * For a more complex model, you could use Proxy to do this stuff, if there some kind
 * of map giving the desired thread behavior of each method.
 * @author Jesse Glick
 */
final class SwungPhadhail implements Phadhail, PhadhailListener {
    
    private static final Map instances = new WeakHashMap(); // Map<Phadhail,Phadhail>
    
    /** factory */
    public static Phadhail forPhadhail(Phadhail _ph) {
        Phadhail ph = (Phadhail)instances.get(_ph);
        if (ph == null) {
            ph = new SwungPhadhail(_ph);
            instances.put(_ph, ph);
        }
        return ph;
    }
    
    private final Phadhail ph;
    private String name = null;
    private String path = null;
    private boolean computingName = false;
    private List children = null; // List<Phadhail>
    private boolean computingChildren = false;
    private Boolean leaf = null;
    private List listeners = null; // List<PhadhailListener>
    
    private SwungPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    private void fireNameChanged() {
        if (listeners != null) {
            PhadhailNameEvent ev = PhadhailNameEvent.create(this, null, null);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                PhadhailListener l = (PhadhailListener)it.next();
                //System.err.println("fireNameChanged for " + this + " to " + l);
                l.nameChanged(ev);
            }
        }
    }
    
    private String getNameOrPath(boolean p) {
        if ((p ? path : name) != null) {
            //System.err.println("cached name for " + this);
            return (p ? path : name);
        } else {
            if (!computingName) {
                computingName = true;
                //System.err.println("calculating name for " + this);
                Worker.start(new Runnable() {
                    public void run() {
                        final String n = ph.getName();
                        final String p = ph.getPath();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                name = n;
                                path = p;
                                computingName = false;
                                //System.err.println("fireNameChanged for " + SwungPhadhail.this);
                                fireNameChanged();
                            }
                        });
                    }
                });
            }
            //System.err.println("dummy name for " + this);
            return (p ? "Please wait..." : "computingName");
        }
    }
    
    public String getName() {
        return getNameOrPath(false);
    }
    
    public String getPath() {
        return getNameOrPath(true);
    }
    
    private Phadhail createPhadhail(final String name, final boolean container) throws IOException {
        Phadhail orig;
        try {
            orig = (Phadhail)Worker.block(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    if (container) {
                        return ph.createContainerPhadhail(name);
                    } else {
                        return ph.createLeafPhadhail(name);
                    }
                }
            });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }
        return forPhadhail(orig);
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        return createPhadhail(name, true);
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        return createPhadhail(name, false);
    }
    
    public void rename(final String nue) throws IOException {
        try {
            Worker.block(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    ph.rename(nue);
                    return null;
                }
            });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }
    }
    
    public void delete() throws IOException {
        try {
            Worker.block(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    ph.delete();
                    return null;
                }
            });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }
    }
    
    private void fireChildrenChanged() {
        if (listeners != null) {
            //System.err.println("fireChildrenChanged");
            PhadhailEvent ev = PhadhailEvent.create(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((PhadhailListener)it.next()).childrenChanged(ev);
            }
        }
    }
    
    public List getChildren() {
        if (children != null) {
            //System.err.println("cached children");
            return children;
        } else {
            //System.err.println("no cached children");
            if (!computingChildren) {
                //System.err.println("computing children");
                computingChildren = true;
                Worker.start(new Runnable() {
                    public void run() {
                        //System.err.println("getting children");
                        final List ch = ph.getChildren();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                //System.err.println("posting children");
                                children = new SwungChildrenList(ch);
                                computingChildren = false;
                                fireChildrenChanged();
                            }
                        });
                    }
                });
            }
            return Collections.EMPTY_LIST;
        }
    }
    
    private static final class SwungChildrenList extends AbstractList {
        private final List orig; // List<Phadhail>
        private final Phadhail[] kids;
        public SwungChildrenList(List orig) {
            this.orig = orig;
            kids = new Phadhail[orig.size()];
        }
        public Object get(int i) {
            if (kids[i] == null) {
                kids[i] = forPhadhail((Phadhail)orig.get(i));
            }
             return kids[i];
        }
        public int size() {
            return kids.length;
        }
    }
    
    public InputStream getInputStream() throws IOException {
        try {
            return (InputStream)Worker.block(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    return ph.getInputStream();
                }
            });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        try {
            return (OutputStream)Worker.block(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    return ph.getOutputStream();
                }
            });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }
    }
    
    public boolean hasChildren() {
        //System.err.println("hasChildren on " + this);
        if (leaf == null) {
            //System.err.println("not cached");
            leaf = (Boolean)Worker.block(new Mutex.Action() {
                public Object run() {
                    //System.err.println("hasChildren: working...");
                    return ph.hasChildren() ? Boolean.FALSE : Boolean.TRUE;
                }
            });
            //System.err.println("leaf=" + leaf);
        }
        return !leaf.booleanValue();
    }
    
    public void addPhadhailListener(PhadhailListener l) {
        if (listeners == null) {
            listeners = new ArrayList();
            Worker.start(new Runnable() {
                public void run() {
                    ph.addPhadhailListener(SwungPhadhail.this);
                }
            });
        }
        listeners.add(l);
    }
    
    public void removePhadhailListener(PhadhailListener l) {
        if (listeners != null && listeners.remove(l) && listeners.isEmpty()) {
            listeners = null;
            Worker.start(new Runnable() {
                public void run() {
                    ph.removePhadhailListener(SwungPhadhail.this);
                }
            });
        }
    }
    
    public String toString() {
        return "SwungPhadhail<" + ph + ">";
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        // XXX should this go ahead and compute them now?
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                children = null;
                computingChildren = false; // XXX right?
                fireChildrenChanged();
            }
        });
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                name = null;
                path = null;
                computingName = false;
                fireNameChanged();
            }
        });
    }
    
}
