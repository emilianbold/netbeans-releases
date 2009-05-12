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
package org.netbeans.modules.j2me.cdc.project.bdj;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author suchys
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor.class, position=200)
public class BDJPropertiesDescriptor implements ProjectPropertiesDescriptor {

    public static String PROP_ORGANIZATION_ID       = "bdj.organization.id";     //NOI18N
    public static String PROP_APPLICATION_ID        = "bdj.application.id";     //NOI18N
    public static String PROP_FILE_ACCESS           = "bdj.file.access";     //NOI18N
    public static String PROP_LIFECYCLE             = "bdj.application.lifecycle";     //NOI18N
    public static String PROP_SERVICE_SELECT        = "bdj.service.selection";     //NOI18N
    public static String PROP_USER_PREFERENCES_READ = "bdj.user.preferences.read";     //NOI18N
    public static String PROP_USER_PREFERENCES_WRITE = "bdj.user.preferences.write";     //NOI18N
    public static String PROP_NETWORK_PERMISSIONS   = "bdj.network.permissions";     //NOI18N
    public static String PROP_DEPLOYMENT_DIR        = "bdj.deployment.dir";     //NOI18N
            
    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);

    public BDJPropertiesDescriptor() {
    }

    public Set getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            set = new HashSet();
            set.add(new PropertyDescriptor(PROP_ORGANIZATION_ID, true, DefaultPropertyParsers.STRING_PARSER,  "56789abc")); //NOI18N
            set.add(new PropertyDescriptor(PROP_APPLICATION_ID, true, DefaultPropertyParsers.STRING_PARSER,  "00004001")); //NOI18N
            set.add(new PropertyDescriptor(PROP_FILE_ACCESS, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "false")); //NOI18N
            set.add(new PropertyDescriptor(PROP_LIFECYCLE, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "false")); //NOI18N
            set.add(new PropertyDescriptor(PROP_SERVICE_SELECT, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "false")); //NOI18N
            set.add(new PropertyDescriptor(PROP_USER_PREFERENCES_READ, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "false")); //NOI18N
            set.add(new PropertyDescriptor(PROP_USER_PREFERENCES_WRITE, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "false")); //NOI18N
            set.add(new PropertyDescriptor(PROP_NETWORK_PERMISSIONS, true, DefaultPropertyParsers.STRING_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_DEPLOYMENT_DIR, true, DefaultPropertyParsers.FILE_REFERENCE_PARSER,  "${build.dir}/deploy")); //NOI18N
            ref = new WeakReference(set);
        }
        //Defensive copy - getting CMEs when creating new configurations
        return new HashSet(set);
    }

}
