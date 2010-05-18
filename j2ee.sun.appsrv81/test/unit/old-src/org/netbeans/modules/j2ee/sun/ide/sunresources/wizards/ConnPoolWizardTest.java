/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import junit.framework.TestCase;
import org.openide.WizardDescriptor;

/**
 *
 * @author vkraemer
 */
public class ConnPoolWizardTest extends TestCase {
    private WizardDescriptor wiz;
    private WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[3];

    public void testCreate() {
        ConnPoolWizard s1 = new ConnPoolWizard().create();
        wiz = new WizardDescriptor(panels);
        s1.initialize(wiz);

        assertNotNull("ResourceConfigHelper was created ", s1.getResourceConfigHelper());
        
        //wiz.getPanel(0);
        //wiz.getResourceConfigHelper();
        //s1.stateChange(new ChangeEvent() {
            
        //});
        //}
        s1.getResourceConfigHelper().getData().addProperty("foo", "bar");
        s1.getResourceConfigHelper().getData().setTargetFile("CPWizardTestFile");
        s1.getWizardInfo();
        s1.hasNext();
        s1.hasPrevious();
        s1.name();
        s1.nextPanel();
        s1.previousPanel();
        s1.setResourceConfigHelper(s1.getResourceConfigHelper());
        s1.instantiate();
    }
    
    public ConnPoolWizardTest(String testName) {
        super(testName);
    }
    
}
