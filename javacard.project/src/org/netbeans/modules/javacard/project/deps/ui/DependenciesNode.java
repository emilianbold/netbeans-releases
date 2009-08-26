
package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class DependenciesNode extends AbstractNode {
    /**
     * Create a dependencies node.  Note, this constructor will not cause a
     * reference to dependent projects or their artifacts to be held, and is
     * for use in the explorer view.  For editing dependencies, use the other
     * constructor.
     * 
     * @param project
     */
    public DependenciesNode (JCProject project) {
        super (DependenciesChildren.createChildren(project), Lookups.singleton(project));
        init();
    }

    public DependenciesNode (JCProject project, ResolvedDependencies rd) {
        super (DependenciesChildren.createChildren(project, rd), Lookups.fixed(project, rd));
        init();
    }

    private void init() {
        setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LIBRARIES_NODE_NAME"));
    }

    @Override
    public Image getIcon(int ignored) {
        return ImageUtilities.loadImage ("org/netbeans/modules/javacard/resources/libraries.gif");
    }

    @Override
    public Image getOpenedIcon(int ignored) {
        return getIcon(ignored);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { new AddDependencyAction() };
    }
}
