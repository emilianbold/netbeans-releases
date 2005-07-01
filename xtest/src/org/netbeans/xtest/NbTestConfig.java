/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Date:           January 6, 2002  5:16 PM
 *
 * @author  lm97939
 */
package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import org.netbeans.xtest.util.XMLFactoryUtil;

/**
 * This is a scanner of DOM tree.
 */
public class NbTestConfig extends Task {
    
    private File file;
    private String config;
    
    private Hashtable allSetups = new Hashtable();
    private Vector cfg = new Vector();
    private String config_setup = null;
    //private Hashtable props = new Hashtable();

    private static MConfig mconfig;
    
    public static MConfig getMConfig() {
        return mconfig;
    }
    
    public void setFile(File f) {
        file = f;
    }
    
    public void setConfig(String c) {
        config = c;
    }

    public void execute() throws BuildException {
        if (file == null) throw new BuildException ("Attribute 'file' is empty.");
        if (config == null) throw new BuildException ("Attribute 'config' is empty.");
        
        Document document = null;
        try {
            DocumentBuilder db = XMLFactoryUtil.newDocumentBuilder();
            db.setEntityResolver(new XTestEntityResolver());
            document = db.parse(file);
        } catch (SAXException saxe) {
            throw new BuildException( "Exception during parsing "+file.getAbsolutePath()+": "+saxe );
        } catch (ParserConfigurationException pce) {
            throw new BuildException( "Exception during parsing "+file.getAbsolutePath()+": "+pce  );
        } catch (IOException ioe) {
            throw new BuildException(  "Exception during parsing "+file.getAbsolutePath()+": "+ioe );
        }
        
        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals("testconfig")) {
            visitElement_testconfig(element);
        }
        else throw new BuildException("Element testconfig expected.");
    }
    
    /** Scan through org.w3c.dom.Element named testconfig. */
    void visitElement_testconfig(org.w3c.dom.Element element) { // <testconfig>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getNodeName().equals("setup"))
                    visitElement_setup(allSetups,nodeElement);
            }
        }
                
        org.w3c.dom.Element rightConfig = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getNodeName().equals("config")) {
                    org.w3c.dom.Attr attr = nodeElement.getAttributeNode("name");
                    if (attr == null) 
                        throw new BuildException("Element 'config' has to have attribute 'name'."); 
                    if (attr.getValue().equals(config)) {
                        if (rightConfig != null)
                            throw new BuildException("More than one config with name "+config+" found.");
                        rightConfig = nodeElement;
                    }
                }
            }
        }
        if (rightConfig == null) 
            throw new BuildException("Config name "+ config + " not found.");
        visitElement_config(rightConfig);
        
        mconfig = new MConfig(cfg);
        if (config_setup != null) {
            MConfig.Setup config_setup_object = (MConfig.Setup)allSetups.get(config_setup);
            if (config_setup_object == null) throw new BuildException("Setup with name "+config_setup+" not found.");
            mconfig.setConfigSetup(config_setup_object);
        }
    }
    
    /** Scan through org.w3c.dom.Element named setup. */
    void visitElement_setup(Hashtable allSetups, org.w3c.dom.Element element) { // <setup>
        // element.getValue();
        org.w3c.dom.Attr attr = element.getAttributeNode("name");
        if (attr == null) throw new BuildException ("Element 'setup' has to have attribute 'name'.");

        String name = attr.getValue();

        org.w3c.dom.Element start_node = null;
        org.w3c.dom.Element stop_node = null;
        
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getNodeName().equals("start")) {
                    if (start_node != null) throw new BuildException("More than one start element found.");
                    start_node = nodeElement;
                }
                if (nodeElement.getNodeName().equals("stop")) {
                    if (stop_node != null) throw new BuildException("More than one stop element found.");
                    stop_node = nodeElement;
                }
            }
        }
        
        MConfig.Setup.StartStop start = null, stop = null;
        if (start_node != null)
            start = visitElement_start(start_node);
        if (stop_node != null)
            stop = visitElement_start(stop_node);
        
        MConfig.Setup setup = new MConfig.Setup();
        setup.setName(name);
        setup.setStart(start);
        setup.setStop(stop);
        allSetups.put(name, setup);
    }
    
    /** Scan through org.w3c.dom.Element named start. */
    MConfig.Setup.StartStop visitElement_start(org.w3c.dom.Element element) { // <start>
        // element.getValue();
        
        MConfig.Setup.StartStop ss = new MConfig.Setup.StartStop();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals("dir")) { // <start dir="???">
                ss.dir = project.resolveFile(ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties()));
            }
            if (attr.getName().equals("target")) { // <start target="???">
                ss.target = ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties());
            }
            if (attr.getName().equals("antfile")) { // <start antfile="???">
                ss.antfile = ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties());
            }
            if (attr.getName().equals("onBackground")) { // <start onBackground="???">
                String ob = ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties());
                if (ob.equalsIgnoreCase("true") || ob.equalsIgnoreCase("yes") || ob.equals("1"))
                    ss.onBackground = true;
                else 
                    if (ob.equalsIgnoreCase("false") || ob.equalsIgnoreCase("no") || ob.equals("0"))
                        ss.onBackground = false;
                    else
                        throw new BuildException ("Unknown value of attribute onBackground: "+ob);
            }
            if (attr.getName().equals("delay")) { // <start delay="???">
                String d = ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties());
                int delay = 0;
                try { 
                    delay = Integer.parseInt(d); 
                    ss.delay = delay;
                }
                catch (NumberFormatException nfe) {
                    throw new BuildException ("Attribute delay has bad number",nfe);
                }
            }
        }
        return ss;
    }
    
    /** Scan through org.w3c.dom.Element named config. */
    void visitElement_config(org.w3c.dom.Element element) { // <config>
        String name = null;
        String default_testtypes = null;
        String default_attributes = null;
        
        Hashtable config_properties = new Hashtable();
        
        // element.getValue();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals("name")) { // <config name="???">
                name = attr.getValue();
            }
            else if (attr.getName().equals("defaultAttributes")) { // <config attributes="???">
                default_attributes = attr.getValue();
            }
            else if (attr.getName().equals("defaultTesttypes")) { // <config testtypes="???">
                default_testtypes = attr.getValue();
            }
            else if (attr.getName().equals("setup")) { // <config setup="???">
                config_setup = attr.getValue();
            }
            else {
                throw new BuildException ("Unexpected attribute '" + attr.getName() + "'.");
            }
        }
        
        if (name == null) throw new BuildException ("Attribute 'name' has to be set.");
        
        visitAllProperties(config_properties, element);
        
        MConfig.TestGroup group;
            
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getNodeName().equals("module")) {
                    group = visitElement_module(nodeElement, default_testtypes, default_attributes);
                    group.addProperties(config_properties);
                    cfg.add(group); 
                }
                if (nodeElement.getNodeName().equals("testtype")) {
                    group = visitElement_testtype(nodeElement, default_attributes);
                    group.addProperties(config_properties);
                    cfg.add(group); 
                }
                
            }
        }
    }
    
    /** Scan through org.w3c.dom.Element named module. */
    MConfig.TestGroup visitElement_module(org.w3c.dom.Element element, String default_testtypes, String default_attributes) { // <module>
        // element.getValue();
        
        String name = null;
        String testtypes = default_testtypes;
        String attributes = default_attributes;
        String setup = null;
        
        MConfig.TestGroup group = new MConfig.TestGroup();
        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals("attributes")) { // <module attributes="???">
                attributes = attr.getValue();
            }
            else if (attr.getName().equals("name")) { // <module name="???">
                name = attr.getValue();
            }
            else if (attr.getName().equals("testtypes")) { // <module testtypes="???">
                testtypes = attr.getValue();
            }
            else if (attr.getName().equals("setup")) { // <module setup="???">
                setup = attr.getValue();
            }
            else {
                throw new BuildException ("Unexpected attribute '" + attr.getName() + "'.");
            }
        }
        
        if (name == null || testtypes == null || attributes == null)
            throw new BuildException ("Element 'module' has to have attriutes 'name', 'testtypes' and 'attributes'.");
        
        Hashtable module_props = new Hashtable();
        
        visitAllProperties(module_props, element);
        
        if (setup != null) {
            MConfig.Setup module_setup = (MConfig.Setup)allSetups.get(setup);
            if (module_setup == null) throw new BuildException("Setup with name "+setup+" not found.");
            group.setSetup(module_setup);
        }
        group.setProperties(module_props);
        StringTokenizer testtypes_tokens = new StringTokenizer(testtypes,",");
        while (testtypes_tokens.hasMoreTokens()) {
            String testtype = testtypes_tokens.nextToken().trim();
            updateTable(group, name, testtype, attributes);
        }
        
        return group;
    }
    
    /** Scan through org.w3c.dom.Element named testtype. */
    MConfig.TestGroup visitElement_testtype(org.w3c.dom.Element element, String default_attributes) { // <testtype>
        // element.getValue();
        
        String name = null;
        String modules = null;
        String attributes = default_attributes;
        String setup = null;
        
        MConfig.TestGroup group = new MConfig.TestGroup();
        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals("attributes")) { // <testtype attributes="???">
                attributes = attr.getValue();
            }
            else if (attr.getName().equals("modules")) { // <testtype modules="???">
                modules = attr.getValue();
            }
            else if (attr.getName().equals("name")) { // <testtype name="???">
                name = attr.getValue();
            }
            else if (attr.getName().equals("setup")) { // <testtype setup="???">
                setup = attr.getValue();
            }
            else {
                throw new BuildException ("Unexpected attribute '" + attr.getName() + "'.");
            }
        }
        
        if (name == null || modules == null || attributes == null)
            throw new BuildException ("Element 'module' has to have attriutes 'name', 'modules' and 'attributes'.");
        
        Hashtable group_props = new Hashtable();
        
        visitAllProperties(group_props, element);
        
        if (setup != null) {
            MConfig.Setup group_setup = (MConfig.Setup)allSetups.get(setup);
            group.setSetup(group_setup);
        }
        group.setProperties(group_props);
        StringTokenizer modules_tokens = new StringTokenizer(modules,",");
        while (modules_tokens.hasMoreTokens()) {
            String module = modules_tokens.nextToken().trim();
            updateTable(group, module, name, attributes);
        } 
        
        return group;
    }
    
    void visitAllProperties(Hashtable table, Element element) {
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getNodeName().equals("property"))
                    visitElement_property(table, nodeElement);   
            }
        }
    }
    
    /** Scan through org.w3c.dom.Element named property. */
    void visitElement_property(Hashtable properties, org.w3c.dom.Element element) { // <property>
        // element.getValue();

        String name = null;
        String value = null;
        String file = null;
        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals("file")) { // <property file="???">
                file = ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties());
            }
            else if (attr.getName().equals("name")) { // <property name="???">
                name =  ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties());
            }
            else if (attr.getName().equals("value")) { // <property value="???">
                value = ProjectHelper.replaceProperties(project, attr.getValue(), project.getProperties());
            }
            else {
                throw new BuildException ("Unexpected attribute '" + attr.getName() + "'.");
            }
        }
        
        if (name == null && file == null) throw new BuildException("Either 'name' or 'file' attribute is empty.",location);
        if (name != null && value == null) throw new BuildException("Attribute 'value' is empty.",location);
        if (value != null && file != null) throw new BuildException("Attributes 'file' and 'value' can't be defined together.",location);
 
        if (name != null) 
            properties.put(name,value);
        if (file != null) 
            convertProperties(properties, file);
        
    }
    
    private void convertProperties(Hashtable table, String filename)  {
       try {  
         File file = project.resolveFile (filename);
         if (!file.exists()) throw new BuildException("Property file "+file.getAbsolutePath()+" not found.");
         java.util.Properties javaprop = new java.util.Properties();
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
         javaprop.load(bis);
         bis.close();
         Enumeration en = javaprop.propertyNames();
         while (en.hasMoreElements()) {
             String name2 = (String) en.nextElement();
             table.put(name2,javaprop.getProperty(name2));
         }
       }
       catch (IOException e) { throw new BuildException(e); }
    }
    
    private void updateTable(MConfig.TestGroup group, String module, String testtype, String attributes) {
        MConfig.Test test = new MConfig.Test(module,testtype);
        StringTokenizer ta = new StringTokenizer(attributes, ",");
        String [] attrs = new String [ta.countTokens()];
        int j = 0;
        while(ta.hasMoreTokens()) {
            String atr = ta.nextToken().trim();
            attrs[j++] = atr;
        }
        test.setAttributes(attrs);
        group.addTest(test);   
    }
}
