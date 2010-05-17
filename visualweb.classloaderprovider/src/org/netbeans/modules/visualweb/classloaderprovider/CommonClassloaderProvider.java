/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.visualweb.classloaderprovider;

import java.util.Properties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/**
 * This intreface is used to lookup a provider for Common Classloader
 * that is shared by the IDE implementation and the user's Project. The actual
 * implementation must have the specified capabilities.
 *
 * @author Sandip V. Chitale
 */
public interface CommonClassloaderProvider {
    public String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    public String J2EE_1_3 = J2eeModule.J2EE_13;
    public String J2EE_1_4 = J2eeModule.J2EE_14;
    public String JAVA_EE_5 = J2eeModule.JAVA_EE_5;

    /**
     * This is used to find out if this Designtime ClassLoader factory is able
     * to handle the specified capabilities. The first one that has capabilities
     * should return true. That is the one that will be used. Note: The order
     * depends on the order in which
     * <code>Lookup.getDefault().lookup(Lookup.Template)</code> return the
     * instances. Currently the Web Project's J2EE platform property is used as
     * a capability.
     */
    public boolean     isCapableOf(Properties capabilities);

    /**
     * The implementors should simply return their ClassLoader which will be
     * their modules classloader. The implemenotrs modules should declare
     * dependencies on the modules and/or library wrapper modules which are
     * shared by the IDE implementation and the user project.
     **/
    public ClassLoader getClassLoader();
}
