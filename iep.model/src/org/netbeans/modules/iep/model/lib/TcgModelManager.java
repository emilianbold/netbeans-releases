/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.model.lib;


import org.netbeans.modules.iep.model.lib.GenUtil;
import org.netbeans.modules.iep.model.lib.ParseXmlException;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgLibraryLoader;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.iep.model.lib.TcgComponentTypeGroup;
import org.openide.util.NbBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Description: The TcgModelManager class This is the main class, which loads
 * the data from library.xml and build the component hierarchy similar to xml
 * file but different objects. component_library = TcgComponentTypeGroup
 * component_type_group = TcgComponentTypeGroup component_type = TcgComponentType
 * property = PropertyType
 *
 * @author Bing Lu
 *
 */
public class TcgModelManager implements TcgModelConstants {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgModelManager.class.getName());

    private static TcgComponentTypeGroup mLibRoot;

    static {
        loadLibraries();
    }

    private static TcgModelManager mInstance = new TcgModelManager();
    
    private TcgModelManager() {
    }

    public static TcgModelManager getInstance() {
        return mInstance;
    }
    
    /**
     * load libary.xml files
     */
    public static void loadLibraries() {
        try {
            mLibRoot = TcgLibraryLoader.loadLibraries();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    
    /**
     * DOCUMENT ME!
     *
     * @return true if the TcgModelManager is initialized
     */
    public static boolean isInitialized() {
        return true;
    }
    
    /**
     * Initialize TcgModelManager with the user-id and password for repository
     * connection
     *
     * @param userId the user-id for repository
     * @param passwd the password for repository
     *
     * @return true if TcgModelManager is established
     */
    public static boolean initialize(String userId, String passwd) {
       return false;
    }

    /**
     * Gets the TcgComponentType with the given path For example:
     * TcgComponentTypeGroup "else" has path "/logic/BasicJavaProgramming/else"
     *
     * @param path the "/" delimited String consists of
     *        TcgComponentTypeGroup.getName()s and TcgComponentType.getName() from
     *        mLibRoot to a TcgComponentType
     *
     * @return The pdsTcgComponentType value
     */
    public static TcgComponentType getTcgComponentType(String path) {

        int idx = path.lastIndexOf('/');

        if (idx <= 0) {
            return null;
        }

        String ctsPath = path.substring(0, idx);
        TcgComponentTypeGroup cts = getTcgComponentTypeGroup(ctsPath);

        if (cts == null) {
            return null;
        }

        String ctPath = path.substring(idx + 1);
        String[] ctNames = GenUtil.getTokens(ctPath, "|");
        TcgComponentType ct = cts.getComponentType(ctNames[0]);

        for (int i = 1, iMax = ctNames.length; i < iMax; i++) {
            ct = ct.getComponentType(ctNames[i]);
        }

        return ct;
    }

    /**
     * Returns a breadth-first list of component type groups starting at the lib root
     */
    public static List getTcgComponentTypeGroupList() {
        List ret = new ArrayList();
        ret.add(mLibRoot);
        ret.addAll(getTcgComponentTypeGroupList(mLibRoot));
        return ret;
    }

    /**
     * Returns a breadth-first list of component type groups
     */
    public static List getTcgComponentTypeGroupList(TcgComponentTypeGroup ctg) {
        List ret = new ArrayList();
        // UnmodifiableCollection
        List ctgList = ctg.getComponentTypeGroupList();
        ret.addAll(ctgList);
        for (int i = 0; i < ctgList.size(); i++) {
            TcgComponentTypeGroup g = (TcgComponentTypeGroup) ctgList.get(i);
            ret.addAll(getTcgComponentTypeGroupList(g));
        }
        return ret;
    }

    /**
     * Gets the TcgComponentTypeGroup with the given path For example:
     * TcgComponentTypeGroup "BasicJavaProgramming" has path "/logic/Basic Java
     * Programming"
     *
     * @param path the "/" delimited String consists of
     *        TcgComponentTypeGroup.getName()s from mLibRoot to a TcgComponentTypeGroup
     *
     * @return The TcgComponentTypeGroup value
     */
    public static TcgComponentTypeGroup getTcgComponentTypeGroup(String path) {

        String[] names = GenUtil.getTokens(path, "/");
        TcgComponentTypeGroup cts = mLibRoot;

        for (int i = 0, iMax = names.length; i < iMax; i++) {
            cts = cts.getComponentTypeGroup(names[i]);
            if (cts == null) {
                return null;
            }    
        }

        return cts;
    }

    public static TcgComponent getComponent(String fullName, InputStream is) throws Exception {
        TcgComponent component = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setValidating(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dcmt = db.parse(is);

            if (dcmt == null) {
            	String message = NbBundle.getMessage(TcgModelManager.class, "TcgModelManager.PARSE_XML_FAILED", new Object[]{"db:model:" + fullName });
            	throw new Exception(message);
            	/*
                throw new ParseXmlException("TcgModelManager.PARSE_XML_FAILED",
                                            "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                            new Object[]{"db:model:" + fullName });
                */
            }
            component = getComponent(dcmt.getDocumentElement());

            if (component == null) {
            	String message = NbBundle.getMessage(TcgModelManager.class, "TcgModelManager.BAD_COMPONENT_ERROR", new Object[]{"db:model:" + fullName });
            	throw new Exception(message);
            	/*
                throw new ParseXmlException("TcgModelManager.BAD_COMPONENT_ERROR",
                                            "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                            new Object[]{"db:model:" + fullName });
                */
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = NbBundle.getMessage(TcgModelManager.class, "TcgModelManager.PARSE_XML_FAILED", new Object[]{"db:model:" + fullName });
        	throw new Exception(message);
        	/*
            throw new ParseXmlException(
                "TcgModelManager.PARSE_XML_FAILED", 
                "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                new Object[]{"db:model:" + fullName }, e);
                */
        }
        return component;
    }

    public static TcgComponent getComponent(String fullName, String content) throws Exception {
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(content.getBytes("UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ParseXmlException(
                "TcgModelManager.PARSE_XML_FAILED", 
                "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                new Object[]{"db:model:" + fullName }, e);
            
        }
        return getComponent(fullName, bais);
    }

    /**
     * This takes a XML Element to return corresponding pdsComponent
     *
     * @param root org.w3c.dom.Element
     *
     * @return Returns a TcgComponent object after traversing Element
     */
    public static TcgComponent getComponent(Element root) {

        TcgComponent component = null;
        TcgComponentType componentType = null;
        NamedNodeMap attribs;
        Node attribNode;

        // Add the attributes if there are any
        attribs = root.getAttributes();

        if (attribs != null) {
            String compName = attribs.getNamedItem("name").getNodeValue();
            String compTitle = attribs.getNamedItem("title").getNodeValue();
            String compType = attribs.getNamedItem("type").getNodeValue();
            componentType = TcgModelManager.getTcgComponentType(compType);
            if (componentType == null) {
                mLog.warning("TcgComponentType : " + compType + " not found.");
                return null;
            }    
            component =componentType
                    .newShallowComponent(compName, compTitle);
        }

        // end if( attribs != null )
        // loop through children nodes if any exist
        if (root.hasChildNodes()) {
            NodeList children;
            int numChildren;
            Node node;

            children = root.getChildNodes();

            // Only recurse if Child Nodes are non-null
            if (children != null) {
                for (int i = 0, iMax = children.getLength(); i < iMax; i++) {
                    node = children.item(i);

                    if ((node == null)
                            || (node.ELEMENT_NODE != node.getNodeType())) {
                        continue;
                    }

                    // A special case could be made for each Node type.
                    if (node.getNodeName().equalsIgnoreCase("component")) {
                        TcgComponent child = getComponent((Element) node);
                        if (child != null) {
                            component.addComponent(child);
                        } else {
                            mLog.warning("null child not added");
                        }
                    } else if (node.getNodeName().equalsIgnoreCase("property")) {

                        // How to Load component_type to property
                        attribs = node.getAttributes();

                        try {
                            if (attribs != null) {
                                String sName =
                                    attribs.getNamedItem("name").getNodeValue();
                                String sValue =
                                    attribs.getNamedItem("value").getNodeValue();
    
                                if (component.hasProperty(sName)) {
                                    component.getProperty(sName).setStringValue(sValue);
                                } else {
                                    mLog.warning("ComponentProperty : " + sName + " not found.");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // end if( attribs != null )
                    }

                    // end if(node.getNodeName().equalsIgnoreCase("property"))
                }

                // end for (int i=0, I=children.getLength(); i < I; i++)
            }

            // end ( children != null )
        }

        // ( root.hasChildNodes() )
        return component;
    }
    
    /**
     * Gets the model with the given full name For example: a model with full
     *
     * @param name the template name
     *
     * @return The model value
     *
     * @exception ModelLoadException Description of the Exception
     * @exception ParseXmlException Description of the Exception
     */
    // UTF-8
    public static TcgComponent getTemplate(String filePath)
        throws ModelLoadException, Exception {

        if (filePath == null) {
            return null;
        }

        TcgComponent component = null;
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String line = null;

            while ((line = bf.readLine()) != null) {
                baos.write(line.getBytes("UTF-8"));
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setValidating(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc =
                db.parse(new ByteArrayInputStream(baos.toByteArray()));

            if (doc == null) {
            	String message = NbBundle.getMessage(TcgModelManager.class, "TcgModelManager.PARSE_XML_FAILED", new Object[]{"db:model:" + filePath });
            	throw new Exception(message);
            	/*
                throw new ParseXmlException("TcgModelManager.PARSE_XML_FAILED",
                                            "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                            new Object[]{"db:model:" + filePath });
                */
            }

            component = getComponent(doc.getDocumentElement());

            if (component == null) {
            	String message = NbBundle.getMessage(TcgModelManager.class, "TcgModelManager.BAD_COMPONENT_ERROR", new Object[]{"db:model:" + filePath });
            	throw new Exception(message);
            	/*
                throw new ParseXmlException("TcgModelManager.BAD_COMPONENT_ERROR",
                                            "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                            new Object[]{"db:model:" + filePath });
                                            
                */
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ModelLoadException("TcgModelManager.FAIL_LOAD_MODEL", 
                                         "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                         new Object[]{filePath }, e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ModelLoadException("TcgModelManager.FAIL_LOAD_MODEL", 
                                         "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                         new Object[]{filePath }, e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ModelLoadException("TcgModelManager.FAIL_LOAD_MODEL", 
                                         "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                         new Object[]{filePath }, e);
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
        }
        return component;
    }

    /**
     * Save component's content to filePath
     *
     * @param component
     * @param filePath
     */
    // UTF-8 FIXME
    public static void saveTemplate(TcgComponent component, String filePath) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            DOMSource source = new DOMSource(component.toXml(doc));
            StreamResult result = new StreamResult(out);
            Transformer trans =
                TransformerFactory.newInstance().newTransformer();

            trans.setOutputProperty("indent", "yes");
            trans.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2");
            trans.transform(source, result);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }    
    }

    public static String getVersion(TcgComponent comp) {
        String ret = null;
        if (comp != null) {
            try {
                ret = (String) comp.getRoot().getProperty("version").getValue();
            } catch (Exception e) {
                mLog.warning("Exception: "  + e);
                e.printStackTrace();
            }
        }
        return ret;
    }
    

}
