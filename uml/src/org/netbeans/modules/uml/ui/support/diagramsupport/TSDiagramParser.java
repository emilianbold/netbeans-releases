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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.ui.support.diagramsupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramTypesManager;
import org.netbeans.modules.uml.core.metamodel.diagrams.TSDiagramDetails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jyothi
 */
public class TSDiagramParser implements IDiagramParser
{

    private String dataFilename;
    private String presFilename;
    private TSDiagramDetails diagramDetails = null;
    private InputStream fisPres;
    private XMLStreamReader readerPres = null;
    private boolean jumpToEnd = false;

    public TSDiagramParser(String filename)
    {
        dataFilename = filename;
        presFilename = filename.substring(0, filename.length()-4) + "etlp";
    }

    public DiagramDetails getDiagramInfo()
    {
        diagramDetails = new TSDiagramDetails();
        diagramDetails.setDiagramFileName(dataFilename);
        diagramDetails.setDiagramFileName2(presFilename);
        
        initialize();
        readXMLPres();
        releaseResources();

        return diagramDetails;
    }

    private void initialize()
    {
        try
        {
            // parse the data file (.etld)
            File infile = new File(dataFilename);
            if (infile.exists() && infile.isFile() && infile.length() > 0)
            {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            }
            
            // parse the presentation file (.etlp)
            infile = null;
            infile = new File(presFilename);
            if (infile.exists() && infile.isFile() && infile.length() > 0)
            {
                FileObject fo = FileUtil.toFileObject(infile);
                diagramDetails.setDateModified(fo.lastModified());
                
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
                fisPres = fo.getInputStream();
                readerPres = factory.createXMLStreamReader(fisPres);
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void releaseResources()
    {
        try
        {
            if (readerPres != null)
                readerPres.close();

            if (fisPres != null)
                fisPres.close();
        }

        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void readXMLPres()
    {
        if (readerPres == null)
            return;

        try
        {
            int event = readerPres.getEventType();

            while (true)
            {
                switch (event)
                {
                    case XMLStreamConstants.START_DOCUMENT:
                        break;
            
                    case XMLStreamConstants.START_ELEMENT:
                        handlePresStartElement();
                        break;
                    
                    case XMLStreamConstants.CHARACTERS:
                        break;
                    
                    case XMLStreamConstants.END_ELEMENT:
                        break;
                        
                    case XMLStreamConstants.END_DOCUMENT:
                        readerPres.close();
                        break;
                }

                if (!readerPres.hasNext() || jumpToEnd)
                    break;

                else
                    event = readerPres.next();
            }
        }

        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    
    private void handlePresStartElement()
    {
        if (readerPres != null && 
            readerPres.getName().getLocalPart().equalsIgnoreCase("diagramInfo"))
        {
            handlePresDiagramInfo();
        }
    }

    // get diagram info from "header" of etlp file
    private void handlePresDiagramInfo()
    {
        if (readerPres == null)
            return;

        if (readerPres.getAttributeCount() > 0)
        {
            diagramDetails.setDiagramXMIID(
                readerPres.getAttributeValue(null, "diagramXMIID"));

            String projectXMID = readerPres.getAttributeValue(
                null, "namespaceToplevelID");
            
            diagramDetails.setDiagramProjectXMIID(projectXMID);
            diagramDetails.setToplevelXMIID(projectXMID);

            diagramDetails.setDiagramNamespaceXMIID(
                readerPres.getAttributeValue(null, "namespaceMEID"));

            diagramDetails.setDiagramName(
                readerPres.getAttributeValue(null, "name"));

            String diagramTypeName = 
                readerPres.getAttributeValue(null, "diagramKind");
            
            diagramDetails.setDiagramTypeName(diagramTypeName);

            diagramDetails.setDiagramType(
                DiagramTypesManager.instance().getDiagramKind(diagramTypeName));

            diagramDetails.setZoom(
                readerPres.getAttributeValue(null, "zoom"));

            diagramDetails.setDocumentation(readerPres.getAttributeValue(null, "docs"));
        }
    }
}
