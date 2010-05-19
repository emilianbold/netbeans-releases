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
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.lsm.DetachedPseudoComp;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;
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
public class FaultVarLSMsCreationTest {

    private static BpelModel mBpelModel;
    private static int varNameCounter = 0;

    @BeforeClass
    public static void initBpelModel() throws Exception {
        //
        mBpelModel = TestProjects.FAULT_VAR_LSMs_CREATION_TEST.getFreshBpelModel();
        assertNotNull(mBpelModel);
        //
    }

    /**
     * In the test's related bpel project takes the Catch/$FaultVar.part and
     * cast it to the derived type. Then looks for the xsd:any inside of the
     * casted part and created a pseudo component. Then the component is used
     * for mapping and the result assignment expression is compared with the
     * predefined text. 
     * It tests left tree
     *
     * @throws java.lang.Exception
     */
    @Test
    public void createLeftTypeCastPseudo() throws Exception {
        //
        boolean inLeftTree = true;
        PreparationData prepData = prepareCastedPartOfFaultVar(inLeftTree);
        //
        MapperSwingTreeModel leftTreeModel = prepData.bmm.getLeftTreeModel();
        TreePath anyElementPath = BpelMapperTestUtils.findInTree(prepData.treeNode,
                leftTreeModel, "Any Element"); // NOI18N
        assertNotNull(anyElementPath);
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
                addPseudoCompCmd(newDPC, anyElementPath, 
                inLeftTree, prepData.tcContext);
        assertTrue(result);
        //
        // Find the attrStr attribute inside of the new Pseudo Element.
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, leftTreeModel,
                "(NewPseudoElement)Any Element/attrStr"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = prepData.bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(prepData.bmm, leftPinPath, rightPinPath);
        //
        Element peer = prepData.testAssign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_FAULT_LT_CREATE_CAST_PSEUDO"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    /**
     * In the test's related bpel project takes the Catch/$FaultVar.part and
     * cast it to the derived type. Then looks for the xsd:any inside of the
     * casted part and created a pseudo component. Then the component is used
     * for mapping and the result assignment expression is compared with the
     * predefined text.
     * It tests right tree
     *
     * @throws java.lang.Exception
     */
    @Test
    public void createRightTypeCastPseudo() throws Exception {
        //
        boolean inLeftTree = false;
        PreparationData prepData = prepareCastedPartOfFaultVar(inLeftTree);
        //
        MapperSwingTreeModel rightTreeModel = prepData.bmm.getRightTreeModel();
        TreePath anyElementPath = BpelMapperTestUtils.findInTree(prepData.treeNode,
                rightTreeModel, "Any Element"); // NOI18N
        assertNotNull(anyElementPath);
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
                addPseudoCompCmd(newDPC, anyElementPath,
                inLeftTree, prepData.tcContext);
        assertTrue(result);
        //
        // Find the attrStr attribute inside of the new Pseudo Element.
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, rightTreeModel,
                "(NewPseudoElement)Any Element/attrStr"); // NOI18N
        assertNotNull(rightPinPath);
        //
        MapperSwingTreeModel leftTreeModel = prepData.bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(leftPinPath);
        //
        BpelMapperTestUtils.addTransitLink(prepData.bmm, leftPinPath, rightPinPath);
        //
        Element peer = prepData.testAssign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_FAULT_RT_CREATE_CAST_PSEUDO"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    /**
     * In the test's related bpel project takes the Catch/$FaultVar.part and
     * cast it to the derived type. Then looks for MultipleComplexElement inside of the
     * casted part and created a new predicate. Then the predicate is used
     * for mapping and the result assignment expression is compared with the
     * predefined text.
     * It tests left tree
     *
     * @throws java.lang.Exception
     */
    @Test
    public void createLeftTypeCastPredicate() throws Exception {
        //
        boolean inLeftTree = true;
        PreparationData prepData = prepareCastedPartOfFaultVar(inLeftTree);
        //
        MapperSwingTreeModel leftTreeModel = prepData.bmm.getLeftTreeModel();
        TreePath predSabjectPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, leftTreeModel, "MultipleComplexElement"); // NOI18N
        assertNotNull(predSabjectPath);
        //
        // Create the new predicate
        BpelMapperPredicate newPred = BpelMapperTestUtils.constructPredicate(
                prepData.tcContext, predSabjectPath, "@strAttr"); // NOI18N
        assertNotNull(newPred);
        //
        // Add predicate to tree
        TreePath newPredicateTPath = new BpelPredicateManager(null).
                addPredicateCmd(newPred, predSabjectPath,
                inLeftTree, prepData.tcContext);
        assertNotNull(newPredicateTPath);
        //
        // Find the attrStr attribute inside of the new Pseudo Element.
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, leftTreeModel,
                "MultipleComplexElement[@strAttr]/ElemB"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = prepData.bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(prepData.bmm, leftPinPath, rightPinPath);
        //
        Element peer = prepData.testAssign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_FAULT_LT_CREATE_CAST_PRED"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    /**
     * In the test's related bpel project takes the Catch/$FaultVar.part and
     * cast it to the derived type. Then looks for MultipleComplexElement inside of the
     * casted part and created a new predicate. Then the predicate is used
     * for mapping and the result assignment expression is compared with the
     * predefined text.
     * It tests right tree
     *
     * @throws java.lang.Exception
     */
    @Test
    public void createRightTypeCastPredicate() throws Exception {
        //
        boolean inLeftTree = false;
        PreparationData prepData = prepareCastedPartOfFaultVar(inLeftTree);
        //
        MapperSwingTreeModel rightTreeModel = prepData.bmm.getRightTreeModel();
        TreePath predSabjectPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, rightTreeModel, "MultipleComplexElement"); // NOI18N
        assertNotNull(predSabjectPath);
        //
        // Create the new predicate
        BpelMapperPredicate newPred = BpelMapperTestUtils.constructPredicate(
                prepData.tcContext, predSabjectPath, "@strAttr"); // NOI18N
        assertNotNull(newPred);
        //
        // Add predicate to tree
        TreePath newPredicateTPath = new BpelPredicateManager(null).
                addPredicateCmd(newPred, predSabjectPath,
                inLeftTree, prepData.tcContext);
        assertNotNull(newPredicateTPath);
        //
        // Find the attrStr attribute inside of the new Pseudo Element.
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, rightTreeModel,
                "MultipleComplexElement[@strAttr]/ElemB"); // NOI18N
        assertNotNull(rightPinPath);
        //
        MapperSwingTreeModel leftTreeModel = prepData.bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(leftPinPath);
        //
        BpelMapperTestUtils.addTransitLink(prepData.bmm, leftPinPath, rightPinPath);
        //
        Element peer = prepData.testAssign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_FAULT_RT_CREATE_CAST_PRED"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    /**
     * In the test's related bpel project takes the Catch/$FaultVar.part and
     * cast it to the derived type. Then looks for the baseTypeElem element inside of the
     * casted part and cast it to the derived type. Then the casted element is used
     * for mapping and the result assignment expression is compared with the
     * predefined text.
     * It tests left tree
     *
     * @throws java.lang.Exception
     */
    @Test
    public void createLeftTypeCastTypeCast() throws Exception {
        //
        boolean inLeftTree = true;
        PreparationData prepData = prepareCastedPartOfFaultVar(inLeftTree);
        //
        MapperSwingTreeModel leftTreeModel = prepData.bmm.getLeftTreeModel();
        TreePath baseTypeElementPath = BpelMapperTestUtils.findInTree(prepData.treeNode,
                leftTreeModel, "baseTypeElem"); // NOI18N
        assertNotNull(baseTypeElementPath);
        //
        // Find global type and cast the left item to it
        GlobalComplexType targetGType = BpelMapperTestUtils.getGlobalTypeByName(
                mBpelModel, "http://xml.netbeans.org/schema/Synchronous",
                "DerivedComplexType", GlobalComplexType.class); // NOI18N
        assertNotNull(targetGType);
        //
        // Create a new cast
        TreePath castedPartPath = new BpelCastManager().addCastCmd(
                targetGType, baseTypeElementPath, inLeftTree, prepData.tcContext);
        assertNotNull(castedPartPath);
        //
        // Find the attrStr attribute inside of the new Pseudo Element.
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, leftTreeModel,
                "(DerivedComplexType)baseTypeElem/strAttrA"); // NOI18N
        assertNotNull(leftPinPath);
        //
        MapperSwingTreeModel rightTreeModel = prepData.bmm.getRightTreeModel();
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                rightTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(rightPinPath);
        //
        BpelMapperTestUtils.addTransitLink(prepData.bmm, leftPinPath, rightPinPath);
        //
        Element peer = prepData.testAssign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_FAULT_LT_CREATE_CAST_CAST"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    /**
     * In the test's related bpel project takes the Catch/$FaultVar.part and
     * cast it to the derived type. Then looks for the baseTypeElem element inside of the
     * casted part and cast it to the derived type. Then the casted element is used
     * for mapping and the result assignment expression is compared with the
     * predefined text.
     * It tests right tree
     *
     * @throws java.lang.Exception
     */
    @Test
    public void createRightTypeCastTypeCast() throws Exception {
        //
        boolean inLeftTree = false;
        PreparationData prepData = prepareCastedPartOfFaultVar(inLeftTree);
        //
        MapperSwingTreeModel rightTreeModel = prepData.bmm.getRightTreeModel();
        TreePath baseTypeElementPath = BpelMapperTestUtils.findInTree(prepData.treeNode,
                rightTreeModel, "baseTypeElem"); // NOI18N
        assertNotNull(baseTypeElementPath);
        //
        // Find global type and cast the left item to it
        GlobalComplexType targetGType = BpelMapperTestUtils.getGlobalTypeByName(
                mBpelModel, "http://xml.netbeans.org/schema/Synchronous",
                "DerivedComplexType", GlobalComplexType.class); // NOI18N
        assertNotNull(targetGType);
        //
        // Create a new cast
        TreePath castedPartPath = new BpelCastManager().addCastCmd(
                targetGType, baseTypeElementPath, inLeftTree, prepData.tcContext);
        assertNotNull(castedPartPath);
        //
        // Find the attrStr attribute inside of the new Pseudo Element.
        TreePath rightPinPath = BpelMapperTestUtils.findInTree(
                prepData.treeNode, rightTreeModel,
                "(DerivedComplexType)baseTypeElem/strAttrA"); // NOI18N
        assertNotNull(rightPinPath);
        //
        MapperSwingTreeModel leftTreeModel = prepData.bmm.getLeftTreeModel();
        TreePath leftPinPath = BpelMapperTestUtils.findInTree(
                leftTreeModel, "/Variables/SimpleTargetVar/strAttr1"); // NOI18N
        assertNotNull(leftPinPath);
        //
        BpelMapperTestUtils.addTransitLink(prepData.bmm, leftPinPath, rightPinPath);
        //
        Element peer = prepData.testAssign.getPeer();
        String assignText = new SimpleDomSerializer().serializeNode(peer);
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_FAULT_RT_CREATE_CAST_CAST"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    // TODO: write tests for special step in TypeCast

    // TODO: remove redandant functions
    // @Test
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
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_RT_CREATE_PSEUDO_ELEMENT"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    // @Test
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
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_LT_CREATE_PREDICATE"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    // @Test
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
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_RT_CREATE_PREDICATE"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    // @Test
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
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_RT_CREATE_TYPECAST_ANYTYPE_ELEMENT"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    // @Test
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
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_LT_CREATE_TYPECAST_ANYTYPE_ELEMENT"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    // @Test
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
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_SIMPLE_LINK"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    // @Test
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
        String snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
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
        snapshot = TestProperties.getMessage(FaultVarLSMsCreationTest.class,
                "MAPPER_SNAPSHOT_SIMPLE_LINK_RECREATION"); // NOI18N
        assertEquals(assignText, snapshot);
    }

    //==========================================================================

    private static class PreparationData {
        public MapperTcContext tcContext;
        public BpelMapperModel bmm;
        public Assign testAssign;
        public MapperTreeNode treeNode;
    }

    /**
     * In the test's related bpel project takes the Catch/$FaultVar.part and
     * cast it to the derived type. The casted part, assign, bpel mapper and
     * static context are returned in the PreparationData object.
     * @return
     */
    private static PreparationData prepareCastedPartOfFaultVar(boolean leftTree) {
        //
        PreparationData resultData = new PreparationData();
        //
        Catch testCatch = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), Catch.class);
        assertNotNull(testCatch);
        //
        Sequence testSeq = BpelMapperTestUtils.findFirstActivity(
                testCatch, "TestCatchSequence", Sequence.class); // NOI18N
        assertNotNull(testSeq);
        //
        varNameCounter++;
        // Creates a new assign with a new name each time.
        Assign testAssign = BpelMapperTestUtils.createNewAssign(
                testSeq, "TestAssign" + varNameCounter, 0); // NOI18N
        assertNotNull(testAssign);
        resultData.testAssign = testAssign;
        //
        resultData.tcContext = BpelMapperTestUtils.loadMapper(testAssign);
        MapperModel mModel = resultData.tcContext.getMapperModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        resultData.bmm = BpelMapperModel.class.cast(mModel);
        //
        MapperSwingTreeModel treeModel = leftTree ? 
            resultData.bmm.getLeftTreeModel() : resultData.bmm.getRightTreeModel();
        //
        // Look for existing casted part
        TreePath castedPartPath = BpelMapperTestUtils.findInTree(treeModel,
                "/Variables/Catch/faultVar/(DerivedComplexType)testPart"); // NOI18N
        if (castedPartPath == null) {
            //
            // If ther isn't any then create new.
            TreePath testPartPath = BpelMapperTestUtils.findInTree(
                    treeModel, "/Variables/Catch/faultVar/testPart"); // NOI18N
            assertNotNull(testPartPath);
            //
            // Find global type and cast the left item to it
            GlobalComplexType targetGType = BpelMapperTestUtils.getGlobalTypeByName(
                    mBpelModel, "http://xml.netbeans.org/schema/Synchronous",
                    "DerivedComplexType", GlobalComplexType.class); // NOI18N
            assertNotNull(targetGType);
            //
            // Create a new cast
            castedPartPath = new BpelCastManager().addCastCmd(
                    targetGType, testPartPath, leftTree, resultData.tcContext);
            assertNotNull(castedPartPath);
        }
        //
        TreeItem castedPartTreeItem = MapperSwingTreeModel.getTreeItem(castedPartPath);
        assertNotNull(castedPartTreeItem);
        assert castedPartTreeItem instanceof MapperTreeNode;
        //
        resultData.treeNode = MapperTreeNode.class.cast(castedPartTreeItem);
        //
        return resultData;
    }

}
