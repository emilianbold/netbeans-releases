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


package org.netbeans.modules.iep.editor.tcg.model;

import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.netbeans.modules.iep.model.lib.TcgCodeType;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
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
            mLog.warning("TcgModelToTypeConverter.Failed_to_open_output_file: " + outFile);
            mLog.warning(e.getMessage());
        } catch (javax.xml.transform.TransformerConfigurationException e) {
            mLog.warning("TcgModelToTypeConverter.Failed_to_initialize_XML_transformer ");
            mLog.warning(e.getMessage());
        } catch (javax.xml.transform.TransformerException e) {
            mLog.warning("TcgModelToTypeConverter.Failed_to_transform_DOM_tree ");
            mLog.warning(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (java.io.IOException e) {
                    mLog.warning("TcgModelToTypeConverter.Failed_to_close_output_file: " + outFile);
                    mLog.warning(e.getMessage());
                }
            }
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
