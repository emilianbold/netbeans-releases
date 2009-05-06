/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.eecommon.api;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.FileObject;

/**
 * Utility class for common procedures
 *
 * @author Peter Williams
 */
public final class Utils {

    private Utils() {
    }

    public static final boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }

    public static final boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }

    public static final boolean strEquals(String one, String two) {
        boolean result = false;

        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }

    public static final boolean strEquivalent(String one, String two) {
        boolean result = false;

        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }

        return result;
    }

    public final static int strCompareTo(String one, String two) {
        int result;

        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }

        return result;
    }

    public static String computeModuleID(J2eeModule module, File dir, String fallbackExt) {
        String moduleID = null;
        FileObject fo = null;
        try {
            fo = module.getContentDirectory();
            if (null != fo) {
                moduleID = ProjectUtils.getInformation(FileOwnerQuery.getOwner(fo)).getName();
            }
        } catch (IOException ex) {
            Logger.getLogger("glassfish-eecommon").log(Level.FINER, null, ex);
        }

        if (null == moduleID || moduleID.trim().length() < 1) {
            J2eeModuleHelper j2eeModuleHelper = J2eeModuleHelper.getJ2eeModuleHelper(module.getModuleType());
            if(j2eeModuleHelper != null) {
                RootInterface rootDD = j2eeModuleHelper.getStandardRootDD(module);
                if(rootDD != null) {
                    try {
                        moduleID = rootDD.getDisplayName(null);
                    } catch (VersionNotSupportedException ex) {
                        // ignore, handle as null below.
                    }
                }
            }
        }
        if (null == moduleID || moduleID.trim().length() < 1) {
            moduleID = simplifyModuleID(dir.getParentFile().getParentFile().getName(), fallbackExt);
        } else {
            moduleID = simplifyModuleID(moduleID, fallbackExt);
        }

        return moduleID;
    }
    
    private static String simplifyModuleID(String candidateID, String fallbackExt) {
        String moduleID = null;

        if (candidateID == null) {
            moduleID = "_default_" + fallbackExt;
        } else if (candidateID.equals("")) {
            moduleID = "_default_" + fallbackExt;
        }

        if (null == moduleID) {
            moduleID = candidateID.replace(' ', '_');
            if (moduleID.startsWith("/")) {
                moduleID = moduleID.substring(1);
            }

            // This moduleID will be later used to construct file path,
            // replace the illegal characters in file name
            //  \ / : * ? " < > | with _
            moduleID = moduleID.replace('\\', '_').replace('/', '_');
            moduleID = moduleID.replace(':', '_').replace('*', '_');
            moduleID = moduleID.replace('?', '_').replace('"', '_');
            moduleID = moduleID.replace('<', '_').replace('>', '_');
            moduleID = moduleID.replace('|', '_');

            // This moduleID will also be used to construct an ObjectName
            // to register the module, so replace additional special
            // characters , =  used in property parsing with -
            moduleID = moduleID.replace(',', '_').replace('=', '_');
        }
        
        return moduleID;
    }
    
    public static class JarFileFilter implements FileFilter {
        public boolean accept(File f) {
            return ((! f.isDirectory()) && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".jar")); //NOI18N
        }
    }
    
}
