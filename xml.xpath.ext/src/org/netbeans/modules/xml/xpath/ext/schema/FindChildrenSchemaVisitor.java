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
import java.util.List;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * This schema visitor is inteneded to look for a children elements or attributes 
 * It looks only children at the next lavel. 
 * The name and namespace of the sought children is specified in the constructor.
 * If the namespace isn't specified, then it looks for a children only by name. 
 * 
 * @author nk160297
 */
public class FindChildrenSchemaVisitor extends AbstractSchemaSearchVisitor {
    
    private XPathSchemaContext mParentContext;
    private String mySoughtName;
    private String mySoughtNamespace;
    private boolean isAttribute; // hints that the sought object is an attribute
    
    private List<SchemaComponent> myFound = new ArrayList<SchemaComponent>();
    
    private boolean mHasAny = false;
    private boolean mHasAnyAttribute = false;
    
//    public FindChildrenSchemaVisitor(
//            String soughtName, String soughtNamespace, boolean isAttribute) {
//        this(null, soughtName,  soughtNamespace, isAttribute);
//    }
    
    public FindChildrenSchemaVisitor(XPathSchemaContext parentContext, 
            String soughtName, String soughtNamespace, boolean isAttribute) {
        //
        super();
        assert soughtName != null : "At least sought name has to be specified!"; // NOI18N
        //
        mParentContext = parentContext;
        mySoughtName = soughtName;
        mySoughtNamespace = soughtNamespace;
        this.isAttribute = isAttribute;

//ENABLE = (soughtName + "").equals("street");
//out();
//out("=== FindChildrenSchemaVisitor: " + soughtName);
    }
    
    @Override
    public void visit(ElementReference er) {
        if (!isAttribute) {
            //
            // # 105159, #130053
            if (!isXdmDomUsed(er)) {
                checkComponent(er);
            }
            //
            String name = fastGetRefName(er.getRef());
            if (!mySoughtName.equals(name)) {
                return;
            }
            super.visit(er);
        }
    }
    
    @Override
    public void visit(AttributeReference ar) {
        if (isAttribute) {
            //
            // # 105159, #130053
            if (!isXdmDomUsed(ar)) {
                checkComponent(ar);
            }
            //
            String name = fastGetRefName(ar.getRef());
            if (!mySoughtName.equals(name)) {
                return;
            }
            super.visit(ar);
        }
    }
    
    //-----------------------------------------------------------------
    
    public List<SchemaComponent> getFound() {
        return myFound;
    }
    
    public boolean hasAny() {
        return mHasAny;
    }
    
    public boolean hasAnyAttribute() {
        return mHasAnyAttribute;
    }
    
    private boolean isChildFound() {
        return myFound.size() > 0;
    }

    public void lookForSubcomponent(SchemaComponent sc) {
//out("S E E : " + sc);

        if (sc instanceof Element) {
//out("1");
            if (sc instanceof TypeContainer) {
//out("1.1");
                NamedComponentReference<? extends GlobalType> typeRef = ((TypeContainer)sc).getType();

                if (typeRef != null) {
//out("1.1.1");
                    GlobalType globalType = typeRef.get();
                
                    if (globalType != null) {
                        globalType.accept(this);
                    }
                }
                LocalType localType = ((TypeContainer)sc).getInlineType();

                if (localType != null) {
//out("1.1.2");
                    localType.accept(this);
                }
            }
            else if (sc instanceof ElementReference) {
//out("1.2");
                NamedComponentReference<GlobalElement> gElemRef = ((ElementReference)sc).getRef();

                if (gElemRef != null) {
//out("1.2.1");
                    GlobalElement gElement = gElemRef.get();
                    lookForSubcomponent(gElement);
                }
                // vlv # 105159
                else if (sc instanceof DocumentComponent) {
//out("1.3");
                  DocumentComponent document = (DocumentComponent) sc;
                  String typeName = document.getPeer().getAttribute("type");
                  typeName = removePrefix(typeName);

                  if (typeName == null || typeName.equals("")) {
                    NodeList list = document.getPeer().getElementsByTagName("xs:extension");

                    for (int i=0; i < list.getLength(); i++) {
                      Node node = list.item(i);

                      if ( !(node instanceof org.w3c.dom.Element)) {
                        continue;
                      }
                      org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                      findInType(element.getAttribute("base"), sc);

                      if (isChildFound()) {
                        break;
                      } 
                    }
                    // find element
                    findElement(mySoughtName, document, sc);
                  }
                  else {
                    findInType(typeName, sc);
                  }
                }
            }
        }
        else if (sc instanceof ComplexType) {
//out("2");
            visitChildren(sc);
        }
        else if (sc instanceof Schema) {
//out("3");
            visitChildren(sc);
        }
        else {
//out("4");
            // Other elements can't containg nested elements or attributes
        }
//out();
    }

    // vlv # 105159
    private void findElement(String name, DocumentComponent document, SchemaComponent sc) {
      findElement(name, document, sc, "xs:element");
      findElement(name, document, sc, "xsd:element");
    }

    // vlv # 105159
    private void findElement(String name, DocumentComponent document, SchemaComponent sc, String tag) {
//out();
//out("--- : tag : " + tag);
      if (name == null || name.equals("")) {
        return;
      }
      NodeList list = document.getPeer().getElementsByTagName(tag);
      
      for (int i=0; i < list.getLength(); i++) {
        Node node = list.item(i);

        if ( !(node instanceof org.w3c.dom.Element)) {
          continue;
        }
        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
//out("--- : node : " + element.getAttribute("name"));
        if (name.equals(element.getAttribute("name"))) {
//out("--- : ---- : Y E S !!!! " );
          myFound.add(sc); //!
          return;
        }
      }
    }

    // vlv # 105159
    private String removePrefix(String value) {
      if (value == null) {
        return null;
      }
      int k = value.indexOf(":");

      if (k == -1) {
        return value;
      }
      return value.substring(k + 1);
    }

    // vlv # 105159
    private void findInType(String typeName, SchemaComponent sc) {
      Schema schema = sc.getModel().getSchema();
      boolean found = findInType(typeName, sc, schema);

      if (found) {
        return;
      }
      Collection<Import> imports = schema.getImports();

      for (Import imp : imports) {
        try { 
          SchemaModel model = imp.resolveReferencedModel();
          found = findInType(typeName, sc, model.getSchema());

          if (found) {
            return;
          }
        }
        catch (CatalogModelException e) {
          continue;
        }
      }
    }

    private boolean findInType(final String typeName, SchemaComponent sc, Schema schema) {
//out("* findInType: " + typeName);
//out("*      schema: " + schema);
      if (typeName == null || typeName.equals("")) {
        return false;
      }
      myGlobalComplexType = null;

      schema.accept(new DeepSchemaVisitor() {
        @Override
        public void visit(GlobalComplexType type) {
//out("SEE TYPE : " + type.getName());
          if (typeName.equals(type.getName())) {
//out("!!!=== FOUND GLOBAL Complex TYPE ==== : " + type.getName());
            myGlobalComplexType = type;
          }
        }
      });

      if (myGlobalComplexType != null) {
        myGlobalComplexType.accept(this);
        return true;
      }
      return false;
    }

    private GlobalComplexType myGlobalComplexType;

    private String getName(SchemaComponent component) {
      if (component instanceof Named) {
        return ((Named) component).getName();
      }
      return "";
    }

    protected void checkComponent(SchemaComponent sc) {
// # 105159
//out("check: " + sc);
        if (sc instanceof ElementReference && sc instanceof DocumentComponent) {
          DocumentComponent document = (DocumentComponent) sc;
          String ref = document.getPeer().getAttribute("ref");
          ref = removePrefix(ref);
//out("           ref: " + ref);
//out("  mySoughtName: " + mySoughtName);

          if (mySoughtName.equals(ref)) {
            addElement(sc);
            return;
          }
        }
        if (sc instanceof Named) {
            String name = ((Named)sc).getName();
            if (mySoughtName.equals(name)) {
                //
                // Compare namespace as well if it is specified
                if (mySoughtNamespace == null || mySoughtNamespace.length() == 0) {
                    addElement(sc);
                } else {
                    Set<String> namespacesSet = XPathSchemaContext.Utilities.
                            getEffectiveNamespaces(sc, mParentContext);
                    //
                    if (namespacesSet.contains(mySoughtNamespace)) {
                        addElement(sc);
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
    
    private void addElement(SchemaComponent element) {
        if (!(element instanceof Named)) {
            myFound.add(element);
            return;
        }

        boolean flag = true;

        for (SchemaComponent el : myFound) {
            if (el instanceof Named) {
                String name1 = ((Named) el).getName();
                String name2 = ((Named) element).getName();
//                String nameSp1 = SchemaModelsStack.getEffectiveNamespace(el,
//                        new SchemaModelsStack());
//                String nameSp2 = SchemaModelsStack.getEffectiveNamespace(element,
//                        new SchemaModelsStack());
//                Set<String> set = XPathSchemaContext.Utilities.getEffectiveNamespaces(el, mParentContext);
//                nameSp1 = set.iterator().next();
//                set = XPathSchemaContext.Utilities.getEffectiveNamespaces(element, mParentContext);
//                nameSp2 = set.iterator().next();
                if (name1 != null && name1.equals(name2)) {
      //              myFound.remove(el);
                    flag = false;
                    break;
                }
            }
        }

        if (flag) {
            myFound.add(element);
        }
    }
    
    private boolean ENABLE;

    private void out() {
      if (ENABLE) {
        System.out.println();
      }
    }

    private void out(Object object) {
      if (ENABLE) {
        System.out.println("*** " + object);
      }
    }
}
