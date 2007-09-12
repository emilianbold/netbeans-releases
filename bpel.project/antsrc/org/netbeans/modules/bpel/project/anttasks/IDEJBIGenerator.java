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

    public IDEJBIGenerator() {
    }

    public IDEJBIGenerator(List depedentProjectDirs , List sourceDirs) {
        super(depedentProjectDirs, sourceDirs);
    }

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
            }
            catch (Exception ex) {
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

    private Logger logger = Logger.getLogger(IDEJBIGenerator.class.getName());
}
