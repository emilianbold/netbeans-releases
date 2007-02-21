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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;


import javax.xml.namespace.QName;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.apache.tools.ant.BuildException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.bpel.project.portmap.DataWriter;

import org.netbeans.modules.bpel.project.anttasks.jbi.Consumer;
import org.netbeans.modules.bpel.project.anttasks.jbi.Provider;

/**
 * Generates JBI.xml
 * @author Sreenivasan Genipudi
 */
public class IDEJBIGenerator extends JBIGenerator {
    //Member variable representing logger
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(IDEJBIGenerator.class.getName());
    //Constant representing the partnerRole

    /**
     * Constructor
     */
    public IDEJBIGenerator() {
    }
    /**
     * Constructor
     * @param depedentProjectDirs List of dependent projects directories
     * @param sourceDirs  List of current source directory
     */
    public IDEJBIGenerator(List depedentProjectDirs , List sourceDirs) {
        super(depedentProjectDirs, sourceDirs);
    }
    /**
     * Process the file to generate JBI.xml
     * @param file input file
     */
    public void processFile(File file) {
        String fileName = file.getName();
        String fileExtension = null;
        int dotIndex = fileName.lastIndexOf('.');
        if(dotIndex != -1) {
            fileExtension = fileName.substring(dotIndex +1);
        }

        if (fileExtension != null && fileExtension.equalsIgnoreCase("bpel")) {
            BpelModel bpelModel = null;
            try {
                bpelModel =IDEBPELCatalogModel.getDefault().getBPELModel(file);
            }catch (Exception ex) {
                this.logger.log(java.util.logging.Level.SEVERE, "Error while creating BPEL Model ", ex);
                throw new RuntimeException("Error while creating BPEL Model ",ex);
            }
           
            
            try {
                populateProviderConsumer(bpelModel);
            }catch (Exception ex) {
                logger.log(Level.SEVERE, "Error encountered while processing BPEL file - "+file.getAbsolutePath());
                throw new RuntimeException(ex);
            }
        }
    }



}