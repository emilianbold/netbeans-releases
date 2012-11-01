/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.libs.ods.jersey;

import com.tasktop.c2c.server.profile.domain.project.Organization;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.ProjectAccessibility;
import com.tasktop.c2c.server.profile.domain.project.ProjectPreferences;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.PasswordAuthentication;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.client.api.ODSFactory;
import org.netbeans.modules.ods.client.api.ODSClient;
import org.netbeans.modules.ods.client.api.ODSException;

/**
 *
 * @author tomas
 */
public class C2CJerseyTest extends NbTestCase {
    
    private static boolean firstRun = true;
    private static String uname;
    private static String passw;
    private static String proxyHost;
    private static String proxyPort;
    public static final String URL = "https://q.tasktop.com";
    
    public C2CJerseyTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        if (firstRun) {
            if (uname == null) {
                BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-team")));
                uname = br.readLine();
                passw = br.readLine();
                proxyHost = br.readLine();
                proxyPort = br.readLine();
                br.close();
            }
            if (firstRun) {
                firstRun = false;
            }
        }
        if (!proxyHost.isEmpty()) {
            System.setProperty("netbeans.system_http_proxy", proxyHost + ":" + proxyPort);
        }
    }
    
//    public void testApp() throws Exception {
//        ClientConfig clientConfig = new DefaultClientConfig();
//        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
//        clientConfig.getClasses().add(ObjectMapperProvider.class);
//        
////        ClientConfig clientConfig = new DefaultApacheHttpClientConfig();
////        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
////        clientConfig.getClasses().add(ObjectMapperProvider.class);
////        clientConfig.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, "http://" + proxyHost + ":" + proxyPort);
//
//        Client c = Client.create(clientConfig);
//        c.addFilter(new HTTPBasicAuthFilter(uname, passw));
//        WebResource root = c.resource("https://q.tasktop.com/alm/api/profile");
//        String ret = root.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
//
//        System.out.println("Hello World: " + ret);
//
//        JSONObject o = root.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
//        System.out.println("JSON Object: " + o.get("profile"));
//
//        System.out.println("NOW WITH JACKSON:");
//        ProfileWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ProfileWrapper.class);
//        System.out.println("Parse User Name: " + wrapper.profile.getUsername());
//        
//        System.out.println("QUERY RET LIKE STRING:");
//        root = c.resource("https://q.tasktop.com/alm/api/projects/search");
//        ret = root.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(String.class, new ProjectsQuery(ProjectRelationship.MEMBER, null));
//        System.out.println(ret);
//        
//        System.out.println("QUERY RET CUSTOM:");
//        root = c.resource("https://q.tasktop.com/alm/api/projects/search");
//        Record r = root.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(Record.class, new ProjectsQuery(ProjectRelationship.MEMBER, null));
//        System.out.println(r.queryResult.resultPage[0].name);
//        
//    }
    
    public void testGetClient() {
        getClient();
    }
    
    public void testGetProfile() throws ODSException {
        Profile profile = getClient().getCurrentProfile();
        
        assertNotNull(profile);
        assertEquals(uname, profile.getUsername());
    }
    
//    public void testRecentActivities() throws CloudException {
//        
//        List<ProjectActivity> as = getClient().getRecentActivities("anagramgame");
//        
//        assertNotNull(as);
//        assertFalse(as.isEmpty());
//    }

    private ODSClient getClient() {
        System.setProperty(ODSJerseyClientFactory.ID, "true");
        ODSClient client = ODSFactory.getInstance().createClient(URL, new PasswordAuthentication(uname, passw.toCharArray()));
        assertEquals(ODSJerseyClient.class, client.getClass());
        return client;
    }
    
    @XmlRootElement
    public static final class Record {
        @XmlElement
        CustomQueryResult queryResult;

        public static final class CustomQueryResult {
            @XmlElement
            Integer offset;
            @XmlElement
            Integer totalResultSize;
            @XmlElement
            Integer pageSize;
            @XmlElement
            CustomProject[] resultPage;
            @XmlElement
            int id;
            
            public static final class CustomProject {
                @XmlElement
                String identifier;
                @XmlElement
                String name;
                @XmlElement
                String description;
                @XmlElement
                ProjectAccessibility accessibility;
                @XmlElement
                ProjectPreferences projectPreferences;
                @XmlElement
                ProjectService[] projectServices;
                @XmlElement
                Integer numWatchers;
                @XmlElement
                Integer numCommiters;
                @XmlElement
                Organization organization;
                @XmlElement
                int id;
            }
        }
    }     
        
    	
//    public class QueryResultWrapper {
//
//        public QueryResultWrapper() {
//        }
//        
//        public QueryResult<Project> queryResult;
//    }
}
