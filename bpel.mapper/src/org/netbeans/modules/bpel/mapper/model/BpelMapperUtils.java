package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.cast.PseudoCompManager;
import org.netbeans.modules.bpel.mapper.model.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathMetadataUtils;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.schema.FindChildrenSchemaVisitor;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 *
 * @author nk160297
 */
public final class BpelMapperUtils {

    /**
     * Calculates XPath type of a source pin
     * @param sourcePin
     * @return
     */
    public static XPathType calculateXPathSourcePinType(SourcePin sourcePin) {
        if (sourcePin instanceof TreeSourcePin) {
            TreePath treePath = ((TreeSourcePin)sourcePin).getTreePath();
            Object lastPathObj = MapperSwingTreeModel.getDataObject(treePath);
            if (lastPathObj instanceof VariableDeclarationScope) {
                return null;
            } else if (lastPathObj instanceof AbstractVariableDeclaration) {
                SchemaComponent varType = EditorUtil.getVariableSchemaType(
                        (AbstractVariableDeclaration)lastPathObj);
                if (varType == null) {
                    // Null can be for example in case of message variable
                    // Message variable can't be directly used by the XPath
                    return null;
                } else {
                    return XPathMetadataUtils.calculateXPathType(varType);
                }
            } else if (lastPathObj instanceof Part) {
                Part part = (Part)lastPathObj;
                SchemaComponent partType = EditorUtil.getPartType(part);
                if (partType != null) {
                    return XPathMetadataUtils.calculateXPathType(partType);
                }
            } else if (lastPathObj instanceof SchemaComponent) {
                return XPathMetadataUtils.calculateXPathType(
                        (SchemaComponent)lastPathObj);
            } else {
                return null;
            }
        } else if (sourcePin instanceof Vertex) {
            Object dataObject = ((Vertex)sourcePin).getDataObject();
            if (dataObject instanceof CoreFunctionType) {
                return ((CoreFunctionType)dataObject).getMetadata().getResultType();
            } else if (dataObject instanceof CoreOperationType) {
                return ((CoreOperationType)dataObject).getMetadata().getResultType();
            } else if (dataObject instanceof ExtFunctionMetadata) {
                return ((ExtFunctionMetadata)dataObject).getResultType();
            } else if (dataObject == XPathNumericLiteral.class) {
                return XPathType.NUMBER_TYPE;
            } else if (dataObject == XPathStringLiteral.class) {
                return XPathType.STRING_TYPE;
            }
        }
        //
        return null;
    }

    /**
     * Choose the vertex item from the specified list, which best fitted to the 
     * specified XPath type. It is used to determine a place to connect a link. 
     * @param itemsList
     * @param type
     * @return
     */
    public static VertexItem findBestFittedItem(List<VertexItem> itemsList, 
            XPathType type) {
        //
        if (itemsList == null || itemsList.isEmpty()) {
            return null;
        }
        //
        if (type == null) {
            type = XPathType.ANY_TYPE;
        }
        //
        for (VertexItem vertItem : itemsList) {
            if (vertItem.isHairline()) {
                // Skip hairlines
                continue;
            }
            //
            Object itemDataObject = vertItem.getDataObject();
            if (itemDataObject instanceof ArgumentDescriptor) {
                XPathType argType = ((ArgumentDescriptor)itemDataObject).
                        getArgumentType();
                if (type.equals(argType)) {
                    return vertItem;
                }
            }
        }
        //
        // Return the first item.
        return itemsList.get(0);
    }
 
    /**
     * Calculates if the specified schema component is repeating or not.
     * @param sComp
     * @return
     */
    public static boolean isRepeating(SchemaComponent sComp) {
        boolean isRepeating = false;
        String maxOccoursStr = null;
        if (sComp instanceof GlobalElement) {
            return false;
        } else if (sComp instanceof LocalElement) {
            LocalElement lElement = (LocalElement)sComp;
            //
            maxOccoursStr = lElement.getMaxOccursEffective();
        } else if (sComp instanceof ElementReference) {
            ElementReference elementRef = (ElementReference)sComp;
            //
            maxOccoursStr = elementRef.getMaxOccursEffective();
        }
        //
        if (maxOccoursStr != null) {
            try {
                int maxOccoursInt = Integer.parseInt(maxOccoursStr);
                isRepeating = maxOccoursInt > 1;  
            } catch (NumberFormatException ex) {
                // Do Nothing
                isRepeating = true;
            }
        }
        //
        return isRepeating;
    }
    
    /**
     * Builds a new GraphSubset, which can contain additional links. 
     * It adds all missing links, which connected to the vertex from subset.
     * @param graphSubset
     * @return
     */
    public static GraphSubset getExtendedSubset(GraphSubset graphSubset) {
        //
        List<Vertex> vertexList = new ArrayList<Vertex>(graphSubset.getVertexCount());
        HashSet<Link> linkSet = new HashSet<Link>();
        // populate vertex set
        for (int index = 0; index < graphSubset.getVertexCount(); index++) {
            Vertex vertex = graphSubset.getVertex(index);
            vertexList.add(vertex);
        }
        // populate link set
        for (int index = 0; index < graphSubset.getLinkCount(); index++) {
            Link link = graphSubset.getLink(index);
            linkSet.add(link);
        }
        //
        for (Vertex vertex : vertexList) {
            List<Link> connectedLinkList = getConnectedLinkList(vertex);
            linkSet.addAll(connectedLinkList);
        }
        //
        List<Link> linkList = new ArrayList<Link>(linkSet);
        //
        return new GraphSubset(
                graphSubset.getTreePath(), 
                graphSubset.getGraph(),
                vertexList, linkList);
    }
    
    public static boolean areVertexDependent(Vertex from, Vertex to) {
        Graph graph = from.getGraph();
        if (to.getGraph() != graph) {
            // Vertexes from different graphs can't be dependent
            return false;
        }
        //
        List<Vertex> dependingVertexList = new ArrayList<Vertex>();
        populateDependingVertexList(from, dependingVertexList);
        //
        return dependingVertexList.contains(to);
    }
    
    private static void populateDependingVertexList(Vertex from, List<Vertex> vertexList) {
        for (int index = 0; index < from.getItemCount(); index++) {
            VertexItem vItem = from.getItem(index);
            if (vItem.isHairline()) {
                continue;
            }
            //
            Link ingoingLink = vItem.getIngoingLink();
            if (ingoingLink == null) {
                continue;
            }
            //
            SourcePin linkSource = ingoingLink.getSource();
            if (linkSource instanceof Vertex) {
                Vertex sourceVertex = (Vertex)linkSource;
                vertexList.add(sourceVertex);
                //
                populateDependingVertexList(sourceVertex, vertexList);
            }
        }
    }

    public static List<Link> getConnectedLinkList(Vertex vertex) {
        List<Link> result = new ArrayList<Link>();
        //
        Link outgoingLink = vertex.getOutgoingLink();
        if (outgoingLink != null) {
            result.add(outgoingLink);
        }
        //
        for (int index = 0; index < vertex.getItemCount(); index++) {
            VertexItem vItem = vertex.getItem(index);
            if (vItem.isHairline()) {
                continue;
            }
            //
            Link ingoingLink = vItem.getIngoingLink();
            if (ingoingLink == null) {
                continue;
            }
            //
            result.add(ingoingLink);
        }
        //
        return result;
    }

    /**
     * Obtains a schema global type of the tree item if possible.
     * @param treeItem
     * @return
     */
    public static GlobalType getGlobalType(Object treeItem) {
        SchemaComponent targetSComp = getAssociatedSchemaComp(treeItem);
        // 
        if (targetSComp == null) {
            return null;
        }
        //
        GlobalType gType = getGlobalType(targetSComp);
        return gType;
    }
    
    /**
     * Takes the type of the schema component if the type is global. 
     * @param sComp
     * @return
     */
    public static GlobalType getGlobalType(SchemaComponent sComp) {
        if (sComp == null) {
            return null;
        }
        //
        GlobalType gType = null;
        //
        if (sComp instanceof GlobalType) {
            gType = (GlobalType)sComp;
        } else if (sComp instanceof TypeContainer) {
            TypeContainer typeContainer = (TypeContainer)sComp;
            NamedComponentReference<? extends GlobalType> typeRef = 
                    typeContainer.getType();
            if (typeRef != null) {
                gType = typeRef.get();
            }
        } else {
            if (sComp instanceof LocalAttribute) {
                NamedComponentReference<GlobalSimpleType> gTypeRef = 
                        ((LocalAttribute)sComp).getType();
                if (gTypeRef != null) {
                    gType = gTypeRef.get();
                }
            } else if (sComp instanceof GlobalAttribute) {
                NamedComponentReference<GlobalSimpleType> gTypeRef = 
                        ((GlobalAttribute)sComp).getType();
                if (gTypeRef != null) {
                    gType = gTypeRef.get();
                }
            }
        }
        //
        return gType;
    }
    
    /**
     * Returns a schema component for the tree item if the item has associated one.
     * @param treeItem
     * @return
     */
    public static SchemaComponent getAssociatedSchemaComp(Object treeItem) {
        SchemaComponent result = null;
        //
        if (treeItem == null) {
            return null;
        } else if (treeItem instanceof SchemaComponent) {
            result = (SchemaComponent)treeItem;
        } else if (treeItem instanceof VariableDeclarationScope) {
            return null;
        } else if (treeItem instanceof VariableDeclaration) {
            result = EditorUtil.getVariableSchemaType((VariableDeclaration)treeItem);
        } else if (treeItem instanceof Part) {
            result = EditorUtil.getPartType((Part)treeItem);
        } else if (treeItem instanceof XPathSchemaContextHolder) {
            XPathSchemaContext sContext = 
                    ((XPathSchemaContextHolder)treeItem).getSchemaContext();
            if (sContext != null) {
                result = XPathSchemaContext.Utilities.getSchemaComp(sContext);
            }
        }
        // 
        return result;
    }

    /**
     * Determines if the tree component has a sibling schema component with 
     * the specified characteristics. It is used to calculate uniqueness of 
     * the new pseudo component. 
     * 
     * @param bpelMapper
     * @param leftTree
     * @param compLocationPath 
     * @param soughtName required name
     * @param soughtNamespace required namespace
     * @param isAttribute indicates if an attribute or element is required
     * @return
     */
    public static boolean hasSibling(BpelMapperModel bpelMapperModel, 
            boolean leftTree, TreeItem treeItem, 
            String soughtName, String soughtNamespace, boolean isAttribute) {
        //
        XPathSchemaContext parentSContext = PathConverter.
                constructContext(treeItem, true);
        //
        FindChildrenSchemaVisitor visitor = 
                new FindChildrenSchemaVisitor(parentSContext, 
                soughtName, soughtNamespace, isAttribute);
        SchemaComponent parentSComp = XPathSchemaContext.Utilities.
                getSchemaCompHolder(parentSContext).getSchemaComponent();
        visitor.lookForSubcomponent(parentSComp);
        List<SchemaComponent> found = visitor.getFound();
        if (found != null && !found.isEmpty()) {
            return true;
        }
        //
        // try looking for Pseudo Components here
        if (found == null || found.isEmpty()) {
            PseudoCompManager pseudoManager = PseudoCompManager.
                    getPseudoCompManager(bpelMapperModel, leftTree);
            if (pseudoManager != null) {
                XPathPseudoComp pseudo = pseudoManager.getPseudoComp(
                        treeItem, true, 
                        soughtName, soughtNamespace, isAttribute);
                return pseudo != null;
            }
        }
        //
        return false;
    }
    
    /**
     * Returns a variable, which is the first in the location path
     * @param treeItem
     * @return
     */
    public static VariableDeclaration getBaseVariable(TreeItem treeItem) {
        Iterator itr = treeItem.iterator();
        while (itr.hasNext()) {
            Object obj = itr.next();
            if (obj instanceof VariableDeclaration) {
                return (VariableDeclaration)obj;
            }
        }
        //
        return null;
    }
    

            
    
}
