package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;

public final class TagImpl implements Tag {

    private static final String ID_ATTR_NAME = "id"; //NOI18N
    private String name;
    private String description;
    private Map<String, Attribute> attrs;

    public TagImpl(String name, String description, Map<String, Attribute> attrs) {
        this.name = name;
        this.description = description;
        this.attrs = attrs;
        //add the default ID attribute
        if (getAttribute(ID_ATTR_NAME) == null) {
            attrs.put(ID_ATTR_NAME, new Attribute.DefaultAttribute(ID_ATTR_NAME, "", false));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasNonGenenericAttributes() {
        return getAttributes().size() > 1; //the ID attribute is the default generic one
    }

    @Override
    public Collection<Attribute> getAttributes() {
        return attrs.values();
    }

    @Override
    public Attribute getAttribute(String name) {
        return attrs.get(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tag[name=").append(getName()).append(", attributes={"); //NOI18N
        for (Attribute attr : getAttributes()) {
            sb.append(attr.toString()).append(",");
        }
        sb.append("}]");
        return sb.toString();
    }
}
