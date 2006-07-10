/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.model;

import java.io.File;
import threaddemo.locking.RWLock;
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
    
    public RWLock lock() {
        return Locks.event();
    }
    
}
