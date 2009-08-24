
package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.Image;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;

/**
 * A descriptor for a Dependency.  Retains the relevant information to create
 * a node for a ResolvedDependency without holding a reference to the
 * ResolvedDependency (which would keep all sorts of other things in memory).
 *
 * @author Tim Boudreau
 */
class DependencyDescriptor {
    private final Image icon;
    private final String path;
    private final String name;
    private final String id;
    private final boolean valid;
    ResolvedDependency rd;

    DependencyDescriptor(Image icon, String path, String name, String id, boolean valid) {
        this.icon = icon;
        this.path = path;
        this.name = name;
        this.valid = valid;
        this.id = id;
    }

    public Image getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isValid() {
        return valid;
    }
}
