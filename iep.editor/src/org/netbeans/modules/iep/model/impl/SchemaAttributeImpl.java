package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.w3c.dom.Element;

public class SchemaAttributeImpl extends ComponentImpl implements SchemaAttribute {

    public SchemaAttributeImpl(IEPModel model) {
        super(model);
        setType("/IEP/Metadata/ColumnMetadata"); //NOI18N
    }
    
    public SchemaAttributeImpl(IEPModel model, Element element) {
        super(model, element);
//        setType("/IEP/Metadata/ColumnMetadata"); //NOI18N
    }

     public void accept(IEPVisitor visitor) {
        visitor.visitSchemaAttribute(this);
     }
         

    public String getAttributeName() {
        return getString(PROP_NAME);
    }

    public void setAttributeName(String attributeName) {
        setString(PROP_NAME, attributeName);
    }
    
    public String getAttributeScale() {
        return getString(PROP_SCALE);
    }

    public void setAttributeScale(String attributeScale) {
        setString(PROP_SCALE, attributeScale);
    }
    
    public String getAttributeSize() {
        return getString(PROP_SIZE);
    }
    
    public void setAttributeSize(String attributeSize) {
        setString(PROP_SIZE, attributeSize);
    }

    public String getAttributeType()  {
        return getString(PROP_TYPE);
    }

    public void setAttributeType(String attributeType) {
        setString(PROP_TYPE, attributeType);
    }
    
    public String getAttributeComment() {
        return getString(PROP_COMMENT);
    }

    public void setAttributeComment(String attributeComment) {
        setString(PROP_COMMENT, attributeComment);
    }

    @Override
    public String toString() {
        StringBuffer resultStrBuffer = new StringBuffer();
        
        resultStrBuffer.append("name: ");
        resultStrBuffer.append(getAttributeName());
        resultStrBuffer.append("type: ");
        resultStrBuffer.append(getAttributeType());
        resultStrBuffer.append("scale: ");
        resultStrBuffer.append(getAttributeScale());
        resultStrBuffer.append("size: ");
        resultStrBuffer.append(getAttributeSize());
        resultStrBuffer.append("comment: ");
        resultStrBuffer.append(getAttributeComment());
        
        return resultStrBuffer.toString();
    }
}
