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

import java.lang.reflect.*;
import java.util.*;
import org.openide.util.Queue;
import spin.*;

/**
 * An asynchronous Phadhail impl using Spin.
 * Spin does not appear to handle nested beans so we do that part manually.
 * We keep a dedicated single thread for running stuff in
 * (Spin's default behavior is to spawn a thread for every method call).
 * @author Jesse Glick
 */
final class SpunPhadhail extends Spin {
    
    /**
     * Could also use RequestProcessor but that seems to have a fair amount
     * of overhead, esp. logging every task when in standalone mode.
     * This is simpler and adequate for the purpose.
     */
    private static final class Worker extends Thread implements Starter {
        
        private final Queue tasks = new Queue();
        
        public Worker() {
            start();
        }
        
        public void run() {
            while (true) {
                Runnable next = (Runnable)tasks.get();
                next.run();
            }
        }
        
        public void start(Runnable run) {
            tasks.put(run);
        }
        
    }
    
    private static final Starter starter = new Worker();
    
    private static final Map instances = new WeakHashMap(); // Map<Phadhail,Phadhail>
    
    /** factory */
    public static Phadhail forPhadhail(Phadhail _ph) {
        Phadhail ph = (Phadhail)instances.get(_ph);
        if (ph == null) {
            Spin spin = new SpunPhadhail(_ph);
            ph = BufferedPhadhail.forPhadhail((Phadhail)spin.getProxy());
            instances.put(_ph, ph);
        }
        return ph;
    }
    
    private SpunPhadhail(Phadhail ph) {
        super(ph, Spin.SPIN_OFF, starter);
    }
    
    /** overridden to recursively wrap phadhails */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = super.invoke(proxy, method, args);
        if (result instanceof Phadhail) {
            return forPhadhail((Phadhail)result);
        } else if (result instanceof List) {
            // I.e. from getChildren(). Need to wrap result phadhails.
            List phs = (List)result; // List<Phadhail>
            return new SpunChildrenList(phs);
        } else if (method.getName().equals("toString")) {
            return "SpunPhadhail<" + result + ">";
        } else {
            // Just pass on the call.
            return result;
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
            if (kids[i] == null) {
                kids[i] = forPhadhail((Phadhail)orig.get(i));
            }
             return kids[i];
        }
        public int size() {
            return kids.length;
        }
    }
    
}
