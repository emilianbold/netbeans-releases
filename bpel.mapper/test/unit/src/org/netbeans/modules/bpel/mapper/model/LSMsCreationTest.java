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

import javax.swing.tree.TreePath;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.bpel.mapper.cast.BpelCastManager;
import org.netbeans.modules.bpel.mapper.cast.BpelPseudoCompManager;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.predicates.BpelPredicateManager;
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils;
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils.TestProperties;
import org.netbeans.modules.bpel.mapper.testutils.SimpleDomSerializer;
import static org.junit.Assert.*;

import org.netbeans.modules.bpel.mapper.testutils.TestProjects;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.xpath.mapper.lsm.DetachedPseudoComp;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.w3c.dom.Element;

/**
 * Tests creation of new LSMs separately and in different combinations.
 *
 * WARNING. Be carefull while adding new tests here.
 * The tests use common BPEL model, which is loaded only once for the sake
 * of performance. So different tests can influence each other.
 * The good idea is to use different variables and different assigns for
 * different tests. 
 *
 * @author Nikita Krjukov
 */
public class LSMsCreationTest {

    private static BpelModel mBpelModel;

    @BeforeClass
    public static void initBpelModel() throws Exception {
        //
        mBpelModel = TestProjects.LSMs_CREATION_TEST.getFreshBpelModel();
        assertNotNull(mBpelModel);
    }

    // TODO: Add PseudoComponent creation tests

    // TODO: Add special test when TypeCast is created inside
    // of another TypeCast at the right tree. Problems with @xsi:type

    @Test
    public void createLeftTreePseudoElementTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(sequence, "Assign6", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperTcContext tcContext = BpelMapperTestUtils.loadMapper(assign);
        MapperModel mModel = tcContext.getMapperModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/VarCTypeA/Any Element"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(rightPinPath);
        //
        // Find global type and cast the left item to it
        GlobalComplexType targetGType = BpelMapperTestUtils.getGlobalTypeByName(
                mBpelModel, "http://xml.netbeans.org/schema/Synchronous",
                "ComplexTypeA", GlobalComplexType.class); // NOI18N
        assertNotNull(targetGType);
        //
        // Create the new detached pseudo component
        DetachedPseudoComp newDPC = new DetachedPseudoComp(
                targetGType, "NewPseudoElement", "testNamespace", false);
        assertNotNull(newDPC);
        //
        // Add new pseudo element to tree
        boolean result = new BpelPseudoCompManager().
                addPseudoCompCmd(newDPC, leftPinPath, true, tcContext);
        assertTrue(result);
        //
        // Find the attrStr attribute inside of the new Pseudo Element.
        leftPinPath = BpelMapperTestUtils.findInTree(leftTreeModel,
                "/Variables/VarCTypeA/(NewPseudoElement)Any Element/attrStr"); // NOI18N
        assertNotNull(leftPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_LT_CREATE_PSEUDO_ELEMENT"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    @Test
    public void createRightTreePseudoElementTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(sequence, "Assign5", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperTcContext tcContext = BpelMapperTestUtils.loadMapper(assign);
        MapperModel mModel = tcContext.getMapperModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/VarCTypeA/Any Element"); // NOI18N
        assertNotNull(rightPinPath);
        //
        // Find global type and cast the left item to it
        GlobalComplexType targetGType = BpelMapperTestUtils.getGlobalTypeByName(
                mBpelModel, "http://xml.netbeans.org/schema/Synchronous",
                "ComplexTypeA", GlobalComplexType.class); // NOI18N
        assertNotNull(targetGType);
        //
        // Create the new detached pseudo component
        DetachedPseudoComp newDPC = new DetachedPseudoComp(
                targetGType, "NewPseudoElement", "testNamespace", false);
        assertNotNull(newDPC);
        //
        // Add new pseudo element to tree
        boolean result = new BpelPseudoCompManager().
                addPseudoCompCmd(newDPC, rightPinPath, false, tcContext);
        assertTrue(result);
        //
        // Find the attrStr attribute inside of the predicated element.
        rightPinPath = BpelMapperTestUtils.findInTree(rightTreeModel,
                "/Variables/VarCTypeA/(NewPseudoElement)Any Element/attrStr"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_RT_CREATE_PSEUDO_ELEMENT"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    @Test
    public void createLeftTreePredicateTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(sequence, "Assign4", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperTcContext tcContext = BpelMapperTestUtils.loadMapper(assign);
        MapperModel mModel = tcContext.getMapperModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/VarCTypeA/elemAMult"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(rightPinPath);
        //
        // Create the new predicate
        BpelMapperPredicate newPred = BpelMapperTestUtils.constructPredicate(
                tcContext, leftPinPath, "@atrA2"); // NOI18N
        assertNotNull(newPred);
        //
        // Add predicate to tree
        TreePath newPredicateTPath = new BpelPredicateManager(null).
                addPredicateCmd(newPred, leftPinPath, true, tcContext);
        assertNotNull(newPredicateTPath);
        //
        // Find the attrStr attribute inside of the predicated element.
        leftPinPath = BpelMapperTestUtils.findInTree(leftTreeModel,
                "/Variables/VarCTypeA/elemAMult[@atrA2]/atrA2"); // NOI18N
        assertNotNull(leftPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_LT_CREATE_PREDICATE"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    @Test
    public void createRightTreePredicateTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(sequence, "Assign3", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperTcContext tcContext = BpelMapperTestUtils.loadMapper(assign);
        MapperModel mModel = tcContext.getMapperModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/VarCTypeA/elemAMult"); // NOI18N
        assertNotNull(rightPinPath);
        //
        // Create the new predicate
        BpelMapperPredicate newPred = BpelMapperTestUtils.constructPredicate(
                tcContext, rightPinPath, "1"); // NOI18N
        assertNotNull(newPred);
        //
        // Add predicate to tree
        TreePath newPredicateTPath = new BpelPredicateManager(null).
                addPredicateCmd(newPred, rightPinPath, false, tcContext);
        assertNotNull(newPredicateTPath);
        //
        // Find the attrStr attribute inside of the predicated element.
        rightPinPath = BpelMapperTestUtils.findInTree(rightTreeModel,
                "/Variables/VarCTypeA/elemAMult[1]/atrA2"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_RT_CREATE_PREDICATE"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    @Test
    public void createRightTreeTypeCastTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(sequence, "Assign2", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperTcContext tcContext = BpelMapperTestUtils.loadMapper(assign);
        MapperModel mModel = tcContext.getMapperModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(rightPinPath);
        //
        // Find global type and cast the left item to it
        GlobalComplexType targetGType = BpelMapperTestUtils.getGlobalTypeByName(
                mBpelModel, "http://xml.netbeans.org/schema/Synchronous",
                "ComplexTypeA", GlobalComplexType.class); // NOI18N
        assertNotNull(targetGType);
        //
        // Create a new cast
        rightPinPath = new BpelCastManager().
                addCastCmd(targetGType, rightPinPath, false, tcContext);
        assertNotNull(leftPinPath);
        //
        // Find the attrStr attribute inside of the casted element.
        rightPinPath = BpelMapperTestUtils.findInTree(rightTreeModel,
                "/Variables/VarCTypeA/(ComplexTypeA)anyTypeElement/attrStr"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_RT_CREATE_TYPECAST_ANYTYPE_ELEMENT"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    @Test
    public void createLeftTreeTypeCastTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(sequence, "Assign1", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperTcContext tcContext = BpelMapperTestUtils.loadMapper(assign);
        MapperModel mModel = tcContext.getMapperModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(leftPinPath);
        //
        // Find global type and cast the left item to it
        GlobalComplexType targetGType = BpelMapperTestUtils.getGlobalTypeByName(
                mBpelModel, "http://xml.netbeans.org/schema/Synchronous",
                "ComplexTypeA", GlobalComplexType.class); // NOI18N
        assertNotNull(targetGType);
        //
        // Create a new cast
        leftPinPath = new BpelCastManager().
                addCastCmd(targetGType, leftPinPath, true, tcContext);
        assertNotNull(leftPinPath);
        //
        // Find the attrStr attribute inside of the casted element.
        leftPinPath = BpelMapperTestUtils.findInTree(leftTreeModel,
                "/Variables/VarCTypeA/(ComplexTypeA)anyTypeElement/attrStr"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_LT_CREATE_TYPECAST_ANYTYPE_ELEMENT"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    @Test
    public void simpleLinkTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(
                sequence, "SimpleLinkTest", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(assign);
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_SIMPLE_LINK"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    @Test
    public void simpleLinkRecreationTest() throws Exception {
        //
        Sequence sequence = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Sequence.class);
        assertNotNull(sequence);
        //
        Assign assign = BpelMapperTestUtils.createNewAssign(
                sequence, "SimpleLinkRecreationTest", 2); // NOI18N
        assertNotNull(assign);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(assign);
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        BpelMapperModel bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel leftTreeModel = bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/VarCTypeA/anyTypeElement"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        Element peer = assign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_SIMPLE_LINK_RECREATION"); // NOI18N
        assertEquals(assignText, snapshot);
        //
        // delete all graphs
        bmm.removeNestedGraphs(null);
        //
        // create the same link again
        BpelMapperTestUtils.addTransitLink(bmm, leftPinPath, rightPinPath);
        //
        peer = assign.getPeer();
        assignText = new SimpleDomSerializer().serializeNode(peer);
        snapshot = TestProperties.getMessage(LSMsCreationTest.class,
                "MAPPER_SNAPSHOT_SIMPLE_LINK_RECREATION"); // NOI18N
        assertEquals(assignText, snapshot);
    }

}
