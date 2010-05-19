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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.model;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContextFactory;
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils;
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils.BmmHashCodeCalculator;
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils.BmmSerializer;
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils.TestProperties;
import org.netbeans.modules.bpel.mapper.testutils.TestProjects;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Does test of a simple mapper model. 
 * The class is an example(pattern) for creating more complex tests. 
 *
 * @author Nikita Krjukov
 */
public class SimpleMapperModelTest {

    private static BpelModel mBpelModel;

    @BeforeClass
    public static void initBpelModel() throws Exception {
        mBpelModel = TestProjects.PURCHASE_ORDER.getBpelModel();
        assertNotNull(mBpelModel);
    }

    @Test
    public void testBuildMapperModel() throws Exception {
        //
        // Find Assign1
        Assign assign = BpelMapperTestUtils.findFirstActivity(
                mBpelModel.getProcess(), "Assign1", Assign.class); // NOI18N
        assertNotNull(assign);
        //
        Lookup emptyLookup = Lookups.fixed();
        Node node = new AbstractNode(Children.LEAF, emptyLookup);
        BpelDesignContext dc = BpelDesignContextFactory.getInstance().
                createBpelDesignContext(assign, node, emptyLookup);
        BpelMapperModelFactory modelFactory = new BpelMapperModelFactory(
                BpelMapperTestUtils.createTcContext(dc), dc);
        //
        MapperModel mModel = modelFactory.constructModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        //
        // Check the mapper model by comparing hash code.
        int hashCode = (new BmmHashCodeCalculator(
                BpelMapperModel.class.cast(mModel))).calculate();
        assertEquals(hashCode, -1280325097);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(
                BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(
                MapperActivitieslTest.class, "MAPPER_SNAPSHOT_SIMPLE_TEST"); // NOI18N
        assertEquals(bmmText, snapshot); 
    }

}
