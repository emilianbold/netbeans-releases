/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.mapper;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;

/**
 *
 * @author Sergey Grinev
 */
public class MappingsTestCase extends RemoteTestBase {

//    public void testHMPW() throws Exception {
//        new HostMappingProviderWindows().findMappings("randomguy@eaglet-sr");
//    }

    public void testHostMappingProviderWindows() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("New connections will not be remembered.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Status       Local     Remote                               Network\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("OK           P:        \\\\serverOne\\pub                     Microsoft Windows Network\n");
        sb.append("Disconnected Y:        \\\\sErvEr_22_\\long name              Microsoft Windows Network\n");
        sb.append("OK           Z:        \\\\name.domen.domen2.zone\\sg155630   Microsoft Windows Network\n");
        sb.append("The command completed successfully.\n");
        Map<String, String> map;
        map = HostMappingProviderWindows.parseNetUseOutput("serverOne", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "p:".equals(map.get("pub"));

        map = HostMappingProviderWindows.parseNetUseOutput("sErvEr_22_", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "y:".equals(map.get("long name"));

        map = HostMappingProviderWindows.parseNetUseOutput("name.domen.domen2.zone", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "z:".equals(map.get("sg155630"));
    }

    public MappingsTestCase(String testName) {
        super(testName);
    }
}
