/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import java.beans.*;
import javax.swing.*;
import org.netbeans.test.editor.app.gui.Main;

import java.util.Vector;
import java.util.Collection;
//import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;

/**
 *
 * @author  ehucka
 * @version
 */
public class Test extends TestGroup {
    
    public static final String AUTHOR = "Author";
    public static final String VERSION = "Version";
    public static final String COMMENT = "Comment";
    
    private String author,version,comment;
    
    public JEditorPane editor;
    public Logger logger;
    
    private static boolean testing = false; /*Are we inside of test suite?*/
    
    /** Creates new Test */
    public Test(String name) {
        super(name);
        author = "NoAuthor";
        version = "1.0";
        comment = "";
    }
    
    public Test(Element node) {
        super(node);
        author = node.getAttribute(AUTHOR);
        version = node.getAttribute(VERSION);
        comment = loadString(node, COMMENT);
        logger = new Logger(Main.editor);
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        node.setAttribute(AUTHOR, author);
        node.setAttribute(VERSION, version);
        return saveString(node, COMMENT, comment);
    }
    
    public JEditorPane getEditor() {
        return editor;
    }
    
    public static void setTesting() {
        testing = true;
    }
    
    public static boolean isTesting() {
        return testing;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor (String value) {
        String oldValue = author;
        author = value;
        firePropertyChange (AUTHOR, oldValue, author);
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion (String value) {
        String oldValue = version;
        version = value;
        firePropertyChange (VERSION, oldValue, version);
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment (String value) {
        String oldValue = comment;
        comment = value;
        firePropertyChange (COMMENT, oldValue, comment);
    }
    
    public void rebuidlLoggers() {
        Test t;
        for(int i=0;i < getChildCount();i++) {
            if (get(i) instanceof TestSubTest) {
                t=(Test)(get(i));
                if (t.logger == null)
                    t.logger=logger;
            }
        }
    }
    
    public void perform() {
        TestNode n;
        
        Main.log("\nTest "+getName()+" starts performing.");
        isPerforming=true;
        for(int i=0;i < getChildCount();i++) {
            if (!isPerforming) break;
            n=get(i);
            if (n instanceof TestAction ) {
                n.perform();
            } else {
                if (n instanceof TestSubTest) {
                    n.perform();
                }
            }
        }
        isPerforming=false;
    }
    
    public void stop() {
        getLogger().stopPerforming();
        isPerforming=false;
    }
    
    //******************************************************************************
    
    private static class loadedToCreated {
        Class loaded;
        Class created;
        public loadedToCreated(Class loaded, Class created) {
            this.loaded = loaded;
            this.created = created;
        }
    }
    
    private static loadedToCreated[] classNames = new loadedToCreated[] {
        new loadedToCreated(Test.class, Test.class),
        new loadedToCreated(TestAction.class, TestAction.class),
        new loadedToCreated(TestCallAction.class, TestCallAction.class),
        new loadedToCreated(TestGroup.class, TestGroup.class),
        new loadedToCreated(TestLogAction.class, TestLogAction.class),
        new loadedToCreated(TestNode.class, TestNode.class),
        new loadedToCreated(TestSetAction.class, TestSetKitAction.class),
        new loadedToCreated(TestSetIEAction.class, TestSetIEAction.class),
        new loadedToCreated(TestSetJavaIEAction.class, TestSetJavaIEAction.class),
        new loadedToCreated(TestSetKitAction.class, TestSetKitAction.class),
        new loadedToCreated(TestStep.class, TestStep.class),
        new loadedToCreated(TestSubTest.class, TestSubTest.class),
    };
    
    public static final Element saveSubNodes(Element node, Vector nodes) {
        Document doc = node.getOwnerDocument();
        
        for (int cntr = 0; cntr < nodes.size(); cntr++) {
            Object item = nodes.elementAt(cntr);
            
            for (int index = 0; index < classNames.length; index++) {
                
                if (item.getClass() == classNames[index].loaded) {
                    Element newNode = doc.createElement(classNames[index].loaded.getName());
                    
                    node.appendChild(((TestNode)item).toXML(newNode));
                };
            };
        };
        return node;
    }
    
    public static final Object loadClass(Class clazz, Object from) {
        Class[] types = new Class[]  {Element.class };
        Object[] par  = new Object[] {from};
        
//        Main.log("Found class: " + clazz.getName());
        
        try  {
            return clazz.getConstructor(types).newInstance(par);
        } catch (java.lang.NoSuchMethodException e) {
            Main.log("Fatal error - attempt to load unloadable class(1).");
            System.exit(0);
        } catch (java.lang.IllegalAccessException e) {
            Main.log("Fatal error - attempt to load unloadable class(2).");
            System.exit(0);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Main.log("Fatal error - attempt to load unloadable class(3).");
            Main.log("Class name: " + clazz);
            Main.log("Exception: ");
            e.getTargetException().printStackTrace(Main.log);
            System.exit(0);
        } catch (java.lang.InstantiationException e) {
            Main.log("Fatal error - attempt to load unloadable class(4).");
            System.exit(0);
        };
        return null;
    }
    
    public static final Vector loadSubNodes(Element node) {
//                Main.log("loadSubNodes start.");
        NodeList nodes = node.getChildNodes();
        Vector   res   = new Vector(10, 10);
        
        for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
//                        Main.log("Trying to find class: " + nodes.item(cntr).getNodeName());
            for (int index = 0; index < classNames.length; index++) {
                if (nodes.item(cntr).getNodeName().equals(classNames[index].loaded.getName())) {
                    res.add(loadClass(classNames[index].created, nodes.item(cntr)));
                };
            };
        };
//                Main.log("loadSubNodes end.");
        return res;
    }
    
    /*
    private static Class[] classNames = new Class[] {
        Test.class,
        TestAction.class,
        TestCallAction.class,
        TestGroup.class,
        TestLogAction.class,
        TestNode.class,
        TestSetAction.class,
        TestStep.class,
        TestSubTest.class
    };
     
    public static final Element saveSubNodes(Element node, Collection nodes) {
        Document doc = node.getOwnerDocument();
        Iterator iterator = nodes.iterator();
     
        while (iterator.hasNext()) {
            TestNode actualNode = (TestNode) iterator.next();
            Class  actualNodeClass = actualNodeClass.getClass();
     
            for (int index = 0; index < classNames.length; index++) {
                if (actualNodeClass == classNames[index]) {
                    Element newNode = doc.createElement(classNames[index].getName());
     
                    node.appendChild(actualNode.toXML(newNode));
                };
            };
        };
        return node;
    }
     
    public static final Collection loadSubNodes(Element node) {
        NodeList nodes = node.getChildNodes();
        Vector   res   = new Vector(10, 10);
     
        for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
            for (int index = 0; index < classNames.length; index++) {
                if (nodes.item(cntr).getNodeName().equals(classNames[index].getName())) {
                    Class[] types = new Class[] {Element.class };
                    Object[] par  = new Object[] {nodes.item(cntr) };
     
//                    Main.log("Found class: " + classNames[index].getName());
     
                    try {
/*                        System.err.println("Found item: " + nodes.item(cntr));
                        System.err.println("Value: " + nodes.item(cntr).getNodeValue());* /
                        res.add(classNames[index].getConstructor(types).newInstance(par));
                    } catch (java.lang.NoSuchMethodException e) {
                        Main.log("Fatal error - attempt to load unloadable class(1).");
                        System.exit(0);
                    } catch (java.lang.IllegalAccessException e) {
                        Main.log("Fatal error - attempt to load unloadable class(2).");
                        System.exit(0);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        Main.log("Fatal error - attempt to load unloadable class(3).");
                        Main.log("Class name: " + classNames[index]);
                        Main.log("Exception: ");
                        e.getTargetException().printStackTrace(Main.log);
                        System.exit(0);
                    } catch (java.lang.InstantiationException e) {
                        Main.log("Fatal error - attempt to load unloadable class(4).");
                        System.exit(0);
                    };
                };
            };
        };
        return res;
    }*/
    
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
        //        NodeList nodes = el.getChildNodes();
        //        for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
        //            System.err.println("Item: type=" + nodes.item(0).getNodeName() + ", value=" + nodes.item(0).getNodeValue());
        //        }
        //        System.err.println("This node value:" + node.getNodeValue());
        StringBuffer sb = new StringBuffer();
        //        if (el.getLastChild().getNodeType() == Node.ELEMENT_NODE) {
        Node newEl = el/*.getLastChild()*/;
        
        sb.append(newEl.getNodeValue());
        NodeList nodes = newEl.getChildNodes();
        for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
            sb.append(nodes.item(cntr).getNodeValue());
        }
        //        System.err.println(sb.toString());
/*        } else {
            sb.append(el.getLastChild().getNodeValue());
        }*/
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
    
    protected Collection getNewTypes() {
        Collection newTypes = new Vector();
        
        newTypes.add(new NewType () {
            public void create () {
                TestSubTest st;
                NotifyDescriptor.Confirmation dlg=new NotifyDescriptor
                .Confirmation("Create new Logger for new Sub Test?",
                NotifyDescriptor.YES_NO_OPTION);
                if (NotifyDescriptor.OK_OPTION.equals(TopManager.
                getDefault().notify(dlg))) {
                    st=new TestSubTest((getNameCounter()),
                    new Logger(Main.editor));
                } else {
                    st=new TestSubTest((getNameCounter()),
                    getLogger());
                }
                addNode(st);
            }
            
            public String getName() {
                return "Sub Test";
            }
            
            public org.openide.util.HelpCtx getHelpCtx() {
                return null;
            }
        });
        newTypes.add(new NewType () {
            public void create () {
                addNode(
                new TestStep(getNameCounter()));
            }
            
            public String getName() {
                return "Step";
            }
            
            public org.openide.util.HelpCtx getHelpCtx() {
                return null;
            }
        });
        
        newTypes.add(new NewType () {
            public void create () {
                addNode(
                new TestCallAction(getNameCounter()));
            }
            
            public String getName() {
                return "Call action";
            }
            
            public org.openide.util.HelpCtx getHelpCtx() {
                return null;
            }
        });
        newTypes.addAll(generateSetNewTypes());
        return newTypes;
    }
    
}