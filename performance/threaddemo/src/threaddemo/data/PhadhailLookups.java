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

package threaddemo.data;

import java.lang.ref.*;
import java.util.*;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;
import threaddemo.locking.Lock;
import threaddemo.model.Phadhail;

// XXX this is inefficient - e.g. LookNode.getIcon will force the PhadhailLookup
// to be created! PhadhailLook should just ask for any special lookup items, e.g.
// the SaveCookie, otherwise return a simple list with the editor support. There
// should be a way to listen to any phadhails with one listener.

/**
 * Serves "cookies" for phadhails.
 * @author Jesse Glick
 */
public class PhadhailLookups {
    
    /** no instances */
    private PhadhailLookups() {}
    
    private static final Map lookups = new WeakHashMap(); // Map<Phadhail,Reference<PhadhailLookup>>
    
    // XXX rather than being synch, should be readAccess, and modified/saved should be writeAccess
    public static synchronized Lookup getLookup(Phadhail ph) {
        Reference r = (Reference)lookups.get(ph);
        Lookup l = (r != null) ? (Lookup)r.get() : null;
        if (l == null) {
            l = new PhadhailLookup(ph);
            lookups.put(ph, new WeakReference(l));
        }
        return l;
    }
    
    // Access from PhadhailEditorSupport
    static void modified(Phadhail ph, SaveCookie s) {
        ((PhadhailLookup)getLookup(ph)).modified(s);
    }
    
    static void saved(Phadhail ph, SaveCookie s) {
        ((PhadhailLookup)getLookup(ph)).saved(s);
    }

    // XXX #32203 would be really helpful here!
    private static final class PhadhailLookup extends AbstractLookup implements InstanceContent.Convertor {
        
        private static final Object KEY_EDITOR = "editor";
        private static final Object KEY_DOM_PROVIDER = "domProvider";
        
        private final Phadhail ph;
        // XXX Have to keep the InstanceContent separately; it is a field in AbstractLookup
        // but we cannot access it!
        private final InstanceContent c;
        private PhadhailEditorSupport ed = null;
        
        public PhadhailLookup(Phadhail ph) {
            this(ph, new InstanceContent());
        }
        
        private PhadhailLookup(Phadhail ph, InstanceContent c) {
            super(c);
            this.ph = ph;
            this.c = c;
        }
        
        protected void initialize() {
            if (!ph.hasChildren()) {
                c.add(KEY_EDITOR, this);
                if (ph.getName().endsWith(".xml")) {
                    c.add(KEY_DOM_PROVIDER, this);
                }
            }
            super.initialize();
        }
        
        public void modified(SaveCookie s) {
            c.add(s);
        }
        
        public void saved(SaveCookie s) {
            c.remove(s);
        }
        
        private PhadhailEditorSupport getEd() {
            if (ed == null) {
                ed = new PhadhailEditorSupport(ph);
            }
            return ed;
        }
        
        public Object convert(Object obj) {
            if (obj == KEY_EDITOR) {
                return getEd();
            } else {
                assert obj == KEY_DOM_PROVIDER;
                Lock m = ph.lock(); // XXX may need a different lock...
                return new DomSupport(ph, getEd(), m);
            }
        }
        
        public Class type(Object obj) {
            if (obj == KEY_EDITOR) {
                return PhadhailEditorSupport.class; // a bunch of interfaces
            } else {
                assert obj == KEY_DOM_PROVIDER;
                return DomProvider.class;
            }
        }
        
        public String displayName(Object obj) {
            throw new UnsupportedOperationException();
        }
        
        public String id(Object obj) {
            return "PhadhailLookup[" + ph + "," + obj + "]";
        }
        
    }
    
}
