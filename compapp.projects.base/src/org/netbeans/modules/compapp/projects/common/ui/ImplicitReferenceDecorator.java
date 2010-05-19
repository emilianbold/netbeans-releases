/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.projects.common.ui;

import java.util.Map;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.openide.nodes.Node;

/**
 * Node decorator for the treeview that shows the wsdl and xsd files together.
 * 
 * @author chikkala
 */
public class ImplicitReferenceDecorator  extends AbstractReferenceDecorator {

    /** The customizer that created this decorator. */
    private AbstractReferenceCustomizer customizer;
    
    private int prefixCounter = 0;
    
    public ImplicitReferenceDecorator(AbstractReferenceCustomizer customizer) {
        this.customizer = customizer;
    }
    
    @Override
    protected String generatePrefix(Model model) {
        String prefix = "";
        Model ourModel = customizer.getComponentModel();
        if (ourModel instanceof WSDLModel) {
            
            WSDLModel wm = (WSDLModel) ourModel;
            AbstractDocumentComponent def =
                   (AbstractDocumentComponent) wm.getDefinitions();
            // Definitions def = wm.getDefinitions();        
            Map map = def.getPrefixes();        
            for ( int i=0; i < Integer.MAX_VALUE; ++i) {
                prefix = "ns" + prefixCounter++;
                if (!map.containsKey(prefix)) {
                    break;
                }
            }
        }
        return prefix;
    }

    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        return customizer.createExternalReferenceNode(original);
    }

    public DocumentTypesEnum getDocumentType() {
        // return DocumentTypesEnum.schema;
        return null;
    }

    public String getHtmlDisplayName(String name, ExternalReferenceNode node) {
        if (validate(node) != null) {
            return "<s>" + name + "</s>";
        }
        return name;
    }

    public String getNamespace(Model model) {
        
        if (model instanceof WSDLModel) {
            WSDLModel wm = (WSDLModel) model;
            return wm.getDefinitions().getTargetNamespace();
        } else if (model instanceof SchemaModel) {
            SchemaModel sm = (SchemaModel) model;
            return sm.getSchema().getTargetNamespace();
        } else {
            return null;
        }

    }
    /**
     * return null if it is valid!!
     * @param node
     * @return
     */
    public String validate(ExternalReferenceNode node) {
        return null;
    }

}
