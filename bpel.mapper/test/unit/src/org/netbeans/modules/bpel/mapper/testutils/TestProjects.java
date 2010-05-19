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

package org.netbeans.modules.bpel.mapper.testutils;

import java.net.URL;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.soa.ui.TestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Collects information about all known test resources. 
 *
 * @author Nikita Krjukov
 */
public enum TestProjects {
    FAULT_VAR_LSMs_LOAD_TO_MAPPER_TEST("FaultVarLSMsLoadToMapperTest.zip", "FaultVarLSMsLoadToMapperTest/src/MapperTest.bpel"), // NOI18N
    FAULT_VAR_LSMs_CREATION_TEST("FaultVarLSMsCreationTest.zip", "FaultVarLSMsCreationTest/src/MapperTest.bpel"), // NOI18N
    LSMs_CREATION_TEST("LSMsCreationTest.zip", "LSMsCreationTest/src/MapperTest.bpel"), // NOI18N
    LSMs_LOAD_TO_MAPPER_TEST("LSMsLoadToMapperTest.zip", "LSMsLoadToMapperTest/src/MapperTest.bpel"), // NOI18N
    COPY_TYPES("DifferentCopyTypesTest.zip", "/DifferentCopyTypesTest/src/MapperTest.bpel"), // NOI18N
    ACTIVITIES("MapperActivitiesTest.zip", "/MapperActivitiesTest/src/MapperTest.bpel"), // NOI18N
    PURCHASE_ORDER("purchaseorder/PurchaseOrder.bpel"), // NOI18N
    ;

    private static final String TEST_RESOURCES_ROOT = "../resources/"; // NOI18N

    private String mProjectZip;
    private String mBpelFileLocation;
    private BpelModel mBpelModel;

    private TestProjects(String projectZip, String bpelInsideProject) {
        mProjectZip = TEST_RESOURCES_ROOT + projectZip;
        mBpelFileLocation = bpelInsideProject;
    }

    private TestProjects(String bpelFileLocation) {
        mBpelFileLocation = TEST_RESOURCES_ROOT + bpelFileLocation;
    }

    /**
     * This method can return cached model.
     * You can use the getFreshBpelModel() instead.
     * @return
     * @throws java.lang.Exception
     */
    public BpelModel getBpelModel() throws Exception {
        if (mBpelModel == null) {
            mBpelModel = getFreshBpelModel();
        }
        return mBpelModel;
    }

    public BpelModel getFreshBpelModel() throws Exception {
        BpelModel bpelModel = null;
        if (mProjectZip != null) {
            URL url = getClass().getResource(mProjectZip);
            url = new URL("jar:" + url.toString() + "!/" + mBpelFileLocation); // NOI18N
            FileObject fo = URLMapper.findFileObject(url);
            bpelModel = TestUtils.loadXamModel(BpelModelImpl.class,
                    fo, null, false);
        } else {
            // Load the BPEL model
            bpelModel = TestUtils.loadXAMModel(BpelModelImpl.class,
                    getClass(), mBpelFileLocation, null, false);
        }
        //
        return bpelModel;
    }

}
