/*
 * XMLNode.java
 *
 * Created on November 18, 2002, 10:29 AM
 */

package org.netbeans.test.editor.app.core;

import java.beans.PropertyChangeListener;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.w3c.dom.Element;

/**
 *
 * @author  eh103527
 */
public interface XMLNode {
    
    public Element toXML(Element node);
    public void fromXML(Element node) throws BadPropertyNameException;
    public Properties getProperties();
    public Object getProperty(String name) throws BadPropertyNameException;
    public void setProperty(String name,Object value) throws BadPropertyNameException;
    
    public void addPropertyChangeListener(PropertyChangeListener list);
    public void removePropertyChangeListener(PropertyChangeListener list);
}
