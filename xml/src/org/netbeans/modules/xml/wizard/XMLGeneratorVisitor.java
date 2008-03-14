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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
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
    private String prefix;
    private AXIModel axiModel;
    private int depth = 0;
    private String schemaFileName;
    private String rootElement;
    private Writer writer;
    
    public XMLGeneratorVisitor(String schemaFileName, XMLContentAttributes attr, Writer writer) {
        super();
        this.contentAttr=attr;
        this.prefix = contentAttr.getPrefix();
        this.schemaFileName = schemaFileName;
        this.writer = writer;
       
    }
    
    public void generateXML(String rootElement) {        
        //TO DO better exception handling
        if(rootElement == null || schemaFileName == null || schemaFileName.equals("") || rootElement.equals(""))
                return;
        this.rootElement = rootElement;
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
        this.axiModel = AXIModelFactory.getDefault().getModel(model);
        Element element = findAXIGlobalElement(rootElement);
        if(element != null) {
            this.visit(element);
        }
        
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
            
            //dont print the root element 
            if(element.getName().equals(rootElement))
                return;
            
            buffer.append((getTab() == null) ? element.getName() : getTab() + "<" + contentAttr.getPrefix() + ":" +element.getName() );
            if(element.getAttributes().size() != 0) {
                buffer.append(" " + getAttributes(element) );
            }
           if(newLine)
                writer.write(buffer.toString() +">" +"\n");
            else
                writer.write(buffer.toString() + ">");
            
            //write the default/fixed value of the element, if any
            writer.write(getComponentValue(element));
        }
        if(component instanceof AnyElement) {
            AnyElement element = (AnyElement)component;
            buffer.append((getTab() == null) ? element : getTab() + element);
            writer.write(buffer.toString() + "\n");
        }
        
        
    }
        
    
    private String getAttributes(Element element) {
        StringBuffer attrs = new StringBuffer();
        for(AbstractAttribute attr : element.getAttributes()) {
             if(attr instanceof Attribute) {
                if(!contentAttr.generateOptionalAttributes()){ 
                   if(((Attribute)attr).getUse().equals(Use.REQUIRED)){
                        attrs.append(attr+ "=\"" + getComponentValue((Attribute)attr) + "\" ");
                    }
                    continue;
                }
            }
            if(attr instanceof Attribute)
                attrs.append(attr+ "=\"" + getComponentValue((Attribute)attr) + "\" ");
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
            if( ((Element)component).getName().equals(rootElement))
                return;
            
            if(component.getChildElements().isEmpty())
                writer.write("</" + prefix + ":" +((Element)component).getName() + ">" + "\n");
            else
               writer.write(getTab() + "</" + prefix + ":" +((Element)component).getName() + ">" + "\n");
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


   
}
