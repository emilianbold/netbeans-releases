package org.netbeans.modules.iep.model;




/**
 *
 * 
 */
public interface IEPVisitor {
        
        
        void visitComponent(Component component);
        
        void visitProperty(Property property);
        
        void visitImport(Import imp);
        
        void visitDocumentation(Documentation doc);
        
        void visitPlanComponent(PlanComponent component);
        
        void visitOperatorComponentContainer(OperatorComponentContainer component);
        
        void visitSchemaComponentContainer(SchemaComponentContainer component);
        
        void visitLinkComponentContainer(LinkComponentContainer component);
        
        void visitOperatorComponent(OperatorComponent component);
        
        void visitSchemaComponent(SchemaComponent component);
        
        void visitLinkComponent(LinkComponent component);
        
        void visitSchemaAttribute(SchemaAttribute component);
        
        
}
