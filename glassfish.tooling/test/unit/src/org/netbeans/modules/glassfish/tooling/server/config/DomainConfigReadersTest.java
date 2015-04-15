/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.server.config;

import java.io.File;
import java.util.Map;
import org.netbeans.modules.glassfish.tooling.server.parser.HttpData;
import org.netbeans.modules.glassfish.tooling.server.parser.HttpListenerReader;
import org.netbeans.modules.glassfish.tooling.server.parser.NetworkListenerReader;
import org.netbeans.modules.glassfish.tooling.server.parser.TargetConfigNameReader;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class DomainConfigReadersTest {
    
    private static final String DOMAIN_CONFIG_FILE = System.getProperty("user.dir")
            + "/src/test/java/org/netbeans/modules/glassfish/tooling/server/config/domain.xml";
    
    @Test
    public void testReadAdminPort() {
        File domainXML = new File(DOMAIN_CONFIG_FILE);
        TargetConfigNameReader configNameReader = new TargetConfigNameReader();
        TreeParser.readXml(domainXML, configNameReader);
        String targetConfigName = configNameReader.getTargetConfigName();
        HttpListenerReader httpReader = new HttpListenerReader(targetConfigName);
        NetworkListenerReader networkReader = new NetworkListenerReader(targetConfigName);
        TreeParser.readXml(domainXML, httpReader, networkReader);
        Map<String, HttpData> result = httpReader.getResult();
        result.putAll(networkReader.getResult());
        HttpData adminData = result.get("admin-listener");
        assertTrue(adminData.getPort() == 4848);
    }
    
}
