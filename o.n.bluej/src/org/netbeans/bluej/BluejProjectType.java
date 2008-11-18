/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.bluej;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Factory for simple bluej based projects.
 * @author Milos Kleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.support.ant.AntBasedProjectType.class)
public final class BluejProjectType implements AntBasedProjectType {
    
    public static final String TYPE = "org.netbeans.bluej.bluejproject"; // NOI18N
    private static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/bluej-project/1"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/bluej-project-private/1"; // NOI18N
    
    private List weakList = new ArrayList();
    /** Do nothing, just a service. */
    public BluejProjectType() {}
    
    public String getType() {
        return TYPE;
    }
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        Iterator it = weakList.iterator();
        while (it.hasNext()) {
            WeakReference ref = (WeakReference) it.next();
            Project elem = (Project)ref.get();
            if (elem != null && elem.getProjectDirectory().equals(helper.getProjectDirectory())) {
                return elem;
            }
        }
        Project toReturn =  new BluejProject(helper);
        weakList.add(new WeakReference(toReturn));
        return toReturn;
    }

    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }
    
}
