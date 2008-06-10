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
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import static org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server.*;
import static org.netbeans.junit.NbModuleSuite.Configuration;
/**
 *
 * @author Jindrich Sedek
 */
public class J2eeTestCase extends JellyTestCase {

    private static Map<Server, Boolean> isRegistered = new EnumMap<Server, Boolean>(Server.class);
    private static final String GLASSFISH_PATH = "com.sun.aas.installRoot";
    private static final String TOMCAT_PATH = "org.netbeans.modules.tomcat.autoregister.catalinaHome";
    private static final String JBOSS_PATH = "org.netbeans.modules.j2ee.jboss4.installRoot";
    private static final Logger LOG = Logger.getLogger(J2eeTestCase.class.getName());

    public J2eeTestCase(String name) {
        super(name);
    }

    protected static Configuration addGlassfishTests(Configuration conf, String... testNames) {
        Configuration result = conf;
        String glassfishPath = System.getProperty("j2ee.appserver.path");
        if (isValidPath(glassfishPath) && isValidPath(glassfishPath + "/domains/domain1")) {
            LOG.info("Setting server path " + glassfishPath);
            System.setProperty(GLASSFISH_PATH, glassfishPath);
            isRegistered.put(GLASSFISH, true);
            result = conf.addTest(testNames);
        }else{
            conf.addTest("testEmpty");
        }
        return result;
    }

    protected static Configuration addTomcatTests(Configuration conf, String... testNames) {
        Configuration result = conf;
        String tomcatPath = System.getProperty("tomcat.home");
        if (isValidPath(tomcatPath)) {
            LOG.info("Setting server path " + tomcatPath);
            System.setProperty(TOMCAT_PATH, tomcatPath);
            System.setProperty("org.netbeans.modules.tomcat.autoregister.token", "1");
            result = conf.addTest(testNames);
            isRegistered.put(TOMCAT, true);
        }else{
            conf.addTest("testEmpty");
        }
        return result;
    }

    protected static Configuration addJBossTests(Configuration conf, String... testNames) {
        Configuration result = conf;
        String jbossPath = System.getProperty("jboss.home");
        if (isValidPath(jbossPath)) {
            LOG.info("Setting server path " + jbossPath);
            System.setProperty(JBOSS_PATH, jbossPath);
            result = conf.addTest(testNames);
            isRegistered.put(JBOSS, true);
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

    protected static Configuration addServerTests(Configuration conf, String... testNames) {
        if (isServerRegistered()) {
            LOG.info("adding server tests");
            return conf.addTest(testNames);
        } else {
            Configuration result = conf;
            result = addTomcatTests(conf, testNames);
            if (isRegistered(TOMCAT)) {
                return result;
            }
            result = addGlassfishTests(conf, testNames);
            if (isRegistered(GLASSFISH)) {
                return result;
            }
            result = addJBossTests(conf, testNames);
            if (isRegistered(JBOSS)) {
                return result;
            }
            LOG.info("no server to add tests");
            return conf.addTest("testEmpty");
        }
    }

    protected static boolean isRegistered(Server server) {
        Boolean result = isRegistered.get(server);
        if (result == null){
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
                default: 
                    throw new IllegalArgumentException("Unsupported server");
            }
        }
        isRegistered.put(server, result);
        return result;
    }
    
    protected J2eeServerNode getServerNode(){
        for (Server serv : Server.values()) {
            if (isRegistered(serv)) {
                return getServerNode(serv);
            }
        }
        throw new IllegalArgumentException("No server is registred in IDE");
    }
    
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
            default:
                throw new IllegalArgumentException("Unsupported server");
        }
    }
    
    protected static boolean isServerRegistered() {
        for (Server serv : Server.values()) {
            if (isRegistered(serv)) {
                return true;
            }
        }
        return false;
    }

    protected static enum Server {

        TOMCAT, GLASSFISH, JBOSS
    }
    
    public void testEmpty(){
        // nothing to do
    }
}
