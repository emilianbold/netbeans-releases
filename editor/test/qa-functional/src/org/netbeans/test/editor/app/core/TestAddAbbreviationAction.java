/*
 * TestAddAbbreviationAction.java
 *
 * Created on December 10, 2002, 3:17 PM
 */

package org.netbeans.test.editor.app.core;

import java.util.Map;
import org.netbeans.modules.java.editor.options.JavaOptions;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.MultiLineStringProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.core.properties.Property;
import org.netbeans.test.editor.app.core.properties.StringProperty;
import org.netbeans.test.editor.app.util.ParsingUtils;
import org.openide.options.SystemOption;
import org.w3c.dom.Element;

/**
 *
 * @author  eh103527
 */
public class TestAddAbbreviationAction extends TestAddAction {
    
    private String abbrevName = "";
    
    private String abbrevContent = "";
    
    public static String ABBREV_NAME = "AbbreviationName";
    
    public static String ABBREV_CONTENT = "AbbreviationContent";
    
    /** Creates a new instance of TestAddAbbreviationAction */
    public TestAddAbbreviationAction(int num) {
        this("addAbbreviation"+Integer.toString(num));
    }
    
    public TestAddAbbreviationAction(String name) {
        super(name);
    }
    
    public TestAddAbbreviationAction(Element node) {
        super(node);
        setAbbrevName(ParsingUtils.fromSafeString(node.getAttribute(ABBREV_NAME)));
        if ((abbrevContent = ParsingUtils.loadString(node, ABBREV_CONTENT)) == null) {
            abbrevContent="";
        }
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        setAbbrevName(ParsingUtils.fromSafeString(node.getAttribute(ABBREV_NAME)));
        if ((abbrevContent = ParsingUtils.loadString(node, ABBREV_CONTENT)) == null) {
            abbrevContent="";
        }
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(ABBREV_NAME, new StringProperty(abbrevName));
        ret.put(ABBREV_CONTENT, new MultiLineStringProperty(abbrevContent));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(ABBREV_NAME) == 0) {
            return new StringProperty(abbrevName);
        } else if (name.compareTo(ABBREV_CONTENT) == 0) {
            return new MultiLineStringProperty(abbrevContent);
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value) throws BadPropertyNameException {
        if (value == null) {
            throw new NullPointerException();
        } else if (name.compareTo(ABBREV_NAME) == 0) {
            setAbbrevName(((Property)(value)).getProperty());
        } else if (name.compareTo(ABBREV_CONTENT) == 0) {
            setAbbrevContent(((Property)(value)).getProperty());
        } else {
            super.setProperty(name, value);
        }
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        node.setAttribute(ABBREV_NAME, ParsingUtils.toSafeString(getAbbrevName()));
        node = ParsingUtils.saveString(node, ABBREV_CONTENT, abbrevContent);
        return node;
    }
    
    public void perform() {
        super.perform();
        if (abbrevName == null || abbrevName.length() == 0) {
            System.err.println("Error performing Add Abbreviation Action: abbreviation name is empty.");
            return;
        }
        JavaOptions opts = (JavaOptions)(SystemOption.findObject(JavaOptions.class));
        Map map=opts.getAbbrevMap();
        map.put(abbrevName, abbrevContent);
        opts.setAbbrevMap(map);
    }
    
    /** Getter for property abbrevName.
     * @return Value of property abbrevName.
     *
     */
    public java.lang.String getAbbrevName() {
        return abbrevName;
    }
    
    /** Setter for property abbrevName.
     * @param abbrevName New value of property abbrevName.
     *
     */
    public void setAbbrevName(java.lang.String abbrevName) {
        String old = getAbbrevName();
        
        this.abbrevName = abbrevName;
        firePropertyChange(ABBREV_NAME, old, abbrevName);
    }
    
    /** Getter for property abbrevContent.
     * @return Value of property abbrevContent.
     *
     */
    public java.lang.String getAbbrevContent() {
        return abbrevContent;
    }
    
    /** Setter for property abbrevContent.
     * @param abbrevContent New value of property abbrevContent.
     *
     */
    public void setAbbrevContent(java.lang.String abbrevContent) {
        String old = getAbbrevContent();
        
        this.abbrevContent = abbrevContent;
        firePropertyChange(ABBREV_CONTENT, old, abbrevContent);
    }
    
}
