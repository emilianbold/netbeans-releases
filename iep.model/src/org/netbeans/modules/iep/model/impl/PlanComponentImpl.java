package org.netbeans.modules.iep.model.impl;

import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Import;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.ModelConstants;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.w3c.dom.Element;

public class PlanComponentImpl extends ComponentImpl implements PlanComponent {

    public PlanComponentImpl(IEPModel model) {
        super(model);
    }

    public PlanComponentImpl(IEPModel model, Element element) {
        super(model, element);
    }
    
    @Override
    public IEPComponent createChild (Element childEl) {
        IEPComponent child = null;
        
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                    localName = childEl.getTagName();
            }
            if (localName.equals(COMPONENT_CHILD)) {
                    String name = childEl.getAttribute("name");
                    if(name.length() != 0) {
                        if(name.equals(ModelConstants.COMPONENT_OPERATORS)) {
                            child = new OperatorComponentContainerImpl(getModel(), childEl);
                        } else if(name.equals(ModelConstants.COMPONENT_LINKS)) {
                            child = new LinkComponentContainerImpl(getModel(), childEl);
                        } else if(name.equals(ModelConstants.COMPONENT_SCHEMAS)) {
                            child = new SchemaComponentContainerImpl(getModel(), childEl);
                        } else {
                            child = super.createChild(childEl);
                        }
                    }
            } else {
                child = super.createChild(childEl);
            }
        }
        
        return child;
    }
    
    @Override
    public void accept(IEPVisitor visitor) {
        visitor.visitPlanComponent(this);
    }
     
    
    public LinkComponentContainer getLinkComponentContainer() {
        List<LinkComponentContainer> children = getChildren(LinkComponentContainer.class);
        if(children.size() != 0) {
            return children.get(0);
        }
        return null;
    }

    public OperatorComponentContainer getOperatorComponentContainer() {
        List<OperatorComponentContainer> children = getChildren(OperatorComponentContainer.class);
        if(children.size() != 0) {
            return children.get(0);
        }
        return null;
        
    }

    public SchemaComponentContainer getSchemaComponentContainer() {
        List<SchemaComponentContainer> children = getChildren(SchemaComponentContainer.class);
        if(children.size() != 0) {
            return children.get(0);
        }
        return null;
    }

    public List<Import> getImports() {
        List<Import> children = getChildren(Import.class);
        return children;
    }

    public String getTargetNamespace() {
        return getAttribute(ATTR_TARGETNAMESPACE);
    }
    
    public void setTargetNamespace(String targetNamespace) {
        setAttribute(TARGETNAMESPACE_PROPERTY, ATTR_TARGETNAMESPACE, targetNamespace);
    }
    
    public void setPackageName(String packageName) {
        setAttribute(PACKAGENAME_PROPERTY, ATTR_PACKAGENAME, packageName);
    }
    
    public String getPackageName() {
        return getAttribute(ATTR_PACKAGENAME);
    }
}
