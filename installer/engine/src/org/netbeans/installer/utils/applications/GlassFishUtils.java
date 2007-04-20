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
 *
 * $Id$
 */
package org.netbeans.installer.utils.applications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Kirill Sorokin
 */
public class GlassFishUtils {
    private static Map<File, Version> knownVersions = new HashMap<File, Version>();
    
    private GlassFishUtils() {
        // does nothing
    }
    
    public static void createDomain(File location, String domainName, String username, String password, String httpPort, String httpsPort, String adminPort) throws IOException {
        createDomain(location, domainName, username, password, httpPort, httpsPort, adminPort, null);
    }
    
    public static void createDomain(File location, String domainName, String username, String password, String httpPort, String httpsPort, String adminPort, String domainProperties) throws IOException {
        final File passwordFile = createPasswordFile(password, location);
        
        if (passwordFile == null) {
            throw new DomainCreationException();
        }
        
        final ExecutionResults results = SystemUtils.executeCommand(location,
                getAsadmin(location).getAbsolutePath(),
                "create-domain",
                "--interactive=false",
                "--adminport",
                adminPort,
                "--adminuser",
                username,
                "--passwordfile",
                passwordFile.getAbsolutePath(),
                "--instanceport",
                httpPort,
                "--domainproperties",
                "http.ssl.port=" + httpsPort + (domainProperties != null ? ":" + domainProperties : ""),
                "--savemasterpassword=true",
                domainName
                );
        
        if (results.getStdOut().indexOf(COULD_NOT_CREATE_DOMAIN_MARKER) != -1) {
            throw new DomainCreationException(CLI_130);
        }
        
        if (results.getErrorCode() > 0) {
            throw new DomainCreationException(results.getErrorCode());
        }
    }
    
    public static boolean startDomain(File location, String domainName) throws IOException {
        String executable = getAsadmin(location).getAbsolutePath();
        
        ExecutionResults results = SystemUtils.executeCommand(location,
                executable,
                "start-domain",
                domainName
                );
        
        return results.getErrorCode() != Integer.MAX_VALUE;
    }
    
    public static boolean startDefaultDomain(File location) throws IOException {
        return startDomain(location, DEFAULT_DOMAIN);
    }
    
    public static boolean stopDomain(File location, String domainName) throws IOException {
        String executable = getAsadmin(location).getAbsolutePath();
        
        ExecutionResults results = SystemUtils.executeCommand(location,
                executable,
                "stop-domain",
                domainName
                );
        
        return results.getErrorCode() != Integer.MAX_VALUE;
    }
    
    public static boolean stopDefaultDomain(File location) throws IOException {
        return stopDomain(location, DEFAULT_DOMAIN);
    }
    
    public static boolean deleteDomain(File location, String domainName) throws IOException {
        String executable = getAsadmin(location).getAbsolutePath();
        
        if (!stopDomain(location, domainName)) {
            return false;
        }
        
        ExecutionResults results = SystemUtils.executeCommand(location,
                executable,
                "delete-domain",
                domainName
                );
        
        return results.getErrorCode() != Integer.MAX_VALUE;
    }
    
    public static File createPasswordFile(String password, File location) throws IOException {
        File passwordFile = FileUtils.createTempFile(location);
        
        String contents =
                "AS_ADMIN_ADMINPASSWORD="  + password + "\n" +
                "AS_ADMIN_PASSWORD="       + password + "\n" +
                "AS_ADMIN_MASTERPASSWORD=" + password;
        
        FileUtils.writeFile(passwordFile, contents);
        
        return passwordFile;
    }
    
    public static File getJavaHome(File location) throws IOException {
        File asconf;
        
        if (SystemUtils.isWindows()) {
            asconf = new File(location, "config/asenv.bat");
        } else {
            asconf = new File(location, "config/asenv.conf");
        }
        
        String contents = FileUtils.readFile(asconf);
        
        Matcher matcher = Pattern.compile("AS_JAVA=\"?(.+?)\"?$", Pattern.MULTILINE).matcher(contents);
        
        if (matcher.find()) {
            return new File(matcher.group(1));
        } else {
            return null;
        }
    }
    
    public static Version getVersion(File location) throws IOException {
        if (knownVersions.get(location) != null) {
            return knownVersions.get(location);
        }
        
        String executable = getAsadmin(location).getAbsolutePath();
        
        ExecutionResults results = SystemUtils.executeCommand(location,
                executable,
                "version",
                "--verbose");
        
        Version version = null;
        
        Matcher matcher = Pattern.compile("[0-9][0-9_\\.]+[0-9]").matcher(results.getStdOut());
        
        if (matcher.find()) {
            version = Version.getVersion(matcher.group());
        }
        
        if (version != null) {
            knownVersions.put(location, version);
        }
        
        return version;
    }
    
    public static boolean validateCredentials(File location, String domainName, String username, String password) throws IOException, XMLException {
        String executable = getAsadmin(location).getAbsolutePath();
        
        int adminPort = getAdminPort(location, domainName);
        
        File passwordFile = createPasswordFile(password, location);
        
        startDomain(location, domainName);
        
        ExecutionResults results = SystemUtils.executeCommand(location,
                executable,
                "list-admin-objects",
                "--port",
                Integer.toString(adminPort),
                "--user",
                username,
                "--passwordfile",
                passwordFile.getAbsolutePath()
                );
        
        FileUtils.deleteFile(passwordFile);
        
        stopDomain(location, domainName);
        
        if (results.getStdOut().contains("successfully")) {
            return true;
        } else {
            return false;
        }
    }
    
    public static int getAdminPort(File location, String domainName) throws IOException, XMLException {
        try {
            final DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(new GlassFishDtdEntityResolver(location));
            
            final Element documentElement = builder.parse(getDomainXml(
                    location,
                    domainName)).getDocumentElement();
            
            final Element httpServiceElement = XMLUtils.getChild(
                    documentElement,
                    "configs/config/http-service");
            
            if (httpServiceElement != null) {
                for (Element element: XMLUtils.getChildren(
                        httpServiceElement,
                        "http-listener")) {
                    if (element.getAttribute("security-enabled").equals("false")) {
                        final String id = element.getAttribute("id");
                        final String port = element.getAttribute("port");
                        
                        if (id.contains("admin")) {
                            return new Integer(port);
                        }
                    }
                }
            }
            
            return Integer.parseInt("4848");
        } catch (ParserConfigurationException e) {
            throw new XMLException("Cannot configure parser", e);
        } catch (SAXException e) {
            throw new XMLException("Something wrong while parsing", e);
        }
    }
    
    public static int getHttpPort(File location, String domainName) throws IOException, XMLException {
        try {
            final DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(new GlassFishDtdEntityResolver(location));
            
            final Element documentElement = builder.parse(getDomainXml(
                    location,
                    domainName)).getDocumentElement();
            
            final Element httpServiceElement = XMLUtils.getChild(
                    documentElement,
                    "configs/config/http-service");
            
            if (httpServiceElement != null) {
                for (Element element: XMLUtils.getChildren(
                        httpServiceElement,
                        "http-listener")) {
                    if (element.getAttribute("security-enabled").equals("false")) {
                        return Integer.parseInt(element.getAttribute("port"));
                    }
                }
            }
            
            return Integer.parseInt("8080");
        } catch (ParserConfigurationException e) {
            throw new XMLException("Cannot configure parser", e);
        } catch (SAXException e) {
            throw new XMLException("Something wrong while parsing", e);
        }
    }
    
    public static void deployWar(File location, File war, String domainName, String user, String password) throws IOException, XMLException {
        String executable = getAsadmin(location).getAbsolutePath();
        
        File passwordFile = createPasswordFile(password, location);
        String adminPort = Integer.toString(getAdminPort(location, domainName));
        
        try {
            ExecutionResults results = SystemUtils.executeCommand(location,
                    executable,
                    "deploy",
                    "--host",
                    "localhost",
                    "--port",
                    adminPort,
                    "--user",
                    user,
                    "--passwordfile",
                    passwordFile.getAbsolutePath(),
                    war.getAbsolutePath()
                    );
            
            if (results.getErrorCode() == ExecutionResults.TIMEOUT_ERRORCODE) {
                throw new IOException("Deployment failed to finish in a timely manner");
            }
        } finally {
            FileUtils.deleteFile(passwordFile);
        }
    }
    
    public static void undeployWar(File location, String name, String domainName, String user, String password) throws IOException, XMLException {
        String executable = getAsadmin(location).getAbsolutePath();
        
        File passwordFile = createPasswordFile(password, location);
        String adminPort = Integer.toString(getAdminPort(location, domainName));
        
        try {
            SystemUtils.executeCommand(location,
                    executable,
                    "undeploy",
                    "--host",
                    "localhost",
                    "--port",
                    adminPort,
                    "--user",
                    user,
                    "--passwordfile",
                    passwordFile.getAbsolutePath(),
                    name
                    );
        } finally {
            FileUtils.deleteFile(passwordFile);
        }
    }
    
    public static void prependClassPath(File location, String domainName, File file) throws IOException, XMLException {
        final Document document =
                getDomainXmlDocument(location, domainName);
        
        final Element element = XMLUtils.getChild(
                document.getDocumentElement(),
                "configs/config/java-config");
        
        final String separator = SystemUtils.getPathSeparator();
        
        String contents = element.getAttribute("classpath-suffix");
        
        contents = file.getAbsolutePath() + separator + contents;
        contents = contents.replace(separator + separator, separator);
        contents = contents.replaceAll("^" + separator, "");
        contents = contents.replaceAll(separator + "$", "");
        
        element.setTextContent(contents);
        
        saveDomainXmlDocument(location, domainName, document);
    }
    
    public static void appendClassPath(File location, String domainName, File file) throws IOException, XMLException {
        final Document document =
                getDomainXmlDocument(location, domainName);
        
        final Element element = XMLUtils.getChild(
                document.getDocumentElement(),
                "configs/config/java-config");
        
        final String separator = SystemUtils.getPathSeparator();
        
        String contents = element.getAttribute("classpath-suffix");
        
        contents = contents + separator + file.getAbsolutePath();
        contents = contents.replace(separator + separator, separator);
        contents = contents.replaceAll("^" + separator, "");
        contents = contents.replaceAll(separator + "$", "");
        
        element.setTextContent(contents);
        
        saveDomainXmlDocument(location, domainName, document);
    }
    
    public static void removeClassPath(File location, String domainName, File file) throws IOException, XMLException {
        final Document document =
                getDomainXmlDocument(location, domainName);
        
        final Element element = XMLUtils.getChild(
                document.getDocumentElement(),
                "configs/config/java-config");
        
        final String separator = SystemUtils.getPathSeparator();
        
        String contents = element.getAttribute("classpath-suffix");
        
        contents = contents.replace(file.getAbsolutePath(), "");
        contents = contents.replace(separator + separator, separator);
        contents = contents.replaceAll("^" + separator, "");
        contents = contents.replaceAll(separator + "$", "");
        
        element.setTextContent(contents);
        
        saveDomainXmlDocument(location, domainName, document);
    }
    
    @Deprecated
    public static void addJvmOption(File location, String domainName, String option) throws IOException, XMLException {
        setJvmOption(location, domainName, option);
    }    
    
    public static void setJvmOption(File location, String domainName, String option) throws IOException, XMLException {
        final Document document =
                getDomainXmlDocument(location, domainName);
        
        final Element element = XMLUtils.getChild(
                document.getDocumentElement(),
                "configs/config/java-config");
        
        final Element jvmOptionElement = document.createElement("jvm-options");
        jvmOptionElement.setTextContent(option);
        
        element.appendChild(jvmOptionElement);
        
        saveDomainXmlDocument(location, domainName, document);
    }
    
    public static void removeJvmOption(File location, String domainName, String option) throws IOException, XMLException {
        final Document document =
                getDomainXmlDocument(location, domainName);
        
        final Element element = XMLUtils.getChild(
                document.getDocumentElement(),
                "configs/config/java-config");
        
        for (Element child: XMLUtils.getChildren(element, "jvm-options")) {
            if (child.getTextContent().equals(option)) {
                element.removeChild(child);
            }
        }
        
        saveDomainXmlDocument(location, domainName, document);
    }
    
    public static File getAsadmin(File location) {
        return new File(location, "bin/" + (SystemUtils.isWindows() ? "asadmin.bat" : "asadmin"));
    }
    
    public static File getDomainXml(File location, String domainName) {
        return new File(location, "domains/" + domainName + "/config/domain.xml");
    }
    
    public static Document getDomainXmlDocument(File location, String domainName) throws IOException, XMLException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(new GlassFishDtdEntityResolver(location));
            
            return builder.parse(getDomainXml(location, domainName));
        } catch (ParserConfigurationException e) {
            throw new XMLException("Cannot configure parser", e);
        } catch (SAXException e) {
            throw new XMLException("Something wrong while parsing", e);
        }
    }
    
    public static void saveDomainXmlDocument(File location, String domainName, Document document) throws IOException, XMLException {
        try {
            OutputStream outputStream = new FileOutputStream(getDomainXml(location, domainName));
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, document.getDoctype().getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, document.getDoctype().getSystemId());
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            
            outputStream.close();
        } catch (TransformerConfigurationException e) {
            throw new XMLException("Cannot configure transformer", e);
        } catch (TransformerException e) {
            throw new XMLException("Something wrong with transformer", e);
        }
    }
    
    public static File getDomainConfig(File location, String domainName) {
        return new File(location, "domains/" + domainName + "/config");
    }
    
    private static class GlassFishDtdEntityResolver implements EntityResolver {
        private File dtdsLocation;
        
        public GlassFishDtdEntityResolver(File location) {
            this.dtdsLocation = new File(location, "lib/dtds");
        }
        
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            String filename = new File(new URL(systemId).getFile()).getName();
            
            return new InputSource(new FileInputStream(new File(dtdsLocation, filename)));
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class DomainCreationException extends IOException {
        private String message;
        
        public DomainCreationException() {
            super();
            
            message = "Could not create domain - " +
                    "failed to create the password file.";
        }
        
        public DomainCreationException(String errorNumber) {
            super();
            
            message = "Could not create domain - " +
                    "error " + errorNumber + " occurred.";
        }
        
        public DomainCreationException(int errorCode) {
            super();
            
            if (errorCode < Integer.MAX_VALUE) {
                message = "Could not create domain - " +
                        "the process returned " + errorCode;
            } else {
                message = "Could not create domain - " +
                        "the process was killed due to timeout.";
            }
        }
        
        @Override
        public String getMessage() {
            return message;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String INSTALL_ADDON_COMMAND = 
            "install-addon"; // NOI18N
    
    public static final String UNINSTALL_ADDON_COMMAND = 
            "uninstall-addon"; // NOI18N
    
    public static final String DEFAULT_DOMAIN = 
            "domain1"; // NOI18N
    
    public static final String CLI_130 = 
            "CLI130"; // NOI18N
    
    public static final String COULD_NOT_CREATE_DOMAIN_MARKER =
            CLI_130 + " Could not create domain"; // NOI18N
    
    
}
