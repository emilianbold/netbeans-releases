package org.netbeans.modules.web.jsfapi.api;

public class Attribute {

    private String name;
    private String description;
    private boolean required;

    public Attribute(String name, String description, boolean required) {
        this.name = name;
        this.description = description;
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        return "Attribute[name=" + getName() + ", required=" + isRequired() + "]"; //NOI18N
    }
}
