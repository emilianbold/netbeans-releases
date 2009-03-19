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
package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * This schema visitor is inteneded to look for a children elements or attributes 
 * It looks only children at the next lavel. 
 * 
 * @author nk160297
 */
public class CachingSchemaSearchVisitor extends AbstractSchemaSearchVisitor {

    // Maps the Namespace URI to a list of Schema models.
    private HashMap<String, ArrayList<SchemaModel>> mModelsCache;
    
    private XPathSchemaContext mParentContext;
    private String mySoughtName;
    private String mySoughtNamespace;
    private boolean isAttribute;

    // Collects the found Schema components.
    private List<SchemaComponent> myFound;

    private boolean mHasAny = false;
    private boolean mHasAnyAttribute = false;
    
    public CachingSchemaSearchVisitor() {
        mModelsCache = new HashMap<String, ArrayList<SchemaModel>>();
        myFound = new ArrayList<SchemaComponent>();
    }

    public List<SchemaComponent> getFound() {
        return myFound;
    }

    public boolean hasAny() {
        return mHasAny;
    }
    
    public boolean hasAnyAttribute() {
        return mHasAnyAttribute;
    }
    
    public void lookForSubcomponent(XPathSchemaContext parentContext, 
            SchemaComponent parent, String soughtName,
            String soughtNamespace, boolean isAttribute) {
        assert soughtName != null : "At least sought name has to be specified!"; // NOI18N
        //
        myFound.clear();
        mParentContext = parentContext;
        mySoughtName = soughtName;
        mySoughtNamespace = soughtNamespace;
        this.isAttribute = isAttribute;
        lookForSubcomponentImpl(parent);
        //
        return;
    }

    public void lookForSubcomponentImpl(SchemaComponent sc) {
        if (sc instanceof Element) {
            if (sc instanceof TypeContainer) {
                NamedComponentReference<? extends GlobalType> typeRef = 
                        ((TypeContainer) sc).getType();
                if (typeRef != null) {
                    GlobalType globalType = 
                            resolve(sc, typeRef, GlobalType.class, false);
                    if (globalType != null) {
                        globalType.accept(this);
                    }
                }
                LocalType localType = ((TypeContainer) sc).getInlineType();
                if (localType != null) {
                    localType.accept(this);
                }
            } else if (sc instanceof ElementReference) {
                NamedComponentReference<GlobalElement> gElemRef = 
                        ((ElementReference) sc).getRef();
                if (gElemRef != null) {
                    GlobalElement gElement = 
                            resolve(sc, gElemRef, GlobalElement.class, true);
                    lookForSubcomponentImpl(gElement);
                }
            }
        } else if (sc instanceof ComplexType) {
            visitChildren(sc);
        } else if (sc instanceof Schema) {
            visitChildren(sc);
        } else {
            // Other elements can't containg nested elements or attributes
        }
    }

    //--------------------------------------------------------------------------
    @Override
    public void visit(ElementReference er) {
        NamedComponentReference<GlobalElement> geRef = er.getRef();
        if (geRef != null) {
            GlobalElement ge = resolve(er, geRef, GlobalElement.class, true);
            if (ge != null) {
                visit(ge);
            }
        }
    }

    @Override
    public void visit(AttributeReference ar) {
        NamedComponentReference<GlobalAttribute> gaRef = ar.getRef();
        if (gaRef != null) {
            GlobalAttribute ga = resolve(ar, gaRef, GlobalAttribute.class, true);
            if (ga != null) {
                visit(ga);
            }
        }
    }

    @Override
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> gagRef = agr.getGroup();
        if (gagRef != null) {
            GlobalAttributeGroup gag = resolve(agr, gagRef,
                    GlobalAttributeGroup.class, false);
            if (gag != null) {
                visit(gag);
            }
        }
    }

    @Override
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> ggRef = gr.getRef();
        if (ggRef != null) {
            GlobalGroup gg = resolve(gr, ggRef, GlobalGroup.class, false);
            if (gg != null) {
                visit(gg);
            }
        }
    }

    @Override
    public void visit(SimpleExtension se) {
        NamedComponentReference<GlobalType> gtRef = se.getBase();
        if (gtRef != null) {
            GlobalType gt = resolve(se, gtRef, GlobalType.class, false);
            if (gt != null) {
                visitChildren(gt);
            }
        }
        visitChildren(se);
    }

    @Override
    public void visit(ComplexExtension ce) {
        NamedComponentReference<GlobalType> gtRef = ce.getBase();
        if (gtRef != null) {
            GlobalType gt = resolve(ce, gtRef, GlobalType.class, false);
            if (gt != null) {
                visitChildren(gt);
            }
        }
        visitChildren(ce);
    }

    //--------------------------------------------------------------------------

    /**
     * @param refOwnerComp is a Schema component which contains the reference.
     * It is used to calculate a namespace URI by a prefix.
     * @param ref is the ref object to reslve
     * @param soughtClass the class of the reference. It is determined by the 
     * owner component.
     * @param preCheckName has to be true for Elements and Attributes
     * @return the resolved SchemaComponent or null. 
     */
    private <RefCls extends NamedReferenceable<SchemaComponent> & SchemaComponent> 
            RefCls resolve(
            SchemaComponent refOwnerComp,
            NamedComponentReference<? extends RefCls> ref,
            Class<RefCls> soughtClass, boolean preCheckName) {
        //
        // Take referent name and prefix from the refString.
        String refString = ref.getRefString();
        String splitRefString[] = refString.split(":", 2);
        String nsPrefix = null;
        String soughtName = null;
        //
        if (splitRefString.length == 1) {
            soughtName = splitRefString[0];
            if (preCheckName && !mySoughtName.equals(soughtName)) {
                return null;
            }
            nsPrefix = "";
        } else if (splitRefString.length == 2) {
            soughtName = splitRefString[1];
            if (preCheckName && !mySoughtName.equals(soughtName)) {
                return null;
            }
            nsPrefix = splitRefString[0];
        }
        //
        ArrayList<SchemaModel> schemaModelList = null;
        String nsUri = getNamespaceUri(refOwnerComp, nsPrefix);
        if (nsUri != null) {
            if (preCheckName && mySoughtNamespace != null && 
                    !mySoughtNamespace.equals(nsUri)) {
                // The sought global element(attribute) has the same name 
                // But it is located at another namespace.
                return null;
            }
            //
            // Try looking the referent in the cache first
            schemaModelList = mModelsCache.get(nsUri);
            if (schemaModelList != null) {
                for (int index = 0; index < schemaModelList.size(); index++) {
                    SchemaModel schemaModel = schemaModelList.get(index);
                    Schema schema = schemaModel.getSchema();
                    if (schema == null) {
                        continue;
                    }
                    List<SchemaComponent> sCompList = schema.getChildren();
                    for (SchemaComponent sComp : sCompList) {
                        if (soughtClass.isInstance(sComp) && sComp instanceof Named) {
                            String compName = ((Named)sComp).getName();
                            if (soughtName.equals(compName)) {
                                return soughtClass.cast(sComp);
                            }
                        }
                    }
                }
            }
        }
        //
        // If it didn't manage to find the referent in the cache
        // then try finding it with the standard way.
        SchemaComponent referent = ref.get();
        if (referent == null) {
            return null;
        }
        //
        // Put the model to the cache
        SchemaModel model = (SchemaModel) referent.getModel();
        if (model != null) {
            if (schemaModelList == null) {
                schemaModelList = new ArrayList();
                schemaModelList.add(model);
                mModelsCache.put(nsUri, schemaModelList);
            } else {
                boolean alreadyCached = false;
                //
                for (SchemaModel schemaModel : schemaModelList) {
                    if (!schemaModel.equals(model)) {
                        continue;
                    }
                    alreadyCached = true;
                    break;
                }
                //
                if (!alreadyCached) {
                    schemaModelList.add(model);
                }
            }
        }
        return soughtClass.cast(referent);
    }

    /**
     * Recursively resolving the namespace prefix starting from the specified
     * schema component (from child to parent).
     * 
     * @param sComp
     * @param nsPrefix
     * @return return the namespace URI or null
     */
    private String getNamespaceUri(SchemaComponent sComp, String nsPrefix) {
        Map prefixes = ((AbstractDocumentComponent) sComp).getPrefixes();
        String result = (String) prefixes.get(nsPrefix);
        if (result == null) {
            SchemaComponent parentComp = (SchemaComponent) sComp.getParent();
            if (parentComp != null) {
                result = getNamespaceUri(parentComp, nsPrefix);
            }
        }
        return result;
    }

    //--------------------------------------------------------------------------

    protected void checkComponent(SchemaComponent sc) {
        if (sc instanceof Named) {
            String name = ((Named) sc).getName();
            if (mySoughtName.equals(name)) {
                if (mySoughtNamespace == null || mySoughtNamespace.length() == 0) {
                    addSchemaComponent(sc);
                } else { 
                    Set<String> namespacesSet = XPathSchemaContext.Utilities.
                    getEffectiveNamespaces(sc, mParentContext);
                    if (namespacesSet.contains(mySoughtNamespace)) {
                        addSchemaComponent(sc);
                    }
                }
            }
        }
        if (sc instanceof AnyElement) {
            mHasAny = true;
        }
        if (sc instanceof AnyAttribute) {
            mHasAnyAttribute = true;
        }
    }

    private void addSchemaComponent(SchemaComponent element) {
        if (!(element instanceof Named)) {
            myFound.add(element);
            return;
        }

        boolean flag = true;

        for (SchemaComponent el : myFound) {
            if (el instanceof Named) {
                String name1 = ((Named) el).getName();
                String name2 = ((Named) element).getName();
                String nameSp1 = null;
                String nameSp2 = null;
                Set<String> set =XPathSchemaContext.Utilities.
                        getEffectiveNamespaces(el, mParentContext);
                if (set != null && set.size() > 0) {
                    nameSp1 = set.iterator().next();
                }
                set =XPathSchemaContext.Utilities.
                        getEffectiveNamespaces(element, mParentContext);
                if (set != null && set.size() > 0) {
                    nameSp2 = set.iterator().next();
                }


                if (name1 != null && name1.equals(name2) && equals(nameSp1, nameSp2)) {
     //               myFound.remove(el);
                    flag = false;
                    break;
                }
            }

        }

        if (flag) {
            myFound.add(element);
        }
    }

    private boolean equals(Object object1, Object object2) {
        if (object1 == object2) {return  true; }
        return (object1 == null || object2 == null) ? false : object1.equals(object2);
    }

}
