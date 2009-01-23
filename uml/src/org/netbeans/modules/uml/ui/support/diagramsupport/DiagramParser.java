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
import java.util.HashMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jyothi
 */
public class DiagramParser implements IDiagramParser 
{

    private String filename;
    private DiagramDetails diagInfo = new DiagramDetails();
    private XMLStreamReader reader = null;
    private boolean jumpToEnd = false;
    private HashMap<String, DiagramInfo> map;
    private boolean  diagramModelMappingFlag = false;
    
    public DiagramParser(String filename) 
    {
        this.filename = filename;
    }
    
    public DiagramDetails getDiagramInfo()
    {
        diagInfo = new DiagramDetails();
        
        if (filename != null && filename.trim().length() > 0) {
            FileObject fo = FileUtil.toFileObject(new File (filename));
            InputStream ins = null;
            try {
                
                if ( fo != null && fo.getSize() > 0 ) { 
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
                    ins = fo.getInputStream();
                    reader = factory.createXMLStreamReader(ins);
                    readXML();
                    diagInfo.setDateModified(fo.lastModified());
                }
                else {
//                    System.err.println(" Corrupted diagram file. Cannot open the diagram."+filename);
                }
            } catch (XMLStreamException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            finally {
                try {
                    if ( reader != null) {
                        reader.close();
                    }
                    if (ins != null) {
                        ins.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (XMLStreamException ex) {
                    Exceptions.printStackTrace(ex);
                } 
            }
        }
 
        return diagInfo;
    }
    

    private void readXML() throws XMLStreamException {
        
        if (reader == null) {
            return;
        }
        int event = reader.getEventType();
        while (true) {
            switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    handleStartElement();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (reader.isWhiteSpace()) {
                        break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    reader.close();
                    break;
                } 

            if (!reader.hasNext() || jumpToEnd) {
//                    System.out.println("---DiagramParser: DiagInfo = name "+diagInfo.getDiagramName()+"  namespace = "+diagInfo.getDiagramNamespaceXMIID()
//                            +"  proj id = "+diagInfo.getDiagramProjectXMIID()+" diagType = "+diagInfo.getDiagramTypeName()
//                            +" diagXMIID = "+diagInfo.getDiagramXMIID()+"  zoom = "+diagInfo.getZoom());
                break;
            } else {
                event = reader.next();
            }
        }
    }
    
    private void handleStartElement() throws XMLStreamException {
        if (reader != null) {
            String localPart = reader.getName().getLocalPart();
            if (localPart!= null && localPart.equalsIgnoreCase("Diagram")) {
                handleDiagram();
            }
            else if(localPart!= null && localPart.equalsIgnoreCase("GraphNode") && !jumpToEnd) {
                processGraphNode();
                return;
            }
        }
    }
    
    private void handleDiagram() throws XMLStreamException {
        String localPart = null;
        if (reader.getAttributeCount() > 0) {
            diagInfo.setDiagramXMIID(reader.getAttributeValue(null, "xmi.id"));
            diagInfo.setDiagramName(reader.getAttributeValue(null, "name"));
            diagInfo.setZoom(reader.getAttributeValue(null, "zoom"));
        }
        while (reader.hasNext()) {
            if (XMLStreamConstants.START_ELEMENT == reader.next()) { //we are only intersted in data of particular start elements
                localPart = reader.getName().getLocalPart();
                if (localPart.equalsIgnoreCase("DiagramElement.property")) {
                    processProperties(diagInfo);                        
                }
                else if (localPart.equalsIgnoreCase("SimpleSemanticModelElement")) {
                    diagInfo.setDiagramTypeName(reader.getAttributeValue(null, "typeinfo"));
                }
                //if we encounter contained.. we should exit this method and let others handle the rest
                else if (localPart.equalsIgnoreCase("GraphElement.contained")) {
                    if (!diagramModelMappingFlag)
                    {
                        jumpToEnd = true;
                    }                    
                    return;
                }
            }
        }
    }

     private void processGraphNode()
    {
        try
        {
            String meid;
            String peid = reader.getAttributeValue(null, "xmi.id");

            while (reader.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == reader.next())
                {
                    //we are only intersted in data of particular start elements
                    if (reader.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge.element"))
                    {
                        reader.nextTag();
                        //get the  xmi.idref
                        meid = reader.getAttributeValue(null, "xmi.idref");
                        
                        DiagramInfo info = map.get(meid);
                        if(info != null)
                        {
                            info.addPeid(peid);
                        }
                        else
                        {
                            info = new DiagramInfo();
                            info.addPeid(peid);
                            map.put(meid, info);
                            
                            //populate the map with meid and peid
                            map.put(meid, info);
                        
                        }
                        return;
                    }
                }
                else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("GraphNode"))
                {
                    return;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void processProperties(DiagramDetails diagInfo) throws XMLStreamException {
        if ( reader == null) {
            return;
        }
        while (reader.hasNext()) {
            if (XMLStreamConstants.START_ELEMENT == reader.next() && reader.getName().getLocalPart().equalsIgnoreCase("Property")) {
                if (reader.getAttributeCount() > 0) {
                    if (reader.getAttributeValue(null, "key").equalsIgnoreCase("netbeans-diagram-projectID")) {
                        diagInfo.setDiagramProjectXMIID(reader.getAttributeValue(null, "value"));
                    }
                    else if (reader.getAttributeValue(null, "key").equalsIgnoreCase("netbeans-diagram-namespace")) {
                        diagInfo.setDiagramNamespaceXMIID(reader.getAttributeValue(null, "value"));
                    }
                }
            }
            else if (reader.isEndElement() && reader.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property")) {
                return;
            }
        }
    }

    public HashMap getDiagramModelMap()
    {
        diagramModelMappingFlag = true;
        map = new HashMap();

        if (filename != null && filename.trim().length() > 0) {
            FileObject fo = FileUtil.toFileObject(new File (filename));
            InputStream ins = null;
            try {

                if ( fo != null && fo.getSize() > 0 ) {
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
                    ins = fo.getInputStream();
                    reader = factory.createXMLStreamReader(ins);
                    readXML();                    
                }
                else {
//                    System.err.println(" Corrupted diagram file. Cannot open the diagram."+filename);
                }
            } catch (XMLStreamException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            finally {
                try {
                    if ( reader != null) {
                        reader.close();
                    }
                    if (ins != null) {
                        ins.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (XMLStreamException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        diagramModelMappingFlag = false;
        return map;
    }

}
