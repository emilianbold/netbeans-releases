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

package org.netbeans.modules.project.libraries;

import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;



/** Checks that libraries are updated as sson as correct library type
 * provider is registered.
 *
 * @author Jaroslav Tulach
 */
public class LibrariesStorageRefreshTest extends NbTestCase {
    
    private FileObject storageFolder;
    LibrariesStorage storage;
    
    public LibrariesStorageRefreshTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new LibrariesStorageTest.TestEntityCatalog());
        this.storageFolder = TestUtil.makeScratchDir(this);
        LibrariesStorageTest.createLibraryDefinition(this.storageFolder,"Library1");
        this.storage = new LibrariesStorage (this.storageFolder);
    }

    public void testGetLibrariesAfterEnablingProvider() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        LibrariesStorageTest.TestListener l = new LibrariesStorageTest.TestListener ();
        this.storage.addPropertyChangeListener(l);
        assertEquals("No libraries found", 0, libs.length);
        LibrariesStorageTest.registerLibraryTypeProvider();
        Thread.sleep(1000); //The lookup for path fires with delay, wait for the event
        libs = this.storage.getLibraries();        
        assertEquals("Libraries count",1,libs.length);        
        assertEquals("One change", 1, l.getEventNames().size());
    }
    
    
}
