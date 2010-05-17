/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.dlight.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * D-Light logger, it is enabled when running NetBeans
 * with the parameter -J-Ddlight.logger=&lt;level&gt;
 */
public final class DLightLogger {
    private static boolean assertionsEnabled = false;

    static {
        assert(assertionsEnabled = true);
    }

    public static final Logger instance = Logger.getLogger("dlight.logger"); // NOI18N

    public static final Logger getLogger(Object obj) {
        return getLogger(obj.getClass());
    }

    public static final Logger getLogger(Class clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setParent(instance);
        return logger;
    }

    public static void assertTrue(boolean value) {
        if (assertionsEnabled) {
            assertTrue(value, "Assertion error"); //NOI18N
        }
    }

    public static void assertFalse(boolean value) {
        if (assertionsEnabled) {
            assertTrue(!value, "Assertion error"); //NOI18N
        }
    }

    public static void assertFalse(boolean value, String message) {
        assertTrue(!value, message);
    }

    public static void assertTrue(boolean value, String message) {
        if (assertionsEnabled && !value) {
            instance.log(Level.SEVERE, message, new Exception(message));
        }
    }

    public static final void assertNonUiThread() {
        assertFalse(SwingUtilities.isEventDispatchThread(), "Should not be called from UI thread"); //NOI18N
    }
}
