/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.utils;

import org.netbeans.modules.maven.j2ee.JavaEEMavenBaseTest;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestSupport;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestConstants;

/**
 * Tests related to creating deployment descriptor using MavenProjectSupport class
 * @author Martin Janicek
 */
public class CreateDDTest extends JavaEEMavenBaseTest {

    public CreateDDTest(String name) {
        super(name);
    }
    
    
    /***********************************************************************************************************
     * Calling createDDIfRequired with server set in auxiliary properties but without passing him as a parameter
     ***********************************************************************************************************/
    public void testCreateDDIfRequired_nullServerPassed_webLogic() {
        MavenProjectSupport.setServerID(project, JavaEEMavenTestConstants.WEBLOGIC);
        MavenProjectSupport.createDDIfRequired(project, null);
        
        assertEquals(true, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
    
    public void testCreateDDIfRequired_nullServerPassed_glassfish() {
        MavenProjectSupport.setServerID(project, JavaEEMavenTestConstants.GLASSFISH);
        MavenProjectSupport.createDDIfRequired(project, null);
        
        assertEquals(false, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
    
    public void testCreateDDIfRequired_nullServerPassed_tomcat() {
        MavenProjectSupport.setServerID(project, JavaEEMavenTestConstants.TOMCAT);
        MavenProjectSupport.createDDIfRequired(project, null);
        
        assertEquals(false, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
    
    public void testCreateDDIfRequired_nullServerPassed_jboss() {
        MavenProjectSupport.setServerID(project, JavaEEMavenTestConstants.JBOSS);
        MavenProjectSupport.createDDIfRequired(project, null);
        
        assertEquals(false, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
    
    
    
    /****************************************************************************
     * Calling createDDIfRequired with server passed to the method as a parameter
     ****************************************************************************/
    public void testCreateDDIfRequired_weblogicPassed() {
        MavenProjectSupport.createDDIfRequired(project, JavaEEMavenTestConstants.WEBLOGIC);
        assertEquals(true, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
    
    public void testCreateDDIfRequired_glassfishPassed() {
        MavenProjectSupport.createDDIfRequired(project, JavaEEMavenTestConstants.GLASSFISH);
        assertEquals(false, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
    
    public void testCreateDDIfRequired_tomcatPassed() {
        MavenProjectSupport.createDDIfRequired(project, JavaEEMavenTestConstants.TOMCAT);
        assertEquals(false, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
    
    public void testCreateDDIfRequired_jbossPassed() {
        MavenProjectSupport.createDDIfRequired(project, JavaEEMavenTestConstants.JBOSS);
        assertEquals(false, JavaEEMavenTestSupport.isWebDDpresent(project));
    }
}
