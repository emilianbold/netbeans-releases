package org.netbeans.api.progress.transactional;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Container for arguments to, and return values from, parallelized
 * transactions.  See Transaction.createParallel().
 * <p/>
 * Represents a pair of values of potentially heterogenous types.
 * Access is thread-safe.
 *
 * @author Tim Boudreau
 * @param <A> The return type of the first transaction
 * @param <B> The return type of the second transaction
 */
public final class ParallelValue<A, B> {
    final AtomicReference<A> a = new AtomicReference<A>();
    final AtomicReference<B> b = new AtomicReference<B>();
    ParallelValue() {

    }

    /**
     * Create a new ParallelValue with the assigned values.
     * @param a The A value
     * @param b The B value
     */
    public ParallelValue (A a, B b) {
        this.a.set(a);
        this.b.set(b);
    }

    /**
     * Get the A value
     *
     * @return the type A value
     */
    public A a() {
        return a.get();
    }

    /**
     * Get the B value
     *
     * @return The type B value
     */
    public B b() {
        return b.get();
    }

    boolean isNull() {
        return a() == null && b() == null;
    }
}
