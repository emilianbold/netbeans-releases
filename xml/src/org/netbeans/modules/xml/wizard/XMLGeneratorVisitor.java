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
package org.netbeans.modules.xml.wizard;

import org.netbeans.modules.xml.wizard.XMLContentAttributes;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLGeneratorVisitor extends DeepAXITreeVisitor {
            
    /**
     * Creates a new instance of PrintAXITreeVisitor
     */
    
    private XMLContentAttributes contentAttr;
    private String elemPrefix ="", attrPrefix ="", defaultPrefix;
    private AXIModel axiModel;
    private int depth = 0;
    private String schemaFileName;
    private Element rElement;
    private StringBuffer writer;
    private String primaryTNS;
    Map<String, String> namespaceToPrefix;
    private int counter = 1;
    private static final String PREFIX = "ns"; // NOI18N
    private boolean qualifiedElem;
    
    public XMLGeneratorVisitor(String schemaFileName, XMLContentAttributes attr, StringBuffer writer) {
        super();
        this.contentAttr=attr;
        this.defaultPrefix = contentAttr.getPrefix() + ":";
        this.schemaFileName = schemaFileName;
        this.writer = writer;
        this.namespaceToPrefix = contentAttr.getNamespaceToPrefixMap();
       
    }
    
    //method added for Junit testing
    
   public void generateXML(String rootElement, SchemaModel model){
        if(model.getSchema().getAttributeFormDefaultEffective().equals(Form.QUALIFIED))
            attrPrefix = defaultPrefix;
        if(model.getSchema().getElementFormDefaultEffective().equals(Form.QUALIFIED))
            elemPrefix =defaultPrefix;
        this.axiModel = AXIModelFactory.getDefault().getModel(model);
        rElement = findAXIGlobalElement(rootElement);
        primaryTNS = rElement.getTargetNamespace();
        if(rElement != null) {
            this.visit(rElement);
        }
        contentAttr.setNamespaceToPrefixMap(namespaceToPrefix);
   }
    
    public void generateXML(String rootElement) {        
        //TO DO better exception handling
        if(rootElement == null || schemaFileName == null || schemaFileName.equals("") || rootElement.equals(""))
                return;
        File f = new File(schemaFileName);
        f = FileUtil.normalizeFile(f);
        FileObject fObj =FileUtil.toFileObject(f);
        //temp fix to handle http based xsd files
        if(fObj == null)
            return;
        ModelSource ms = null;
        try {
            ms = Utilities.createModelSource(fObj, true);
        } catch (Exception e){
            //dont do anything
            return;
        }
        if(ms == null)
            return;
        SchemaModel model = SchemaModelFactory.getDefault().getModel(ms);
        generateXML(rootElement, model);
        
    }
    
    public void generateXML(Element element) {
         if(element != null) {
            this.visit(element);
        }
    }
    
    public void visit(Element element) { 
       int occurs = getOccurence(element.getMinOccurs(), element.getMaxOccurs());
       
        //do we need to generate optional elements
       if( !contentAttr.generateOptionalElements() ) {
           if(isElementOptional(element))
               return;
       }
        for(int i=0; i < occurs ; i++) {
            visitChildren(element);
        }
    }
    
    protected void visitChildren(AXIComponent component) {
        try {            
           printModel(component);        
           depth++;
           this.visitChildrenForXML(component);
           this.postVisitChildren(component);
           depth--;
        } catch (Exception e){
            //need to figure out how to handle this exception
        }
    }

    private boolean isElementOptional(Element element) {
        int i = Integer.parseInt(element.getMinOccurs());
        if(i ==0)
            return true;
        else
            return false;
    }

            
    private void printModel(AXIComponent component) throws IOException {
        StringBuffer buffer = new StringBuffer();
        boolean newLine = true;
        if(component.getChildElements().isEmpty())
            newLine = false;
            
        if(component instanceof Compositor) {
            Compositor compositor = (Compositor)component;
            buffer.append((getTab() == null) ? compositor : getTab() + compositor);
            buffer.append("<min=" + compositor.getMinOccurs() + ":max=" + compositor.getMaxOccurs() + ">");
            return;
        }
        if(component instanceof Element) {
            Element element = (Element)component;
            
            //set prefix
            String prefix = setPrefixForElement(element);
            
            //dont print the root element 
            if (element.equals(rElement)) {
                //check if root element has attributes;
                if (element.getAttributes().size() != 0) {
                    int i = writer.lastIndexOf("\n");
                    if (i != -1) {
                        writer = writer.insert(i - 1, " " + getAttributes(element));
                    }
                }
                return;
            }
            
            buffer.append((getTab() == null) ? element.getName() : getTab() + "<" + prefix  +element.getName() );
            if(element.getAttributes().size() != 0) {
                buffer.append(" " + getAttributes(element) );
            }
           if(newLine)
                writer.append(buffer.toString() +">" +"\n");
            else
                writer.append(buffer.toString() + ">");
            
            //write the default/fixed value of the element, if any
            writer.append(getComponentValue(element));
        }
        
        
    }
        
    
    private String getAttributes(Element element) {
        String lprefix;
        StringBuffer attrs = new StringBuffer();
        for(AbstractAttribute attr : element.getAttributes()) {
            lprefix = attrPrefix;
            if(isGlobal(attr))
               lprefix = contentAttr.getPrefix() + ":";
            if(attr instanceof Attribute) {
                if(!contentAttr.generateOptionalAttributes()){ 
                   if(((Attribute)attr).getUse().equals(Use.REQUIRED)){
                        attrs.append(lprefix + attr+ "=\"" + getComponentValue((Attribute)attr) + "\" ");
                    }
                    continue;
                }
            }
            if(attr instanceof Attribute)
                attrs.append(lprefix + attr+ "=\"" + getComponentValue((Attribute)attr) + "\" ");
            else
                attrs.append(attr+"= \" \" ");            
        }
        if(attrs.length() > 0)
            return attrs.toString().substring(0, attrs.length()-1);
        else
            return attrs.toString();
    }
    
    private String getTab() {
        String tabStr = "    ";
        
        if(depth == 0) {
            return null;
        }
        
        StringBuffer tab = new StringBuffer();
        for(int i=0; i<depth ; i++) {
            tab.append(tabStr);
        }
        return tab.toString();
    }
    
    protected void visitChildrenForXML(AXIComponent component) {
        if( !super.canVisit(component) )
            return;
                
        if(component instanceof Compositor) {
           Compositor.CompositorType type =((Compositor)component).getType();
           if(type.equals(Compositor.CompositorType.CHOICE) ){
               List<AXIComponent> children = component.getChildren();
               if(children != null && children.size() > 0 ) {
                   component.getChildren().get(0).accept(this);
               }
               return;
           }           
        }
                
        for(AXIComponent child: component.getChildren()) {
            child.accept(this);
        }
        
    }
    
    private int getOccurence(String minOccurs, String maxOccurs) {
        if(maxOccurs.equals("unbounded"))
            return contentAttr.getPreferredOccurences();
        
        int min = Integer.parseInt(minOccurs);
        int max = Integer.parseInt(maxOccurs);
        
        if(contentAttr.getPreferredOccurences() > min && contentAttr.getPreferredOccurences() <max)
            return contentAttr.getPreferredOccurences();
        
        if(contentAttr.getPreferredOccurences() > max)
            return max;
        
        if(contentAttr.getPreferredOccurences() < min )
            return min;
        
        return min;
    }
    
    private void postVisitChildren(AXIComponent component) throws IOException {
        if(component instanceof Element) {
            //dont write the closing root element
            if( ((Element)component).equals(rElement))
                return;
            
             //set prefix
            String prefix  = setPrefixForElement((Element)component);
            
            if(component.getChildElements().isEmpty())
                writer.append("</" + prefix +((Element)component).getName() + ">" + "\n");
            else
               writer.append(getTab() + "</" + prefix +((Element)component).getName() + ">" + "\n");
        }
    }
    
     private Element findAXIGlobalElement(String name) {
        if(name == null)
            return null;
        
        for(Element e : axiModel.getRoot().getElements()) {
            if(e.getName().equals(name)) {
                return e;
            }
        }
        
        return null;
    }
     
   private String getComponentValue(AXIComponent component) {
       String value = null;
       if(component instanceof Attribute ) {
           Attribute attribute = (Attribute)component;
           value = attribute.getFixed();
           if(value == null)
               value = attribute.getDefault();
       } else if(component instanceof Element) {
           Element element =(Element)component;
           value = element.getFixed();
           if(value == null)
               value = element.getDefault();    
       }
       
       if(value != null)
           return value;   
       else
           return "";
       
    }

    private String generatePrefix(){
        String generatedName = PREFIX + counter++;
        while(namespaceToPrefix.containsValue(generatedName) )
            generatedName = PREFIX + counter++;
        return generatedName;
    
    }
    
    private String setPrefixForElement(Element element ){
        String prefix = elemPrefix;
        String ns;
        
        if(element.isReference())
            ns = element.getReferent().getTargetNamespace();
        else
            ns = element.getTargetNamespace();
        
        if(ns == null)
            return prefix ;
        
        if(! ns.equals(primaryTNS)) {
               if(namespaceToPrefix == null)
                    namespaceToPrefix = new HashMap<String, String>();
                
                      
                String pre = namespaceToPrefix.get(ns);
                if(pre == null || pre.equals("")) {
                    pre = generatePrefix();
                    namespaceToPrefix.put(ns, pre);
                }
                prefix = pre + ":";
                return prefix;
        } 
        if(isGlobal(element)){
            return defaultPrefix;
        } 
        
        return prefix;  
         
    }
    
    private boolean isGlobal(AXIComponent component) {
      AXIComponent original = component.getOriginal();
      if (original.getComponentType() == ComponentType.REFERENCE) {
          return true;
      }
      return original.isGlobal();
  } 
   
}
