/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.universe;

import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/** Release of the build harness. */
public enum HarnessVersion {

    // should proceed in chronological order so we can do compatibility tests with compareTo

    /** Unknown version - platform might be invalid, or just predate any 5.0 release version. */
    UNKNOWN,
    V50,
    /** Harness version found in 5.0 update 1 and 5.5. */
    V50u1,
    /** Harness version found in 5.5 update 1. */
    V55u1,
    V60,
    V61,
    V65,
    V67,
    V68,
    V69;

    /** Gets a quick display name. */
    public String getDisplayName() {
        switch (this) {
            case V50:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_5.0");
            case V50u1:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_5.0u1");
            case V55u1:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_5.5u1");
            case V60:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_6.0");
            case V61:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_6.1");
            case V65:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_6.5");
            case V67:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_6.7");
            case V68:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_6.8");
            case V69:
                return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_6.9");
            default:
                assert this == UNKNOWN;
        }
        return NbBundle.getMessage(HarnessVersion.class, "LBL_harness_version_unknown");
    }

    /** Detects harness version based on org-netbeans-modules-apisupport-harness.jar */
    static HarnessVersion forHarnessModuleVersion(SpecificationVersion v) {
        if (v.compareTo(new SpecificationVersion("1.20")) >= 0) { // NOI18N
            return V69;
        } else if (v.compareTo(new SpecificationVersion("1.18")) >= 0) { // NOI18N
            return V68;
        } else if (v.compareTo(new SpecificationVersion("1.14")) >= 0) { // NOI18N
            return V67;
        } else if (v.compareTo(new SpecificationVersion("1.12")) >= 0) { // NOI18N
            return V65;
        } else if (v.compareTo(new SpecificationVersion("1.11")) >= 0) { // NOI18N
            return V61;
        } else if (v.compareTo(new SpecificationVersion("1.10")) >= 0) { // NOI18N
            return V60;
        } else if (v.compareTo(new SpecificationVersion("1.9")) >= 0) { // NOI18N
            return V55u1;
        } else if (v.compareTo(new SpecificationVersion("1.7")) >= 0) { // NOI18N
            return V50u1;
        } else if (v.compareTo(new SpecificationVersion("1.6")) >= 0) { // NOI18N
            return V50;
        } else {
            // earlier than beta2? who knows...
            return UNKNOWN;
        }
    }

}
