/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.glassfish.common;

import org.junit.Test;

/**
 *
 * @author vkraemer
 */
public class GlassfishInstanceTest {

    public GlassfishInstanceTest() {
    }

    /**
     * Test of create method, of class GlassfishInstance.
     */
    @Test
    public void testCreate_10args() {
        System.out.println("create");
        String displayName = "";
        String installRoot = "";
        String glassfishRoot = "";
        String domainsDir = "";
        String domainName = "";
        int httpPort = 0;
        int adminPort = 0;
        String url = "";
        String uriFragment = "";
        GlassfishInstanceProvider gip = null;
        GlassfishInstance expResult = null;
        GlassfishInstance result = GlassfishInstance.create(displayName, installRoot, glassfishRoot, domainsDir, domainName, httpPort, adminPort, "foo:localhost:port", uriFragment, GlassfishInstanceProvider.getEe6());
        assert result != null : "got a null";
        GlassfishInstance result2 = GlassfishInstance.create(displayName, installRoot, glassfishRoot, domainsDir, domainName, httpPort, adminPort,  "foo:localhost:port", uriFragment, GlassfishInstanceProvider.getEe6());
        assert result.equals(result2);
        result2 = GlassfishInstance.create(displayName, installRoot, glassfishRoot, domainsDir, domainName, httpPort, adminPort,  "foo:127.0.0.1:port", uriFragment, GlassfishInstanceProvider.getEe6());
        assert result.equals(result2);
    }

}