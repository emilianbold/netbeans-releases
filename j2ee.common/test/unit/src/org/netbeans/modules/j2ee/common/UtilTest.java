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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class UtilTest extends NbTestCase {
    
    /** Creates a new instance of UtilTest */
    public UtilTest(String testName) {
        super(testName);
    }

    public void testIsHigherJavaEEVersionJavaEE5() {
        assertFalse(Util.isAtLeastJavaEE5(Profile.J2EE_13));
        assertFalse(Util.isAtLeastJavaEE5(Profile.J2EE_14));

        assertTrue(Util.isAtLeastJavaEE5(Profile.JAVA_EE_5));
        assertTrue(Util.isAtLeastJavaEE5(Profile.JAVA_EE_6_FULL));
        assertTrue(Util.isAtLeastJavaEE5(Profile.JAVA_EE_6_WEB));
        assertTrue(Util.isAtLeastJavaEE5(Profile.JAVA_EE_7_FULL));
        assertTrue(Util.isAtLeastJavaEE5(Profile.JAVA_EE_7_WEB));
    }

    public void testIsHigherJavaEEVersionJavaEE6full() {
        assertFalse(Util.isAtLeastJavaEE6Web(Profile.J2EE_13));
        assertFalse(Util.isAtLeastJavaEE6Web(Profile.J2EE_14));
        assertFalse(Util.isAtLeastJavaEE6Web(Profile.JAVA_EE_5));

        assertTrue(Util.isAtLeastJavaEE6Web(Profile.JAVA_EE_6_WEB));
        assertTrue(Util.isAtLeastJavaEE6Web(Profile.JAVA_EE_6_FULL));
        assertTrue(Util.isAtLeastJavaEE6Web(Profile.JAVA_EE_7_WEB));
        assertTrue(Util.isAtLeastJavaEE6Web(Profile.JAVA_EE_7_FULL));
    }
    
}
