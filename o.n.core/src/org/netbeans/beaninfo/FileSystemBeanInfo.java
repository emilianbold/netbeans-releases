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

package org.netbeans.beaninfo;

import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

import org.openide.filesystems.*;
import org.openide.util.Exceptions;

/** Object that provides beaninfo for {@link FileSystem}s.
*
* @author Ian Formanek
*/
public class FileSystemBeanInfo extends SimpleBeanInfo {

    /* Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] desc = new PropertyDescriptor[] {
                   new PropertyDescriptor ("readOnly", FileSystem.class, "isReadOnly", null), // 0 // NOI18N
                   new PropertyDescriptor ("valid", FileSystem.class, "isValid", null), // 1 // NOI18N
                   new PropertyDescriptor ("hidden", FileSystem.class, "isHidden", "setHidden"), // 2 // NOI18N
                   new PropertyDescriptor ("actions", FileSystem.class, "getActions", null), // 3 // NOI18N
                   new PropertyDescriptor ("displayName", FileSystem.class, "getDisplayName", null), // 4 // NOI18N
                   new PropertyDescriptor ("root", FileSystem.class, "getRoot", null), // 5 // NOI18N
                   new PropertyDescriptor ("status", FileSystem.class, "getStatus", null), // 6 // NOI18N
            };
            ResourceBundle bundle = NbBundle.getBundle(FileSystemBeanInfo.class);
            desc[0].setHidden(true);
            desc[1].setDisplayName (bundle.getString("PROP_valid"));
            desc[1].setHidden(true);
/*
            desc[1].setShortDescription (bundle.getString("HINT_valid"));
            desc[1].setExpert (true);
*/
            desc[2].setHidden(true);
            desc[3].setHidden (true);
            desc[4].setHidden (true);
            desc[5].setHidden (true);
            desc[6].setHidden (true);
            return desc;
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
            return super.getPropertyDescriptors();
        }
    }

}
