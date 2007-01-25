/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.editor.tcg.model;

import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;




/**
 * Internal tool to convert a model file containing a hierarchy of TcgComponent
 * to a hierarchy of TcgComponentType and output the result as XML.
 *
 * @author Bing Lu
 *
 * @since June 13, 2002
 */
public class TcgModelToTypeConverter {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgModelToTypeConverter.class.getName());

    /**
     * Convert the model to component type library and writes to file
     *
     * @param fullModelName fullModelName
     * @param outFile output file name
     */
    public static void convert(TcgComponent c, String outFile) {

        try {
            IIOMetadataNode rootNode = new IIOMetadataNode("component_type_group");

            rootNode.setAttribute("name", "model");
            rootNode.setAttribute("icon", "");
            rootNode.setAttribute("title", "Model");

            IIOMetadataNode childNode = new IIOMetadataNode("component_type_group");

            childNode.setAttribute("name", "Connector Forms");
            childNode.setAttribute("icon", "");
            childNode.setAttribute("title", "Connector Forms");
            rootNode.appendChild(childNode);

            Element e = TcgModelToTypeConverter.toXml(c);

            childNode.appendChild(e);
            write(rootNode, outFile);
        } catch (Exception e) {
            mLog.warning(e.getMessage());
        }
    }

    /**
     * TcgMain method
     *
     * @param args Description of the Parameter
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            TcgModelToTypeConverter.usage();
            System.exit(0);
        }
    }

    /**
     * Recursively convert the TcgComponent to TcgComponentType hierarchy
     *
     * @param c TcgComponent to convert
     *
     * @return XML element representing the TcgComponentType hierarchy
     */
    static Element toXml(TcgComponent c) {

        TcgComponentType type = c.getType();
        IIOMetadataNode rootNode = new IIOMetadataNode("component_type");

        rootNode.setAttribute("name", c.getName());
        rootNode.setAttribute("icon", type.getIconName());
        rootNode.setAttribute("title", type.getTitle());
        rootNode.setAttribute("allowsChildren",
                              Boolean.valueOf(type.getAllowsChildren()).toString());
        rootNode.setAttribute("visible",
                              Boolean.valueOf(type.isVisible()).toString());

        // Lets check out if this node has any property
        List propList = type.getPropertyTypeList();

        for (int i = 0, ii = propList.size(); i < ii; i++) {
            TcgPropertyType propType = (TcgPropertyType) propList.get(i);
            IIOMetadataNode propertyNode = new IIOMetadataNode("property");

            propertyNode.setAttribute("name", propType.getName());

            if (propType.isMultiple()) {
                String s = propType.getType().toString();

                propertyNode.setAttribute("type",
                                          s.substring(0, s.length() - 4));
            } else {
                propertyNode.setAttribute("type",
                                          propType.getType().toString());
            }

            propertyNode.setAttribute("editor", propType.getEditorName());
            propertyNode.setAttribute("default",
                                      (propType.getDefaultValue() == null)
                                      ? ""
                                      : propType.getDefaultValue().toString());
            propertyNode.setAttribute("renderer", propType.getRendererName());
            propertyNode.setAttribute("title", propType.getTitle());
            propertyNode.setAttribute("description", propType.getDescription());
            propertyNode.setAttribute("access", propType.getAccess());
            propertyNode.setAttribute("required",
                                      Boolean.valueOf(propType.isRequired())
                                          .toString());
            propertyNode.setAttribute("multiple",
                                      Boolean.valueOf(propType.isMultiple())
                                          .toString());
            rootNode.appendChild(propertyNode);
        }

        List codeTypes = type.getCodeTypeList();

        for (Iterator itr = codeTypes.iterator(); itr.hasNext();) {
            TcgCodeType aTcgCodeType = (TcgCodeType) itr.next();
            IIOMetadataNode codeNode = new IIOMetadataNode("code");

            codeNode.setAttribute("type", aTcgCodeType.getName());
            codeNode.setAttribute("file", aTcgCodeType.getTemplateName());
            rootNode.appendChild(codeNode);
        }

        /*
         *  Lets check out if this node has any children if it does then recurse
         *  until we hit the bottom
         */
        List compList = c.getComponentList();

        for (int i = 0, ii = compList.size(); i < ii; i++) {
            TcgComponent pdscomponent = (TcgComponent) compList.get(i);

            rootNode.appendChild(TcgModelToTypeConverter.toXml(pdscomponent));
        }

        return (Element) rootNode;
    }

    /**
     * Method usage
     *
     *
     */
    static void usage() {

        mLog.info(
            "java org.netbeans.modules.iep.editor.tcg.model.TcgModelToTypeConverter modelFile outputFile");
        mLog.info("\tmodelFile:  Model file name");
        mLog.info("\toutputFile:  output file name");
    }

    /**
     * Method write
     *
     *
     * @param rootNode
     * @param outFile
     *
     */
    static void write(IIOMetadataNode rootNode, String outFile) {

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(outFile);

            DOMSource source = new DOMSource(rootNode);
            StreamResult result = new StreamResult(out);
            Transformer trans =
                TransformerFactory.newInstance().newTransformer();

            trans.setOutputProperty("indent", "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                    "2");
            trans.transform(source, result);
            mLog.info("Conversion succeeded!");
        } catch (java.io.FileNotFoundException e) {
            mLog.warning("Failed to open output file: " + outFile);
            mLog.warning(e.getMessage());
        } catch (javax.xml.transform.TransformerConfigurationException e) {
            mLog.warning("Failed to initialize XML transformer");
            mLog.warning(e.getMessage());
        } catch (javax.xml.transform.TransformerException e) {
            mLog.warning("Failed to transform DOM tree");
            mLog.warning(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (java.io.IOException e) {
                    mLog.warning("Failed to close output file: " + outFile);
                    mLog.warning(e.getMessage());
                }
            }
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
