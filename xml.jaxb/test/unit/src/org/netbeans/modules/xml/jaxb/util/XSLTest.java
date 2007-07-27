/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.jaxb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author gmpatil
 */
public class XSLTest extends NbTestCase {
    private static final String CONFIG_FILE1 = "/data/ConfigFile1.xml"; //NOI18N
    private static final String CONFIG_FILE2 = "/data/ConfigFile2.xml"; //NOI18N    
    private static final String CONFIG_FILE3 = "/data/ConfigFile3.xml"; //NOI18N        
    private static final String BUILD_FILE1 = "/data/BuildFile1.xml";   //NOI18N
    private static final String BUILD_FILE2 = "/data/BuildFile2.xml"; //NOI18N 
    private static final String BUILD_FILE3 = "/data/BuildFile3.xml"; //NOI18N     
    
    private static final String XSL_FILE = 
            "/org/netbeans/modules/xml/jaxb/resources/JAXBBuild.xsl"; //NOI18N
    private static final String TEMP_BUILD_FILE = "jaxb_build" ; //NOI18N
    
    public XSLTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
    }
    
    public void tearDown() throws Exception {
    }
    
    private InputStream getInputStream(String filePath){
        return this.getClass().getResourceAsStream(filePath);
    }
    
    private String getString(InputStream stream) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while (line != null){
            line = br.readLine();
            if (line != null){
                sb.append(line);
            }
        }
        
        return sb.toString();
    }
    
    private void compareStream(InputStream file1, InputStream file2) throws IOException{
        boolean ret = false;
        String str1 = getString(file1);
        //System.out.println("Str1:" + str1 + ":Str1");
        String str2 = getString(file2);
        //System.out.println("Str2:" + str2 + ":Str2");        
        //System.out.println("Length:" + str1.length() + ":" + str2.length());                                
        assertEquals(str1, str2);
    }
    
    private void transformConfig2Build(String configFile, String buildFile){
        try {
            Source xmlSource = new StreamSource(getInputStream(configFile));
            Source xslSource = new StreamSource(getInputStream(XSL_FILE));
            File tmpFile = java.io.File.createTempFile(TEMP_BUILD_FILE, ".xml");
            //System.out.println("tmpFile:" + tmpFile.getAbsolutePath());
            tmpFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tmpFile);
            Result result = new StreamResult(fos);
            TransformerFactory fact = TransformerFactory.newInstance();
            fact.setAttribute("indent-number", 4); //NOI18N
            Transformer xformer = fact.newTransformer(xslSource);
            xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
            xformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
            xformer.transform(xmlSource, result);
            // Compare.
            fos.close();
            compareStream(getInputStream(buildFile), new FileInputStream(tmpFile));
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            fail("TransformerConfigurationException");
        } catch (TransformerException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            fail("TransformerException");
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            fail("IOException");
        }
    }
    
    /**
     * Test the XSL style sheet.
     **/
    public void testXformConfig2BuildWithPkg(){
        transformConfig2Build(CONFIG_FILE1, BUILD_FILE1);
        System.out.println("testXformConfig2BuildWithPkg done.");
    }

    /**
     * Test the XSL style sheet.
     **/
    public void testXformConfig2BuildWithoutPkg(){
        transformConfig2Build(CONFIG_FILE2, BUILD_FILE2);
        System.out.println("testXformConfig2BuildWithoutPkg done.");
    }

    /**
     * Test the XSL style sheet.
     **/
    public void testXformConfig2BuildEmptySchema(){
        transformConfig2Build(CONFIG_FILE3, BUILD_FILE3);
        System.out.println("testXformConfig2BuildEmptySchema done.");
    }
    
}
