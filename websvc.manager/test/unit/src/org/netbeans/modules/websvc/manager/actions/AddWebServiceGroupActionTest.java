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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.actions;

import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.test.SetupUtil;
import org.openide.nodes.Node;

/**
 *
 * @author quynguyen
 */
public class AddWebServiceGroupActionTest extends NbTestCase {

    public AddWebServiceGroupActionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SetupUtil.commonSetUp(getWorkDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SetupUtil.commonTearDown();
    }

    /**
     * Test of performAction method, of class AddWebServiceGroupAction.
     */
    public void testPerformAction() {
        System.out.println("performAction");
        AddWebServiceGroupAction action = new AddWebServiceGroupAction();
        List<WebServiceGroup> groups = WebServiceListModel.getInstance().getWebServiceGroupSet();
        WebServiceGroup[] originalGroups = groups.toArray(new WebServiceGroup[groups.size()]);
        int originalSize = originalGroups.length;
        
        action.performAction(new Node[0]);

        assertTrue("Web Service group was not added", groups.size() == originalSize + 1);

        WebServiceGroup[] groupArr = groups.toArray(new WebServiceGroup[groups.size()]);
        for (int i = 0; i < groupArr.length; i++) {
            WebServiceGroup group = groupArr[i];
            boolean found = false;
            for (int j = 0; j < originalGroups.length; j++) {
                if (group == originalGroups[j]) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                WebServiceListModel.getInstance().removeWebServiceGroup(group.getId());
            }
        }

        groups = WebServiceListModel.getInstance().getWebServiceGroupSet();
        assertTrue("Web Service group not removed", groups.size() == originalSize);
    }
}
