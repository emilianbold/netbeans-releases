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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.eclipse.osgi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.netbeans.libs.bugtracking.BugtrackingRuntime;
import org.openide.util.NbBundle;

/**
 * @author Maros Sandor
 *
 * // XXX need correct message handling
 */
public class NLS {
    
    public static void initializeMessages(String bundleName, Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if((field.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) == (Modifier.PUBLIC | Modifier.STATIC)) {
                try {
                    field.set(null, NbBundle.getBundle(bundleName).getString(field.getName()));
                } catch (IllegalArgumentException ex) {
                    // should not happen
                    BugtrackingRuntime.LOG.log(Level.SEVERE, bundleName, ex);
                } catch (IllegalAccessException ex) {
                    // should not happen
                    BugtrackingRuntime.LOG.log(Level.SEVERE, bundleName, ex);
                }
            }
        }
    }

    public static String bind(String m, Object o) {
        return MessageFormat.format(m, o);
    }

    public static String bind(String m, String s) {
        return MessageFormat.format(m, s);
    }

    public static String bind(String m, String s1, String s2) {
        return MessageFormat.format(m, s1, s2);
    }

    public static String bind(String m, Object[] args) {
        return MessageFormat.format(m, args);
    }

    public static String bind(String m, Object obj1, Object obj2) {
        return MessageFormat.format(m, obj1, obj2);
    }
}
