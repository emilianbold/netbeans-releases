/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server.actions.ui;

import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.coherence.server.CoherenceInstance;
import org.netbeans.modules.coherence.server.CoherenceInstanceProvider;
import org.netbeans.modules.coherence.server.CoherenceProperties;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CloneServerPanelTest extends NbTestCase {

    public CloneServerPanelTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetUniqueName() throws Exception {
        assertEquals("Coherence 3.7 Clone(1)", CloneServerPanel.getUniqueName("Coherence 3.7"));

        InstanceProperties instanceProperties = InstancePropertiesManager.getInstance().createProperties("Coherence");
        instanceProperties.putInt(CoherenceProperties.PROP_ID, 123456);
        instanceProperties.putString(CoherenceProperties.PROP_DISPLAY_NAME, "Coherence 3.7 Clone(1)");
        CoherenceInstance instance = CoherenceInstance.create(instanceProperties);
        CoherenceInstanceProvider.getCoherenceProvider().addServerInstance(instance);
        assertEquals("Coherence 3.7 Clone(2)", CloneServerPanel.getUniqueName("Coherence 3.7 Clone(1)"));

        InstanceProperties instanceProperties2 = InstancePropertiesManager.getInstance().createProperties("Coherence");
        instanceProperties2.putInt(CoherenceProperties.PROP_ID, 987654);
        instanceProperties2.putString(CoherenceProperties.PROP_DISPLAY_NAME, "Coherence 3.7 Clone(2)");
        CoherenceInstance instance2 = CoherenceInstance.create(instanceProperties2);
        CoherenceInstanceProvider.getCoherenceProvider().addServerInstance(instance2);
        assertEquals("Coherence 3.7 Clone(3)", CloneServerPanel.getUniqueName("Coherence 3.7 Clone"));
    }

}
