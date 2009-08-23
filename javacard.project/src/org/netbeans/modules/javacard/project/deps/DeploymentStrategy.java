/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.javacard.project.deps;

import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public enum DeploymentStrategy {
    ALREADY_ON_CARD,
    DEPLOY_TO_CARD,
    INCLUDE_IN_PROJECT_CLASSES,
    ;

    @Override
    public String toString() {
        return NbBundle.getMessage(DeploymentStrategy.class, name());
    }

    public String getDescription() {
        return NbBundle.getMessage(DeploymentStrategy.class, name() + ".desc");
    }
}
