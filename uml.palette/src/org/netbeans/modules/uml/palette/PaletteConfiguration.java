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

/*
 * PaletteConfiguration.java
 *
 * Created on March 18, 2005, 10:08 AM
 */

package org.netbeans.modules.uml.palette;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;

import org.openide.filesystems.Repository;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.palette.model.ModelingPaletteNodeDescriptor;
import org.netbeans.modules.uml.palette.model.PaletteElement;

/**
 *
 * @author Thuy Nguyen
 */
public class PaletteConfiguration {
    private static String resourceFileObj = "UML/PaletteDefinition/PaletteItemDefinition.xml";
    private static String PALETTE_ID = "paletteid";
    private static String ELEMENT_NAME = "name";
    private static String ELEMENT_TOOLTIP = "tooltip_key";
    private static String ELEMENT_ICON16 = "icon16";
    private static String ELEMENT_ICON32 = "icon32";
    private static String ELEMENT_ID = "id";
    private static Document document;
    private static Element rootElem;
    
    /**
     * Creates a new instance of PaletteConfiguration
     */
    public PaletteConfiguration() {
        load();
    }
    
    public static ModelingPaletteNodeDescriptor getPaletteElement(FileObject fo) {
        String imageResPath = "org/netbeans/modules/uml/palette/ui/images/"; // NOI18N
        
        if(document == null)
            load();       
        
        String elemAttrs[] = (String[]) getElement((String) fo.getAttribute("element_id")); // NOI18N
        
        ModelingPaletteNodeDescriptor paletteElement = null;
        if(elemAttrs != null) {
            Image icon16 = Utilities.loadImage(imageResPath + elemAttrs[2]);
            if (icon16 == null) {
                icon16 = Utilities.loadImage("org/netbeans/modules/palette/resources/unknown16.gif"); // NOI18N
            }
            Image icon32 = Utilities.loadImage(imageResPath + elemAttrs[3]);
            if (icon32 == null) {
                icon32 = icon16; 
            }
            
            // create a palette element with name, tooltip_key, icon16, icon32 and button_id
            paletteElement = new PaletteElement(elemAttrs[0], elemAttrs[1], icon16, icon32, elemAttrs[4]); // NOI18N
            //Log.out(" fo.attr2 " + fo.getAttribute("element_id") + " returning " + paletteElement.getButtonID()); // NOI18N
        } else {
            Log.out("Null elemAttrs[] found for element_id " + fo.getName());
        }
        return paletteElement;
    }
    
    private static String [] getElement(String paletteId) {
        String str[] = new String[5];
        if (rootElem == null  || paletteId == null)
            return null;
        
        List buttonNodes = rootElem.selectNodes("Palette/Button"); // NOI18N
        
        String pId = null;
        for(Iterator iterator = buttonNodes.iterator();
        iterator.hasNext();){
            Element btNode = (Element)iterator.next();    
            // get attributes value of "paletteId"
            pId = getAttrValue(btNode, PALETTE_ID);
            if (pId.equals(paletteId)) {
                str[0] = getAttrValue(btNode, ELEMENT_NAME);
                str[1] = getAttrValue(btNode, ELEMENT_TOOLTIP);
                str[2] = getAttrValue(btNode, ELEMENT_ICON16);
                str[3] = getAttrValue(btNode, ELEMENT_ICON32);
                str[4] = getAttrValue(btNode, ELEMENT_ID);
                break;
            }
        }
        return str;
    }
    
    private static String getAttrValue(Element elem, String attrStr) {
        String retVal = "";
        if (elem == null )
           return retVal;
        
        Attribute attr = elem.attribute(attrStr);
        if (attr != null) {
            retVal = attr.getValue();
        } 
        return retVal;
    }
    
    private static void load() {
        if (document == null ) {
            FileObject fileObj = Repository.getDefault().getDefaultFileSystem().findResource(resourceFileObj);
            InputStream inStream = null;
            URL urlVal = (URL) fileObj.getAttribute("originalFile");
            if (urlVal != null) {
                String resPath = urlVal.getPath();
//                System.out.println("resPath: "+ resPath);
//                URL resUrl = PaletteConfiguration.class.getResource(resPath);
//                String resURLPath = resUrl.getPath();
//                System.out.println("resURLPath: "+ resURLPath);
                inStream = PaletteConfiguration.class.getResourceAsStream(resPath);
                if (inStream == null)
                    return;
                
                try{
                    SAXReader dom4JSAXReader = new SAXReader();
                    dom4JSAXReader.setStripWhitespaceText(true);
                    dom4JSAXReader.setIgnoreComments(true);
                    document = dom4JSAXReader.read(inStream);
                    rootElem = document.getRootElement();
                } catch(Exception any){
                    any.printStackTrace();
                } finally  {
                    if (inStream != null) {
                        try {
                            inStream.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}


