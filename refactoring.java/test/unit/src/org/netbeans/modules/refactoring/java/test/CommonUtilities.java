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

package org.netbeans.modules.refactoring.java.test;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.net.*;
import java.util.zip.*;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import org.netbeans.junit.NbPerformanceTest.PerformanceData;

/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class CommonUtilities {
    
    private static int size=0;
    private static DocumentBuilderFactory dbf=null;
    private static DocumentBuilder db=null;
    private static Document allPerfDoc=null;
    private static Element testResultsTag, testTag, perfDataTag, testSuiteTag=null;
    private static String projectsDir; // <nbextra>/data/
    private static String tempDir; // <nbjunit.workdir>/tmpdir/
    
    static {
        String workDir = System.getProperty("nbjunit.workdir");
        if (workDir != null) {
            projectsDir = workDir + File.separator;
            try {
                projectsDir = new File(projectsDir + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + "nbextra" + File.separator + "data")
                        .getCanonicalPath() + File.separator;
            } catch (IOException ex) {
                System.err.println("Exception: " + ex);
            }

            tempDir = workDir + File.separator;
            try {
                File dir = new File(tempDir + File.separator + "tmpdir");
                tempDir = dir.getCanonicalPath() + File.separator;
                dir.mkdirs();
            } catch (IOException ex) {
                System.err.println("Exception: " + ex);
            }
        }
    }
    
    /**
     * Returns data directory path ending with file.separator
     * @return &lt;nbextra&gt;/data/
     */
    public static String getProjectsDir() {
        return projectsDir;
    }

    /**
     * Returns temprorary directory path ending with file.separator
     * @return &lt;nbjunit.workdir&gt;/tmpdir/
     */
    public static String getTempDir() {
        return tempDir;
    }
    
    public static void cleanTempDir() throws IOException {
        File dir = new File(tempDir);
        deleteFile(dir);
        dir.mkdirs();
    }

    // private method for deleting a file/directory (and all its subdirectories/files)
    public static void deleteFile(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            
        }
        // file is a File :-)
        boolean result = file.delete();
        if (result == false ) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = " + file.getPath());
        }
    }
    
    /** Creates a new instance of Utilities */
    public CommonUtilities() {
    }

    public static String getTimeIndex() {
        return new SimpleDateFormat("HHmmssS",Locale.US).format(new Date());
    }
    
    public static String jEditProjectOpen() {

/* Temporary solution - download jEdit from internal location */

        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        int BUFFER = 2048;

        try {
            URL url = new URL("http://spbweb.russia.sun.com/~ok153203/jEdit41.zip");

            out = new BufferedOutputStream(new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip"));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }

        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(new File(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip"));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName()).mkdirs();
                    continue;
                }
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip";
    }
    
    /**
     * Copy file f1 to f2
     * @param f1 file 1
     * @param f2 file 2
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void copyFile(java.io.File f1, java.io.File f2) throws java.io.FileNotFoundException, java.io.IOException{
        int data;
        java.io.InputStream fis = new java.io.BufferedInputStream(new java.io.FileInputStream(f1));
        java.io.OutputStream fos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(f2));
        
        while((data=fis.read())!=-1){
            fos.write(data);
        }
    }
    
    public static void xmlTestResults(String path, String suite, String name, String classname, String sname, String unit, String pass, long threshold, long[] results, int repeat) {

        PrintStream out = System.out;

        System.out.println();
        System.out.println("#####  Results for "+name+"   #####");
        System.out.print("#####        [");
        for(int i=1;i<=repeat;i++)             
            System.out.print(results[i]+"ms, ");
        System.out.println("]");
        for (int i=1;i<=name.length()+27;i++)
            System.out.print("#");
        System.out.println();
        System.out.println();

        path=System.getProperty("nbjunit.workdir");
        File resGlobal=new File(path+File.separator+"allPerformance.xml");

        try {
            dbf=DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
         } catch (Exception ex) {
            ex.printStackTrace (  ) ;
        }

        if (!resGlobal.exists()) {
            try {
                resGlobal.createNewFile();
                out = new PrintStream(new FileOutputStream(resGlobal));
                out.print("<TestResults>\n");
                out.print("</TestResults>");
                out.close();
            } catch (IOException ex) {
            ex.printStackTrace (  ) ;
            }
         }

        try {
              allPerfDoc = db.parse(resGlobal);
            } catch (Exception ex) {
            ex.printStackTrace (  ) ;
            }
            
        testResultsTag=allPerfDoc.getDocumentElement();

        testTag=null;
        for (int i=0;i<allPerfDoc.getElementsByTagName("Test").getLength();i++) {
            if (("name=\""+name+"\"").equalsIgnoreCase( allPerfDoc.getElementsByTagName("Test").item(i).getAttributes().getNamedItem("name").toString() ) ) {
                testTag =(Element)allPerfDoc.getElementsByTagName("Test").item(i);
                break;
            }
        }

        if (testTag!=null) {
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) perfDataTag.setAttribute("runOrder", "1");
                    else perfDataTag.setAttribute("runOrder", "2");
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }
        else {
            testTag=allPerfDoc.createElement("Test");
            testTag.setAttribute("name", name);
            testTag.setAttribute("unit", unit);
            testTag.setAttribute("results", pass);
            testTag.setAttribute("threshold", new Long(threshold).toString());
            testTag.setAttribute("classname", classname);
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) perfDataTag.setAttribute("runOrder", "1");
                    else perfDataTag.setAttribute("runOrder", "2");
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }

            testSuiteTag=null;
            for (int i=0;i<allPerfDoc.getElementsByTagName("Suite").getLength();i++) {
                if (suite.equalsIgnoreCase(allPerfDoc.getElementsByTagName("Suite").item(i).getAttributes().getNamedItem("suitename").getNodeValue())) {
                    testSuiteTag =(Element)allPerfDoc.getElementsByTagName("Suite").item(i);
                    break;
                }
            }

            if (testSuiteTag==null) {
                testSuiteTag=allPerfDoc.createElement("Suite");
                testSuiteTag.setAttribute("name", sname);
                testSuiteTag.setAttribute("suitename", suite);
                testSuiteTag.appendChild(testTag);
            } else {
                testSuiteTag.appendChild(testTag);
            }

        testResultsTag.appendChild(testSuiteTag);


        try {
            out = new PrintStream(new FileOutputStream(resGlobal));
        } catch (FileNotFoundException ex) {
        }

        Transformer tr=null;
        try {
            tr = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
        }

        tr.setOutputProperty(OutputKeys.INDENT, "no");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource docSrc = new DOMSource(allPerfDoc);
        StreamResult result = new StreamResult(out);

        try {
            tr.transform(docSrc, result);
        } catch (TransformerException ex) {
        }
        out.close();
    }

    public static void processUnitTestsResults(String className, PerformanceData pd) {
        long[] result=new long[2];
        result[1]=pd.value;
        CommonUtilities.xmlTestResults(System.getProperty("nbjunit.workdir"), "Unit Tests Suite", pd.name, className, className, pd.unit, "passed", 120000 , result, 1);
    }
}
