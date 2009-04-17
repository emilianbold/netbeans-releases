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

package org.netbeans.modules.j2me.cdc.project.savaje;

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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor.class, position=30)
public class SavajePropertiesDescriptor implements ProjectPropertiesDescriptor {

    public static final String SAVAJE_BUNDLE_DEBUG = "savaje.bundle.debug"; //NOI18N
    public static final String SAVAJE_BUNDLE_DEBUG_PORT = "savaje.bundle.debug.port"; //NOI18N
    public static final String SAVAJE_BUNDLE_BASE = "savaje.bundle.base"; //NOI18N
    public static final String SAVAJE_APPLICATION_ICON = "savaje.application.icon"; //NOI18N
    public static final String SAVAJE_APPLICATION_ICON_SMALL = "savaje.application.icon.small"; //NOI18N
    public static final String SAVAJE_APPLICATION_ICON_FOCUSED = "savaje.application.icon.focused"; //NOI18N
    public static final String SAVAJE_APPLICATION_UID = "savaje.application.uid"; //NOI18N
    
    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);
    
    /** Creates a new instance of SavajePropertiesDescriptor */
    public SavajePropertiesDescriptor() {
    }
    
    public Set getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            String EMPTY = ""; //NOI18N
            set = new HashSet();
            set.add(new PropertyDescriptor(SAVAJE_BUNDLE_DEBUG, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "false")); //NOI18N
            set.add(new PropertyDescriptor(SAVAJE_BUNDLE_DEBUG_PORT, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(SAVAJE_BUNDLE_BASE, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(SAVAJE_APPLICATION_ICON, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(SAVAJE_APPLICATION_ICON_SMALL, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(SAVAJE_APPLICATION_ICON_FOCUSED, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(SAVAJE_APPLICATION_UID, true, DefaultPropertyParsers.STRING_PARSER,  "TBD")); //NOI18N
            ref = new WeakReference(set);
        }
        return new HashSet(set);
    }

}
