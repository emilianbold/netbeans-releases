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

import java.io.File;
import threaddemo.locking.Lock;
import threaddemo.locking.Locks;

/**
 * Simple synchronous phadhail implementation that can only be used from the
 * event thread, like a Swing model.
 * (However as in Swing, listeners may be added or removed from any thread.)
 * @author Jesse Glick
 */
public class SynchPhadhail extends AbstractPhadhail {
    
    private static final Factory FACTORY = new Factory() {
        public AbstractPhadhail create(File f) {
            return new SynchPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private SynchPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public Lock lock() {
        return Locks.eventLock();
    }
    
}
