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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import static org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server.*;
import static org.netbeans.junit.NbModuleSuite.Configuration;
/**
 *
 * @author Jindrich Sedek
 */
public class J2eeTestCase extends JellyTestCase {
    private static final String PID_FILE_PREFIX = "J2EE_TEST_CASE_PID_FILE";
    private static final String GLASSFISH_PATH = "com.sun.aas.installRoot";
    private static final String TOMCAT_PATH = "org.netbeans.modules.tomcat.autoregister.catalinaHome";
    private static final String JBOSS_PATH = "org.netbeans.modules.j2ee.jboss4.installRoot";
    private static final String GLASSFISH_HOME = "glassfish.home";
    private static final String TOMCAT_HOME = "tomcat.home";
    private static final String JBOSS_HOME = "jboss.home";
    private static final Logger LOG = Logger.getLogger(J2eeTestCase.class.getName());
    private static boolean serversLogged = false;

    public J2eeTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createPid();
    }

    /**
     * Create a temp file starting with J2EE_TEST_CASE_PID_FILE and ending with 
     * the pid of the test process. The pid is used by hudson to print stacktrace
     * before aborting build because of timeout.
     * 
     * @throws java.io.IOException
     */
    private void createPid() throws IOException{
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(0, pid.indexOf('@'));
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath);
        for (String file : tmpDir.list()) {
            if (file.startsWith(PID_FILE_PREFIX)){
                if ( !(new File(tmpDir, file).delete()) )
                    LOG.warning("File '"+ tmpDirPath + File.pathSeparator + file + "' not successfully deleted!");
            }
        }
        if (! (new File(tmpDir, PID_FILE_PREFIX + pid).createNewFile()))
            LOG.warning("File '"+ tmpDirPath + File.pathSeparator + PID_FILE_PREFIX + pid + "' not successfully created!");;
    }
    
    private static void registerGlassfish() {
        String glassfishPath = getServerHome(GLASSFISH);
        if (isValidPath(glassfishPath) && isValidPath(glassfishPath + "/domains/domain1")) {
            LOG.info("Setting server path " + glassfishPath);
            System.setProperty(GLASSFISH_PATH, glassfishPath);
        }
    }

    private static void registerTomcat() {
        String tomcatPath = getServerHome(TOMCAT);
        if (isValidPath(tomcatPath)) {
            LOG.info("Setting server path " + tomcatPath);
            System.setProperty(TOMCAT_PATH, tomcatPath);
            System.setProperty("org.netbeans.modules.tomcat.autoregister.token", "1");
        }
    }

    private static void registerJBoss() {
        String jbossPath = getServerHome(JBOSS);
        if (isValidPath(jbossPath)) {
            LOG.info("Setting server path " + jbossPath);
            System.setProperty(JBOSS_PATH, jbossPath);
        }
    }

    private static String getServerHome(Server server){
        switch (server){
            case JBOSS:
                return System.getProperty(JBOSS_HOME);
            case GLASSFISH:
                String glassfishPath = System.getProperty(GLASSFISH_HOME);
                if (glassfishPath == null){
                    glassfishPath = System.getProperty("j2ee.appserver.path");
                }
                return glassfishPath;
            case TOMCAT:
                return System.getProperty(TOMCAT_HOME);
        }
        return null;
    }

    private static boolean isValidPath(String path) {
        if (path == null) {
            return false;
        }
        LOG.info("Validating path: " + path);
        File f = new File(path);
        if (f.isDirectory()) {
            LOG.info(path + " - is valid directory");
            return true;
        } else {
            if(!f.exists()) {
                LOG.info(path + " - does not exists!");
            } else {
                LOG.info(path + " - exists, but it is not a directory!");
            }
            return false;
        }
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
            if (server.equals(GLASSFISH) || server.equals(ANY)){
                registerGlassfish();
                if (isRegistered(GLASSFISH)) {
                    return addTest(conf, clazz, testNames);
                }
            }
            if (server.equals(TOMCAT) || server.equals(ANY)){
                registerTomcat();
                if (isRegistered(TOMCAT)) {
                    return addTest(conf, clazz, testNames);
                }
            }
            if (server.equals(JBOSS) || server.equals(ANY)){
                registerJBoss();
                if (isRegistered(JBOSS)) {
                    return addTest(conf, clazz, testNames);
                }
            }
            LOG.info("no server to add tests");
            if (!serversLogged){
                serversLogged = true;
                logServer(JBOSS_HOME, getServerHome(JBOSS));
                logServer(TOMCAT_HOME, getServerHome(TOMCAT));
                logServer(GLASSFISH_HOME, getServerHome(GLASSFISH));
            }
            try{
                return conf.addTest("testEmpty");
            }catch (IllegalStateException exc){
                //empty configuration
                return conf.addTest(J2eeTestCase.class, "testEmpty");
            }
        }
    }
    
    private static void logServer(String propName, String value){
        if (value == null){
            LOG.info(propName + " is not set");
        }else{
            LOG.info(propName + " is " + value);
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
                return J2eeServerNode.invoke("GlassFish");
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
        if ((testNames == null) || (testNames.length == 0)){
            return conf;
        }
        if (clazz == null){
            return conf.addTest(testNames);
        }else{
            return conf.addTest(clazz, testNames);
        }
    }

    /**
     * Resolve missing server. This method should be called after opening some project.
     * If the Missing server dialog appears, it's closed and first server from
     * project properties is used to resolve the missing server problem.
     *
     * Project build script regeneration dialog is closed as well if it appears.
     * @param projectName name of project
     */
    protected void resolveServer(String projectName) {
        waitScanFinished();
        String openProjectTitle = Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "MSG_Broken_Server_Title");
        if (JDialogOperator.findJDialog(openProjectTitle, true, true) != null) {
            new NbDialogOperator(openProjectTitle).close();
            LOG.info("Resolving server");
            // open project properties
            ProjectsTabOperator.invoke().getProjectRootNode(projectName).properties();
            // "Project Properties"
            String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
            NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
            // select "Run" category
            new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
            // set default server
            new JComboBoxOperator(propertiesDialogOper).setSelectedIndex(0);
            propertiesDialogOper.ok();
            // if setting default server, it scans server jars; otherwise it continues immediatelly
            waitScanFinished();
        }
        String editPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.Bundle", "TXT_BuildImplRegenerateTitle");
        int count = 0;
        while ((JDialogOperator.findJDialog(editPropertiesTitle, true, true) != null) && (count < 10)) {
            count++;
            JDialogOperator dialog = new NbDialogOperator(editPropertiesTitle);
            String regenerateButtonTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.Bundle", "CTL_Regenerate");
            JButtonOperator butt = new JButtonOperator(dialog, regenerateButtonTitle);
            butt.push();
            LOG.info("Closing buildscript regeneration");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException exc) {
                LOG.log(Level.INFO, "interrupt exception", exc);
            }
            if (dialog.isVisible()){
                dialog.close();
            }
        }
    }   
}
