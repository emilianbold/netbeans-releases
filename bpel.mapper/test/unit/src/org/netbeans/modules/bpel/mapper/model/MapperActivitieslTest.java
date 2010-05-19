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
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils.BmmSerializer;
import org.netbeans.modules.bpel.mapper.testutils.BpelMapperTestUtils.TestProperties;
import org.netbeans.modules.bpel.mapper.testutils.TestProjects;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 * Tests BPEL mapper over different BPEL activities.
 * Mapper has to work with the following activities:
 * Assign
 * Copy
 * Wait
 * OnAlarmPick
 * OnAlarmEvent
 * If
 * ElseIf
 * While
 * RepeatUntil
 * ForEach
 *
 * @author Nikita Krjukov
 */
public class MapperActivitieslTest {

    private static BpelModel mBpelModel;

    @BeforeClass
    public static void initBpelModel() throws Exception {
        mBpelModel = TestProjects.ACTIVITIES.getBpelModel();
        assertNotNull(mBpelModel);
    }

    @Test
    public void testAssign() throws Exception {
        //
        BpelMapperTestUtils.testModelBuildByAssign(mBpelModel,
                "Assign1", "MAPPER_SNAPSHOT_ASSIGN",
                MapperActivitieslTest.class); // NOI18N
    }

    @Test
    public void testWait() throws Exception {
        //
        Wait wait = BpelMapperTestUtils.findFirstActivity(
                mBpelModel.getProcess(), "Wait1", Wait.class); // NOI18N
        assertNotNull(wait);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(wait);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_WAIT"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

    @Test
    public void testOnAlarmPick() throws Exception {
        //
        OnAlarmPick onAlarmPick = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), OnAlarmPick.class); 
        assertNotNull(onAlarmPick);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(onAlarmPick);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_OnAlarmPick"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

    @Test
    public void testOnAlarmEvent() throws Exception {
        //
        OnAlarmEvent onAlarmEvent = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), OnAlarmEvent.class); 
        assertNotNull(onAlarmEvent);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(onAlarmEvent);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_OnAlarmEvent"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

    @Test
    public void testIf() throws Exception {
        //
        If ifEntity = BpelMapperTestUtils.findFirstActivity(
                mBpelModel.getProcess(), "If1", If.class); // NOI18N
        assertNotNull(ifEntity);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(ifEntity);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_IF"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

    @Test
    public void testElseIf() throws Exception {
        //
        ElseIf elseIf = BpelMapperTestUtils.findFirstUnnamed(
                mBpelModel.getProcess(), ElseIf.class);
        assertNotNull(elseIf);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(elseIf);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_ELSEIF"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

    @Test
    public void testWhile() throws Exception {
        //
        While whileEntity = BpelMapperTestUtils.findFirstActivity(
                mBpelModel.getProcess(), "While1", While.class); // NOI18N
        assertNotNull(whileEntity);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(whileEntity);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_WHILE"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

    @Test
    public void testRepeatUntil() throws Exception {
        //
        RepeatUntil repeatUntil = BpelMapperTestUtils.findFirstActivity(
                mBpelModel.getProcess(), "RepeatUntil1", RepeatUntil.class); // NOI18N
        assertNotNull(repeatUntil);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(repeatUntil);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_REPEATUNTIL"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

    @Test
    public void testForEach() throws Exception {
        //
        ForEach forEach = BpelMapperTestUtils.findFirstActivity(
                mBpelModel.getProcess(), "ForEach1", ForEach.class); // NOI18N
        assertNotNull(forEach);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(forEach);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(MapperActivitieslTest.class,
                "MAPPER_SNAPSHOT_FOREACH"); // NOI18N
        assertEquals(bmmText, snapshot);
    }

}
