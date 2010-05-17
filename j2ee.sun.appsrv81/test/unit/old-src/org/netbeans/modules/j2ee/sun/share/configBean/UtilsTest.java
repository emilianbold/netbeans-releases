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

package org.netbeans.modules.j2ee.sun.share.configbean;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import junit.framework.TestCase;

/**
 *
 * @author vkraemer
 */
public class UtilsTest extends TestCase {

    /** Test of makeCE method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. */
    public void testMakeCE() {
        ConfigurationException ce = Utils.makeCE(null, null, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'null' has params '{1}' '{2}' '{3}' '{4}' '{5}'");
        ce = Utils.makeCE("ERR_UnexpectedRuntimeException", null, null);
        assertEquals(ce.getMessage(),"Unexpected runtime exception");     
        ce = Utils.makeCE("unknownMessageKeyValue", null, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params '{1}' '{2}' '{3}' '{4}' '{5}'");
        Object args[] = new Object[0];
        ce = Utils.makeCE("ERR_UnexpectedRuntimeException", args, null);
        assertEquals(ce.getMessage(),"Unexpected runtime exception");     
        ce = Utils.makeCE("unknownMessageKeyValue", args, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params '{1}' '{2}' '{3}' '{4}' '{5}'");
        args = new Object[1];
        ce = Utils.makeCE("unknownMessageKeyValue", args, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params 'null' '{2}' '{3}' '{4}' '{5}'");
        args[0] = "Foobar";
        ce = Utils.makeCE("unknownMessageKeyValue", args, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params 'Foobar' '{2}' '{3}' '{4}' '{5}'");
    }

    public UtilsTest(String testName) {
        super(testName);
    }
    
    /** Test of getFQNKey method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. 
    public void testGetFQNKey() {
        System.out.println("testGetFQNKey");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of getUriFromKey method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. 
    public void testGetUriFromKey() {
        System.out.println("testGetUriFromKey");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of getFilenameFromKey method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. 
    public void testGetFilenameFromKey() {
        System.out.println("testGetFilenameFromKey");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    */
}
