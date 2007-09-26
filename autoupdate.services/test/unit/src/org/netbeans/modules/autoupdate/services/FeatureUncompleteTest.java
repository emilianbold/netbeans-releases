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
package org.netbeans.modules.autoupdate.services;

import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.updateprovider.*;
import org.netbeans.api.autoupdate.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class FeatureUncompleteTest extends NbTestCase

{

    private static File catalogFile;
    private static URL catalogURL;
    protected boolean modulesOnly = true;
    List<UpdateUnit> keepItNotToGC;

    public FeatureUncompleteTest (String testName) {
        super (testName);
    }

    public static class MyProvider implements UpdateProvider {

        public String getName () {
            return FeatureNotUpToDateTest.class.getName ();
        }

        public String getDisplayName () {
            return getName ();
        }

        public String getDescription () {
            return getName ();
        }

        public Map<String, UpdateItem> getUpdateItems () throws IOException {
            Map<String, UpdateItem> items = InstalledModuleProvider.getDefault ().getUpdateItems ();
            assertNotNull ("Installed modules must found.", items);
            URL independentURL = TestUtils.class.getResource ("data/org-yourorghere-independent.nbm");
            assertNotNull ("NBM data/org-yourorghere-independent.nbm found.", independentURL);
            File independentFile = new File(independentURL.getFile());
            assertTrue ("NBM data/org-yourorghere-independent.nbm exists.", independentFile.exists ());

            String source = "local-downloaded";
            List<UpdateUnit> units =  UpdateUnitProviderFactory.getDefault ().create (source, new File[] {independentFile}).
                    getUpdateUnits (UpdateManager.TYPE.MODULE);
            assertEquals("Only once module found.", 1, units.size());
            
            Map<String, UpdateItem> res = InstalledModuleProvider.getDefault ().getUpdateItems ();
            
            ModuleUpdateUnitImpl moduleUpdateUnitImpl = (ModuleUpdateUnitImpl) Trampoline.API.impl (units.get (0));
            assertFalse ("Non empty updates for " + units.get (0), moduleUpdateUnitImpl.getAvailableUpdates ().isEmpty ());
            ModuleUpdateElementImpl moduleUpdateElementImpl = (ModuleUpdateElementImpl) Trampoline.API.impl (units.get (0).getAvailableUpdates ().get (0));
            
            ModuleInfo info = moduleUpdateElementImpl.getModuleInfo ();
            UpdateItemImpl moreItemImpl = new InstalledModuleItem (
                    info.getCodeNameBase (),
                    info.getSpecificationVersion ().toString (),
                    info,
                    null, // XXX author
                    null, // installed cluster
                    null);
            UpdateItem moreModuleItem = Utilities.createUpdateItem (moreItemImpl);
            
            Set<String> deps = new HashSet<String> (items.size ());
            for (String id : items.keySet ()) {
                String dep;
                UpdateItem item = items.get (id);
                assertNotNull ("Impl of " + item + " available", Trampoline.SPI.impl (item));
                UpdateItemImpl itemImpl = Trampoline.SPI.impl (item);
                assertTrue ("Impl of " + item + "is ModuleItem", itemImpl instanceof ModuleItem);
                ModuleItem moduleItem = (ModuleItem) itemImpl;
                dep = moduleItem.getModuleInfo ().getCodeNameBase () + " > " + moduleItem.getSpecificationVersion ();
                deps.add (dep);
            }
            
            // add more item
            deps.add (info.getCodeNameBase () + " > " + info.getSpecificationVersion ());
            
            res.put ("testFeatueVsStandaloneModules",
                    UpdateItem.createFeature (
                        "testFeatueVsStandaloneModules",
                        "1.0",
                        deps,
                        null,
                        null,
                        null));
            res.put (info.getCodeNameBase (), moreModuleItem);
            return res;
        }

        public boolean refresh (boolean force) throws IOException {
            return true;
        }

        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        this.clearWorkDir ();
        TestUtils.setUserDir (getWorkDirPath ());
        TestUtils.testInit ();
        MockServices.setServices (MyProvider.class);
        assert Lookup.getDefault ().lookup (MyProvider.class) != null;
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);
    }

    public void testUncompleteFeature () {
        if (true) return ; // XXX
        assertNotNull ("A feature found.", UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.FEATURE));
        List<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.FEATURE);
        assertEquals ("Only once feature there.", 1, units.size ());
        UpdateUnit feature = units.get (0);
        assertNotNull (feature + " is installed.", feature.getInstalled ());
        assertFalse (feature + " has some available updates.", feature.getAvailableUpdates ().isEmpty ());
        assertTrue ("Some module is missing.", feature.getInstalled ().getDescription ().indexOf ("Not yet") != -1);
        assertTrue ("NBM data/org-yourorghere-independent.nbm exists is missing", feature.getInstalled ().getDescription ().indexOf ("independent") != -1);
    }
    
}
