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

import java.lang.ref.*;
import java.lang.reflect.InvocationTargetException;
import org.openide.util.Mutex;

// XXX implement!

/**
 * Support for bidirectional construction of a derived model from an underlying model.
 * Based on a mutex which is assumed to control both models.
 * Handles all locking and scheduling associated with such a system.
 * It is possible to "nest" supports so that the derived model of one is the
 * underlying model of another - but they must still share a common mutex.
 *
 * <p>"Derive" means to take the underlying model (not represented explicitly here,
 * but assumed to be "owned" by the subclass) and produce the derived model;
 * typically this will involve parsing or the like. This operates in a read mutex.
 *
 * <p>"Recreate" means to take a new derived model (which may in fact be the same
 * as the old derived model but with different structure) and somehow change the
 * underlying model on that basis.
 *
 * <p>"Initiate" means to start derivation asynchronously, not waiting for the
 * result to be complete; this operation is idempotent, i.e. you can call it
 * whenever you think you might like the value later, but it will not cause
 * gratuitous extra derivations.
 *
 * <p>"Invalidate" means to signal that the underlying model has somehow changed
 * and that if there is any derived model it should be considered stale.
 * Invalidating when there is not yet any derived model is a no-op.
 *
 * <p>There are four different kinds of "values" which are employed by this class
 * and which you should be careful to differentiate:
 *
 * <ol>
 *
 * <li><p>The state of the underlying model. This is <em>not</em> explicitly modeled
 * by this class. Subclasses are expected to use that state as needed in
 * {@link #doDerive} and {@link #doRecreate}.
 *
 * <li><p>The state ("value") of the derived model. This is never null and is the
 * return value of {@link #doDerive}, {@link #doRecreate}, {@link #getValueBlocking},
 * {@link #getValueNonBlocking}, and {@link #getStaleValueNonBlocking} (except
 * where those methods are documented to return null), as well as the first
 * parameter to {@link #doRecreate} and {@link #doDerive} and the parameter to
 * {@link #createReference}.
 *
 * <li><p>Deltas in the underlying model. These may in fact be entire new copies
 * of an underlying model, or some diff-like structure, or an {@link java.util.EventObject},
 * etc. - whatever seems most convenient. These are never null and are the argument
 * type of {@link #invalidate} and the second argument type of {@link #doDerive}.
 *
 * <li><p>Deltas in the derived model. Again these may be of the same form as the
 * derived model itself - just replacing the model wholesale - or they may be some
 * kind of diff or event structure. These are again never null and are the argument
 * for {@link #mutate} and the second argument for {@link #doRecreate}.
 *
 * </ol>
 *
 * <p>Setting a new derived value explicitly always sets it immediately.
 * When getting the derived value, you have several choices. You can ask for the
 * exact value, if necessary waiting for it to be derived for the first time, or
 * rederived if it is stale. Or you can ask for the value if it is fresh or accept
 * null if it is missing or stale. Or you can ask for the value if it is fresh or
 * stale and accept null if it is missing. The latter two operations do not block
 * (except to get the read mutex) and so are valuable in views.
 *
 * <p>Derivation is started immediately after an initiate operation if there is
 * no derived model yet. If there is a model but it is stale and you ask to
 * initiate derivation, by default this also starts immediately, but you may
 * instead give a delay before the new derivation starts (assuming no one asks
 * for the exact derived value before then); this is useful for cases where
 * derivation is time-consuming (e.g. a complex parse) and for performance
 * reasons you wish to avoid triggering it too frivolously. For example, you may
 * be invalidating the derived model after every keystroke which changes a text
 * document, but would prefer to wait a few seconds before showing new results.
 *
 * <p>In case a recreate operation is attempted during a delay in which the model
 * is stale, or simply while a derivation is in progress with or without a preceding
 * delay, there is a conflict: the recreated model is probably a modification of
 * the old stale underlying model, and it is likely that setting it as the new derived
 * model and recreating the underlying model would clobber intermediate changes in the
 * underlying model, causing data loss. By default this support will signal an exception
 * if this is attempted, though subclasses may choose to suppress that and forcibly
 * set the new derived model and recreate the underlying model. Subclasses are better advised
 * to use the exception, and ensure that views of the derived model either handle
 * it gracefully (e.g. offering the user an opportunity to retry the modification
 * on the new derived model when it is available, or just beeping), or put the
 * derived view into a read-only mode temporarily while there is a stale underlying
 * model so that such a situation cannot arise.
 *
 * <p>There is a kind of "external clobbering" that can occur if the view does not
 * update itself promptly after a recreation (generally, after a change in the
 * derived model leading to a fresh value) but only with some kind of delay. In
 * that case an attempted change to the derived model may be working with obsolete
 * data. The support does <em>not</em> try to handle this case; the view is
 * responsible for detecting it and reacting appropriately.
 *
 * <p>Another kind of "clobbering" can occur in case the underlying model is not
 * completely controlled by the mutex. For example, it might the native filesystem,
 * which can change at any time without acquiring a lock in the JVM. In that case
 * an attempted mutation may be operating against a model derived from an older
 * state of the underlying model. Again, this support does <em>not</em> provide a
 * solution for this problem. Subclasses should attempt to detect such a condition
 * and recover from it gracefully, e.g. by throwing an exception from
 * <code>doRecreate</code> or by merging changes.
 *
 * <p>Derivation and recreation may throw checked exceptions. In such cases the
 * underlying and derived models should be left in a consistent state if at all
 * possible. If derivation throws an exception, the derived model will be considered
 * stale, but no attempt to rederive the model will be made unless the underlying
 * model is invalidated; subsequent calls to {@link #getValueBlocking} with the
 * same underlying model will result in the same exception being thrown repeatedly.
 * Views should generally put themselves into a read-only mode in this case.
 * If recreation throws an exception, this is propagated to {@link #mutate} but
 * otherwise nothing is changed.
 *
 * <p>You can attach a listener to this class. You will get an event when the
 * status of the support changes. All events are fired as soon as possible in the
 * read mutex.
 *
 * @author Jesse Glick
 */
public abstract class TwoWaySupport {
    
    private final Mutex m;
    
    /**
     * Create an uninitialized support.
     * No derivation or recreation is scheduled initially.
     * @param m the associated mutex
     */
    protected TwoWaySupport(Mutex m) {
        if (m == Mutex.EVENT) throw new IllegalArgumentException("Mutex.EVENT can deadlock TwoWaySupport!");
        this.m = m;
    }
    
    /**
     * Get the associated mutex.
     * @return the mutex
     */
    public Mutex getMutex() {
        return m;
    }
    
    /**
     * Compute the derived model from the underlying model.
     *
     * <p>This method is called with a read lock held on the mutex.
     * However for derived models with mutable state you may need to acquire an
     * additional simple lock (monitor) on some part of the model to refresh its
     * state - this is not a true write, but other readers should be locked out
     * until it is finished. For purely functional derived models that are
     * replaced wholesale, this is not necessary.
     *
     * <p>Note that derivations never run in parallel, even though they are in a
     * read mutex. In this implementation, all derivations in fact run in a dedicated
     * thread if they are invoked asynchronously using {@link #initiate}, but that
     * may change.
     *
     * <p>{@link TwoWayListener#derived} will be triggered after this method
     * completes. However, in the case of a derived model with internal
     * state with a complex relationship to the underlying model, it may not be
     * apparent from a {@link TwoWayEvent.Derived} what the changes to the derived
     * model were. Therefore, an implementation of this method may wish to fire
     * suitable changes to listeners on the derived model, rather than extracting
     * this information from the derived event.
     *
     * @param oldValue the old value of the derived model, or null if it had
     *                 never been calculated before
     * @param underlyingDelta a change in the underlying model, or null if no
     *                        particular change was signalled
     * @return the new value of the derived model (might be the same object as
     *         the old value)
     * @throws Exception (checked only!) if derivation of the model failed
     */
    protected abstract Object doDerive(Object oldValue, Object underlyingDelta) throws Exception;
    
    /**
     * Recreate the underlying model from the derived model.
     *
     * <p>This method is called with a write lock held on the mutex.
     *
     * <p>It is expected that any changes to the underlying model will be notified
     * to the relevant listeners within the dynamic scope of this method. Normally
     * an implementation will also notify changes to the derived model, unless that
     * has been done by other code already.
     *
     * @param oldValue the old value of the derived model, or null if it was
     *                 never derived
     * @param derivedDelta a change in the derived model
     * @return the new value of the derived model (might be the same object as
     *         the old value)
     * @throws Exception (checked only!) if recreation of the underlying model failed
     */
    protected abstract Object doRecreate(Object oldValue, Object derivedDelta) throws Exception;
    
    /**
     * Get the value of the derived model, blocking as needed until it is ready.
     * This method acquires the read mutex and may block further for
     * {@link #doDerive}.
     * @return the value of the derived model (never null)
     * @throws InvocationTargetException if <code>doDerive</code> was called
     *                                   and threw an exception (possibly from an
     *                                   earlier derivation run that is still broken)
     */
    public final Object getValueBlocking() throws InvocationTargetException {
        // XXX
        return null;
    }
    
    /**
     * Get the value of the derived model, if it is ready and fresh.
     * This method acquires the read mutex but otherwise does not block.
     * @return the value of the derived model, or null if it is stale or has never
     *         been computed at all
     */
    public final Object getValueNonBlocking() {
        // XXX
        return null;
    }
    
    /**
     * Get the value of the derived model, if it is ready (fresh or stale).
     * This method acquires the read mutex but otherwise does not block.
     * @return the value of the derived model, or null if it has never been
     *         computed at all
     */
    public final Object getStaleValueNonBlocking() {
        // XXX
        return null;
    }
    
    /**
     * Change the value of the derived model and correspondingly update the
     * underlying model.
     * <p>This method acquires the write mutex and calls {@link #doRecreate}
     * if it does not throw <code>ClobberException</code>.
     * @param derivedDelta a change to the derived model
     * @return the new value of the derived model
     * @throws ClobberException in case {@link #permitsClobbering} is false and
     *                          the old value of the derived model was stale or
     *                          missing
     * @throws InvocationTargetException if <code>doRecreate</code> throws an
     *                                   exception
     */
    public final Object mutate(Object derivedDelta) throws ClobberException, InvocationTargetException {
        if (derivedDelta == null) throw new NullPointerException();
        // XXX
        return null;
    }
    
    /**
     * Indicate that any current value of the derived model is invalid and
     * should no longer be used if exact results are desired.
     * <p>This method acquires the read mutex but does not block otherwise.
     * @param underlyingDelta a change to the underlying model
     */
    public final void invalidate(Object underlyingDelta) {
        if (underlyingDelta == null) throw new NullPointerException();
        // XXX
    }

    /**
     * Initiate creation of the derived model from the underlying model.
     * This is a no-op unless that process has not yet been started or if the
     * value of the derived model is already fresh and needs no rederivation.
     * <p>This method does not attempt to acquire the mutex nor does it block.
     */
    public final void initiate() {
        // XXX
    }
    
    /**
     * Add a listener to lifecycle changes in the support.
     * <p>A listener may be added multiple times and must be removed once
     * for each add.
     * <p>This method may be called from any thread and will not block.
     * @param l a listener to add
     */
    public final void addTwoWayListener(TwoWayListener l) {
        // XXX
    }
    
    /**
     * Add a listener to lifecycle changes in the support.
     * <p>This method may be called from any thread and will not block.
     * @param l a listener to remove
     */
    public final void removeTwoWayListener(TwoWayListener l) {
        // XXX
    }
    
    /**
     * Supply an optional delay before rederivation of a model after an invalidation.
     * If zero (the default), there is no intentional delay. The delay is irrelevant
     * in the case of {@link #getValueBlocking}.
     * @return a delay in milliseconds (>= 0)
     */
    protected long delay() {
        return 0L;
    }
    
    /**
     * Indicate whether this support permits changes to the derived model via
     * {@link #mutate} to "clobber" underived changes to the underlying model.
     * If false (the default), such attempts will throw {@link ClobberException}.
     * If true, they will be permitted, though a clobber event will be notified
     * rather than a recreate event.
     * @return true to permit clobbering, false to forbid it
     */
    protected boolean permitsClobbering() {
        return false;
    }
    
    /**
     * Create a reference to the derived model.
     * The support will only retain this reference (though event objects will
     * strongly refer to the derived model when appropriate).
     * If the referent is collected, the support returns to an underived state.
     *
     * <p>This implementation always creates a strong reference that will never
     * be collected so long as the support itself is not collected.
     * @param value a derived model object
     * @param q a reference queue supplied by the support
     * @return a reference to the model enqueued on that reference queue
     */
    protected Reference createReference(Object value, ReferenceQueue q) {
        // Does not matter what the queue is.
        return new StrongReference(value, q);
    }
    
    private static final class StrongReference extends WeakReference {
        private Object value;
        public StrongReference(Object value, ReferenceQueue q) {
            super(value, q);
            assert value != null;
            this.value = value;
        }
        public Object get() {
            return value;
        }
        public void clear() {
            super.clear();
            value = null;
        }
    }
    
}
