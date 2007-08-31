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
package org.netbeans.modules.vmd.flow;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.flow.visual.FlowEdgeDescriptor;
import org.netbeans.modules.vmd.api.flow.FlowSupport;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DataSerializer;
import org.netbeans.modules.vmd.api.io.ProjectTypeInfo;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Debug;
import org.w3c.dom.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * @author David Kaspar
 */
// TODO - polish serialized data - save dpi value for proper deserialization
public class FlowDataSerializer implements DataSerializer {

    private static final String FLOW_DOCUMENT_NODE = "FlowScene"; // NOI18N
    private static final String VERSION_ATTR = "version"; // NOI18N
    private static final String NODE_NODE = "Node"; // NOI18N
    private static final String EDGE_NODE = "Edge"; // NOI18N
    private static final String COMPONENTID_ATTR = "componentID"; // NOI18N
    private static final String DESCRIPTORID_ATTR = "descriptorID"; // NOI18N
    private static final String X_NODE_ATTR = "x"; // NOI18N
    private static final String Y_NODE_ATTR = "y"; // NOI18N
    private static final String X_EDGE_ATTR = "x"; // NOI18N
    private static final String Y_EDGE_ATTR = "y"; // NOI18N

    private static final String VERSION_VALUE_1 = "1"; // NOI18N

    public Node serializeData (DataObjectContext context, DesignDocument document, Document file) {
        ProjectTypeInfo info = ProjectTypeInfo.getProjectTypeInfoFor (context.getProjectType ());
        if (! info.getTags ().contains (FlowSupport.PROJECT_TYPE_TAG_FLOW))
            return null;

        FlowAccessController accessController = document.getListenerManager ().getAccessController (FlowAccessController.class);
        FlowScene scene = accessController.getScene ();
        Node node = file.createElement (FLOW_DOCUMENT_NODE);
        setAttribute (file, node, VERSION_ATTR, VERSION_VALUE_1); // NOI18N

        Set<?> objects = scene.getObjects ();
        for (Object o : objects) {
            if (scene.isNode (o)) {
                FlowDescriptor descriptor = (FlowDescriptor) o;
                Widget widget = scene.findWidget (descriptor);
                if (widget != null) {
                    Point location = widget.getPreferredLocation ();
                    if (location != null) {
                        Node data = file.createElement (NODE_NODE);
                        setAttribute (file, data, COMPONENTID_ATTR, Long.toString (descriptor.getRepresentedComponent ().getComponentID ()));
                        setAttribute (file, data, DESCRIPTORID_ATTR, descriptor.getDescriptorID ());
                        setAttribute (file, data, X_NODE_ATTR, Integer.toString (location.x));
                        setAttribute (file, data, Y_NODE_ATTR, Integer.toString (location.y));
                        node.appendChild (data);
                    }
                }
            } else if (scene.isEdge (o)) {
                FlowDescriptor descriptor = (FlowDescriptor) o;
                Widget widget = scene.findWidget (descriptor);
                if (widget instanceof ConnectionWidget) {
                    ConnectionWidget conn = (ConnectionWidget) widget;
                    java.util.List<Point> controlPoints = conn.getControlPoints ();
                    if (conn.getRoutingPolicy () == ConnectionWidget.RoutingPolicy.DISABLE_ROUTING_UNTIL_END_POINT_IS_MOVED  &&  ! controlPoints.isEmpty ()) {
                        Node data = file.createElement (EDGE_NODE);
                        setAttribute (file, data, COMPONENTID_ATTR, Long.toString (descriptor.getRepresentedComponent ().getComponentID ()));
                        setAttribute (file, data, DESCRIPTORID_ATTR, descriptor.getDescriptorID ());
                        for (int i = 0; i < controlPoints.size (); i ++) {
                            Point point = controlPoints.get (i);
                            setAttribute (file, data, X_EDGE_ATTR + i, Integer.toString (point.x));
                            setAttribute (file, data, Y_EDGE_ATTR + i, Integer.toString (point.y));
                        }
                        node.appendChild (data);
                    }
                }
            }
        }

        return node;
    }

    public boolean deserializeData (final DataObjectContext context, final DesignDocument document, final Node data) {
        ProjectTypeInfo info = ProjectTypeInfo.getProjectTypeInfoFor (context.getProjectType ());
        if (! info.getTags ().contains (FlowSupport.PROJECT_TYPE_TAG_FLOW))
            return false;

        if (! FLOW_DOCUMENT_NODE.equals (data.getNodeName ()))
            return false;

        if (! VERSION_VALUE_1.equals (getAttributeValue (data, VERSION_ATTR)))
            return false;

        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                final FlowScene scene = document.getListenerManager ().getAccessController (FlowAccessController.class).getScene ();

                document.getTransactionManager ().readAccess (new Runnable() {
                    public void run () {
                        deserializeDataVersion1 (document, data, scene);
                    }
                });

                scene.validate ();
            }
        });

        return true;
    }

    private void deserializeDataVersion1 (DesignDocument document, Node data, FlowScene scene) {
        boolean isAnythingLoaded = false;
        for (Node node : getChildNode (data)) {
            if (NODE_NODE.equals (node.getNodeName ())) {
                long componentID = Long.parseLong (getAttributeValue (node, COMPONENTID_ATTR));
                String descriptorid = getAttributeValue (node, DESCRIPTORID_ATTR);
                int x, y;
                try {
                    x = Integer.parseInt (getAttributeValue (node, X_NODE_ATTR));
                    y = Integer.parseInt (getAttributeValue (node, Y_NODE_ATTR));
                } catch (NumberFormatException e) {
                    continue;
                }
                DesignComponent representedComponent = document.getComponentByUID (componentID);
                if (representedComponent == null || descriptorid == null)
                    continue;
                FlowNodeDescriptor descriptor = new FlowNodeDescriptor (representedComponent, descriptorid);
                Widget widget = scene.findWidget (descriptor);
                if (widget != null) {
                    widget.setPreferredLocation (new Point (x, y));
                    isAnythingLoaded = true;
                }
            } else if (EDGE_NODE.equals (node.getNodeName ())) {
                long componentID = Long.parseLong (getAttributeValue (node, COMPONENTID_ATTR));
                String descriptorid = getAttributeValue (node, DESCRIPTORID_ATTR);
                DesignComponent representedComponent = document.getComponentByUID (componentID);
                if (representedComponent == null || descriptorid == null)
                    continue;
                FlowEdgeDescriptor descriptor = new FlowEdgeDescriptor (representedComponent, descriptorid, null, false, null, false);
                Widget widget = scene.findWidget (descriptor);
                if (widget instanceof ConnectionWidget) {
                    ConnectionWidget conn = (ConnectionWidget) widget;
                    conn.setRoutingPolicy (ConnectionWidget.RoutingPolicy.DISABLE_ROUTING_UNTIL_END_POINT_IS_MOVED);
                    ArrayList<Point> controlPoints = new ArrayList<Point> ();
                    for (int i = 0; ; i ++) {
                        int x, y;
                        try {
                            x = Integer.parseInt (getAttributeValue (node, X_EDGE_ATTR + i));
                            y = Integer.parseInt (getAttributeValue (node, X_EDGE_ATTR + i));
                        } catch (NumberFormatException e) {
                            break;
                        }
                        controlPoints.add (new Point (x, y));
                    }
                    if (! controlPoints.isEmpty ()) {
                        conn.setRoutingPolicy (ConnectionWidget.RoutingPolicy.DISABLE_ROUTING_UNTIL_END_POINT_IS_MOVED);
                        conn.setControlPoints (controlPoints, false);
                    }
                }
            }
        }
        if (! isAnythingLoaded)
            scene.layoutScene ();
    }

    private static String getAttributeValue (Node node, String attr) {
        try {
            if (node != null) {
                NamedNodeMap map = node.getAttributes ();
                if (map != null) {
                    node = map.getNamedItem (attr);
                    if (node != null)
                        return node.getNodeValue ();
                }
            }
        } catch (DOMException e) {
            Debug.warning (e);
        }
        return null;
    }

    private static void setAttribute (Document xml, Node node, String name, String value) {
        NamedNodeMap map = node.getAttributes ();
        Attr attribute = xml.createAttribute (name);
        attribute.setValue (value);
        map.setNamedItem (attribute);
    }

    private static Node[] getChildNode (Node node) {
        NodeList childNodes = node.getChildNodes ();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength () : 0];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = childNodes.item (i);
        return nodes;
    }

}
