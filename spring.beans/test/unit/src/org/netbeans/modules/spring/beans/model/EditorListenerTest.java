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

package org.netbeans.modules.spring.beans.model;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.openide.util.WeakSet;

/**
 *
 * @author Andrei Badea
 */
public class EditorListenerTest extends ConfigFileTestCase {

    public EditorListenerTest(String testName) {
        super(testName);
    }

    public void testRegister() throws Exception {
        ConfigFileGroup group = ConfigFileGroup.create(Collections.singletonList(configFile));
        SpringConfigModelController controller = SpringConfigModelController.create(group);
        EditorListener.getInstance().register(controller);

        assertEquals(1, EditorListener.getInstance().file2Controllers.size());
        WeakSet<SpringConfigModelController> controllers = EditorListener.getInstance().file2Controllers.get(configFile);
        assertEquals(1, controllers.size());
        assertSame(controller, controllers.iterator().next());

        WeakReference<SpringConfigModelController> controllerRef = new WeakReference<SpringConfigModelController>(controller);
        controller = null;
        assertGC("Should be possible to GC the controller", controllerRef);

        File configFile2 = createConfigFileName("dispatcher-servlet.xml");
        ConfigFileGroup group2 = ConfigFileGroup.create(Collections.singletonList(configFile2));
        SpringConfigModelController controller2 = SpringConfigModelController.create(group2);

        // Force an expunge of the stale files.
        for (int i = 0; i < EditorListener.EXPUNGE_EVERY; i++) {
            EditorListener.getInstance().register(controller2);
        }

        assertEquals(1, EditorListener.getInstance().file2Controllers.size());
        assertNull(EditorListener.getInstance().file2Controllers.get(configFile));
        controllers = EditorListener.getInstance().file2Controllers.get(configFile2);
        assertEquals(1, controllers.size());
        assertSame(controller2, controllers.iterator().next());

        WeakReference<SpringConfigModelController> controller2Ref = new WeakReference<SpringConfigModelController>(controller2);
        controller2 = null;
        assertGC("Should be possible to GC the controller", controller2Ref);
    }
}
