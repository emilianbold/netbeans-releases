/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.javacard.project.deps;

import org.openide.util.NbBundle;

/**
 * Describes what the build script should do with a given dependency.
 *
 * @author Tim Boudreau
 */
public enum DeploymentStrategy {
    //DO NOT REFACTOR!  These names are used in build-impl.xsl and project.xml
    ALREADY_ON_CARD,
    DEPLOY_TO_CARD,
    INCLUDE_IN_PROJECT_CLASSES,
    ;

    @Override
    public String toString() {
        return NbBundle.getMessage(DeploymentStrategy.class, name());
    }

    public String getDescription() {
        return NbBundle.getMessage(DeploymentStrategy.class, name() + ".desc"); //NOI18N
    }
}
