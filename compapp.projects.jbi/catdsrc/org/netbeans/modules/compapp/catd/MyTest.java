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

package org.netbeans.modules.compapp.catd;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;

import org.w3c.dom.Document;
//import com.meterware.httpunit.*;
import junit.framework.*;

/**
 * An example of testing servlets using httpunit and JUnit.
 **/
public class MyTest extends TestCase {
/*TODO ADD
    private DocumentBuilder builder;
    private Properties testProps;
    
    public static void main(String args[]) {
        try {
            junit.textui.TestRunner.run( suite() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static Test suite() throws Exception {
        junit.framework.TestSuite suite = new junit.framework.TestSuite();
        
        
        // Create a test for each relevant .properties file
        File[] inputDir = new File("input").listFiles();
        if (inputDir != null) {
            for (int count = 0; count < inputDir.length; count++) {
                final String testPropertiesPostfix = ".properties";
                
                if (inputDir[count].isDirectory()) {
                    FileFilter testPropertiesFilter = new FileFilter() {
                        public boolean accept(File f) {
                            if (f.isDirectory()) return false;
                            return f.getName().endsWith(testPropertiesPostfix);
                        }
                    };
                    
                    File[] testPropertiesFiles = inputDir[count].listFiles(testPropertiesFilter);
                    
                    if (testPropertiesFiles != null) {
                        for (int testCnt = 0; testCnt < testPropertiesFiles.length; testCnt++) {
                            String testPropertiesFile = testPropertiesFiles[testCnt].getAbsolutePath();
                            
                            Properties testProps = loadProperties(testPropertiesFile);
                            testProps.put("testpropertiesfilename", testPropertiesFiles[testCnt].getName());
                            testProps.put("absoluteinputdir", inputDir[count].getAbsolutePath());
                            testProps.put("inputdirname", inputDir[count].getName());
                            
                            // Invoke.properties files define simple service invocation tests
                            suite.addTest(new MyTest("testWSDL", testProps));
                        }
                    }
                }
            }
        }
        
        return suite;
    }
    
    static Properties loadProperties(String propertiesFile) throws IOException{
        FileInputStream fis = null;
        Properties props = null;
        try {
            fis = new FileInputStream(new File(propertiesFile));
            props = new java.util.Properties();
            props.load(fis);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        return props;
    }
    
    public MyTest( String name, Properties props ) {
        super( name );
        testProps = props;
    }
    
    protected void setUp() {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            builder = fact.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void testWSDL() throws Exception {
        WebConversation     conversation = new WebConversation();
        String destination = testProps.getProperty("destination");
        if (destination != null && destination.length() > 0) {
            WebRequest request = new GetMethodWebRequest( destination + "?WSDL" );

            WebResponse response = conversation.getResponse( request );

            int i = response.getContentLength();
            InputSource is = new InputSource(response.getInputStream());

            Document doc = builder.parse(is);

            doc.getDocumentElement();
            System.out.println("My test");
        }
        
    }
*/
}
