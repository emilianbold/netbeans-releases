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
    
    private SpunPhadhail(Phadhail ph) {
        super(ph, Spin.SPIN_OFF, starter);
    }
    
    /** overridden to recursively wrap phadhails */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("toString")) {
            return "SpunPhadhail<" + super.invoke(proxy, method, args) + ">";
        } else if (method.getName().equals("lock")) {
            return Locks.eventLock();
        } else {
            // XXX what about hashCode/equals? Should these be thread-safe?
            assert method.getName().endsWith("PhadhailListener") || EventQueue.isDispatchThread() : method.getName();
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof PhadhailListener) {
                        // Need to wrap these too!
                        Spin spin = new Spin(args[i], Spin.SPIN_OVER, starter);
                        // XXX should really be refiring different events...
                        args[i] = spin.getProxy();
                    }
                }
            }
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
    
}
