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

import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils;
import org.netbeans.modules.bpel.mapper.testutils.TestProjects;
import org.netbeans.modules.bpel.model.api.BpelModel;

/**
 * Tests loading LSMs to BPEL mapper. There are following LSMs
 * - Predicate
 * - TypeCast
 * - PseudoComponent
 *
 * The elements or attributes of the type xsi:anyType is the special case. 
 *
 * @author Nikita Krjukov
 */
public class LSMsLoadToMapperTest {

    private static BpelModel mBpelModel;

    @BeforeClass
    public static void initBpelModel() throws Exception {
        //
        mBpelModel = TestProjects.LSMs_LOAD_TO_MAPPER_TEST.getBpelModel();
        assertNotNull(mBpelModel);
    }

    @Test
    public void testLeftPredicateTypeCast() throws Exception {
        testModelBuildByAssign("Assign1", "MAPPER_SNAPSHOT_LT_PREDICATE_TYPECAST"); // NOI18N
    }

    @Test
    public void testLeftPredicatePseudoComponent() throws Exception {
        testModelBuildByAssign("Assign2", "MAPPER_SNAPSHOT_LT_PREDICATE_PSEUDOCOMP"); // NOI18N
    }
    
    @Test
    public void testLeftTypeCastPredicate() throws Exception {
        testModelBuildByAssign("Assign3", "MAPPER_SNAPSHOT_LT_TYPECAST_PREDICATE"); // NOI18N
    }

    @Test
    public void testLeftTypeCastPseudoComponent() throws Exception {
        testModelBuildByAssign("Assign4", "MAPPER_SNAPSHOT_LT_TYPECAST_PSEUDOCOMP"); // NOI18N
    }

    @Test
    public void testLeftPseudoComponentPredicate() throws Exception {
        testModelBuildByAssign("Assign5", "MAPPER_SNAPSHOT_LT_PSEUDOCOMP_PREDICATE"); // NOI18N
    }

    @Test
    public void testLeftPseudoComponentTypeCast() throws Exception {
        testModelBuildByAssign("Assign6", "MAPPER_SNAPSHOT_LT_PSEUDOCOMP_TYPECAST"); // NOI18N
    }

    @Test
    public void testLeftTypeCastPredicateSameNode() throws Exception {
        testModelBuildByAssign("Assign13", "MAPPER_SNAPSHOT_LT_TYPECAST_PREDICATE_SAME"); // NOI18N
    }

    @Test
    public void testLeftPseudoComponentPredicateSameNode() throws Exception {
        testModelBuildByAssign("Assign14", "MAPPER_SNAPSHOT_LT_PSEUDOCOMP_PREDICATE_SAME"); // NOI18N
    }

    @Test
    public void testLeftVariableTypeCastPredicate() throws Exception {
        testModelBuildByAssign("Assign17", "MAPPER_SNAPSHOT_LT_VAR_TYPECAST_PREDICATE"); // NOI18N
    }

    @Test
    public void testLeftVariablTypeCastTypeCast() throws Exception {
        testModelBuildByAssign("Assign18", "MAPPER_SNAPSHOT_LT_VAR_TYPECAST_TYPECAST"); // NOI18N
    }

    @Test
    public void testLeftVariableTypeCastPseudoComponent() throws Exception {
        testModelBuildByAssign("Assign19", "MAPPER_SNAPSHOT_LT_VAR_TYPECAST_PSEUDOCOMP"); // NOI18N
    }

    @Test
    public void testLeftPartTypeCastPredicate() throws Exception {
        testModelBuildByAssign("Assign20", "MAPPER_SNAPSHOT_LT_PART_TYPECAST_PREDICATE"); // NOI18N
    }

    @Test
    public void testLeftPartTypeCastTypeCast() throws Exception {
        testModelBuildByAssign("Assign21", "MAPPER_SNAPSHOT_LT_PART_TYPECAST_TYPECAST"); // NOI18N
    }

    @Test
    public void testLeftPartTypeCastPseudoComponent() throws Exception {
        testModelBuildByAssign("Assign22", "MAPPER_SNAPSHOT_LT_PART_TYPECAST_PSEUDOCOMP"); // NOI18N
    }

    @Test
    public void testRightPredicateTypeCast() throws Exception {
        testModelBuildByAssign("Assign7", "MAPPER_SNAPSHOT_RT_PREDICATE_TYPECAST"); // NOI18N
    }

    @Test
    public void testRightPredicatePseudoComponent() throws Exception {
        testModelBuildByAssign("Assign8", "MAPPER_SNAPSHOT_RT_PREDICATE_PSEUDOCOMP"); // NOI18N
    }
    
    @Test
    public void testRightTypeCastPredicate() throws Exception {
        testModelBuildByAssign("Assign9", "MAPPER_SNAPSHOT_RT_TYPECAST_PREDICATE"); // NOI18N
    }
    
    @Test
    public void testRightTypeCastPseudoComponent() throws Exception {
        testModelBuildByAssign("Assign10", "MAPPER_SNAPSHOT_RT_TYPECAST_PSEUDOCOMP"); // NOI18N
    }

    @Test
    public void testRightPseudoComponentPredicate() throws Exception {
        testModelBuildByAssign("Assign11", "MAPPER_SNAPSHOT_RT_PSEUDOCOMP_PREDICATE"); // NOI18N
    }

    @Test
    public void testRightPseudoComponentTypeCast() throws Exception {
        testModelBuildByAssign("Assign12", "MAPPER_SNAPSHOT_RT_PSEUDOCOMP_TYPECAST"); // NOI18N
    }

    @Test
    public void testRightTypeCastPredicateSame() throws Exception {
        testModelBuildByAssign("Assign15", "MAPPER_SNAPSHOT_RT_TYPECAST_PREDICATE_SAME"); // NOI18N
    }

    @Test
    public void testRightPseudoComponentPredicateSame() throws Exception {
        testModelBuildByAssign("Assign16", "MAPPER_SNAPSHOT_RT_PSEUDOCOMP_PREDICATE_SAME"); // NOI18N
    }
    
    @Test
    public void testRightVariableTypeCastPredicate() throws Exception {
        testModelBuildByAssign("Assign23", "MAPPER_SNAPSHOT_RT_VAR_TYPECAST_PREDICATE"); // NOI18N
    }

    @Test
    public void testRightVariableTypeCastTypeCast() throws Exception {
        testModelBuildByAssign("Assign24", "MAPPER_SNAPSHOT_RT_VAR_TYPECAST_TYPECAST"); // NOI18N
    }

    @Test
    public void testRightVariableTypeCastPseudoComponent() throws Exception {
        testModelBuildByAssign("Assign25", "MAPPER_SNAPSHOT_RT_VAR_TYPECAST_PSEUDOCOMP"); // NOI18N
    }

    @Test
    public void testRightPartTypeCastPredicate() throws Exception {
        testModelBuildByAssign("Assign26", "MAPPER_SNAPSHOT_RT_PART_TYPECAST_PREDICATE"); // NOI18N
    }

    @Test
    public void testRightPartTypeCastTypeCast() throws Exception {
        testModelBuildByAssign("Assign27", "MAPPER_SNAPSHOT_RT_PART_TYPECAST_TYPECAST"); // NOI18N
    }

    @Test
    public void testRightPartTypeCastPseudoComponent() throws Exception {
        testModelBuildByAssign("Assign28", "MAPPER_SNAPSHOT_RT_PART_TYPECAST_PSEUDOCOMP"); // NOI18N
    }
    
    @Test
    public void testLeftPseudoAttributeInsidePseudoElement() throws Exception {
        testModelBuildByAssign("Assign29", "MAPPER_SNAPSHOT_LT_PSEUDOATTR_PSEUDOELEM"); // NOI18N
    }

    @Test
    public void testRightPseudoAttributeInsidePseudoElement() throws Exception {
        testModelBuildByAssign("Assign30", "MAPPER_SNAPSHOT_RT_PSEUDOATTR_PSEUDOELEM"); // NOI18N
    }

    @Test
    public void testLeftAnyTypeElementOrAttribute() throws Exception {
        testModelBuildByAssign("Assign31", "MAPPER_SNAPSHOT_LT_ANYTYPE_ELEMENT_OR_ATTR"); // NOI18N
    }

    @Test
    public void testRightAnyTypeElementOrAttribute() throws Exception {
        testModelBuildByAssign("Assign32", "MAPPER_SNAPSHOT_RT_ANYTYPE_ELEMENT_OR_ATTR"); // NOI18N
    }


    private static void testModelBuildByAssign(String assignName, String snapshotBundleKey) {
        BpelMapperTestUtils.testModelBuildByAssign(mBpelModel,
                assignName, snapshotBundleKey, LSMsLoadToMapperTest.class);
    }

}
