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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.uml.drawingarea.persistence.util.XMIConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author jyothi
 */
public class XMIWriter {
private static String SPACE1 = " ";
    private static String SPACE5 = "     ";

    public static void writeElement(BufferedWriter bw, String eltName, HashMap attrList) {
        try {
            writePartialBeginElement(bw, eltName);
            //has attributes ?
            if (attrList != null && attrList.size() > 0) {
                writeAttributes(bw, attrList);
            }
            bw.write(">");
            bw.newLine();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void writeEmptyElement(BufferedWriter bw, String eltName, HashMap attrList) {
        try {
            writePartialBeginElement(bw, eltName);
            if (attrList != null && attrList.size() > 0) {
                writeAttributes(bw, attrList);
            }
            bw.write("/>");
            bw.newLine();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void writeEndElement(BufferedWriter bw, String element) {
        if (bw != null) {
            try {
                bw.write(getEndTag(element));
                bw.newLine();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static void writeDiagramViewport(BufferedWriter bw, Point viewport) {
        try {
            if (viewport != null) {
                bw.write(getStartTag(XMIConstants.UML_DIAGRAM_VIEWPORT));
                bw.newLine();
                writeXMIField(bw, Integer.toString(viewport.x));
                writeXMIField(bw, Integer.toString(viewport.y));
                bw.write(getEndTag(XMIConstants.UML_DIAGRAM_VIEWPORT));
                bw.newLine();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void writeGraphElementPosition(BufferedWriter bw, String elementType, Point location) {
        try {
            if (location != null) {
                if (elementType.equals("GRAPHELEMENT")) {
                    bw.write(getStartTag(XMIConstants.UML_GRAPHELEMENT_POSITION));
                } else if (elementType.equals("GRAPHCONNECTOR")) {
                    bw.write(getStartTag(XMIConstants.UML_GRAPHCONNECTOR_POSITION));
                }

                bw.newLine();
                writeXMIField(bw, Integer.toString(location.x));
                writeXMIField(bw, Integer.toString(location.y));
                if (elementType.equals("GRAPHELEMENT")) {
                    bw.write(getEndTag(XMIConstants.UML_GRAPHELEMENT_POSITION));
                } else if (elementType.equals("GRAPHCONNECTOR")) {
                    bw.write(getEndTag(XMIConstants.UML_GRAPHCONNECTOR_POSITION));
                }

                bw.newLine();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    private static void writeXMIField(BufferedWriter bw, String value) {
        try {
            bw.write(SPACE5);
            bw.write(getStartTag(XMIConstants.XMI_FIELD));
            bw.write(value);
            bw.write(getEndTag(XMIConstants.XMI_FIELD));
            bw.newLine();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void writeGraphNodeSize(BufferedWriter bw, Dimension size) {
        try {
            if (size != null) {
                bw.write(getStartTag(XMIConstants.UML_GRAPHNODE_SIZE));
                bw.newLine();
                writeXMIField(bw, Integer.toString(size.width));
                writeXMIField(bw, Integer.toString(size.height));
                bw.write(getEndTag(XMIConstants.UML_GRAPHNODE_SIZE));
                bw.newLine();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This will write the first line in the xmi doc
     * <XMI xmi.version="1.2" xmlns:UML="org.omg.xmi.namespace.UML" timestamp="2005-06-23 12:57:17">
     */
    public static void writeStartDocument(BufferedWriter bw) {
        if (bw != null) {
            try {
                bw.write("<" + XMIConstants.XMI_ROOT_TAG);
                writeAttributeString(bw, XMIConstants.XMI_VERSION, getVersion());
                writeAttributeString(bw, XMIConstants.XMI_XMLNS, getNameSpace());
                writeAttributeString(bw, XMIConstants.TIMESTAMP, getTimeStamp());
                bw.write(">");
                bw.newLine();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    static String getVersion() {
        return "1.2";
    }

    static String getTimeStamp() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date());
    }
    
    static String getNameSpace() {
        return "org.omg.xmi.namespace.UML";
    }

    /** This will write the last line in the doc
     * </XMI>
     */
    public static void writeEndDocument(BufferedWriter bw) {
        if (bw != null) {
            try {
                bw.write(getEndTag(XMIConstants.XMI_ROOT_TAG));
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void writeAttributes(BufferedWriter bw, HashMap attrMap) {
        if (attrMap != null) {
            Set set = attrMap.entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                writeAttributeString(bw, me.getKey(), me.getValue());
            }
        }
    }
    public static void writeProperties(BufferedWriter bw, List<HashMap> properties) {
        if (properties != null && properties.size() > 0) {
            writeBeginElement(bw, XMIConstants.UML_DIAGRAMELEMENT_PROPERTY);   
            for (Iterator<HashMap> it = properties.iterator(); it.hasNext();) {
                writeEmptyElement(bw, XMIConstants.UML_PROPERTY, it.next());
            }           
            writeEndElement(bw, XMIConstants.UML_DIAGRAMELEMENT_PROPERTY);
        }
    }
//WayPoints should be Point (not double)
    public static void writeEdgeWayPoints(BufferedWriter bw, List<Point> wayPoints) {
        try {
            if (wayPoints != null) {
                bw.write(getStartTag(XMIConstants.UML_GRAPHEDGE_WAYPOINTS));
                bw.newLine();
                for (Point pt : wayPoints) {
                    bw.write(SPACE5);
                    bw.write(getStartTag(XMIConstants.XMI_FIELD));
                    bw.newLine();
                    bw.write(SPACE5);
                    writeXMIField(bw, Integer.toString(pt.x));
                    bw.write(SPACE5);
                    writeXMIField(bw, Integer.toString(pt.y));
                    bw.write(SPACE5);
                    bw.write(getEndTag(XMIConstants.XMI_FIELD));
                    bw.newLine();
                }
                bw.write(getEndTag(XMIConstants.UML_GRAPHEDGE_WAYPOINTS));
                bw.newLine();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void writeSimpleSymanticModel(BufferedWriter bw, HashMap attrMap) {
        if (attrMap != null) {
            writeBeginElement(bw, XMIConstants.UML_GRAPHELEMENT_SEMANTICMODEL);
            writeEmptyElement(bw, XMIConstants.UML_SIMPLESEMANTICMODELELEMENT, attrMap);
            writeEndElement(bw, XMIConstants.UML_GRAPHELEMENT_SEMANTICMODEL);
        }
    }

    public static void writeSymanticModelBridge(BufferedWriter bw, HashMap attrMap, String elementType, String idref) {
        if (attrMap != null) {
            writeBeginElement(bw, XMIConstants.UML_GRAPHELEMENT_SEMANTICMODEL);            
            writeElement(bw, XMIConstants.UML_UML2SEMANTICMODELBRIDGE, attrMap);
            //MODEL BRIDGE ELEMENT
            writeBeginElement(bw, XMIConstants.UML_UML2SEMANTICMODELBRIDGE_ELEMENT);
            //<UML:Package xmi.idref="I11b8a6bm10480c06e65mm7147"/>
            writeIDRef(bw, elementType, idref);
            writeEndElement(bw, XMIConstants.UML_UML2SEMANTICMODELBRIDGE_ELEMENT);
            writeEndElement(bw, XMIConstants.UML_UML2SEMANTICMODELBRIDGE);
            writeEndElement(bw, XMIConstants.UML_GRAPHELEMENT_SEMANTICMODEL);
        }
    }


    public static void writeGraphConnectorIDREF(BufferedWriter bw, String elementType, String idref) {
        writeBeginElement(bw, XMIConstants.UML_GRAPHCONNECTOR_GRAPHEDGE);
        writeIDRef(bw, elementType, idref);
        writeEndElement(bw, XMIConstants.UML_GRAPHCONNECTOR_GRAPHEDGE);
    }


    public static void writeBeginElement(BufferedWriter bw, String element) {
        if (bw != null) {
            try {
                if (!element.equals("")) {
                    bw.write(getStartTag(element));
                    bw.newLine();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void writePartialBeginElement(BufferedWriter bw, String element) {
        if (bw != null) {
            try {
                bw.write("<" + element);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void writeDiagramOwner(BufferedWriter bw) {
        writeBeginElement(bw, XMIConstants.UML_DIAGRAM_OWNER);
        writeEndElement(bw, XMIConstants.UML_DIAGRAM_OWNER);
    }

    private static String getStartTag(String str) {
        return "<" + str + ">";
    }
    
    private static String getEndTag(String str) {
        return "</" + str + ">";
    }

    private static void writeIDRef(BufferedWriter bw, String elementType, String idref) {
        if (bw != null && elementType != null && idref != null) {
            try {
                writePartialBeginElement(bw, XMIConstants.UML+elementType);
                writeAttributeString(bw, XMIConstants.XMI_IDREF, idref);
                bw.write("/>");
                bw.newLine();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    

    
    private static void writeAttributeString(BufferedWriter bw, Object attrName, Object attrValue) {
        if (bw != null) {
            try {
                if (attrName != null && attrValue != null) {
                    bw.write(SPACE1 + attrName + "=\"" + attrValue.toString() + "\"" + SPACE1);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static void writeComment(BufferedWriter bw, String comment) {
        if (bw != null) {
            try {
                if (comment != null ) {
                    bw.write(SPACE1 + "<!-- JYOTHI : " + comment+SPACE1+"-->");
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
