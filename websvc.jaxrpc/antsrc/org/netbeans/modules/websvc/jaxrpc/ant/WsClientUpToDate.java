/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.jaxrpc.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.Project;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Ant task that calculates wscompile targets and determines if the generated
 * web service client is up to date or needs to be regenerated.
 *
 * @credits Pattern of execute method lifted from ant <uptodate> task.
 *
 * @author Peter Williams
 */
public class WsClientUpToDate extends Task {

    private String _property;
    private String _value;
    private File _sourceWsdlFile;
    private File _sourceConfigFile;
    private File _targetGenDir;
    private File _targetConfigFile;

    private String _targetPackage; // !PW FIXME derive this by parsing config file.

    /**
     * The property to set if the calculated output java files and wsdl file are
     * newer than the source wsdl file AND the target config file is newer than
     * the source config file.
     *
     * @param property the name of the property to set if Targets are up-to-date.
     */
    public void setProperty(String property) {
        _property = property;
    }
    
    /**
     * The value to set the named property to if the calculated output java files
     * and wsdl file are newer than the source wsdl file AND the target config
     * file is newer than the source config file.  Defaults to false.
     *
     * @param value the value to set the property to if Targets are up-to-date
     */
    public void setValue(String value) {
        _value = value;
    }

    /**
     * Returns the value, or "true" if a specific value wasn't provided.
     */
    private String getValue() {
        return (_value != null) ? _value : "false";
    }
    
    /**
     * The wsdl file for which we want to confirm the existence of an uptodate
     * generated client if the property is to be set.
     *
     * @param file the wsdl file we are checking against.
     */
    public void setSourceWsdl(File file) {
        _sourceWsdlFile = file;
    }
    
    /**
     * The wscompile config file for which we want to confirm the existence of 
     * an uptodate generated client if the property is to be set.  If this property
     * is not set, the config file is assumed to be of the form "[source wsdl filename]-config.xml"
     * and present in the same directory as the source wsdl file.
     *
     * @param file the wscompile config file we are checking against.
     */
    public void setSourceConfig(File file) {
        _sourceConfigFile = file;
    }
    
    /**
     * The target directory that the source to the web service client is 
     * generated into.  Actual source files will be in the appropriate package.
     *
     * @param file the target directory where the web service client is generated
     */
    public void setTargetDir(File file) {
        _targetGenDir = file;
    }
    
    /**
     * The target config file.  If this property is not specified, the target config
     * file is assumed to be $targetdir/wsdl/$config-file-name
     *
     * @param file the target config file.  (Ant applies macro expansion to the source
     * file to create this in the client generation script.)
     */
    public void setTargetConfig(File file) {
        _targetConfigFile = file;
    }
    
    /**
     * The target package that the web service client source is generated into.
     *
     * !PW FIXME we should parse the config file and obtain this directly.
     *
     * @param package the target package that the web service client source is generated into.
     */
    public void setTargetPackage(String targetPackage) {
        _targetPackage = targetPackage;
    }

    
    /**
     * Evaluate all targets and determine if they are newer than the appropriate
     * source (and thus uptodate).
     */
    public boolean eval() {
        if(!_sourceWsdlFile.exists()) {
            throw new BuildException(_sourceWsdlFile.getAbsolutePath() + " not found.");
        }
        
        if(_sourceConfigFile == null) {
            String cfgNameExt = getBaseName(_sourceWsdlFile) + "-config.xml";
            _sourceConfigFile = new File(_sourceWsdlFile.getParentFile(), cfgNameExt);
        }
        
        if(!_sourceConfigFile.exists()) {
            throw new BuildException(_sourceConfigFile.getAbsolutePath() + " not found.");
        }
        
        if(!_targetGenDir.exists()) {
            log("The target generation directory \"" + _targetGenDir.getAbsolutePath()
                + "\" does not exist.", Project.MSG_VERBOSE);
            return false;
        }
        
        if(!_targetGenDir.isDirectory()) {
            throw new BuildException("property targetdir must be a directory.");
        }
        
        if(_targetConfigFile == null) {
            _targetConfigFile = new File(_targetGenDir, "wsdl" + File.separator + _sourceConfigFile.getName());
        }
        
        if(!_targetConfigFile.exists()) {
            log("The target config file \"" + _targetConfigFile.getAbsolutePath()
                + "\" does not exist.", Project.MSG_VERBOSE);
            return false;
        }
        
        // source wsdl exists.  source config exists.  target gendir exists.  target config exists.
        
//        System.out.println("source wsdl:   " + _sourceWsdlFile.getPath());
//        System.out.println("source config: " + _sourceConfigFile.getPath());
//        System.out.println("target dir:    " + _targetGenDir.getPath());
//        System.out.println("target config: " + _targetConfigFile.getPath());
        
        // Compare config files first:
        if(_targetConfigFile.lastModified() < _sourceConfigFile.lastModified()) {
            log("Config file \"" + _sourceConfigFile.getAbsolutePath() + " is out of date.", Project.MSG_VERBOSE);
            return false;
        }

        // Now gather data to compare generated client source.
        
        // Need to determine target package and convert to a relative directory path.
        if(_targetPackage == null) {
            _targetPackage = getClientPackage(_sourceConfigFile);
        }
        
//        System.out.println("target package: " + _targetPackage);
        
        // Need to determine target filenames for web service client source code.
        File targetClientDir = new File(_targetGenDir, convertPackage(_targetPackage));
        if(!targetClientDir.exists()) {
            log("The target client source directory \"" + targetClientDir.getAbsolutePath()
                + "\" does not exist.", Project.MSG_VERBOSE);
            return false;
        }
        if(!targetClientDir.isDirectory()) {
            // !PW Could throw a build exception on this one, but I'll let wscompile
            // deal with the fallout instead -- it might clean things up or it might fail.
            log("The target client source directory \"" + targetClientDir.getAbsolutePath()
                + "\" should be a directory.", Project.MSG_VERBOSE);
            return false;
        }

//        System.out.println("target client dir: " + targetClientDir.getPath());
        
        List/*File*/ clientSources = getClientSources(targetClientDir, _sourceWsdlFile);
        
        // validated client source dir, got source file list
        boolean uptodate = (clientSources.size() > 0) ? true : false;
        Iterator iter = clientSources.iterator();
        while(uptodate && iter.hasNext()) {
            File target = (File) iter.next();
            if(!target.exists()) {
                log("Expected target client file \"" + target.getAbsolutePath() + "\" not found.", Project.MSG_VERBOSE);
                uptodate = false;
            } else {
                uptodate = uptodate && (target.lastModified() >= _sourceWsdlFile.lastModified());
            }
        }
        
        return uptodate;
    }
    
    public void execute() throws BuildException {
        if(_property == null) {
            throw new BuildException("property attribute is required.", getLocation());
        }
        if(_sourceWsdlFile == null) {
            throw new BuildException("sourcewsdl attribute is required.", getLocation());
        }
        if(_targetGenDir == null) {
            throw new BuildException("targetdir attribute is required.", getLocation());
        }
        boolean upToDate = eval();
        if(upToDate) {
            this.getProject().setNewProperty(_property, getValue());
            log("Generated client for " + _sourceWsdlFile.getAbsolutePath() + " is up-to-date.", Project.MSG_VERBOSE);
        }
    }
    
    protected String getBaseName(File f) {
        String name = f.getName();
        int i = name.lastIndexOf('.');
        if(i > -1) {
            name = name.substring(0, i);
        }
        return name;
    }
    
    /** Converts a java package into a relative path
     */
    protected String convertPackage(String p) {
        StringBuffer path = new StringBuffer(p.length());
        String [] dirs = p.split("\\.");
        for(int i = 0; i < dirs.length; i++) {
            if(i > 0) {
                path.append(File.separator);
            }
            path.append(dirs[i]);
        }
        return path.toString();
    }
    
    /** Reads the wsdl file and computes a list of expected target files
     *  for the generated web service client.
     *
     *  Phase 1:  Returns list of the service interfaces.
     *
     *  (We can expand this as necessary to ensure correct results.)
     */
    protected List/*File*/ getClientSources(File clientDir, File wsdlFile) {
        ArrayList result = new ArrayList();
        
        List serviceNames = getServiceNames(wsdlFile);
        Iterator iter = serviceNames.iterator();
        while(iter.hasNext()) {
            String sn = (String) iter.next();
            result.add(new File(clientDir, sn + ".java"));
        }
        
        return result;
    }
    
    /** Reads the config file and extracts the target package that the web service
     *  client will be generated into.
     */
    protected String getClientPackage(File configFile) {
        String result = "";

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            ConfigParser handler= new ConfigParser();
            saxParser.parse(configFile, handler);
            result = handler.getPackageName();
        } catch(ParserConfigurationException ex) {
            // Bogus config file, return empty package.
        } catch(SAXException ex) {
            // Bogus config file, return empty package.
        } catch(IOException ex) {
            // Bogus config file, return empty package.
        }
        
        return result;
    }

    private List getServiceNames(File wsdlFile) {
        List result = Collections.EMPTY_LIST;

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            WsdlParser handler= new WsdlParser();
            saxParser.parse(wsdlFile, handler);
            result = handler.getServiceNameList();
        } catch(ParserConfigurationException ex) {
            // Bogus WSDL, return empty list.
        } catch(SAXException ex) {
            // Bogus WSDL, return empty list.
        } catch(IOException ex) {
            // Bogus WSDL, return empty list.
        }

        return result;
    }
    
    /* SAX parser to strip the service names from a WSDL file.
     */
    private static final class WsdlParser extends DefaultHandler {

        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl";
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/";

        private ArrayList serviceNameList;

        private WsdlParser() {
            serviceNameList = new ArrayList();
        }

        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("service".equals(localname)) {
                    serviceNameList.add(attributes.getValue("name"));
                }
            }
        }

        public List/*String*/ getServiceNameList() {
            return serviceNameList;
        }
    }
    
    /* SAX parser to strip the target package location from a WSCOMPILE config file.
     */
    private static final class ConfigParser extends DefaultHandler {
        
        private String packageName;

        private ConfigParser() {
            packageName = null;
        }

        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            String elementName = localname; // element name
            if("".equals(elementName)) {
                elementName = qname; // not namespace-aware
            }

            if("wsdl".equals(elementName)) {
                packageName = attributes.getValue("packageName");
            }
        }

        public String getPackageName() {
            return packageName;
        }
    }
}
