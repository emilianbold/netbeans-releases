package org.netbeans.modules.xml.xam;

/**
 *
 * @author Nam Nguyen
 */
public enum TestAttribute implements Attribute {
    INDEX("index", Integer.class), 
    VALUE("value", String.class);
    
    private String name;
    private Class type;
    private Class subtype;
    
    TestAttribute(String name, Class type) {
        this.name = name;
        this.type = type;
    }
    TestAttribute(String name, Class type, Class subtype) {
        this(name, type);
        this.subtype = subtype;
    }
    
    public String getName() { return name; }
    public Class getType() { return type; }
    public Class getMemberType() { return subtype; }
    public String toString() { return name; }
}
