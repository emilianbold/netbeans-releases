/*
 * XMLDocument.java
 *
 * Created on April 25, 2002, 1:55 PM
 */

package org.netbeans.modules.testtools.wizards;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.openide.loaders.DataObject;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 *
 * @author  as103278
 */
public class XMLDocument {
    
    private Document doc;
    private DataObject dob;
    
    /** Creates a new instance of XMLDocument */
    public XMLDocument(DataObject o) {
        dob=o;
        AntProjectCookie cookie=(AntProjectCookie)o.getCookie(AntProjectCookie.class);
        if (cookie!=null)
            doc=cookie.getDocument();
        doc=new AntProjectSupport(o.getPrimaryFile()).getDocument();
    }
    
    public void setProperty(String name, String valueName, Object valueValue) {
        setElement("property", "name", name, valueName, valueValue);
    }
    
    public void setElement(String element, String valueName, Object valueValue) {
        setElement(element, null, null, valueName, valueValue);
    }
    
    public void setElement(String element, String nameName, String nameValue, String valueName, Object valueValue) {
        if (valueValue==null) return;
        NodeList list = doc.getElementsByTagName(element);
        for (int i=0; i<list.getLength(); i++) try {
            if ((nameName==null)||(nameValue.equals(list.item(i).getAttributes().getNamedItem(nameName).getNodeValue()))) {
                Node n=list.item(i).getAttributes().getNamedItem(valueName);
                if (n!=null)
                    n.setNodeValue(valueValue.toString());
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public String getProperty(String name, String valueName) {
        return getElement("property", "name", name, valueName);
    }
    
    public String getElement(String element, String valueName) {
        return getElement(element, null, null, valueName);
    }
    public String getElement(String element, String nameName, String nameValue, String valueName) {
        NodeList list = doc.getElementsByTagName(element);
        for (int i=0; i<list.getLength(); i++) try {
            if ((nameName==null)||(nameValue.equals(list.item(i).getAttributes().getNamedItem(nameName).getNodeValue()))) {
                Node n=list.item(i).getAttributes().getNamedItem(valueName);
                if (n!=null)
                    return n.getNodeValue();
                else 
                    return null;
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
}
