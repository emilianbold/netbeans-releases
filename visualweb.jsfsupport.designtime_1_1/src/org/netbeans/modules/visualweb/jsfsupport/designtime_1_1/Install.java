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

package org.netbeans.modules.visualweb.jsfsupport.designtime_1_1;


import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openide.modules.ModuleInstall;


/**
 * Module installer that updates BeanInfo search path with packages in this
 * module that contain BeanInfo classes. This is necessary since the BeanInfo
 * classes are in different packages that the beans.
 *
 * @author Peter Zavadsky
 * @author gjmurphy
 */
public class Install extends ModuleInstall {

    /** Packages in this module that contain BeanInfo classes. */
    private static final String[] BEANINFO_PATHS = {
        "org.netbeans.modules.visualweb.faces.dt.component", // NOI18N
        "org.netbeans.modules.visualweb.faces.dt.component.html", // NOI18N
        "org.netbeans.modules.visualweb.faces.dt_1_1.component", // NOI18N
        "org.netbeans.modules.visualweb.faces.dt_1_1.component.html" // NOI18N
    };


    public void restored() {
        // Add our beaninfo packages to introspector search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        String[] newSP = new String[sp.length + BEANINFO_PATHS.length];
        System.arraycopy(sp, 0, newSP, 0, sp.length);
        System.arraycopy(BEANINFO_PATHS, 0, newSP, sp.length, BEANINFO_PATHS.length);
        Introspector.setBeanInfoSearchPath(newSP);
    }

    public void uninstalled() {
        // Remove our beaninfo packages from the introspector search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        List paths = Arrays.asList(sp);
        for (int i = 0; i < BEANINFO_PATHS.length; i++) {
            if(paths.contains(BEANINFO_PATHS[i])) {
                paths = new ArrayList(paths);
                paths.remove(BEANINFO_PATHS[i]);
            }
        }
        Introspector.setBeanInfoSearchPath((String[])paths.toArray(new String[paths.size()]));
    }

}
