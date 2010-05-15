/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.model;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils;
import org.netbeans.modules.bpel.mapper.testutils.TestProjects;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

/**
 * Tests BPEL mapper over different copy types: 
 * Variable only,
 * variable + part,
 * XPath,
 * Variable + Part + Query
 * Variable + Query
 * Correlation Property
 * NM Property
 * XML Literal
 *
 * @author Nikita Krjukov
 */
public class DifferentCopyTypesTest {

    private static BpelModel mBpelModel;

    @BeforeClass
    public static void initBpelModel() throws Exception {
        //
        // Register standard NM Properties
        Repository repository = Lookup.getDefault().lookup(Repository.class);
        assertNotNull(repository);
        //
        // Be aware that the layer.xml contains not all available NM Properties!
        // Use only properties from this layer.xml
        XMLFileSystem layerXmlFs = new XMLFileSystem(
               DifferentCopyTypesTest.class.getResource(
               "/org/netbeans/modules/bpel/mapper/resources/layer.xml"));
        assertNotNull(layerXmlFs);
        repository.addFileSystem(layerXmlFs);
        //
        // Load the BPEL model
        mBpelModel = TestProjects.COPY_TYPES.getBpelModel();
        assertNotNull(mBpelModel);
    }

    @Test
    public void testVarialbeAssignForm() throws Exception {
        testModelBuildByAssign("Assign1", "MAPPER_SNAPSHOT_VAR2VAR"); // NOI18N
    }

    @Test
    public void testVariablePartAssignForm() throws Exception {
        testModelBuildByAssign("Assign2", "MAPPER_SNAPSHOT_PART2PART"); // NOI18N
    }
    
    @Test
    public void testExpressionAssignForm() throws Exception {
        testModelBuildByAssign("Assign3", "MAPPER_SNAPSHOT_XPAT2XPATH"); // NOI18N
    }

    @Ignore
    @Test
    public void testVariablePartQueryAssignForm() throws Exception {
        testModelBuildByAssign("Assign4", "MAPPER_SNAPSHOT_VPQ2VPQ"); // NOI18N
    }

    @Ignore
    @Test
    public void testVariableQueryAssignForm() throws Exception {
        testModelBuildByAssign("Assign5", "MAPPER_SNAPSHOT_VQ2VQ"); // NOI18N
    }

    @Test
    public void testPartnerLinkAssignForm() throws Exception {
        testModelBuildByAssign("Assign6", "MAPPER_SNAPSHOT_PL2PL"); // NOI18N
    }

    @Test
    public void testXMLLiteralAssignForm() throws Exception {
        testModelBuildByAssign("Assign7", "MAPPER_SNAPSHOT_XML_LITERAL"); // NOI18N
    }

    @Test
    public void testCorrelationPropertyAssignForm() throws Exception {
        testModelBuildByAssign("Assign8", "MAPPER_SNAPSHOT_PROP2PROP"); // NOI18N
    }

    @Test
    public void testNMPropertyAssignForm() throws Exception {
        testModelBuildByAssign("Assign9", "MAPPER_SNAPSHOT_NMPROP"); // NOI18N
    }

    private static void testModelBuildByAssign(String assignName, String snapshotBundleKey) {
        BpelMapperTestUtils.testModelBuildByAssign(mBpelModel,
                assignName, snapshotBundleKey, DifferentCopyTypesTest.class);
    }

}
