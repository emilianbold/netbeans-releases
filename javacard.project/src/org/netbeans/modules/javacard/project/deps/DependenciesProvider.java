
package org.netbeans.modules.javacard.project.deps;

import org.openide.util.Cancellable;

/**
 * Interface in the project lookup for its dependencies.  Uses a callback
 * mechanism to ensure project.xml and property resolving is done on a
 * background thread without holding any locks that could be problematic.
 *
 * @author Tim Boudreau
 */
public interface DependenciesProvider {

    public Cancellable requestDependencies (Receiver receiver);

    public interface Receiver {
        public void receive (ResolvedDependencies deps);
        public boolean failed (Throwable failure);
    }
}
