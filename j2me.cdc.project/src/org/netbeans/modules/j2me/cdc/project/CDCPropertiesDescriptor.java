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

package org.netbeans.modules.j2me.cdc.project;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author Adam Sotona
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor.class, position=10)
public class CDCPropertiesDescriptor implements ProjectPropertiesDescriptor {
    
    public static final String APPLICATION_ARGS = "application.args"; //NOI18N
    public static final String APPLICATION_DESCRIPTION_DETAIL = "application.description.detail"; //NOI18N
    public static final String APPLICATION_DESCRIPTION = "application.description"; //NOI18N
    public static final String APPLICATION_NAME = "application.name"; //NOI18N
    public static final String APPLICATION_VENDOR = "application.vendor"; //NOI18N
    public static final String MAIN_CLASS_CLASS = "main.class.class"; //NOI18N
    public static final String MAIN_CLASS = "main.class"; //NOI18N
    public static final String MANIFEST_FILE = "manifest.file"; //NOI18N
    public static final String RESOURCES_DIR = "resources.dir"; //NOI18N
    public static final String RUN_JVMARGS = "run.jvmargs"; //NOI18N
    public static final String PLATFORM_FAT_JAR = "platform.fat.jar"; //NOI18N

        
    private Reference<Set<PropertyDescriptor>> ref = new WeakReference<Set<PropertyDescriptor>>(null);
    
    /** Creates a new instance of CDCPropertiesDescriptor */
    public CDCPropertiesDescriptor() {
    }
    
    public Set<PropertyDescriptor> getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            String EMPTY = ""; //NOI18N
            set = new HashSet<PropertyDescriptor>();
            set.add(new PropertyDescriptor(APPLICATION_ARGS, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_DESCRIPTION_DETAIL, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_DESCRIPTION, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_NAME, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_VENDOR, true, DefaultPropertyParsers.STRING_PARSER,  "Vendor")); //NOI18N
            set.add(new PropertyDescriptor(MAIN_CLASS_CLASS, true, DefaultPropertyParsers.STRING_PARSER,  "applet")); //NOI18N
            set.add(new PropertyDescriptor(MAIN_CLASS, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(MANIFEST_FILE, true, DefaultPropertyParsers.FILE_REFERENCE_PARSER,  "manifest.mf")); //NOI18N
            set.add(new PropertyDescriptor(RESOURCES_DIR, true, DefaultPropertyParsers.FILE_REFERENCE_PARSER,  "resources")); //NOI18N
            set.add(new PropertyDescriptor(RUN_JVMARGS, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(PLATFORM_FAT_JAR, true, DefaultPropertyParsers.BOOLEAN_PARSER, "true")); //NOI18N
            ref = new WeakReference(set);
        }
        //Defensive copy - getting CMEs when creating new configurations
        return new HashSet(set);
    }

}
