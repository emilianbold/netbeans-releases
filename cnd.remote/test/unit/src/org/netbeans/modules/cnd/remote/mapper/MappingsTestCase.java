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

import java.io.StringReader;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 *
 * @author Sergey Grinev
 */
public class MappingsTestCase extends RemoteTestBase {

//    public void testHMPW() throws Exception {
//        new HostMappingProviderWindows().findMappings("localhost", "randomguy@eaglet-sr");
//    }

//    public void testHMPS() throws Exception {
//        Map<String, String> mappings = new HostMappingProviderSamba().findMappings("tester@eaglet-sr", "");
//        assert mappings != null && "/export/pub".equals(mappings.get("pub"));
//    }

    @ForAllEnvironments
    public void testAnalyzer() throws Exception {
        HostMappingsAnalyzer ham = new HostMappingsAnalyzer(getTestExecutionEnvironment()); //sg155630@elif
        final Map<String, String> mappings = ham.getMappings();
        assert mappings != null;
        System.err.println(mappings);
    }

    public void testHostMappingProviderWindows_English() throws Exception {
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

    public void testHostMappingProviderWindows_Russian() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Новые подключения не будут запомнены.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Состояние    Локальный Удаленный                            Сеть\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("OK           P:        \\\\serverOne\\pub                     Microsoft Windows Network\n");
        sb.append("Нет доступа  Y:        \\\\sErvEr_22_\\long name              Microsoft Windows Network\n");
        sb.append("OK           Z:        \\\\name.domen.domen2.zone\\sg155630   Microsoft Windows Network\n");
        sb.append("Команда выполнена успешно.\n");
        Map<String, String> map;
        map = HostMappingProviderWindows.parseNetUseOutput("serverOne", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "p:".equals(map.get("pub"));

        map = HostMappingProviderWindows.parseNetUseOutput("sErvEr_22_", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "y:".equals(map.get("long name"));

        map = HostMappingProviderWindows.parseNetUseOutput("name.domen.domen2.zone", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "z:".equals(map.get("sg155630"));
    }

    public void testHostMappingProviderWindows_German() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Neue Verbindungen werden gespeichert.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Status           Lokal     Remote                               Netzwerk\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("OK               P:        \\\\serverOne\\pub                     Microsoft Windows-Netzwerk\n");
        sb.append("Nicht verfügbar  Y:        \\\\sErvEr_22_\\long name              Microsoft Windows-Netzwerk\n");
        sb.append("OK               Z:        \\\\name.domen.domen2.zone\\sg155630   Microsoft Windows-Netzwerk\n");
        sb.append("Der Befehl wurde erfolgreich ausgefÃ¼hrt.\n");
        Map<String, String> map;
        map = HostMappingProviderWindows.parseNetUseOutput("serverOne", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "p:".equals(map.get("pub"));

        map = HostMappingProviderWindows.parseNetUseOutput("sErvEr_22_", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "y:".equals(map.get("long name"));

        map = HostMappingProviderWindows.parseNetUseOutput("name.domen.domen2.zone", new StringReader(sb.toString()));
        assert map != null && map.size() == 1 && "z:".equals(map.get("sg155630"));
    }

    public void testHostMappingProviderWindows_Unexpected() throws Exception {
        // Test for error processing.
        // In the case net use prints something unexpected no exceptions should be thrown

        // 1. No header
        String netUseOutput_1 =
                "-------------------------------------------------------------------\n" +
                "OK       P:     \\\\server_1\\pub      Microsoft Windows-Netzwerk\n";
        HostMappingProviderWindows.parseNetUseOutput("server_1", new StringReader(netUseOutput_1));

        // 2. No "-----------------" line
        String netUseOutput_2 =
                "Status   Local  Remote               Network\n" +
                "OK       P:     \\\\server_1\\pub      Microsoft Windows-Netzwerk\n";
        HostMappingProviderWindows.parseNetUseOutput("server_1", new StringReader(netUseOutput_2));

        // 3. short host
        String netUseOutput_3 =
                "\n" +
                "Status   Local  Remote               Network\n" +
                "-------------------------------------------------------------------\n" +
                "OK       P:     xx                     Microsoft Windows-Netzwerk\n";
        HostMappingProviderWindows.parseNetUseOutput("server_1", new StringReader(netUseOutput_3));

        // 4. server without "\\"
        String netUseOutput_4 =
                "\n" +
                "Status   Local  Remote               Network\n" +
                "-------------------------------------------------------------------\n" +
                "OK       P:     server_1_pub           Microsoft Windows-Netzwerk\n";
        HostMappingProviderWindows.parseNetUseOutput("server_1", new StringReader(netUseOutput_4));

        // 5. empty line
        HostMappingProviderWindows.parseNetUseOutput("server_1", new StringReader(""));

        // 6. just some crap
        String netUseOutput_6 = "qwe\nasd\n---------------\nzxc\n123\n456\n\n\n";
        HostMappingProviderWindows.parseNetUseOutput("server_1", new StringReader(netUseOutput_6));
    }

    public void testHostMappingProviderSamba() throws Exception {
        Map<String, String> map;
        map = HostMappingProviderSamba.parseOutput(new StringReader(getConfigFile().toString()));
        assert map != null && map.size() == 1 && "/export/pub".equals(map.get("pub"));
    }
    
    
    private static StringBuilder getConfigFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("[global]\n");
        sb.append("\n");
        sb.append("      workgroup = staff\n");
        sb.append("      server string = Eaglet-SR Samba Server\n");
        sb.append("      log file = /var/adm/samba_log.%m\n");
        sb.append("      security = SHARE\n");
        sb.append("\n");
        sb.append("[pub]\n");
        sb.append("\n");
        sb.append("      comment = pub\n");
        sb.append("      path = /export/pub\n");
        sb.append("      force user = tester\n");
        sb.append("      force group = other\n");
        sb.append("      read only = No\n");
        sb.append("      guest ok = No\n");
        return sb;
    }

    public void testSimpleConfigParser() {
        SimpleConfigParser p = new SimpleConfigParser();
        StringBuilder sb = getConfigFile();
        sb.insert(0, "orphan=orphanValue\n");
        p.parse(new StringReader(sb.toString()));
        assert p.getSections().contains("global");
        assert p.getOrphanAttributes().get("orphan").equals("orphanValue");
        assert p.getAttributes("pub").get("path").equals("/export/pub");
    }

//    public void testMappingsValidation() {
//        if (canTestRemote()) {
//            RemotePathMap.validateMapping(getHKey(), rpath, "/net/endif/export");
//        }
//    }

    // we need this since some methods are without @ForAllEnvironments
    public MappingsTestCase(String testName) {
        super(testName);
    }

    public MappingsTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(MappingsTestCase.class);
    }

}
