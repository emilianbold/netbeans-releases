package org.netbeans.modules.web.jsf.navigation.graph;

import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities;
import org.openide.filesystems.FileObject;

/**
 * @author David Kaspar
 */
public class SceneSerializer {
    
    private static final String SCENE_ELEMENT = "Scene"; // NOI18N
    private static final String SCENE_LAST_USED_SCOPE_ATTR = "Scope"; // NOI18N
    private static final String SCENE_SCOPE_ATTR = "Scope"; // NOI18N
    private static final String SCENE_SCOPE_ELEMENT = "Scope"; // NOI18N
    private static final String VERSION_ATTR = "version"; // NOI18NC
    
    private static final String SCENE_FACES_SCOPE = PageFlowToolbarUtilities.getScopeLabel(PageFlowToolbarUtilities.Scope.SCOPE_FACESCONFIG); //NOI18N
    private static final String SCENE_PROJECT_SCOPE = PageFlowToolbarUtilities.getScopeLabel(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);
    
    private static final String SCENE_NODE_COUNTER_ATTR = "nodeIDcounter"; // NOI18N
    private static final String SCENE_EDGE_COUNTER_ATTR = "edgeIDcounter"; // NOI18N
    
    private static final String NODE_ELEMENT = "Node"; // NOI18N
    private static final String NODE_ID_ATTR = "id"; // NOI18N
    private static final String NODE_X_ATTR = "x"; // NOI18N
    private static final String NODE_Y_ATTR = "y"; // NOI18N
    
    private static final String EDGE_ELEMENT = "Edge"; // NOI18N
    private static final String EDGE_ID_ATTR = "id"; // NOI18N
    private static final String EDGE_SOURCE_ATTR = "source"; // NOI18N
    private static final String EDGE_TARGET_ATTR = "target"; // NOI18N
    
    private static final String VERSION_VALUE_1 = "1"; // NOI18N
    private static final String VERSION_VALUE_2 = "2"; // NOI18N
    
    // call in AWT to serialize scene
    //    public static void serialize(PageFlowScene scene, File file) {
    //        Document document = XMLUtil.createDocument(SCENE_ELEMENT, null, null, null);
    //
    //        Node sceneElement = document.getFirstChild();
    //        setAttribute(document, sceneElement, VERSION_ATTR, VERSION_VALUE_1);
    //
    //
    //
    //        //        setAttribute (document, sceneElement, SCENE_NODE_COUNTER_ATTR, Long.toString (scene.nodeIDcounter));
    //        //        setAttribute (document, sceneElement, SCENE_EDGE_COUNTER_ATTR, Long.toString (scene.edgeIDcounter));
    //
    //        for (Page page : scene.getNodes()) {
    //            Element nodeElement = document.createElement(NODE_ELEMENT);
    //            setAttribute(document, nodeElement, NODE_ID_ATTR, page.getDisplayName());
    //            Widget widget = scene.findWidget(page);
    //            Point location = widget.getPreferredLocation();
    //            if( location == null ) {
    //                location = widget.getLocation();
    //            }
    //            setAttribute(document, nodeElement, NODE_X_ATTR, Integer.toString(location.x));
    //            setAttribute(document, nodeElement, NODE_Y_ATTR, Integer.toString(location.y));
    //            sceneElement.appendChild(nodeElement);
    //        }
    //        //        for (String edge : scene.getEdges ()) {
    //        //            Element edgeElement = document.createElement (EDGE_ELEMENT);
    //        //            setAttribute (document, edgeElement, EDGE_ID_ATTR, edge);
    //        //            String sourceNode = scene.getEdgeSource (edge);
    //        //            if (sourceNode != null)
    //        //                setAttribute (document, edgeElement, EDGE_SOURCE_ATTR, sourceNode);
    //        //            String targetNode = scene.getEdgeTarget (edge);
    //        //            if (targetNode != null)
    //        //                setAttribute (document, edgeElement, EDGE_TARGET_ATTR, targetNode);
    //        //            sceneElement.appendChild (edgeElement);
    //        //        }
    //
    //        FileOutputStream fos = null;
    //        try {
    //            fos = new FileOutputStream(file);
    //            XMLUtil.write(document, fos, "UTF-8"); // NOI18N
    //        } catch (Exception e) {
    //            Exceptions.printStackTrace(e);
    //        } finally {
    //            try {
    //                if (fos != null) {
    //                    fos.close();
    //                }
    //            } catch (Exception e) {
    //                Exceptions.printStackTrace(e);
    //            }
    //        }
    //    }
    //
    
    public static void serialize(PageFlowSceneData sceneData, FileObject file) {
        if( file == null || !file.isValid()){
            LOG.warning("Can not serialize locations because file is null.");
            return;
        }
        LOG.entering("SceneSerializer", "serialize");
        Document document = XMLUtil.createDocument(SCENE_ELEMENT, null, null, null);
        
        Node sceneElement = document.getFirstChild();
        setAttribute(document, sceneElement, VERSION_ATTR, VERSION_VALUE_2);
        setAttribute(document, sceneElement, SCENE_LAST_USED_SCOPE_ATTR, sceneData.getCurrentScopeStr());
        Node scopeFacesElement = createScopeElement(document, sceneData, SCENE_FACES_SCOPE);
        if( scopeFacesElement != null ) {
            sceneElement.appendChild( scopeFacesElement );
        }
        Node scopeProjectElement = createScopeElement(document, sceneData, SCENE_PROJECT_SCOPE);
        if( scopeProjectElement != null ) {
            sceneElement.appendChild( scopeProjectElement );
        }
        
        writeToFile(document, file);
        LOG.finest("Serializing to the follwoing file: " + file.toString());
        
        LOG.exiting("SceneSerializer", "serialize");
    }
    /**
     * @param Should be either SCENE_PROJECT_SCOPR or SCENE_FACES_SCOPE
     **/
    private final static Node createScopeElement( Document document, PageFlowSceneData sceneData, String scopeType ){
        Node sceneScopeElement =  null;
        Map<String,Point> facesConfigScopeMap = sceneData.getScopeData(scopeType);
        if( facesConfigScopeMap != null ){
            sceneScopeElement = document.createElement(SCENE_SCOPE_ELEMENT);
            setAttribute(document, sceneScopeElement, SCENE_SCOPE_ATTR, scopeType);
            
            for( String key : facesConfigScopeMap.keySet()){
                Point location = facesConfigScopeMap.get(key);
                if ( location != null ) {
                    Element nodeElement = document.createElement(NODE_ELEMENT);
                    setAttribute(document, nodeElement, NODE_ID_ATTR, key);
                    setAttribute(document, nodeElement, NODE_X_ATTR, Integer.toString(location.x));
                    setAttribute(document, nodeElement, NODE_Y_ATTR, Integer.toString(location.y));
                    sceneScopeElement.appendChild(nodeElement);
                }
            }
        }
        return sceneScopeElement;
        
    }
    
    private final static void writeToFile(Document document, FileObject file ){
        OutputStream fos = null;
        try {
            fos = file.getOutputStream();
            XMLUtil.write(document, fos, "UTF-8"); // NOI18N
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    
    private final static Logger LOG = Logger.getLogger("org.netbeans.modules.web.jsf.navigation");
    // call in AWT to deserialize scene
    public static void deserializeV1(PageFlowSceneData sceneData, FileObject file) {
        LOG.entering("SceneSerializer", "deserializeV1(PageFlowSceneData sceneData, File file)");
        Node sceneElement = getRootNode(file);
        
        
        //        scene.nodeIDcounter = Long.parseLong (getAttributeValue (sceneElement, SCENE_NODE_COUNTER_ATTR));
        //        scene.edgeIDcounter = Long.parseLong (getAttributeValue (sceneElement, SCENE_EDGE_COUNTER_ATTR));
        
        Map<String,Point> sceneInfo = new HashMap<String,Point>();
        for (Node element : getChildNode(sceneElement)) {
            if (NODE_ELEMENT.equals(element.getNodeName())) {
                String pageId = getAttributeValue(element, NODE_ID_ATTR);
                int x = Integer.parseInt(getAttributeValue(element, NODE_X_ATTR));
                int y = Integer.parseInt(getAttributeValue(element, NODE_Y_ATTR));
                
                
            }
        }
        sceneData.setScopeData(SCENE_PROJECT_SCOPE, sceneInfo);
        LOG.exiting("SceneSerializer", "deserialize");
    }
    
    
    public static void deserialize(PageFlowSceneData sceneData, FileObject file) {
        LOG.entering("SceneSerializer", "deserialize(PageFlowSceneData sceneData, File file)");
        Node sceneElement = getRootNode(file);
        if ( VERSION_VALUE_1.equals(getAttributeValue(sceneElement, VERSION_ATTR) )) {
            deserializeV1(sceneData, file);
        } else if ( VERSION_VALUE_2.equals(getAttributeValue(sceneElement, VERSION_ATTR))) {
            
            String lastUsedScope = getAttributeValue(sceneElement, SCENE_LAST_USED_SCOPE_ATTR);
            
            sceneData.setCurrentScope( PageFlowToolbarUtilities.getScope(lastUsedScope)  );
            LOG.fine("Last Used Scope: " + lastUsedScope);
            // TODO: Save the Last Used Scope
            
            
            NodeList scopeNodes = sceneElement.getChildNodes();
            for( int i = 0; i < scopeNodes.getLength(); i++ ){
                Node scopeElement = scopeNodes.item(i);
                if( scopeElement.getNodeName().equals(SCENE_SCOPE_ELEMENT) ){
                    String scope = getAttributeValue(scopeElement, SCENE_SCOPE_ATTR);
                    NodeList pageNodes = scopeElement.getChildNodes();
                    Map<String,Point> sceneInfo = new HashMap<String,Point>();
                    for( int j = 0; j < pageNodes.getLength(); j++ ){
                        Node pageNode = pageNodes.item(j);
                        if( pageNode.getNodeName().equals(NODE_ELEMENT)){
                            String pageDisplayName = getAttributeValue(pageNode, NODE_ID_ATTR);
                            int x = Integer.parseInt(getAttributeValue(pageNode, NODE_X_ATTR));
                            int y = Integer.parseInt(getAttributeValue(pageNode, NODE_Y_ATTR));
                            sceneInfo.put(pageDisplayName, new Point(x, y));
                        }
                    }
                    sceneData.setScopeData(scope, sceneInfo);
                }
            }
        }
        
        LOG.exiting("SceneSerializer", "deserialize(PageFlowSceneData sceneData, File file)");
    }
    
    private static void setAttribute(Document xml, Node node, String name, String value) {
        NamedNodeMap map = node.getAttributes();
        Attr attribute = xml.createAttribute(name);
        attribute.setValue(value);
        map.setNamedItem(attribute);
    }
    
    private static Node getRootNode(FileObject file) {
        InputStream is = null;
        try {
            is = file.getInputStream();
            Document doc = XMLUtil.parse(new InputSource(is), false, false, new ErrorHandler() {
                public void error(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }
                
                public void fatalError(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }
                
                public void warning(SAXParseException e) {
                    Exceptions.printStackTrace(e);
                }
            }, null);
            return doc.getFirstChild();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }
    
    private static String getAttributeValue(Node node, String attr) {
        try {
            if (node != null) {
                NamedNodeMap map = node.getAttributes();
                if (map != null) {                   
                    Node mynode = map.getNamedItem(attr);
                    if (mynode != null) {
                        return mynode.getNodeValue();
                    }
                }
            }
        } catch (DOMException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
    private static Node[] getChildNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength() : 0];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = childNodes.item(i);
        return nodes;
    }
    
}
