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
package org.netbeans.modules.j2me.cdc.project.nsicom;

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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor.class, position=90)
public class NSIcomPropertiesDescriptor implements ProjectPropertiesDescriptor {

    public static String PROP_MONITOR_HOST = "nsicom.application.monitorhost"; //NOI18N
    public static String PROP_VERBOSE      = "nsicom.application.runverbose";  //NOI18N
    public static String PROP_RUN_REMOTE   = "nsicom.application.runremote";  //NOI18N
    public static String PROP_REMOTE_VM    = "nsicom.remotevm.location";      //NOI18N
    public static String PROP_REMOTE_APP   = "nsicom.remoteapp.location";     //NOI18N

    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);

    public NSIcomPropertiesDescriptor() {
    }

    public Set getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            set = new HashSet();
            set.add(new PropertyDescriptor(PROP_MONITOR_HOST, true, DefaultPropertyParsers.STRING_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_VERBOSE, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_RUN_REMOTE, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_REMOTE_VM, true, DefaultPropertyParsers.STRING_PARSER,  "\\Windows\\creme\\bin\\CrEme.exe")); //NOI18N
            set.add(new PropertyDescriptor(PROP_REMOTE_APP, true, DefaultPropertyParsers.STRING_PARSER,  "\\My Documents\\NetBeans Applications")); //NOI18N
            ref = new WeakReference(set);
        }
        return new HashSet(set);
    }

}
