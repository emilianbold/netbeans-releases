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

import java.util.*;
import org.openide.util.Mutex;

/**
 * Similar to DefaultPhadhail but all model methods are locked with a read mutex.
 * @author Jesse Glick
 */
final class LockedPhadhail implements Phadhail {
    
    private static final Mutex.Privileged PMUTEX = new Mutex.Privileged();
    private static final Mutex MUTEX = new Mutex(PMUTEX);
    
    private static final Map instances = new WeakHashMap(); // Map<Phadhail,Phadhail>
    
    /** factory */
    public static Phadhail forPhadhail(Phadhail _ph) {
        Phadhail ph = (Phadhail)instances.get(_ph);
        if (ph == null) {
            ph = new BufferedPhadhail(new LockedPhadhail(_ph));
            instances.put(_ph, ph);
        }
        return ph;
    }
    
    private final Phadhail ph;
    
    private LockedPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    public Phadhail[] getChildren() {
        Phadhail[] _phs;
        PMUTEX.enterReadAccess();
        try {
            _phs = ph.getChildren();
        } finally {
            PMUTEX.exitReadAccess();
        }
        Phadhail[] phs = new Phadhail[_phs.length];
        for (int i = 0; i < _phs.length; i++) {
            phs[i] = forPhadhail(_phs[i]);
        }
        return phs;
    }
    
    public String getDisplayName() {
        PMUTEX.enterReadAccess();
        try {
            return ph.getDisplayName();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public boolean hasChildren() {
        PMUTEX.enterReadAccess();
        try {
            return ph.hasChildren();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
}
