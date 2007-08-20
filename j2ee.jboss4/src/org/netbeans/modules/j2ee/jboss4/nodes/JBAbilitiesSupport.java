/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.jboss4.nodes;

import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.openide.util.Lookup;

/**
 * This class is helper for holding some lazy initialized values that were
 * copy-pasted in several class in previous version of the code. Namely
 * {@link JBEarApplicationsChildren}, {@link JBEjbModulesChildren} and
 * {@link JBWebApplicationsChildren}.
 *
 * @author Petr Hejl
 */
class JBAbilitiesSupport {

    private final Lookup lookup;

    private Boolean remoteManagementSupported = null;

    private Boolean isJB4x = null;

    /**
     * Constructs the JBAbilitiesSupport.
     *
     * @param lookup Lookup that will be asked for {@link JBDeploymentManager} if
     * necessary
     */
    public JBAbilitiesSupport(Lookup lookup) {
        assert lookup != null;
        this.lookup = lookup;
    }

    /**
     * Returns true if the JBoss has installed remote management package.
     *
     * @return true if the JBoss has installed remote management package,
     *             false otherwise
     * @see Util.isRemoteManagementSupported(Lookup)
     */
    public boolean isRemoteManagementSupported() {
        if (remoteManagementSupported == null) {
            remoteManagementSupported = Util.isRemoteManagementSupported(lookup);
        }
        return remoteManagementSupported;
    }

    /**
     * Returns true if the version of the JBoss is 4. Check is based on directory
     * layout.
     *
     * @return true if the version of the JBoss is 4, false otherwise
     * @see JBPluginUtils.isGoodJBServerLocation4x(JBDeploymentManager)
     */
    public boolean isJB4x() {
        if (isJB4x == null) {
            JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
            isJB4x = JBPluginUtils.isGoodJBServerLocation4x(dm);
        }
        return isJB4x;
    }

}
