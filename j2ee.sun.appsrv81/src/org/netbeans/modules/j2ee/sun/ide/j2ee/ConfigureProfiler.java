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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
/**
 *
 * @author Ludovic Champenois
 */
public class ConfigureProfiler {
    
    
    private static final String ASENV_INSERTION_POINT_WIN_STRING    = "set AS_JAVA";
    private static final String ASENV_INSERTION_POINT_NOWIN_STRING  = "AS_JAVA";
    
    
    // replaces the AS_JAVA item in asenv.bat/conf
    public static boolean modifyAsEnvScriptFile( SunDeploymentManagerInterface dm, String targetJavaHomePath) {
        
            String ext = (isUnix() ? "conf" : "bat");
        File irf = dm.getPlatformRoot();
        if (null == irf || !irf.exists()) {
            return false;
        }
        String installRoot = irf.getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
        String asEnvScriptFilePath  = installRoot+"/config/asenv." + ext;
  //      System.out.println("asEnvScriptFilePath="+asEnvScriptFilePath);
        File asEnvScriptFile = new File(asEnvScriptFilePath);
        String lineBreak = System.getProperty("line.separator");
        
        try {
            
            String line;
            FileReader fr = new FileReader(asEnvScriptFile);
            BufferedReader br = new BufferedReader(fr);
            StringBuffer buffer = new StringBuffer();
            
            String asJavaString = (isUnix() ? ASENV_INSERTION_POINT_NOWIN_STRING : ASENV_INSERTION_POINT_WIN_STRING);
            
            // copy config file from disk into memory buffer and modify line containing AS_JAVA definition
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith(asJavaString)) {
                    buffer.append(asJavaString + "=" + targetJavaHomePath);
                } else {
                    buffer.append(line);
                }
                buffer.append(lineBreak);
            }
            br.close();
            
            // flush modified config file from memory buffer back to disk
            FileWriter fw = new FileWriter(asEnvScriptFile);
            fw.write(buffer.toString());
            fw.flush();
            fw.close();
            
            if (isUnix()) Runtime.getRuntime().exec("chmod a+r " + asEnvScriptFile.getAbsolutePath()); //NOI18N
            
            return true;
            
        } catch (Exception ex) {
            
            System.err.println("Modifying " + asEnvScriptFilePath + " failed!\n" + ex.getMessage());
            return false;
            
        }
        
    }
    
    // removes any existing 'profiler' element and creates new one using provided parameters (if needed)
    public static boolean removeProfilerInDOmain(DeploymentManagerProperties dmProps) {
        String domainScriptFilePath = dmProps.getLocation() +"/"+ dmProps.getDomainName() +
                "/config/domain.xml";                                           //NOI18N

        File domainScriptFile = new File(domainScriptFilePath);
        
        // Load domain.xml
        Document domainScriptDocument = loadDomainScriptFile(domainScriptFilePath);
        if (domainScriptDocument == null) return false;
        
        // Remove  'profiler' element(s)
        NodeList profilerElementNodeList = domainScriptDocument.getElementsByTagName("profiler");
        if (profilerElementNodeList != null && profilerElementNodeList.getLength() > 0){
            while (profilerElementNodeList.getLength() > 0) profilerElementNodeList.item(0).getParentNode().removeChild(profilerElementNodeList.item(0));
            // Save domain.xml
            return saveDomainScriptFile(domainScriptDocument, domainScriptFilePath);
        } else {
            return true;//no need to save.
        }

        
    
    }
   // removes any existing 'profiler' element and creates new one using provided parameters (if needed)
    public static boolean instrumentProfilerInDOmain(DeploymentManagerProperties dmProps, String nativeLibraryPath, String[] jvmOptions) {
        String domainScriptFilePath = dmProps.getLocation()+"/" + dmProps.getDomainName() +
                "/config/domain.xml";                                           //NOI18N

        File domainScriptFile = new File(domainScriptFilePath);
        
        // Load domain.xml
        Document domainScriptDocument = loadDomainScriptFile(domainScriptFilePath);
        if (domainScriptDocument == null) return false;
        
        // Remove any previously defined 'profiler' element(s)
        NodeList profilerElementNodeList = domainScriptDocument.getElementsByTagName("profiler");
        if (profilerElementNodeList != null && profilerElementNodeList.getLength() > 0)
            while (profilerElementNodeList.getLength() > 0) profilerElementNodeList.item(0).getParentNode().removeChild(profilerElementNodeList.item(0));
        
        // If no 'profiler' element needs to be defined, the existing one is simply removed (by the code above)
        // (This won't happen for NetBeans Profiler, but is a valid scenario)
        // Otherwise new 'profiler' element is inserted according to provided parameters
        if (nativeLibraryPath != null || jvmOptions != null) {
            
            // Create "profiler" element
            Element profilerElement = domainScriptDocument.createElement("profiler");
            profilerElement.setAttribute("enabled", "true");
            profilerElement.setAttribute("name", "NetBeansProfiler");
            if (nativeLibraryPath != null) profilerElement.setAttribute("native-library-path", nativeLibraryPath);
            
            // Create "jvm-options" element
            if (jvmOptions != null) {
                for (int i = 0; i < jvmOptions.length; i++) {
                    Element jvmOptionsElement = domainScriptDocument.createElement("jvm-options");
                    Text tt = domainScriptDocument.createTextNode(formatJvmOption(jvmOptions[i]));
                    jvmOptionsElement.appendChild(tt);
                    profilerElement.appendChild(jvmOptionsElement);
                }
            }
            
            // Find the "java-config" element
            NodeList javaConfigNodeList = domainScriptDocument.getElementsByTagName("java-config");
            if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
                System.err.println("ConfigFilesUtils: cannot find 'java-config' section in domain config file " + domainScriptFilePath);
                return false;
            }
            
            // Insert the "profiler" element as a first child of "java-config" element
            Node javaConfigNode = javaConfigNodeList.item(0);
            if (javaConfigNode.getFirstChild() != null) javaConfigNode.insertBefore(profilerElement, javaConfigNode.getFirstChild());
            else javaConfigNode.appendChild(profilerElement);
            
        }
        
        // Save domain.xml
        return saveDomainScriptFile(domainScriptDocument, domainScriptFilePath);
    }
    
    // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
    // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
    private static String formatJvmOption(String jvmOption) {
        if (jvmOption.indexOf("\\\"") != -1) {
            String modifiedOption = jvmOption.replaceAll("\\\\\"", "#"); // replace every \" by #
            modifiedOption = modifiedOption.replaceAll("\\\"", ""); // delete all "
            modifiedOption = modifiedOption.replaceAll("#", "\""); // replace every # by "
            return modifiedOption;
        }
        return jvmOption;
     }    
    // creates Document instance from domain.xml
    private static Document loadDomainScriptFile(String domainScriptFilePath) {
        
        Document document = null;
        
        try {
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            dBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
                    InputSource source = new InputSource(reader);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            });
            
            return dBuilder.parse(new File(domainScriptFilePath));
            
        } catch (Exception e) {
            System.err.println("ConfigFilesUtils: unable to parse domain config file " + domainScriptFilePath);
            return null;
        }
        
    }
    
    // saves Document to domain.xml
    private static boolean saveDomainScriptFile(Document domainScriptDocument, String domainScriptFilePath) {
        boolean result = false;
        
        FileWriter domainScriptFileWriter = null;
        
        try {
            
            domainScriptFileWriter = new FileWriter(domainScriptFilePath);
            
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, domainScriptDocument.getDoctype().getPublicId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, domainScriptDocument.getDoctype().getSystemId());
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                
                DOMSource domSource = new DOMSource(domainScriptDocument);
                StreamResult streamResult = new StreamResult(domainScriptFileWriter);
                
                transformer.transform(domSource, streamResult);
                result = true;
            } catch (Exception e) {
                System.err.println("ConfigFilesUtils: Unable to save domain config file " + domainScriptFilePath);
                result = false;
            }
            
        } catch (IOException ioex) {
            System.err.println("ConfigFilesUtils: cannot create output stream for domain config file " + domainScriptFilePath);
            result = false;
        } finally {
            try { if (domainScriptFileWriter != null) domainScriptFileWriter.close(); } catch (IOException ioex2) { System.err.println("SunAS8IntegrationProvider: cannot close output stream for " + domainScriptFilePath); };
        }
        
        return result;
    }
    
    public static boolean isUnix() {
        return File.separatorChar == '/';
    }
    
//////    public static boolean backupFile(String filename) {
//////        
//////        File source = new File(filename);
//////        File target = new File(filename + FILE_BACKUP_EXTENSION);
//////        
//////        if (!source.exists()) {
//////            System.err.println("ConfigFilesUtils: " + source.getAbsolutePath() + " to be backed up not found");
//////            return false;
//////        }
//////        
//////        if (target.exists()) if (!target.delete()) {
//////            System.err.println("ConfigFilesUtils: cannot delete backup target " + target.getAbsolutePath());
//////            return false;
//////        }
//////        
//////        // move source to target to correctly preserve file permissions
//////        if (!source.renameTo(target)) {
//////            System.err.println("ConfigFilesUtils: cannot backup " + source.getAbsolutePath() + " to " + target.getAbsolutePath());
//////            return false;
//////        }
//////        
//////        // re-create source file for further processing
//////        try {
//////            source = new File(filename);
//////            source.createNewFile();
//////            target = new File(source.getAbsolutePath() + FILE_BACKUP_EXTENSION);
//////            
//////            FileChannel sourceChannel = new FileOutputStream(source).getChannel();
//////            FileChannel targetChannel = new FileInputStream(target).getChannel();
//////            targetChannel.transferTo(0, targetChannel.size(), sourceChannel);
//////            targetChannel.close();
//////            sourceChannel.close();
//////            return true;
//////        } catch (Exception ex) {
//////            System.err.println("ConfigFilesUtils: error during copying " + target.getAbsolutePath() + " to " + source.getAbsolutePath() + "\n" + ex);
//////            return false;
//////        }
//////        
//////    }
    
////    public static boolean restoreFile(String filename) {
////        
////        File target = new File(filename);
////        File source = new File(filename + FILE_BACKUP_EXTENSION);
////        
////        if (!source.exists()) {
////            System.err.println("ConfigFilesUtils: cannot find " + source.getAbsolutePath() + " to restore " + target.getAbsolutePath());
////            return false;
////        }
////        
////        if (target.exists()) if (!target.delete()) {
////            System.err.println("ConfigFilesUtils: cannot delete modified " + target.getAbsolutePath());
////            return false;
////        }
////        
////        if (!source.renameTo(target)) {
////            System.err.println("ConfigFilesUtils: cannot restore " + source.getAbsolutePath() + " to " + target.getAbsolutePath());
////            return false;
////        }
////        
////        return true;
////        
////    }
    
}
