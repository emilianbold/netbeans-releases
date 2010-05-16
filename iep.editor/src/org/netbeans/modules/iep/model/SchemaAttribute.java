package org.netbeans.modules.iep.model;

public interface SchemaAttribute extends Component {

    String getAttributeName();
    
    void setAttributeName(String attributeName);
    
    String getAttributeType();
    
    void setAttributeType(String attributeType);
    
    String getAttributeSize();
    
    void setAttributeSize(String attributeSize);
    
    String getAttributeScale();
    
    void setAttributeScale(String attributeScale);
    
    String getAttributeComment();
    
    void setAttributeComment(String attributeComment);
}
