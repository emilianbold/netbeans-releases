
package org.netbeans.modules.javacard.project.deps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 * Represents the set of dependencies of a project, with all files and projects
 * pre-resolved.  This class can be used to modify and then save the dependencies
 * of the project (whereas the Dependencies class simply represents the content
 * of the project.xml, this class locates the actual files used).
 * <p/>
 * Do not construct instances of this object on the event thread, and do not
 * hold a reference to one any longer than needed.  For example, if creating
 * Nodes to represent instances of ResolvedDependency, use the ResolvedDependency
 * to get the icon, display name, and whatever else you need, and then throw
 * away the actual instance of ResolvedDependency.
 *
 * @author Tim Boudreau
 */
public abstract class ResolvedDependencies {
    private final List<ResolvedDependency> deps = Collections.synchronizedList(new ArrayList<ResolvedDependency>());
    private final Dependencies dependencies;
    protected final DependenciesResolver resolver;
    private final Dependencies origDependencies;
    private final ChangeSupport supp = new ChangeSupport(this);
    public ResolvedDependencies (Dependencies dependencies, DependenciesResolver resolver) {
        this.dependencies = dependencies.copy();
        origDependencies = dependencies;
        this.resolver = resolver;
        for (Dependency dep : dependencies.all()) {
            deps.add (new ResolvedDependency(dep, resolver));
        }
    }

    public final List<? extends ResolvedDependency> all() {
        //FIXME - somehow remove() is creating duplicates
        Set<String> ids = new HashSet<String>(deps.size());
        List<ResolvedDependency> result = new ArrayList<ResolvedDependency>();
        for (ResolvedDependency d : deps) {
            if (!ids.contains(d.dep().getID())) {
                result.add (d);
                ids.add (d.dep().getID());
            }
        }
        return result;
//        return Collections.unmodifiableList(deps);
    }

    public final ResolvedDependency add (Dependency d, Map <ArtifactKind, String> paths) {
        dependencies.add(d);
        ResolvedDependency r = new ResolvedDependency(d, resolver, paths);
        deps.add (r);
        supp.fireChange();
        return r;
    }

    public final void remove (ResolvedDependency r) {
        //Corrupted metadata can contain duplicate IDs, so perform the
        //removal by ID rather than just depending on being able to
        //remove the dependency from the list - it may be there more than once
        String id = r.dep().getID();
        removeById(id);
        deps.remove (r);
        supp.fireChange();
    }

    private void removeById (String id) {
        for (Iterator<ResolvedDependency> it = deps.iterator(); it.hasNext();) {
            ResolvedDependency d = it.next();
            if (d.getDependency().getID().equals(id)) {
                it.remove();
            }
        }
        for (Iterator<Dependency> it = new ArrayList<Dependency>(dependencies.all()).iterator(); it.hasNext();) {
            Dependency d = it.next();
            if (d.getID().equals(id)) {
                dependencies.remove(d);
            }
        }
    }

    public final Dependencies getDependencies() {
        Dependencies result = new Dependencies();
        for (ResolvedDependency r : deps) {
            result.add(r.getDependency());
        }
        return result;
    }

    public void removeChangeListener(ChangeListener listener) {
        supp.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        supp.addChangeListener(listener);
    }

    public final boolean isValid() {
        boolean result = true;
        for (ResolvedDependency r : deps) {
            if (!r.isValid()) {
                result = false;
                break;
            }
        }
        return result;
    }

    public final boolean moveUp (ResolvedDependency d) {
        int ix = deps.indexOf(d);
        assert ix >= 0;
        boolean result = ix > 0;
        if (result) {
            deps.remove (d);
            deps.add (ix -1, d);
            supp.fireChange();
        }
        return result;
    }

   public final boolean moveDown (ResolvedDependency d) {
        int ix = deps.indexOf(d);
        assert ix >= 0;
        boolean result = ix < deps.size() - 1;
        if (result) {
            deps.remove (d);
            deps.add (ix + 1, d);
            supp.fireChange();
        }
        return result;
    }

    public final boolean canMoveUp (ResolvedDependency d) {
        return !deps.isEmpty() && !d.equals(deps.get(0));
    }

    public final boolean canMoveDown(ResolvedDependency d) {
        return !deps.isEmpty() && !d.equals(deps.get(deps.size() - 1));
    }

    public final boolean isModified() {
        boolean result = !this.origDependencies.equals(getDependencies());
        if (!result) {
            for (ResolvedDependency r : deps) {
                result = r.isPathsModified() | r.dep().getDeploymentStrategy() != r.getDeploymentStrategy();
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    public final void save() throws IOException {
        if (!isModified()) {
            return;
        }
        doSave();
    }

    public ResolvedDependency get(String id) {
        for (ResolvedDependency d : deps) {
            if (id.equals(d.getDependency().getID())) {
                return d;
            }
        }
        return null;
    }

    protected Dependencies getOriginalDependencies() {
        return origDependencies.copy();
    }

    protected abstract void doSave() throws IOException;
}
