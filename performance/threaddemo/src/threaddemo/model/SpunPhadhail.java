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

import java.awt.EventQueue;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import org.openide.util.Queue;
import spin.*;
import threaddemo.locking.Locks;

/**
 * An asynchronous Phadhail impl using Spin.
 * Spin does not appear to handle nested beans so we do that part manually.
 * We keep a dedicated single thread for running stuff in
 * (Spin's default behavior is to spawn a thread for every method call).
 * @author Jesse Glick
 */
final class SpunPhadhail extends Spin {
    
    private static final Starter starter = new Starter() {
        public void start(Runnable r) {
            Worker.start(r);
        }
    };
    
    private static final Map instances = new WeakHashMap(); // Map<Phadhail,Reference<Phadhail>>
    
    /** factory */
    public static Phadhail forPhadhail(Phadhail _ph) {
        assert EventQueue.isDispatchThread();
        Reference r = (Reference)instances.get(_ph);
        Phadhail ph = (r != null) ? (Phadhail)r.get() : null;
        if (ph == null) {
            Spin spin = new SpunPhadhail(_ph);
            ph = BufferedPhadhail.forPhadhail((Phadhail)spin.getProxy());
            instances.put(_ph, new WeakReference(ph));
        }
        return ph;
    }
    
    private final Phadhail ph;
    
    private SpunPhadhail(Phadhail ph) {
        super(ph, Spin.SPIN_OFF, starter);
        this.ph = ph;
    }
    
    /** overridden to recursively wrap phadhails */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String mname = method.getName();
        if (mname.equals("toString")) {
            return "SpunPhadhail<" + ph + ">";
        } else if (mname.equals("lock")) {
            return Locks.event();
        } else if (mname.equals("equals")) {
            return args[0] == ph ? Boolean.TRUE : Boolean.FALSE;
        } else if (mname.equals("hashCode")) {
            return new Integer(ph.hashCode());
        } else if (mname.endsWith("PhadhailListener")) {
            // Can do this synch - it's thread-safe and fast.
            assert args != null;
            assert args.length == 1;
            // Need to wrap this too!
            Spin spin = new SpunPhadhailListener((PhadhailListener)args[0], (Phadhail)proxy);
            PhadhailListener l = (PhadhailListener)spin.getProxy();
            if (mname.equals("addPhadhailListener")) {
                ph.addPhadhailListener(l);
            } else {
                assert mname.equals("removePhadhailListener") : mname;
                ph.removePhadhailListener(l);
            }
            return null;
        } else {
            assert EventQueue.isDispatchThread() : mname;
            Object result = super.invoke(proxy, method, args);
            if (result instanceof Phadhail) {
                return forPhadhail((Phadhail)result);
            } else if (result instanceof List) {
                // I.e. from getChildren(). Need to wrap result phadhails.
                List phs = (List)result; // List<Phadhail>
                return new SpunChildrenList(phs);
            } else {
                // Just pass on the call.
                return result;
            }
        }
    }
    
    private static final class SpunChildrenList extends AbstractList {
        private final List orig; // List<Phadhail>
        private final Phadhail[] kids;
        public SpunChildrenList(List orig) {
            this.orig = orig;
            kids = new Phadhail[orig.size()];
        }
        public Object get(int i) {
            assert EventQueue.isDispatchThread();
            if (kids[i] == null) {
                kids[i] = forPhadhail((Phadhail)orig.get(i));
            }
            return kids[i];
        }
        public int size() {
            assert EventQueue.isDispatchThread();
            return kids.length;
        }
    }
    
    private static final class SpunPhadhailListener extends Spin {
        
        private final PhadhailListener l;
        private final Phadhail ph;
        
        public SpunPhadhailListener(PhadhailListener l, Phadhail ph) {
            super(l, Spin.SPIN_OVER, starter);
            this.l = l;
            this.ph = ph;
        }
        
        /** overridden to translate PhadhailEvent's */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String mname = method.getName();
            if (mname.equals("toString")) {
                return "SpunPhadhailListener<" + l + ">";
            } else if (mname.equals("equals")) {
                return args[0] == l ? Boolean.TRUE : Boolean.FALSE;
            } else if (mname.equals("hashCode")) {
                return new Integer(l.hashCode());
            } else {
                assert mname.endsWith("Changed"): mname;
                assert EventQueue.isDispatchThread() : mname;
                assert args != null;
                assert args.length == 1;
                // Need to translate the original Phadhail event source to the proxy.
                if (mname.equals("childrenChanged")) {
                    PhadhailEvent orig = (PhadhailEvent)args[0];
                    args[0] = PhadhailEvent.create(ph);
                } else {
                    assert mname.equals("nameChanged");
                    PhadhailNameEvent orig = (PhadhailNameEvent)args[0];
                    args[0] = PhadhailNameEvent.create(ph, orig.getOldName(), orig.getNewName());
                }
                return super.invoke(proxy, method, args);
            }
        }
    
    }
    
}
