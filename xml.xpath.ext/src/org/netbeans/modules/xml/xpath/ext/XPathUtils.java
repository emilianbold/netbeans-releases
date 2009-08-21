/*
 * Utils.java
 * 
 * Created on 31.08.2007, 15:40:45
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xpath.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.netbeans.modules.xml.xpath.ext.schema.FindChildrenSchemaVisitor;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext.SchemaCompPair;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * Utility class.
 * 
 * @author nk160297
 */
public class XPathUtils {

    /**
     * Converts the qName to a String. 
     * Only the prefix and the local part is used. 
     * The namespace URI is ignored. 
     * 
     * This method is intended to print an entity.
     */ 
    public static String qNameObjectToString(QName qName) {
        String prefix = qName.getPrefix();
        if (prefix == null || prefix.length() == 0) {
            return qName.getLocalPart();
        } else {
            return prefix + ":" + qName.getLocalPart();
        }
                
    }
    
    /**
     * Converts the qName to a String. 
     * Only the prefix and the namespace URI is used. 
     * The local part is ignored. 
     * 
     * This method is intended to print a namespace which is held in a QName.
     */ 
    public static String gNameNamespaceToString(QName qName) {
        String prefix = qName.getPrefix();
        String nsUri = qName.getNamespaceURI();
        //
        if (prefix == null || prefix.length() == 0) {
            return "{" + nsUri + "}";
        } else {
            return "{" + nsUri + "}" + prefix;
        }
    }
    
    /**
     * Check if the namespace URI is specified for the name. 
     * If the name isn't specified, then try resolve it from the prefix.
     * Returns the corrected name if possible. Otherwise returns old name.
     * @param nsContext
     * @param name
     * @return
     */
    public static QName resolvePrefix(NamespaceContext nsContext, QName name) {
        String nsUri = name.getNamespaceURI();
        if (nsUri == null || nsUri.length() == 0 && nsContext != null) {
            //
            String nsPrefix = name.getPrefix();
            nsUri = nsContext.getNamespaceURI(nsPrefix);
            //
            if (nsUri != null) {
                String localPart = name.getLocalPart();
                QName newName = new QName(nsUri, localPart);
                name = newName;
            }
        }
        //
        return name;
    }
    
    public static boolean equalsIgnorNsUri(QName qName1, QName qName2) {
        return (qName1.getLocalPart().equals(qName2.getLocalPart())) && 
                (qName1.getPrefix().equals(qName2.getPrefix()));
    }
    
    public static boolean samePredicatesArr(
            XPathPredicateExpression[] predArr1, 
            XPathPredicateExpression[] predArr2) {
        //
        // Compare predicates count
        int counter1 = predArr1 == null ? 0 : predArr1.length;
        int counter2 = predArr2 == null ? 0 : predArr2.length;
        if (counter1 != counter2) {
            return false;
        }
        // Compare predicates one by one
        for (int index = 0; index < counter1; index++) {
            XPathPredicateExpression predicate1 = predArr1[index];
            XPathPredicateExpression predicate2 = predArr2[index];
            String predText1 = predicate1.getExpressionString();
            String predText2 = predicate2.getExpressionString();
            if (!(predText1.equals(predText2))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Determines if a namespace prefix is required for the specified schema component. 
     * @param sComp
     * @return
     */
    public static boolean isPrefixRequired(SchemaComponent sComp) {
        if (sComp instanceof LocalElement) {
            Form form = ((LocalElement)sComp).getFormEffective();
            if (form == Form.QUALIFIED) {
                return true;
            } else {
                return false;
            }
        } else if (sComp instanceof GlobalElement) {
            return true;
        } else if (sComp instanceof LocalAttribute) {
            Form form = ((LocalAttribute)sComp).getFormEffective();
            if (form == Form.QUALIFIED) {
                return true;
            } else {
                return false;
            }
        } else if (sComp instanceof GlobalElement || 
                sComp instanceof ElementReference || 
                sComp instanceof GlobalAttribute) {
            // all global objects have to be with a prefix
            return true;
        }
        //
        assert true : "Unsupported schema component in the BPEL mapper tree!"; // NOI18N
        return false;
    }
    
    /**
     * Checks if the specified Schema component has any subcomponents.
     * @param sComp
     * @return
     */
    public static boolean hasSubcomponents(SchemaComponent sComp) {
        FindAllChildrenSchemaVisitor checker = 
                new FindAllChildrenSchemaVisitor(true, true, true);
        checker.lookForSubcomponents(sComp);
        List<SchemaComponent> found = checker.getFound();
        return found != null && !found.isEmpty();
    }
    
    /**
     * Constructs a list of SchemaCompPair objects. Each SchemaCompPair represents 
     * a child Schema component of a parent. Parents are taken from the 
     * specified parent SchemaContext. The context can contain several schema
     * components. So children mean children of all possible parent components 
     * in current context. 
     * 
     * Childrent can be real children schema components or pseudo components. 
     * See XPathPseudoComp class.
     * 
     * @param xPathModel
     * @param parentSContext
     * @param lookForElements indicates if it necessary to add elements to 
     * the result list.
     * @param lookForAttributes indicates if it necessary to add attributes to 
     * the result list.
     * @param collectAny indicates if it necessary to add xsd:any or 
     * xsd:anyAttribute to the result list. 
     * @return result list
     */
    public static List<SchemaCompPair> findSubcomponents(
            XPathModel xPathModel, XPathSchemaContext parentSContext, 
            boolean lookForElements, boolean lookForAttributes, boolean collectAny) {
        //
        assert parentSContext != null;
        ArrayList<SchemaCompPair> result = new ArrayList<SchemaCompPair>();
        //
        XPathCastResolver castResolver = xPathModel.getXPathCastResolver();
        Set<SchemaCompPair> parentCompPairSet = parentSContext.getSchemaCompPairs(); 
        //
        boolean processPseudoComp = 
                castResolver != null && parentCompPairSet.size() == 1;
        //
        for (SchemaCompPair parentCompPair : parentCompPairSet) {
            SchemaCompHolder parentCompHolder = parentCompPair.getCompHolder();
            //
            FindAllChildrenSchemaVisitor visitor = 
                    new FindAllChildrenSchemaVisitor(
                    lookForElements, lookForAttributes, false);
            visitor.lookForSubcomponents(parentCompHolder.getSchemaComponent());
            //
            List<SchemaComponent> foundComps = visitor.getFound();
            for (SchemaComponent foundComp : foundComps) {
                SchemaCompPair newPair = 
                        new SchemaCompPair(foundComp, parentCompHolder);
                result.add(newPair);
            }
            //
            if (processPseudoComp) {
                if (visitor.hasAny() || visitor.hasAnyAttribute()) {
                    List<XPathPseudoComp> pcList = 
                            castResolver.getPseudoCompList(parentSContext);
                    for (XPathPseudoComp pseudoComp : pcList) {
                        if (pseudoComp.isAttribute()) {
                            if (lookForAttributes && visitor.hasAnyAttribute()) {
                                SchemaCompHolder scHolder = 
                                        SchemaCompHolder.Factory.construct(pseudoComp);
                                SchemaCompPair newPair = 
                                        new SchemaCompPair(scHolder, parentCompHolder);
                                result.add(newPair);
                            }
                        } else {
                            if (lookForElements && visitor.hasAny()) {
                                SchemaCompHolder scHolder = 
                                        SchemaCompHolder.Factory.construct(pseudoComp);
                                SchemaCompPair newPair = 
                                        new SchemaCompPair(scHolder, parentCompHolder);
                                result.add(newPair);
                            }
                        }
                    }
                }
            }
        }
        //
        return result;
    }
    
    /**
     * Collects a list of SchemaCompHoder objects, which are global accessible.
     * 
     * @param xPathModel
     * @param lookForElements indicates if it necessary to add elements to 
     * the result list.
     * @param lookForAttributes indicates if it necessary to add attributes to 
     * the result list.
     * @param collectAny indicates if it necessary to add xsd:any or 
     * xsd:anyAttribute to the result list. 
     * @return result list
     */
    public static List<SchemaComponent> findRootComponents(XPathModel xPathModel, 
            boolean lookForElements, boolean lookForAttributes, boolean collectAny) {
        //
        ArrayList<SchemaComponent> result = new ArrayList<SchemaComponent>();
        //
        ExternalModelResolver exModelResolver = xPathModel.getExternalModelResolver();
        //
        if (exModelResolver != null) {
            //
            // Look for all available root elements (attributes) 
            // in all available models
            // Pseudo components can be only inside of another schema components, 
            // So they can't be global. It doesn't necessary to look them here.
            Collection<SchemaModel> sModels = exModelResolver.getVisibleModels();
            for (SchemaModel sModel : sModels) {
                Schema schema = sModel.getSchema();
                FindAllChildrenSchemaVisitor visitor = 
                        new FindAllChildrenSchemaVisitor(
                        lookForElements, lookForAttributes, false);
                visitor.lookForSubcomponents(schema);
                //
                List<SchemaComponent> foundComps = visitor.getFound();
                result.addAll(foundComps);
            }
        }
        //
        return result;
    }
    
    /**
     * Returns the list of SchemaCompHolder components, which are children 
     * of the specified parent schema component component (and context) 
     * and have specified name and namespace. 
     * Usually only one component has to be returned. 
     * Not only real schema components can be childrent but also pseudo components, 
     * which are result of casting xsd:any or xsd:anyAttribute. 
     * 
     * @param xPathModel
     * @param parentContext
     * @param parent 
     * @param soughtName required name
     * @param soughtNamespace required namespace
     * @param isAttribute indicates if an attribute or element is required
     * @param cachingVisitor force using the cacheing visitor if it specified. 
     * It can be null. 
     * @return
     */
    public static List<SchemaCompHolder> getChildren(
            XPathModel xPathModel, XPathSchemaContext parentContext,
            SchemaComponent parent, String soughtName, 
            String soughtNamespace, boolean isAttribute) {
        List<SchemaComponent> found = null;
        boolean hasAny = false;
        boolean hasAnyAttribute = false;
        ArrayList<SchemaCompHolder> result = new ArrayList<SchemaCompHolder>();
        //
        FindChildrenSchemaVisitor visitor =
                new FindChildrenSchemaVisitor(parentContext,
                soughtName, soughtNamespace, isAttribute);
        visitor.lookForSubcomponent(parent);
        found = visitor.getFound();
        hasAny = visitor.hasAny();
        hasAnyAttribute = visitor.hasAnyAttribute();
        //
        if (found == null || found.isEmpty()) {
            //
            // try looking for Pseudo Components here
            XPathCastResolver castResolver = xPathModel.getXPathCastResolver();
            if (castResolver != null) {
                List<XPathPseudoComp> pcList = 
                        castResolver.getPseudoCompList(parentContext);
                if (pcList != null) {
                    for (XPathPseudoComp pseudoComp : pcList) {
                        if (!hasAnyAttribute && isAttribute) {
                            // AnyAttribute isn't supported
                            continue;
                        }
                        //
                        if (!hasAny && !isAttribute) {
                            // Any isn't supported
                            continue;
                        }
                        //
                        if (isAttribute != pseudoComp.isAttribute()) {
                            continue;
                        }
                        //
                        if (!(soughtName.equals(pseudoComp.getName()))) {
                            // Different name
                            continue;
                        }
                        if (!(soughtNamespace.equals(pseudoComp.getNamespace()))) {
                            // different namespace
                            continue;
                        } 
                        //
                        SchemaCompHolder compHolder = 
                                SchemaCompHolder.Factory.construct(pseudoComp);
                        result.add(compHolder);
                    }
                }
            }
        } else {
            convertToHolders(found, result);
        }
        //
        return result;
    }
    
    /**
     * Converts the list of Schema components to the list of SC holders 
     * @param scList
     * @param target
     */
    private static void convertToHolders(List<SchemaComponent> scList, 
            ArrayList<SchemaCompHolder> target) {
        for (SchemaComponent sc : scList) {
            SchemaCompHolder newHolder = SchemaCompHolder.Factory.construct(sc);
            if (newHolder != null) {
                target.add(newHolder);
            }
        }
    }
}
