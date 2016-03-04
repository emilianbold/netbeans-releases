/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectAccessibility;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.profile.domain.project.WikiMarkupLanguage;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.service.TaskServiceClient;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.api.ODCSServer;
import static org.netbeans.modules.odcs.client.ODCSClientTest.uname;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.client.api.ODCSFactory;

/**
 *
 * @author tomas
 */
public final class TestUtils {
    public static String TEST_PRODUCT = "Unit Test Product";
    public static final String TEST_COMPONENT1 = "Component1";
    public static final String TEST_COMPONENT2 = "Component2";
    public static final String TEST_COMPONENT3 = "Component3";
    
    public static final String TEST_USER1 = "tina.testsuite";
    public static final String TEST_USER2 = "tom.testsuite";
    
    public static final String MY_PROJECT = "qa-dev_netbeans-test"; 
    
    public static void ensureTestProject(String url, String uname, String passw) throws ODCSException, MalformedURLException, EntityNotFoundException, ValidationException, InterruptedException {
        ODCSClientImpl client = getClient(url, uname, passw);
        
        Project project = null;
        try {
            project = client.getProjectById(MY_PROJECT);
        } catch (ODCSException ex) {
            if(ex.getCause() instanceof EntityNotFoundException) {
                // not found
            } else {
                throw ex;
            }            
        }
        
        if(project == null) {        
            System.out.println(" + createTestProject " + MY_PROJECT);
            ODCSServer server = ODCSManager.getDefault().createServer("dcs test", url);
            server.login(uname, passw.toCharArray());
            String name = MY_PROJECT.substring("qa-dev".length() + 1);
            ODCSProject p = server.createProject(name, "NetBeans dummy testing project", ProjectAccessibility.PRIVATE.name(), WikiMarkupLanguage.CONFLUENCE.name());
            project = getProjectWhenInitialized(MY_PROJECT, client);                      

            client.addMember(project, TEST_USER2);
            client.addMember(project, TEST_USER2);
            createProduct(client, project, TEST_PRODUCT, Arrays.asList(TEST_COMPONENT1, TEST_COMPONENT2, TEST_COMPONENT3), TEST_USER1);
        }
    }

    private static Project getProjectWhenInitialized(String projectId, ODCSClientImpl client) throws InterruptedException, IllegalStateException {
        long t = 0;
        Project project = null;
        while(true) {
            try {
                project = client.getProjectById(projectId);
                if(project != null) {
                    List<ProjectService> services = project.getProjectServices();
                    Set<ServiceType> expectedServices = EnumSet.of(ServiceType.SCM, ServiceType.TASKS, ServiceType.WIKI, ServiceType.BUILD);
                    for (ProjectService s : services) {
                        if(!s.isAvailable()) {
                            break;
                        }
                        expectedServices.remove(s.getServiceType());
                    }
                    if(expectedServices.isEmpty()) {
                        break;
                    } else {                        
                        t = waitABit(t);
                    }
                } else {
                    t = waitABit(t);
                }
            } catch (ODCSException ex) {
                if(ex.getCause() instanceof EntityNotFoundException) {
                    // not found
                    t = waitABit(t);
                }
            }
        }
        return project;
    }

    private static long waitABit(long t) throws InterruptedException, IllegalStateException {
        long wait = 10000;
        Thread.sleep(wait);
        if(t + wait > 180000) {
            throw new IllegalStateException("timeout");
        }
        return t + wait;
    }    
    
    static ODCSClientImpl getClient(String url, String uname, String passw) {
        ODCSClient client = ODCSFactory.getInstance().createClient(url, new PasswordAuthentication(uname, passw.toCharArray()));        
        return (ODCSClientImpl) client;
    }    
    
    private static void createProduct(ODCSClientImpl odcsClient, Project project, String productName, List<String> components, String user) throws EntityNotFoundException, ValidationException, ODCSException {        
        TaskServiceClient tasksClient = odcsClient.getTasksClient(project.getIdentifier());        
        
        Product product = getProduct(tasksClient, productName);
        if(product == null) {
            product = new Product();
            product.setName(productName);                                    
            tasksClient.createProduct(product);
            
            product = getProduct(tasksClient, productName);
            if(product == null) {
                throw new IllegalStateException("created product should not be null");            
            }
        }
        List<TaskUserProfile> users = odcsClient.getRepositoryContext(product.getName()).getUsers();
        TaskUserProfile tup = null;
        for (TaskUserProfile u : users) {
            if(u.getLoginName().equals(user)) {                    
                tup = u;
                break;
            }
        }
        for (String component : components) {
            Component c = new Component();
            c.setName(component);
            c.setDescription(component);
            c.setProduct(product);
            c.setInitialOwner(tup);
            tasksClient.createComponent(c);
        }
        
        tasksClient.createIteration(new Iteration("first iteration"));
        tasksClient.createIteration(new Iteration("second iteration"));
        
        tasksClient.createCustomField(new FieldDescriptor("customfield", "this is a custom field", FieldType.TEXT));

    }

    protected static Product getProduct(TaskServiceClient tasksClient, String productName) {
        List<Product> products = tasksClient.getRepositoryContext().getProducts();
        for (Product p : products) {
            if(p.getName().equals(productName)) {
                return p;
            }
        }
        return null;
    }
}
