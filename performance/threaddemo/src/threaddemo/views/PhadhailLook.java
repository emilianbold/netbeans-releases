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

package threaddemo.views;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.ref.*;
import java.util.*;
import javax.swing.Action;
import javax.swing.event.*;
import org.netbeans.spi.looks.*;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import threaddemo.data.*;
import threaddemo.model.*;

/**
 * A look which wraps phadhails.
 * @author Jesse Glick
 */
final class PhadhailLook extends Look implements PhadhailListener, LookupListener, ChangeListener {
    
    private static final Map phadhails2Results = new HashMap(); // Map<Phadhail,Lookup.Result>
    private static final Map results2Phadhails = new HashMap(); // Map<Lookup.Result,Phadhail>
    private static final Map phadhails2DomProviders = new HashMap(); // Map<Phadhail,DomProvider>
    private static final Map domProviders2Phadhails = new HashMap(); // Map<DomProvider,Phadhail>
    
    PhadhailLook() {
        super("PhadhailLook");
    }
    
    public String getDisplayName() {
        return "Phadhails";
    }
    
    protected void attachTo(Object o) {
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
        Phadhail ph = (Phadhail)o;
        return !ph.hasChildren() &&
               PhadhailLookups.getLookup(ph).lookup(DomProvider.class) == null;
    }
    
    // XXX Look.getChildObjects should not be called off AWT, yet sometimes it is (by Children)
    private static final Map children = new WeakHashMap(); // Map<Phadhail,Reference<List<Object>>>
    public List getChildObjects(final Object o, Lookup e) {
        if (!EventQueue.isDispatchThread()) {
            // XXX see comment above... need to hack around threading of Children here
            // keeping a strong hash map does not work 100% - there may be a couple
            // entries left if the children are computed but never asked for
            Reference r = (Reference)children.remove(o);
            List l = (r != null) ? (List)r.get() : null;
            if (l != null) {
                return l;
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        children.put(o, new WeakReference(getChildObjects(o, null)));
                        fireChange(o, Look.GET_CHILD_OBJECTS);
                    }
                });
                return null;
            }
        }
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
                if (p.isValid()) {
                    // XXX do this block atomically in a lock?
                    try {
                        return Collections.singletonList(p.getDocument().getDocumentElement());
                    } catch (IOException x) {
                        assert false : x;
                    }
                } else {
                    // XXX call p.start() again?
                    System.err.println("DOM tree is invalid");
                }
            }
            return null;
        }
    }
    
    public String getName(Object o, Lookup e) {
        Phadhail ph = (Phadhail)o;
        return ph.getName();
    }

    public String getDisplayName(Object o, Lookup e) {
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
            SystemAction.get(ToolsAction.class),
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
        Phadhail ph = (Phadhail)o;
        Lookup.Result r = (Lookup.Result)phadhails2Results.get(ph);
        if (r == null) {
            r = PhadhailLookups.getLookup(ph).lookup(new Lookup.Template());
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
        Phadhail ph = (Phadhail)results2Phadhails.get(r);
        assert ph != null;
        fireChange(ph, Look.GET_LOOKUP_ITEMS);
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        assert ev.getPhadhail().mutex().canRead();
        fireChange(ev.getPhadhail(), Look.GET_CHILD_OBJECTS);
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        assert ev.getPhadhail().mutex().canRead();
        fireChange(ev.getPhadhail(), Look.GET_NAME);
        fireChange(ev.getPhadhail(), Look.GET_DISPLAY_NAME);
    }
    
    public void stateChanged(ChangeEvent e) {
        System.err.println("PL: got change");
        DomProvider p = (DomProvider)e.getSource();
        Phadhail ph = (Phadhail)domProviders2Phadhails.get(p);
        assert ph != null;
        fireChange(ph, Look.GET_CHILD_OBJECTS);
    }
    
}
