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
package org.netbeans.modules.uml.drawingarea.persistence.util;

/**
 *
 * @author Jyothi
 */
public class XMIConstants {

    public static final String UML = "UML:";
    //Attribute constants
    public static final String XMI_ID = "xmi.id";
    public static final String ISVISIBLE = "isVisible";
    public static final String NAME = "name";
    public static final String ZOOM = "zoom";
    public static final String PRESENTATION = "presentation";
    public static final String TYPEINFO = "typeinfo";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String XMI_IDREF = "xmi.idref";
    public static final String DOCUMENTATION = "documentation";

    // Tag constants
    public static final String XMI_ROOT_TAG = "XMI";
    public static final String XMI_VERSION = "xmi.version";
    public static final String XMI_XMLNS = "xmlns:UML";
    public static final String TIMESTAMP = "timestamp";
    public static final String XMI_HEADER = "XMI.header";
    public static final String XMI_DOCUMENTATION = "XMI.documentation";
    public static final String XMI_EXPORTER = "XMI.exporter";
    public static final String XMI_EXPORTERVERSION = "XMI.exporterVersion";
    public static final String XMI_METAMODELVERSION = "XMI.metaModelVersion";
    public static final String XMI_CONTENT = "XMI.content";
    public static final String XMI_FIELD = "XMI.field";

    public static final String UML_DIAGRAM = "UML:Diagram";
    public static final String UML_GRAPHELEMENT_POSITION = "UML:GraphElement.position";
    public static final String UML_GRAPHNODE_SIZE = "UML:GraphNode.size";
    public static final String UML_DIAGRAM_VIEWPORT = "UML:Diagram.viewport";
    public static final String UML_GRAPHELEMENT_SEMANTICMODEL = "UML:GraphElement.semanticModel";
    public static final String UML_SIMPLESEMANTICMODELELEMENT = "UML:SimpleSemanticModelElement";
    public static final String UML_GRAPHELEMENT_CONTAINED = "UML:GraphElement.contained";

    public static final String UML_DIAGRAMELEMENT_PROPERTY = "UML:DiagramElement.property";
    public static final String UML_PROPERTY = "UML:Property";

    public static final String UML_GRAPHNODE = "UML:GraphNode";
    public static final String UML_UML2SEMANTICMODELBRIDGE = "UML:Uml2SemanticModelBridge";
    public static final String UML_UML2SEMANTICMODELBRIDGE_ELEMENT = "UML:Uml2SemanticModelBridge.element";

    //CONNECTOR CONSTANTS
    public static final String UML_DIAGRAM_OWNER = "UML:Diagram.owner";
    public static final String UML_GRAPHELEMENT_ANCHORAGE = "UML:GraphElement.anchorage";
    public static final String UML_GRAPHCONNECTOR = "UML:GraphConnector";
    public static final String UML_GRAPHCONNECTOR_POSITION = "UML:GraphConnector.position";
    public static final String UML_GRAPHCONNECTOR_GRAPHEDGE = "UML:GraphConnector.graphEdge";
    
    public static final String UML_GRAPHEDGE = "UML:GraphEdge";
    public static final String UML_GRAPHEDGE_WAYPOINTS = "UML:GraphEdge.waypoints";
    public static final String UML_GRAPHEDGE_ANCHORS = "UML:GraphEdge.anchor";
            
    public static final String UML_GRAPHELEMENT_DEPENDENCIES = "UML:GraphElement.dependencies";
}
