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
package org.netbeans.jellytools.modules.j2ee;

import java.io.File;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.junit.NbModuleSuite;
import static org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server.*;
import static org.netbeans.junit.NbModuleSuite.Configuration;
/**
 *
 * @author Jindrich Sedek
 */
public class J2eeTestCase extends JellyTestCase {

    private static final String GLASSFISH_PATH = "com.sun.aas.installRoot";
    private static final String TOMCAT_PATH = "org.netbeans.modules.tomcat.autoregister.catalinaHome";
    private static final String JBOSS_PATH = "org.netbeans.modules.j2ee.jboss4.installRoot";
    private static final Logger LOG = Logger.getLogger(J2eeTestCase.class.getName());

    public J2eeTestCase(String name) {
        super(name);
    }

    private static Configuration addGlassfishTests(Configuration conf, Class<? extends TestCase> clazz, String... testNames) {
        Configuration result = conf;
        String glassfishPath = System.getProperty("j2ee.appserver.path");
        if (isValidPath(glassfishPath) && isValidPath(glassfishPath + "/domains/domain1")) {
            LOG.info("Setting server path " + glassfishPath);
            System.setProperty(GLASSFISH_PATH, glassfishPath);
            result = addTest(conf, clazz, testNames);
        }else{
            conf.addTest("testEmpty");
        }
        return result;
    }

    private static Configuration addTomcatTests(Configuration conf, Class<? extends TestCase> clazz, String... testNames) {
        Configuration result = conf;
        String tomcatPath = System.getProperty("tomcat.home");
        if (isValidPath(tomcatPath)) {
            LOG.info("Setting server path " + tomcatPath);
            System.setProperty(TOMCAT_PATH, tomcatPath);
            System.setProperty("org.netbeans.modules.tomcat.autoregister.token", "1");
            result = addTest(conf, clazz, testNames);
        }else{
            conf.addTest("testEmpty");
        }
        return result;
    }

    private static Configuration addJBossTests(Configuration conf, Class<? extends TestCase> clazz, String... testNames) {
        Configuration result = conf;
        String jbossPath = System.getProperty("jboss.home");
        if (isValidPath(jbossPath)) {
            LOG.info("Setting server path " + jbossPath);
            System.setProperty(JBOSS_PATH, jbossPath);
            result = addTest(conf, clazz, testNames);
        }else{
            conf.addTest("testEmpty");
        }
        return result;
    }

    private static boolean isValidPath(String path) {
        if (path == null) {
            return false;
        }
        File f = new File(path);
        if (!f.exists()) {
            return false;
        }
        return f.isDirectory();
    }
    
    /**
     *
     * Create all modules suite. 
     * 
     * @param server server needed for the suite
     * @param clazz class object to create suite for
     * @param testNames test names to add into suite
     * @return executable Test instance 
     */
    protected static Test createAllModulesServerSuite(Server server, Class<? extends TestCase> clazz, String... testNames){
        Configuration result = NbModuleSuite.createConfiguration(clazz);
        result = addServerTests(server, result, testNames).enableModules(".*").clusters(".*");
        return NbModuleSuite.create(result);
    }

    /**
     * Add tests into configuration. Tests are added only if it's sure there 
     * is some server registered in the IDE.
     * 
     * @param conf test configuration
     * @param testNames names of added tests
     * @return clone of the test configuration
     */
    protected static Configuration addServerTests(Configuration conf, String... testNames) {
        return addServerTests(ANY, conf, testNames);
    }
    
    /**
     * Add tests into configuration.
     * Tests are added only if there is the server instance registered in the 
     * IDE.
     * 
     * @param server server that is needed by tests
     * @param conf test configuration
     * @param testNames names of added tests
     * @return clone of the test configuration
     */
    protected static Configuration addServerTests(Server server, Configuration conf, String... testNames) {
        return addServerTests(server, conf, null, testNames);
    }
    
    /**
     * Add tests into configuration.
     * Tests are added only if there is the server instance registered in the 
     * IDE.
     * 
     * @param server server that is needed by tests
     * @param conf test configuration
     * @param clazz tested class
     * @param testNames names of added tests
     * @return clone of the test configuration
     */
    protected static Configuration addServerTests(Server server, Configuration conf, Class<? extends TestCase> clazz, String... testNames) {
        if (isRegistered(server)) {
            LOG.info("adding server tests");
            return addTest(conf, clazz, testNames);
        } else {
            Configuration result = conf;
            if (server.equals(TOMCAT) || server.equals(ANY)){
                result = addTomcatTests(conf, clazz, testNames);
                if (isRegistered(TOMCAT)) {
                    return result;
                }
            }
            if (server.equals(GLASSFISH) || server.equals(ANY)){
                result = addGlassfishTests(conf, clazz, testNames);
                if (isRegistered(GLASSFISH)) {
                    return result;
                }
            }
            if (server.equals(JBOSS) || server.equals(ANY)){
                result = addJBossTests(conf, clazz, testNames);
                if (isRegistered(JBOSS)) {
                    return result;
                }
            }
            LOG.info("no server to add tests");
            return conf.addTest("testEmpty");
        }
    }
    /**
     * Returns <code>true</code> if given server is registered in the IDE, 
     * <code>false</code> otherwise
     * @param server to decide about
     * @return <code>true</code> if the <code>server</code> is registered
     */
    protected static boolean isRegistered(Server server) {
        boolean result;
        switch (server){
            case GLASSFISH:
                result =  System.getProperty(GLASSFISH_PATH) != null;
                break;
            case JBOSS:
                result = System.getProperty(JBOSS_PATH) != null;
                break;
            case TOMCAT:
                result = System.getProperty(TOMCAT_PATH) != null;
                break;
            case ANY:
                for (Server serv : Server.values()) {
                    if (serv.equals(ANY)){
                        continue;
                    }
                    if (isRegistered(serv)) {
                        return true;
                    }
                }
                return false;
            default: 
                throw new IllegalArgumentException("Unsupported server");
        }
        return result;
    }
    
    /**
     * Returns J2eeServerNode for given server
     * 
     * @param server 
     * @return J2eeServerNode for given server
     */
    protected J2eeServerNode getServerNode(Server server){
        if (!isRegistered(server)){
            throw new IllegalArgumentException("Server is not registred in IDE");
        }
        switch (server){
            case GLASSFISH:
                return J2eeServerNode.invoke("GlassFish V2");
            case JBOSS: 
                return J2eeServerNode.invoke("JBoss");
            case TOMCAT:
                return J2eeServerNode.invoke("Tomcat");
            case ANY:
                for (Server serv : Server.values()) {
                    if (serv.equals(ANY)){
                        continue;
                    }
                    if (isRegistered(serv)) {
                        return getServerNode(serv);
                    }
                }
                throw new IllegalArgumentException("No server is registred in IDE");
            default:
                throw new IllegalArgumentException("Unsupported server");
        }
    }
    
    public static enum Server {

        TOMCAT, GLASSFISH, JBOSS, ANY
    }
    /**
     * Empty test is executed while there is missing server and other tests would 
     * fail because of missing server.
     */
    public void testEmpty(){
        // nothing to do
    }
    
    private static Configuration addTest(Configuration conf, Class<? extends TestCase> clazz, String... testNames){
        if (clazz == null){
            return conf.addTest(testNames);
        }else{
            return conf.addTest(clazz, testNames);
        }
    }
    
}
