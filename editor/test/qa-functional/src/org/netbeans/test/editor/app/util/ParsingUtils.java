/*
 * ParsingUtils.java
 *
 * Created on November 18, 2002, 10:57 AM
 */

package org.netbeans.test.editor.app.util;

import org.netbeans.test.editor.app.core.*;

import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  eh103527
 */
public class ParsingUtils {
    
    /** Creates a new instance of ParsingUtils */
    public ParsingUtils() {
    }
    
    private static class LoadedToCreated {
        Class loaded;
        Class created;
        
        public LoadedToCreated(Class loaded, Class created) {
            this.loaded = loaded;
            this.created = created;
        }
    }
    
    private static LoadedToCreated[] classNames = new LoadedToCreated[] {
        new LoadedToCreated(Test.class, Test.class),
        new LoadedToCreated(TestAction.class, TestAction.class),
        new LoadedToCreated(TestCallAction.class, TestCallAction.class),
        new LoadedToCreated(TestGroup.class, TestGroup.class),
        new LoadedToCreated(TestLogAction.class, TestLogAction.class),
        new LoadedToCreated(TestStringAction.class, TestStringAction.class),
        new LoadedToCreated(TestCompletionAction.class, TestCompletionAction.class),
        new LoadedToCreated(TestAddAbbreviationAction.class, TestAddAbbreviationAction.class),
        new LoadedToCreated(TestNode.class, TestNode.class),
        new LoadedToCreated(TestSetAction.class, TestSetKitAction.class),
        new LoadedToCreated(TestSetIEAction.class, TestSetIEAction.class),
        new LoadedToCreated(TestSetJavaIEAction.class, TestSetJavaIEAction.class),
        new LoadedToCreated(TestSetKitAction.class, TestSetKitAction.class),
        new LoadedToCreated(TestSetCompletionAction.class, TestSetCompletionAction.class),
        new LoadedToCreated(TestStep.class, TestStep.class),
        new LoadedToCreated(TestSubTest.class, TestSubTest.class)
    };
    
    public static final Element saveSubNodes(Element node, Vector nodes) {
        Document doc = node.getOwnerDocument();
        
        for (int cntr = 0; cntr < nodes.size(); cntr++) {
            Object item = nodes.elementAt(cntr);
            
            for (int index = 0; index < classNames.length; index++) {
                
                if (item.getClass() == classNames[index].loaded) {
                    //                    Element newNode = doc.createElement(classNames[index].loaded.getName());//packages version
                    String name = classNames[index].loaded.getName();
                    Element newNode = doc.createElement(name.substring(name.lastIndexOf('.')+1)); //no packages names
                    
                    node.appendChild(((TestNode)item).toXML(newNode));
                    break;
                }
            }
        }
        return node;
    }
    
    public static final Object loadClass(Class clazz, Object from) {
        Class[] types = new Class[]  {Element.class };
        Object[] par  = new Object[] {from};
        
        try  {
            return clazz.getConstructor(types).newInstance(par);
        } catch (java.lang.NoSuchMethodException e) {
            System.err.println("Fatal error - attempt to load unloadable class(1).");
        } catch (java.lang.IllegalAccessException e) {
            System.err.println("Fatal error - attempt to load unloadable class(2).");
        } catch (java.lang.reflect.InvocationTargetException e) {
            System.err.println("Fatal error - attempt to load unloadable class(3).");
            System.err.println("Class name: " + clazz);
            System.err.println("Exception: ");
            e.getTargetException().printStackTrace(System.out);
        } catch (java.lang.InstantiationException e) {
            System.err.println("Fatal error - attempt to load unloadable class(4).");
        };
        return null;
    }
    
    public static final Vector loadSubNodes(Element node) {
        NodeList nodes = node.getChildNodes();
        Vector   res   = new Vector(10, 10);
        String nn,cn;
        
        for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
            nn=nodes.item(cntr).getNodeName();
            if (nn.indexOf('#') == 0) {
                continue;
            }
            if (nn.indexOf('.') > -1)
                nn=nn.substring(nn.lastIndexOf('.')+1);
            for (int index = 0; index < classNames.length; index++) {
                cn=classNames[index].loaded.getName();
                if (cn.indexOf(nn) > -1) { //without packages name version
                    //if (nodes.item(cntr).getNodeName().equals(classNames[index].loaded.getName())) { //with packages names version
                    res.add(loadClass(classNames[index].created, nodes.item(cntr)));
                    break;
                }
            }
        }
        return res;
    }
    
    public static final Element saveString(Element node, String name, String what) {
        Element newNode = node.getOwnerDocument().createElement(name);
        
        newNode.appendChild(node.getOwnerDocument().createCDATASection("\"" + what + "\""));
        node.appendChild(newNode);
        return node;
    }
    
    public static final String loadString(Element node, String name) {
        NodeList mainNodes = node.getElementsByTagName(name);
        
        if (mainNodes.getLength() == 0)
            return null;
        Element el = (Element)mainNodes.item(0);
        StringBuffer sb = new StringBuffer();
        Node newEl = el;
        
        sb.append(newEl.getNodeValue());
        NodeList nodes = newEl.getChildNodes();
        for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
            sb.append(nodes.item(cntr).getNodeValue());
        }
        String raw = sb.toString();
        int first  = raw.indexOf('\"');
        int last   = raw.lastIndexOf('\"');
        if (first != (-1) && last != (-1))
            return raw.substring(first + 1, last);
        else
            return raw;
    }
    
    private static class Twin {
        private String source;
        private String image;
        public Twin(String asource, String aimage) {
            source = asource;
            image = aimage;
        }
        
        public String toImage(String what) {
            int index = 0;
            
            while ((index = what.indexOf(source, index)) != (-1)) {
                String prefix = what.substring(0, index);
                String postfix = what.substring(index + source.length());
                
                what = prefix + image + postfix;
                index += image.length();
            };
            return what;
        }
        
        public String fromImage(String what) {
            int index = 0;
            
            while ((index = what.indexOf(image, index)) != (-1)) {
                String prefix = what.substring(0, index);
                String postfix = what.substring(index + image.length());
                
                what = prefix + source + postfix;
                index += source.length();
            };
            return what;
        }
    }
    
    private static Twin[] arr;
    
    static {
        arr = new Twin[] {
            new Twin("\000", "\\00"),
            new Twin("\001", "\\01"),
            new Twin("\002", "\\02"),
            new Twin("\003", "\\03"),
            new Twin("\004", "\\04"),
            new Twin("\005", "\\05"),
            new Twin("\006", "\\06"),
            new Twin("\007", "\\07"),
            new Twin("\008", "\\08"),
            new Twin("\009", "\\09"),
            new Twin("\010", "\\0A"),
            new Twin("\011", "\\0B"),
            new Twin("\012", "\\0C"),
            new Twin("\013", "\\0D"),
            new Twin("\014", "\\0E"),
            new Twin("\015", "\\0F"),
            new Twin("\016", "\\10"),
            new Twin("\017", "\\11"),
            new Twin("\018", "\\12"),
            new Twin("\019", "\\13"),
            new Twin("\020", "\\14"),
            new Twin("\021", "\\15"),
            new Twin("\022", "\\16"),
            new Twin("\023", "\\17"),
            new Twin("\024", "\\18"),
            new Twin("\025", "\\19"),
            new Twin("\026", "\\1A"),
            new Twin("\027", "\\1B"),
            new Twin("\028", "\\1C"),
            new Twin("\029", "\\1D"),
            new Twin("\030", "\\1E"),
            new Twin("\031", "\\1F")
        };
    }
    
    public static String toSafeString(String what) {
        for (int cntr = 0; cntr < arr.length; cntr++) {
            what = arr[cntr].toImage(what);
        };
        return what;
    }
    
    public static String fromSafeString(String what) {
        for (int cntr = 0; cntr < arr.length; cntr++) {
            what = arr[cntr].fromImage(what);
        };
        return what;
    }
    
    public static int parseInt(String what) {
        return parseInt(what, 0);
    }
    
    public static int parseInt(String what, int prefered) {
        try {
            return Integer.parseInt(what);
        } catch (java.lang.NumberFormatException e) {
            return prefered;
        }
    }
    
    public static boolean readBoolean(Element node, String name) {
        String attribute = node.getAttribute(name);
        
        if (attribute == null) {
            return false;
        }
        if ("true".equalsIgnoreCase(attribute)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static int readInt(Element node, String name) {
        String attribute = node.getAttribute(name);
        
        if (attribute == null || attribute.length() == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(attribute);
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
            return 0;
        }
    }
}
