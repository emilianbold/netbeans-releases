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

package threaddemo.util;

import org.openide.util.Mutex;

// XXX full Javadoc
// XXX implement!

/**
 * Support for bidirectional construction of a derived model from an original model.
 * Based on a mutex which is assumed to control both models.
 * Handles all locking and scheduling associated with such a system.
 * <p>"Derive" means to take the original model (not represented explicitly here,
 * but assumed to be "owned" by the subclass) and produce the derived model;
 * typically this will involve parsing or the like. This operates in a read mutex.
 * <p>"Recreate" means to take a new derived model (which may in fact be the same
 * as the old derived model but with different structure) and somehow change the
 * original model on that basis. If the old and new derived model are the same
 * object, you may pass an additional "delta" argument that captures the nature
 * of the change; this might be an EventObject or something similar.
 * <p>"Initiate" means to start derivation asynchronously, not waiting for the
 * result to be complete; this operation is idempotent, i.e. you can call it
 * whenever you think you might like the value later, but it will not cause
 * gratuitous extra derivations.
 * <p>"Invalidate" means to signal that the original model has somehow changed
 * and that if there is any derived model it should be considered stale.
 * Invalidating when there is not yet any derived model is a no-op.
 * <p>Setting a new derived value explicitly always sets it immediately.
 * When getting the derived value, you have several choices. You can ask for the
 * exact value, if necessary waiting for it to be derived for the first time, or
 * rederived if it is stale. Or you can ask for the value if it is fresh or accept
 * null if it is missing or stale. Or you can ask for the value if it is fresh or
 * stale and accept null if it is missing. The latter two operations do not block
 * (except to get the read mutex) and so are valuable in views.
 * <p>Derivation is started immediately after an initiate operation if there is
 * no derived model yet. If there is a model but it is stale and you ask to
 * initiate derivation, by default this also starts immediately, but you may
 * instead give a delay before the new derivation starts (assuming no one asks
 * for the exact derived value before then); this is useful for cases where
 * derivation is time-consuming (e.g. a complex parse) and for performance
 * reasons you wish to avoid triggering it too frivolously. For example, you may
 * be invalidating the derived model after every keystroke which changes a text
 * document, but would prefer to wait a few seconds before showing new results.
 * <p>In case a recreate operation is attempted during a delay in which the model
 * is stale, or simply while a derivation is in progress with or without a preceding
 * delay, there is a conflict: the recreated model is probably a modification of
 * the old stale original model, and it is likely that setting it as the new derived
 * model and recreating the original model would clobber intermediate changes in the
 * original model, causing data loss. By default this support will signal an exception
 * if this is attempted, though subclasses may choose to suppress that and forcibly
 * set the new derived model and recreate the original. Subclasses are better advised
 * to use the exception, and ensure that views of the derived model either handle
 * it gracefully (e.g. offering the user an opportunity to retry the modification
 * on the new derived model when it is available, or just beeping), or put the
 * derived view into a read-only mode temporarily while there is a stale original
 * model so that such a situation cannot arise.
 * <p>You can attach a listener to this class. You will get an event when the
 * status of the support changes. All events are fired as soon as possible in the
 * read mutex.
 * @author Jesse Glick
 */
public abstract class TwoWaySupport {
    
    private final Mutex m;
    
    /**
     * Create an uninitialized support.
     * No derivation or recreation is scheduled initially.
     */
    protected TwoWaySupport(Mutex m) {
        if (m == Mutex.EVENT) throw new IllegalArgumentException("Mutex.EVENT can deadlock TwoWaySupport!");
        this.m = m;
    }
    
    protected abstract Object doDerive(Object delta);
    
    protected abstract void doRecreate(Object oldDerived, Object newDerived, Object delta);
    
    public final Object getValueBlocking() {
        // XXX
        return null;
    }
    
    public final Object getValueNonBlocking() {
        // XXX
        return null;
    }
    
    public final Object getStaleValueNonBlocking() {
        // XXX
        return null;
    }
    
    public final void setValue(Object derived, Object delta) throws ClobberException {
        // XXX
    }
    
    public final void invalidate(Object delta) {
        // XXX
    }
    
    public final void initiate() {
        // XXX
    }
    
    public final void addTwoWayListener(TwoWayListener l) {
        // XXX
    }
    
    public final void removeTwoWayListener(TwoWayListener l) {
        // XXX
    }
    
    protected long delay() {
        return 0L;
    }
    
    protected boolean permitsClobbering() {
        return false;
    }
    
}
