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

package org.netbeans.modules.editor.mimelookup;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
@RandomlyFails // NB-Core-Build #1103, probably due to TestUtilities.sleepForWhile
public class MimePathLookupTest extends NbTestCase {

    /** Creates a new instance of MimePathLookupTest */
    public MimePathLookupTest(String name) {
        super(name);
    }

    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader()
        );
    }
    
    public void testAddingMimeDataProvider() throws Exception {
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkAddingMimeDataProvider(String instanceFile, Class<T> markerClass) throws Exception {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        
        Collection markers = lookup.lookupAll(markerClass);
        assertEquals("There should be no markers", 0, markers.size());
        
        // Add the data provider
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        markers = lookup.lookupAll(markerClass);
        assertEquals("No markers found", 1, markers.size());
    }

    public void testAddingMimeDataProvider2() throws Exception {
        checkAddingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkAddingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkAddingMimeDataProvider2(String instanceFile, Class<T> markerClass) throws Exception {
        MimePath path = MimePath.get("text/x-java");
        
        Lookup.Result result = MimeLookup.getLookup(path).lookupResult(markerClass);
        Collection markers = result.allInstances();
        assertEquals("There should be no markers", 0, markers.size());

        L listener = new L();
        result.addLookupListener(listener);
        assertEquals("There should be no changes received", 0, listener.resultChangedCnt);
        
        // Add the data provider
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        assertEquals("No changes received", 1, listener.resultChangedCnt);
        markers = result.allInstances();
        assertEquals("No markers found", 1, markers.size());
    }
    
    public void testRemovingMimeDataProvider() throws Exception {
        checkRemovingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkRemovingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkRemovingMimeDataProvider(String instanceFile, Class<T> markerClass) throws Exception {
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();

        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        
        Collection markers = lookup.lookupAll(markerClass);
        assertEquals("No markers found", 1, markers.size());

        TestUtilities.deleteFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        markers = lookup.lookupAll(markerClass);
        assertEquals("There should be no markers", 0, markers.size());
    }

    public void testRemovingMimeDataProvider2() throws Exception {
        checkRemovingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkRemovingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkRemovingMimeDataProvider2(String instanceFile, Class<T> markerClass) throws Exception {
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();

        MimePath path = MimePath.get("text/x-java");
        Lookup.Result result = MimeLookup.getLookup(path).lookupResult(markerClass);
        Collection markers = result.allInstances();
        assertEquals("No markers found", 1, markers.size());

        L listener = new L();
        result.addLookupListener(listener);
        assertEquals("There should be no changes received", 0, listener.resultChangedCnt);

        // Remove the data provider
        TestUtilities.deleteFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        assertEquals("No changes received", 1, listener.resultChangedCnt);
        markers = result.allInstances();
        assertEquals("There should be no markers", 0, markers.size());
    }
    
    private static class L implements LookupListener {
        public int resultChangedCnt = 0;
        
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    } // End of L class
}
