/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * Util.java
 *
 * Created on March 25, 2005, 2:56 PM
 */

package org.netbeans.modules.compapp.catd.util;

import org.netbeans.modules.compapp.projects.jbi.util.EditableProperties;
import com.sun.esb.management.api.configuration.ConfigurationService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import junit.framework.TestCase;
import org.netbeans.modules.compapp.projects.jbi.AdministrationServiceHelper;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstanceReader;

/**
 *
 * @author blu
 */
public class Util {

    public static String getFileContent(File f) {
        String ret = null;
        InputStreamReader input = null;
        StringWriter output = null;
        try {
            input = new InputStreamReader(new FileInputStream(f), "UTF-8");
            output = new StringWriter();
            char[] buf = new char[1024];
            int n = 0;
            while ((n = input.read(buf)) != -1) {
                output.write(buf, 0, n);
            }
            output.flush();
            ret = output.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return ret;
    }

    public static String getFileContentWithoutCRNL(File f) {
        String ret = null;
        InputStreamReader input = null;
        StringWriter output = null;
        try {
            input = new InputStreamReader(new FileInputStream(f), "UTF-8");
            output = new StringWriter();
            char[] buf = new char[1024];
            int n = 0;
            while ((n = input.read(buf)) != -1) {
                output.write(buf, 0, n);
            }
            output.flush();
            ret = output.toString();
            ret = ret.replaceAll("\n","");
            ret = ret.replaceAll("\r","");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return ret;
    }

    public static List<Set<String>> getFileContentWithoutCRNL(File f, int linesPerElement, int[] setSizes) {
        List<Set<String>> setList = new ArrayList<Set<String>>();
        List<String> lineList = new ArrayList<String>();
        List<String> elementList = new ArrayList<String>();
        // read lines
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String s = null;
            while ((s = input.readLine()) != null) {
                s = s.replaceAll("\n","").replaceAll("\r","");
                lineList.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // calculate elements
        int elementCount = lineList.size() / linesPerElement;
        for (int i = 0; i < elementCount; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < linesPerElement; j++) {
               sb.append(lineList.get(i*linesPerElement + j));
            }
            elementList.add(sb.toString());
        }

        // calculate sets
        for (int i = 0; i < setSizes.length; i++) {
            HashSet<String> set = new HashSet<String>();
            setList.add(set);
            for (int j = 0; j < setSizes[i]; j++) {
                if (elementList.isEmpty()) {
                    break;
                }
                set.add(elementList.remove(0));
            }
        }
        return setList;
    }

    public static String replaceAll(String s, String match, String replacement) {
        StringBuffer sb = new StringBuffer();
        String temp = s;
        while (true) {
            int i = temp.indexOf(match);
            if (i < 0) {
                sb.append(temp);
                return sb.toString();
            }
            sb.append(temp.substring(0, i));
            sb.append(replacement);
            temp = temp.substring(i + match.length());
        }
    }

    /**
     * Gets the server instance configuration.
     * 
     * @param netBeansUserDir   NetBeans user directory
     * @return  server instance configuration
     */
    private static ServerInstance getServerInstance(String netBeansUserDir) {
        ServerInstance instance = null;
        
        if (netBeansUserDir != null) {
            String j2eeServerInstanceUrl = null;
            try {
                Properties privateProps = loadProperties("nbproject/private/private.properties");
                j2eeServerInstanceUrl = (String) privateProps.get("j2ee.server.instance");
            } catch (IOException ex) {
                System.err.println("Error: Failed to load project properties.");
            }

            if (j2eeServerInstanceUrl != null) {
                String settingsFileName = netBeansUserDir + ServerInstanceReader.RELATIVE_FILE_PATH;
                File settingsFile = new File(settingsFileName);
                if (settingsFile.exists()) {
                    ServerInstanceReader settings = new ServerInstanceReader(settingsFileName);
                    List<ServerInstance> list = settings.getServerInstances();
                    for (ServerInstance serverInstance : list) {
                        String url = serverInstance.getUrl();
                        if (j2eeServerInstanceUrl.equals(url)) {
                            instance = serverInstance;
                            break;
                        }
                    }
                }
            }
        }

        return instance;
    }

    /**
     * Utility method to load a properties file
     */
    public static Properties loadProperties(String propertiesFile) throws IOException {
        Properties props = new Properties();
        // EditableProperties takes case of encoding.
        EditableProperties editableProps = new EditableProperties();
        editableProps.load(new FileInputStream(propertiesFile));
        for (String key : editableProps.keySet()) {
            props.put(key, editableProps.getProperty(key));
        }

        return props;
    }

    /**
     * If default properties file name is AAA.properties, and context is "xx_yy"
     * Then load properties in order AAA.properties, AAA_xx.properties, AAA_xx_yy.properties with the property value
     * loaded later overwriting the property value loaded earlier. That is, AAA_xx_yy.properties has higher priority
     * than AAA_xx.properties, which in turn has higher priority than AA.properties.
     * 
     * @param propertiesFile The default properties file.
     * @param context Format xx, xx_yy, xx_yy_zz, etc
     * @return Properties 
     * @throws java.io.IOException
     */
    public static Properties loadProperties(String propertiesFile, String context) throws IOException {
        String[] contextComponent = new String[0];
        if (context != null && !context.trim().equals("")) {
            LinkedList<String> contextComponentList = new LinkedList<String>();
            StringTokenizer st = new StringTokenizer(context.trim(), "_");
            while (st.hasMoreTokens()) {
                contextComponentList.add(st.nextToken());
            }
            contextComponent = contextComponentList.toArray(new String[0]);
        }    
        Properties ret = loadProperties(propertiesFile);
        StringBuffer sb = new StringBuffer(propertiesFile.substring(0, propertiesFile.length() - 11)); // 11: .properties
        for (int i = 0; i < contextComponent.length; i++) {
            sb.append("_" + contextComponent[i]);
            String contextPropFilePath = sb.toString() + ".properties";
            File contextPropFile = new File(contextPropFilePath);
            if (contextPropFile.exists() && contextPropFile.isFile()) {
                Properties props = loadProperties(contextPropFilePath);
                for (Object key : props.keySet()) {
                    ret.put(key, props.getProperty((String)key));
                }    
            }
        }
        return ret;
    }
    
    /**
     * Send a soap message
     * @param destination URL to send to
     * @param message message to send
     * @param expectedHttpStatus expected http status code or null if success is expected
     * @return reply soap message
     */
    public static SOAPMessage sendMessage(String logPrefix,
            boolean logDetails,
            SOAPConnection connection,
            String destination,
            SOAPMessage message,
            String expectedHttpStatus,
            String expectedHttpWarning,
            String soapAction) throws SOAPException, Exception {

        // Add soapAction if not null
        if (soapAction != null) {
            MimeHeaders hd = message.getMimeHeaders();
            hd.setHeader("SOAPAction", soapAction);
        }

        // Store standard error output temporarily if we expect a certain error as we do not want
        // to see the SAAJ output in this case
        java.io.PrintStream origErr = null;
        java.io.ByteArrayOutputStream bufferedErr = null;
        java.io.PrintStream stdErr = null;
        if ((expectedHttpStatus != null && !expectedHttpStatus.startsWith("2")) || expectedHttpWarning != null) {
            origErr = System.err;
            bufferedErr = new java.io.ByteArrayOutputStream();
            stdErr = new java.io.PrintStream(bufferedErr);
            System.setErr(stdErr);
        }

        // Send the message and get a reply
        SOAPMessage reply = null;
        long start = 0;
        if (logDetails) {
            start = System.currentTimeMillis();
        }

        // Currently only deal with http soap bc because soap binding is the 
        // only supported binding type in test driver.
        if (destination.indexOf("${") != -1 && destination.indexOf("}") != -1) {

            String nbUserDir = System.getProperty("NetBeansUserDir");
            
            ServerInstance serverInstance = getServerInstance(nbUserDir);

            if (serverInstance == null) {
                throw new RuntimeException("Unknown server instance.");
            }
            
            // Translate ${HttpDefaultPort} first
            String httpDefaultPort = "HttpDefaultPort";
            if (destination.indexOf("${" + httpDefaultPort + "}") != -1) {
                try {
                    ConfigurationService configService =
                            AdministrationServiceHelper.getConfigurationService(serverInstance);
                    Map<String, Object> configMap =
                            configService.getComponentConfigurationAsMap(
                            "sun-http-binding", "server");
                    Object httpDefaultPortValue = configMap.get(httpDefaultPort);
                    System.out.println("");
                    if (httpDefaultPortValue != null) {
                        int httpDefaultPortIntValue =
                                Integer.parseInt(httpDefaultPortValue.toString());
                        if (httpDefaultPortIntValue != -1) {
                            destination = destination.replace("${" + httpDefaultPort + "}",
                                    "" + httpDefaultPortIntValue);
                            System.out.println("Replace '${HttpDefaultPort}' in WSDL soap location by '" +
                                    httpDefaultPortIntValue + "' defined in sun-http-binding.");
                        } else {
                            System.out.println("WARNING: 'HttpDefaultPort' is not defined in sun-http-binding.");
                        }
                    } else {
                        System.out.println("WARNING: 'HttpDefaultPort' is not found in sun-http-binding's component configuration.");
                    }
                } catch (Exception ex) {
                    if (stdErr != null) {
                        System.setErr(origErr);
                        stdErr.flush();
                        stdErr.close();
                        origErr.print(bufferedErr.toString());
                    }
                    throw ex;
                }
            }

            // Translate ${HttpsDefaultPort} next
            String httpsDefaultPort = "HttpsDefaultPort";
            if (destination.indexOf("${" + httpsDefaultPort + "}") != -1) {
                try {
                    ConfigurationService configService =
                            AdministrationServiceHelper.getConfigurationService(serverInstance);
                    Map<String, Object> configMap =
                            configService.getComponentConfigurationAsMap(
                            "sun-http-binding", "server");
                    Object httpsDefaultPortValue = configMap.get(httpsDefaultPort);
                    System.out.println("");
                    if (httpsDefaultPortValue != null) {
                        int httpsDefaultPortIntValue =
                                Integer.parseInt(httpsDefaultPortValue.toString());
                        if (httpsDefaultPortIntValue != -1) {
                            destination = destination.replace("${" + httpsDefaultPort + "}",
                                    "" + httpsDefaultPortIntValue);
                            System.out.println("Replace '${HttpsDefaultPort}' in WSDL soap location by '" +
                                    httpsDefaultPortIntValue + "' defined in sun-http-binding.");
                        } else {
                            System.out.println("WARNING: 'HttpsDefaultPort' is not defined in sun-http-binding.");
                        }
                    } else {
                        System.out.println("WARNING: 'HttpsDefaultPort' is not found in sun-http-binding's component configuration.");
                    }
                } catch (Exception ex) {
                    if (stdErr != null) {
                        System.setErr(origErr);
                        stdErr.flush();
                        stdErr.close();
                        origErr.print(bufferedErr.toString());
                    }
                    throw ex;
                }
            }
        }

        boolean httpSuccess = true;
        try {
            reply = connection.call(message, destination);
        } catch (SOAPException ex) {
            httpSuccess = false;
            // This currently relies on the implementation details
            // to check for the HTTP status as no standard way is currently provide by saaj
            // It expectes an exception message of the format "Bad response: (404Error"
            // - where 404 is the status code in this example
            if (expectedHttpStatus == null || (expectedHttpWarning != null && bufferedErr.toString().indexOf(expectedHttpWarning) < 0)) {
                if (stdErr != null) {
                    System.setErr(origErr);
                    stdErr.flush();
                    stdErr.close();
                    origErr.print(bufferedErr.toString());
                }
                throw ex;
            } else {
                if (ex.getMessage().indexOf(expectedHttpStatus) > -1) {
                    if (logDetails) {
                        System.out.println(logPrefix + " Expected HTTP status code " + expectedHttpStatus + " found in reply. ");
                    }
                } else {
                    if (stdErr != null) {
                        System.setErr(origErr);
                        stdErr.flush();
                        stdErr.close();
                        origErr.print(bufferedErr.toString());
                    }
                    TestCase.fail(logPrefix + " Expected HTTP status code " + expectedHttpStatus + " NOT found in reply: " + ex.getMessage());
                }
            }
        }
        long end = 0;
        if (logDetails) {
            end = System.currentTimeMillis();
        }

        // Ensure standard error isn't redirected/buffered anymore
        if (origErr != null) {
            System.setErr(origErr);
            if (stdErr != null) {
                stdErr.close();
            }
        }

        if (logDetails) {
            System.out.println(logPrefix + " Call took " + (end - start) + " ms");
        }

        // If the test expected the call to fail, check that it did.
        if (expectedHttpStatus != null && httpSuccess && !expectedHttpStatus.startsWith("2")) {
            TestCase.fail(logPrefix + " Call returned an unexpected 'success' HTTP status code instead of the expected HTTP status code " + expectedHttpStatus);
        }

        return reply;
    }    
}
