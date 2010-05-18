/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.design.model.patterns;

import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

/**
 *
 * @author Alexey
 */
public class ReThrowPattern extends BasicActivityPattern{
    /** Creates a new instance of BasicActivityPattern */
    public ReThrowPattern(DiagramModel model) {
        super(model);
    }
    

    protected void createElementsImpl() {
        VisualElement element = ContentElement.createReThrow();
        appendElement(element);
        registerTextElement(element);
    }
    
    
    public String getDefaultName() {
        return "Rethrow"; // NOI18N
    }  
    
    public NodeType getNodeType() {
        return NodeType.RETHROW;
    }
}
