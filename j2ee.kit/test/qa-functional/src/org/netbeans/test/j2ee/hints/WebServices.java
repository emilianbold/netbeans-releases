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
 * Software is Sun Micro//Systems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//Systems, Inc. All Rights Reserved.
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
package org.netbeans.test.j2ee.hints;

import java.io.File;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author Jindrich Sedek
 */
public class WebServices extends HintsUtils{
    
    /**
     * Creates a new instance of WebServices
     */
    public WebServices(String S) {
        super(S);
    }
    
    public void prepareProject() {
        ProjectSupport.openProject(new File(getDataDir(), "projects/WebServiceHintsEJB"));
    }
    
    public void testEndpointInterface() throws Exception{
        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/EndpointInterface.java"));
    }
    
    public void testExceptions() throws Exception{
        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/Exceptions.java"));
    }

    public void testHandlers() throws Exception{
        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/Handlers.java"));
    }

    public void testHandlers2() throws Exception{
        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/Handlers.java"), 1, null, 2);
    }
    
    public void testIOParameters() throws Exception{
        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/IOParametrs.java"));
    }

    public void testReturnValue() throws Exception{
        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/ReturnValue.java"));
    }

    public void testServiceName() throws Exception{
        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/ServiceName.java"));
    }
   
    private void hintTest(File file) throws Exception {
        hintTest(file, 0, null, 1);
    }

//    public void testAddOperation() throws Exception{ //issue #110297
//        hintTest(new File(getDataDir(), "projects/WebServiceHintsEJB/src/java/hints/AddOperation.java"), 0, "Add", 1, false);             
//    }
    public static void main(String[] args) throws Exception{
       new WebServices("TEST").testIOParameters();
    }

}
