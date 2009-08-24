
package org.netbeans.modules.javacard.project.deps;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 * Represents one "resolved" dependency which is mutable, and is able to
 * provide actual files for its various artifact kinds.
 *
 * @author Tim Boudreau
 */
public class ResolvedDependency {
    private Dependency dep;
    protected final DependenciesResolver resolver;
    private Map<ArtifactKind, String> initialPaths = new HashMap<ArtifactKind, String>();
    private Map<ArtifactKind, String> paths = new HashMap<ArtifactKind, String>();
    private final boolean initialized;
    public ResolvedDependency (Dependency dep, DependenciesResolver resolver) {
        this (dep, resolver, null);
    }

    public ResolvedDependency (Dependency dep, DependenciesResolver resolver, Map<ArtifactKind, String> m) {
        this.dep = dep;
        this.resolver = resolver;
        if (m == null) {
            for (ArtifactKind kind : dep.getKind().supportedArtifacts()) {
                initialPaths.put(kind, getOriginalPath(kind));
                paths.put (kind, getOriginalPath(kind));
            }
        } else {
            initialPaths.putAll(m);
            paths.putAll(m);
        }
        initialized = true;
    }

    public boolean isValid() {
        return resolver.isValid(getDependency());
    }

    Dependency dep() {
        return dep;
    }

    public boolean isProject() {
        return dep.getKind().isProjectDependency();
    }

    public FileObject resolve (ArtifactKind kind) {
        String path = getPath (kind);
        if (path == null) {
            return null;
        }
        return resolver.resolveArtifact(getDependency(), kind);
    }

    public String getPath(ArtifactKind kind) {
        String path = paths.get(kind);
        if (path == null) {
            return getOriginalPath(kind);
        } else {
            return path;
        }
    }

    public Path getAntPath(ArtifactKind kind) {
        return resolver.resolveAntPath(getPath(kind));
    }

    private String getOriginalPath(ArtifactKind kind) {
        if (!initialized) {
            File f = resolver.resolveFile(getDependency(), kind);
            return f == null ? null : f.getAbsolutePath();
        }
        return initialPaths.get(kind);
    }

    public void setPath (ArtifactKind kind, String path) {
        paths.put (kind, path);
    }

    public void reset(ArtifactKind kind) {
        paths.remove(kind);
    }

    public DependencyKind getKind() {
        return dep.getKind();
    }

    private DeploymentStrategy depStrategy;
    public DeploymentStrategy getDeploymentStrategy() {
        return depStrategy == null ? dep.getDeploymentStrategy() : depStrategy;
    }

    public void setDeploymentStrategy(DeploymentStrategy depStrategy) {
        this.depStrategy = depStrategy;
    }

    public boolean isModified() {
        boolean result = isPathsModified() || isDependencyModified();
        return result;
    }

    public boolean isPathsModified() {
        boolean result = !initialPaths.equals(paths);
        return result;
    }

    public boolean isDependencyModified() {
        return depStrategy != null && !depStrategy.equals(dep.getDeploymentStrategy());
    }

    public File resolveFile (ArtifactKind kind) {
        return resolver.resolveFile(getDependency(), kind);
    }

    public Set<ArtifactKind> getModifiedArtifactKinds() {
        Set<ArtifactKind> result = new HashSet<ArtifactKind>();
        for (Map.Entry<ArtifactKind, String> e : paths.entrySet()) {
            ArtifactKind a = e.getKey();
            String path = e.getValue();
            if (path != null && !path.equals(initialPaths.get(a))) {
                result.add (a);
            }
        }
        return result;
    }

    public Set<DeploymentStrategy> supportedDeploymentStrategies() {
        return getKind().supportedDeploymentStrategies();
    }

    public Set<ArtifactKind> supportedArtifactKinds() {
        return getKind().supportedArtifacts();
    }

    public Dependency getDependency() {
        if (isModified()) {
            return new Dependency(dep.getID(), dep.getKind(), getDeploymentStrategy());
        }
        return dep;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o.getClass() == ResolvedDependency.class)) {
            return false;
        }
        ResolvedDependency r = (ResolvedDependency) o;
        return getDependency().equals(r.getDependency());
    }

    @Override
    public int hashCode() {
        return getDependency().hashCode();
    }
}
