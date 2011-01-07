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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.autoupdate.UpdateItem;

public class UtilitiesTest extends NbTestCase {

    public UtilitiesTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(
            MockUpdateProvider.class,
            MockInstalledModuleProvider.class
        );
        MockModuleInfo enabled = MockModuleInfo.create("module.one", "1.0", true);
        MockModuleInfo disabled = MockModuleInfo.create("module.two", "1.0", false);
        MockInstalledModuleProvider.setModuleItems(enabled, disabled);
        
        Map<String,UpdateItem> ui = new HashMap<String, UpdateItem>();
        ui.put(enabled.getCodeNameBase(), enabled.toUpdateItem("1.1"));
        ui.put(disabled.getCodeNameBase(), disabled.toUpdateItem("1.1"));
        MockUpdateProvider.setUpdateItems(ui);
    }

    @RandomlyFails // NB-Core-Build #5746: "Pending items are provided" from MockUpdateProvider.getUpdateItems
    public void testIgnoresDisabledModules() {
        List<UpdateUnit> uu = UpdateManager.getDefault().getUpdateUnits();
        List<UnitCategory> categories = Utilities.makeUpdateCategories(uu, true);
        
        assertNotNull("Categories created", categories);
        assertEquals("Something in there", 1, categories.size());
        List<Unit> units = categories.get(0).getUnits();
        assertEquals("Only one unit in category: " + units, 1, units.size());
        assertEquals("Only the enabled module present: " + units, "module.one", units.get(0).getDisplayName());
    }

}