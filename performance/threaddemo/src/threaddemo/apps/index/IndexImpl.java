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

package threaddemo.apps.index;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.WeakListeners;
import org.w3c.dom.*;
import threaddemo.data.DomProvider;
import threaddemo.data.PhadhailLookups;
import threaddemo.locking.Lock;
import threaddemo.model.*;

// XXX make an IndexImpl be GCable and not hold onto Phadhail's

/**
 * Actual implementation of the index.
 * @author Jesse Glick
 */
final class IndexImpl implements Index, Runnable, PhadhailListener, ChangeListener {
    
    private static final Logger logger = Logger.getLogger(IndexImpl.class.getName());
    
    private final Phadhail root;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private boolean running = false;
    private final LinkedList/*<Phadhail>*/ toProcess = new LinkedList();
    private final Map/*<Phadhail,Map<String,int>>*/ processed = new /*Weak*/HashMap();
    private final Map/*<DomProvider,Phadhail>*/ domProviders2Phadhails = new WeakHashMap();
    private final Map/*<Phadhail,Phadhail>*/ phadhails2Parents = new WeakHashMap();
    
    public IndexImpl(Phadhail root) {
        this.root = root;
    }
    
    public Lock getLock() {
        return root.lock();
    }
    
    public Map getData() {
        assert getLock().canRead();
        Map/*<String,int>*/ data = (Map)processed.get(root);
        if (data != null) {
            return Collections.unmodifiableMap(data);
        } else {
            return Collections.EMPTY_MAP;
        }
    }
    
    public Phadhail getRoot() {
        return root;
    }
    
    public void addChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChange() {
        ChangeListener[] ls;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            ls = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < ls.length; i++) {
            ls[i].stateChanged(ev);
        }
    }
    
    public void start() {
        synchronized (toProcess) {
            if (!running) {
                toProcess.add(root);
                Thread t = new Thread(this, "IndexImpl parsing: " + root);
                t.setDaemon(true);
                t.start();
                running = true;
            }
        }
    }
    
    public void cancel() {
        synchronized (toProcess) {
            running = false;
        }
    }
    
    public void run() {
        while (true) {
            final Phadhail next;
            synchronized (toProcess) {
                if (!running) {
                    break;
                }
                while (toProcess.isEmpty()) {
                    try {
                        toProcess.wait();
                    } catch (InterruptedException e) {
                        assert false : e;
                    }
                }
                next = (Phadhail)toProcess.removeFirst();
            }
            process(next);
        }
    }
    
    private void process(final Phadhail ph) {
        getLock().read(new Runnable() {
            public void run() {
                if (processed.containsKey(ph)) {
                    // Already computed, do nothing.
                    return;
                }
                if (ph.hasChildren()) {
                    processChildren(ph);
                } else {
                    // Data, maybe.
                    final Map computed = compute(ph);
                    getLock().writeLater(new Runnable() {
                        public void run() {
                            processed.put(ph, computed);
                            if (!computed.isEmpty()) {
                                bubble(ph);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private void processChildren(Phadhail ph) {
        Iterator/*<Phadhail>*/ kids = ph.getChildren().iterator();
        synchronized (toProcess) {
            while (kids.hasNext()) {
                Phadhail kid = (Phadhail)kids.next();
                phadhails2Parents.put(kid, ph);
                if (!toProcess.contains(kid)) {
                    toProcess.add(kid);
                }
            }
            toProcess.notify();
        }
        // XXX use WeakListener instead? ... not if Index is long-lived though
        ph.removePhadhailListener(this);
        ph.addPhadhailListener(this);
    }
    
    private int count;
    private Map compute(Phadhail ph) {
        assert getLock().canRead();
        assert !ph.hasChildren();
        logger.log(Level.FINER, "Computing index for {0} [#{1}]", new Object[] {ph, new Integer(++count)});
        DomProvider p = (DomProvider)domProviders2Phadhails.get(ph);
        if (p == null) {
            // XXX technically should listen to lookup changes...
            p = (DomProvider)PhadhailLookups.getLookup(ph).lookup(DomProvider.class);
            if (p == null) {
                logger.finer("no DomProvider here");
                return Collections.EMPTY_MAP;
            }
            domProviders2Phadhails.put(p, ph);
        }
        Document d;
        try {
            d = p.getDocument();
        } catch (IOException e) {
            logger.log(Level.FINE, "Parsing failed for {0}: {1}", new Object[] {ph.getName(), e.getMessage()});
            return Collections.EMPTY_MAP;
        }
        // Wait till after p.getDocument(), since that will fire stateChanged
        // the first time it is called (not ready -> ready)
        p.addChangeListener(WeakListeners.change(this, p));
        Map/*<String,int>*/ m = new HashMap();
        NodeList l = d.getElementsByTagName("*");
        for (int i = 0; i < l.getLength(); i++) {
            String name = ((Element)l.item(i)).getTagName();
            Integer x = (Integer)m.get(name);
            if (x == null) {
                m.put(name, new Integer(1));
            } else {
                m.put(name, new Integer(x.intValue() + 1));
            }
        }
        logger.log(Level.FINER, "Parse succeeded for {0}", ph);
        logger.log(Level.FINEST, "Parse results: {0}", m);
        return m;
    }
    
    private void bubble(Phadhail ph) {
        assert getLock().canWrite();
        logger.log(Level.FINER, "bubble: {0} data size: {1}", new Object[] {ph, new Integer(processed.size())});
        logger.log(Level.FINEST, "bubble: {0} data: {1}", new Object[] {ph, processed});
        if (ph == root) {
            getLock().read(new Runnable() {
                public void run() {
                    fireChange();
                }
            });
        } else {
            Phadhail parent = (Phadhail)phadhails2Parents.get(ph);
            assert parent != null : ph;
            assert parent.hasChildren();
            Iterator/*<Phadhail>*/ kids = parent.getChildren().iterator();
            Map/*<String,int>*/ recalc = new HashMap();
            while (kids.hasNext()) {
                Phadhail kid = (Phadhail)kids.next();
                Map/*<String,int>*/ subdata = (Map)processed.get(kid);
                if (subdata == null) {
                    // OK, kid is simply not yet calculated, will bubble changes later.
                    continue;
                }
                Iterator it = subdata.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry)it.next();
                    String name = (String)e.getKey();
                    Integer x1 = (Integer)e.getValue();
                    if (recalc.containsKey(name)) {
                        Integer x2 = (Integer)recalc.get(name);
                        recalc.put(name, new Integer(x1.intValue() + x2.intValue()));
                    } else {
                        recalc.put(name, x1);
                    }
                }
            }
            processed.put(parent, recalc);
            bubble(parent);
        }
    }
    
    private void invalidate(final Phadhail ph) {
        getLock().writeLater(new Runnable() {
            public void run() {
                processed.remove(ph);
                synchronized (toProcess) {
                    if (!toProcess.contains(ph)) {
                        toProcess.add(ph);
                        toProcess.notify();
                    }
                }
            }
        });
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        Phadhail ph = ev.getPhadhail();
        logger.log(Level.FINER, "childrenChanged: {0}", ph);
        invalidate(ph);
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        // ignore
    }
    
    public void stateChanged(ChangeEvent e) {
        DomProvider p = (DomProvider)e.getSource();
        Phadhail ph = (Phadhail)domProviders2Phadhails.get(p);
        assert ph != null;
        logger.log(Level.FINER, "stateChanged: {0}", ph);
        invalidate(ph);
    }
    
}
