/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.views;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.looks.Look;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.OpenAction;
import org.openide.actions.RenameAction;
import org.openide.actions.SaveAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import threaddemo.data.DomProvider;
import threaddemo.data.PhadhailLookups;
import threaddemo.data.PhadhailNewType;
import threaddemo.locking.Locks;
import threaddemo.model.Phadhail;
import threaddemo.model.PhadhailEvent;
import threaddemo.model.PhadhailListener;
import threaddemo.model.PhadhailNameEvent;

/**
 * A look which wraps phadhails.
 * @author Jesse Glick
 */
final class PhadhailLook extends Look implements PhadhailListener, LookupListener, ChangeListener {
    
    private static final Logger logger = Logger.getLogger(PhadhailLook.class.getName());
    
    private static final Map phadhails2Results = new IdentityHashMap(); // Map<Phadhail,Lookup.Result>
    private static final Map results2Phadhails = new IdentityHashMap(); // Map<Lookup.Result,Phadhail>
    private static final Map phadhails2DomProviders = new IdentityHashMap(); // Map<Phadhail,DomProvider>
    private static final Map domProviders2Phadhails = new IdentityHashMap(); // Map<DomProvider,Phadhail>
    
    PhadhailLook() {
        super("PhadhailLook");
    }
    
    public String getDisplayName() {
        return "Phadhails";
    }
    
    protected void attachTo(Object o) {
        assert o instanceof Phadhail : o;
        Phadhail ph = (Phadhail)o;
        ph.addPhadhailListener(this);
    }
    
    protected void detachFrom(Object o) {
        Phadhail ph = (Phadhail)o;
        ph.removePhadhailListener(this);
        Lookup.Result r = (Lookup.Result)phadhails2Results.remove(ph);
        if (r != null) {
            r.removeLookupListener(this);
            assert results2Phadhails.containsKey(r);
            results2Phadhails.remove(r);
        }
        DomProvider p = (DomProvider)phadhails2DomProviders.remove(ph);
        if (p != null) {
            p.removeChangeListener(this);
            assert domProviders2Phadhails.containsKey(p);
            domProviders2Phadhails.remove(p);
        }
    }
    
    public boolean isLeaf(Object o, Lookup e) {
        assert EventQueue.isDispatchThread();
        Phadhail ph = (Phadhail)o;
        return !ph.hasChildren() && PhadhailLookups.getLookup(ph).lookup(DomProvider.class) == null;
    }
    
    public List getChildObjects(final Object o, Lookup e) {
        assert EventQueue.isDispatchThread();
        Phadhail ph = (Phadhail)o;
        if (ph.hasChildren()) {
            return ph.getChildren();
        } else {
            DomProvider p = (DomProvider)PhadhailLookups.getLookup(ph).lookup(DomProvider.class);
            if (p != null) {
                if (!phadhails2DomProviders.containsKey(ph)) {
                    phadhails2DomProviders.put(ph, p);
                    assert !domProviders2Phadhails.containsKey(p);
                    domProviders2Phadhails.put(p, ph);
                    p.addChangeListener(this);
                    p.start();
                }
                if (p.isReady()) {
                    logger.finer("DOM tree is ready, will ask for its document element");
                    // XXX do this block atomically in a lock?
                    try {
                        return Collections.singletonList(p.getDocument().getDocumentElement());
                    } catch (IOException x) {
                        assert false : x;
                    }
                } else {
                    logger.finer("DOM tree is not ready");
                    p.start();
                    // Cf. PhadhailLookSelector.StringLook:
                    return Collections.singletonList("Please wait...");
                }
            }
            return null;
        }
    }
    
    public String getName(Object o, Lookup e) {
        assert EventQueue.isDispatchThread();
        Phadhail ph = (Phadhail)o;
        return ph.getName();
    }

    public String getDisplayName(Object o, Lookup e) {
        assert EventQueue.isDispatchThread();
        Phadhail ph = (Phadhail)o;
        return ph.getPath();
    }
    
    public boolean canRename(Object o, Lookup e) {
        return true;
    }
    
    public void rename(Object o, String newName, Lookup e) throws IOException {
        Phadhail ph = (Phadhail)o;
        ph.rename(newName);
    }
    
    public boolean canDestroy(Object o, Lookup e) {
        return true;
    }
    
    public void destroy(Object o, Lookup e) throws IOException {
        Phadhail ph = (Phadhail)o;
        ph.delete();
        // XXX since this fires no changes of its own...
        fireChange(ph, Look.DESTROY);
    }
    
    public Action[] getActions(Object o, Lookup e) {
        return new Action[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(SaveAction.class),
            null,
            SystemAction.get(NewAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            //SystemAction.get(ToolsAction.class),
        };
    }
    
    public NewType[] getNewTypes(Object o, Lookup e) {
        Phadhail ph = (Phadhail)o;
        if (ph.hasChildren()) {
            return new NewType[] {
                new PhadhailNewType(ph, false),
                new PhadhailNewType(ph, true),
            };
        } else {
            return new NewType[0];
        }
    }
    
    public Collection getLookupItems(Object o, Lookup env) {
        assert EventQueue.isDispatchThread();
        Phadhail ph = (Phadhail)o;
        Lookup.Result r = (Lookup.Result)phadhails2Results.get(ph);
        if (r == null) {
            Lookup l = PhadhailLookups.getLookup(ph);
            r = l.lookup(new Lookup.Template(Object.class));
            assert r != null : "Null lookup from " + l + " in " + ph;
            phadhails2Results.put(ph, r);
            assert !results2Phadhails.containsKey(r);
            results2Phadhails.put(r, ph);
            r.addLookupListener(this);
        }
        return r.allItems();
    }
    
    public void resultChanged(LookupEvent ev) {
        // XXX #33372: should be able to do ev.getResult()
        Lookup.Result r = (Lookup.Result)ev.getSource();
        final Phadhail ph = (Phadhail)results2Phadhails.get(r);
        assert ph != null;
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ph, Look.GET_LOOKUP_ITEMS);
            }
        });
    }
    
    public void childrenChanged(final PhadhailEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ev.getPhadhail(), Look.GET_CHILD_OBJECTS);
            }
        });
    }
    
    public void nameChanged(final PhadhailNameEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ev.getPhadhail(), Look.GET_NAME | Look.GET_DISPLAY_NAME);
            }
        });
    }
    
    public void stateChanged(ChangeEvent e) {
        logger.finer("got change");
        DomProvider p = (DomProvider)e.getSource();
        final Phadhail ph = (Phadhail)domProviders2Phadhails.get(p);
        assert ph != null;
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ph, Look.GET_CHILD_OBJECTS);
            }
        });
    }
    
}
