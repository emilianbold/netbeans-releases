/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.editor.Coloring;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.openide.filesystems.FileLock;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.OutputStream;

/** MIME Option XML file for Fonts and Colors settings.
 *  Fonts and colors settings are loaded and saved in XML format
 *  according to EditorFontsColors-1_0.dtd
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class FontsColorsMIMEOptionFile extends MIMEOptionFile{
    
    /** Elements */
    public static final String TAG_ROOT = "fontscolors"; //NOI18N
    static final String TAG_FONTCOLOR = "fontcolor"; //NOI18N
    static final String TAG_ELEMENTCOLOR = "elementcolor"; //NOI18N
    static final String TAG_FONT = "font"; //NOI18N
    
    /** Elementcolor attributes */
    static final String ATTR_NAME = "name"; // NOI18N
    static final String ATTR_COLOR = "color"; // NOI18N
    
    /** Fontcolor attributes */
    static final String ATTR_SYNTAXNAME = "syntaxName"; // NOI18N
    static final String ATTR_FORECOLOR = "foreColor"; // NOI18N
    static final String ATTR_BGCOLOR = "bgColor"; // NOI18N
    
    /** Font attributes */
    static final String ATTR_SIZE = "size"; // NOI18N
    static final String ATTR_STYLE = "style"; // NOI18N
    
    /** File name of this MIMEOptionFile */
    static final String FILENAME = "fontsColors"; //NOI18N
    
    public FontsColorsMIMEOptionFile(BaseOptions base, Object proc) {
        super(base, proc);
    }

    /** Loads settings from XML file.
     * @param propagate if true - propagates the loaded settings to Editor UI */
     protected synchronized void loadSettings(boolean propagate){
        Document doc = dom;
        Element rootElement = doc.getDocumentElement();
        
        if (!TAG_ROOT.equals(rootElement.getTagName())) {
            // Wrong root element
            return;
        }
        
        // gets current coloring map
        Map cm = new HashMap( SettingsUtil.getColoringMap(base.getKitClass(), false, true) ); // !!! !evaluateEvaluators
        cm.put(null, base.getKitClass().getName() ); // add kit class
        
        Map mapa = cm;
        properties.clear();
        
        NodeList fc = rootElement.getElementsByTagName(TAG_FONTCOLOR);
        for (int i=0;i<fc.getLength();i++){
            Node node = fc.item(i);
            Element FCElement = (Element)node;
            
            if (FCElement == null){
                continue;
            }
            
            // set all attributes to null
            String  foreColor  = null;
            String  bgColor    = null;
            String  fontName   = null;
            String  fontSize   = null;
            String  fontStyle  = null;
            
            String  syntaxName = FCElement.getAttribute(ATTR_SYNTAXNAME);
            
            foreColor  = FCElement.getAttribute(ATTR_FORECOLOR);
            bgColor    = FCElement.getAttribute(ATTR_BGCOLOR);
            
            // scan FONT element and its attribs
            NodeList fnt = FCElement.getElementsByTagName(TAG_FONT);
            for (int j=0;j<fnt.getLength();j++){
                Node nodeFnt = fnt.item(j);
                Element FNTElement = (Element)nodeFnt;
                if (FNTElement == null){
                    continue;
                }
                fontName  = FNTElement.getAttribute(ATTR_NAME);
                if (fontName.length() == 0) fontName=null;
                fontSize  = FNTElement.getAttribute(ATTR_SIZE);
                if (fontSize.length() == 0) fontSize=null;
                fontStyle = FNTElement.getAttribute(ATTR_STYLE);
                if (fontStyle.length() == 0) fontStyle=null;
            }
            
            // try to get default kit specific coloring for syntaxName
            Coloring clr = SettingsUtil.getColoring(base.getKitClass(),syntaxName,false);
            
            // if null, then get kit specific coloring for default syntaxName
            if (clr==null) clr = SettingsUtil.getColoring(base.getKitClass(),SettingsNames.DEFAULT_COLORING,false);
            
            // default font, we will change the size, style or name if presented in XML
            // (all attribs are optional in DTD) if all will be null, we will inherit default Font
            Font font = clr.getFont();
            
            if (font==null){
                Coloring def = SettingsUtil.getColoring(base.getKitClass(),SettingsNames.DEFAULT_COLORING,false);
                if (def!=null) font = def.getFont();
            }
            
            int fontSizeInt;
            int fontStyleInt;
            Font newFont = null;
            
            if ((fontName!=null) || (fontSize!=null) || (fontStyle!=null)){
                // if some of font attribs isn't null we have to create new font
                if (fontName == null) {
                    // fontName is not specified in XML, get default value
                    if (font!=null) {
                        fontName = font.getName();
                    }
                    // if fontName is null the default AWT font will be used
                }
                
                // retrieve font size in integer represenation,  use default value if null
                if (fontSize == null) {
                    fontSizeInt = (font != null)? font.getSize() : 12;
                } else {
                    fontSizeInt = OptionUtilities.string2Int(fontSize);
                    if (fontSizeInt == -1) {
                        fontSizeInt = (font != null)? font.getSize() : 12;
                    }
                }
                
                // retrieve font style in integer represenation, use default value if null
                if (fontStyle == null){
                    fontStyleInt = (font != null)? font.getStyle() : 0;
                } else{
                    fontStyleInt = OptionUtilities.getFontStyle(fontStyle);
                }
                newFont = new Font(fontName, fontStyleInt, fontSizeInt);
            }
            
            // create new coloring from XML settings
            Coloring coloring = new Coloring(newFont,OptionUtilities.string2Color(foreColor),OptionUtilities.string2Color(bgColor));
            
            if (mapa.containsKey(syntaxName)){
                mapa.put(syntaxName,coloring);
                // add property to local map.
                properties.put(syntaxName,coloring);
            }
        }
        
        // setColoringMap without saving to XML
        if ( (propagate) && (properties.size()>0)){
            base.setColoringMap(mapa, false);
        }
        
        // Let's set the Elements color
        NodeList ec = rootElement.getElementsByTagName(TAG_ELEMENTCOLOR);
        for (int i=0;i<ec.getLength();i++){
            Node node = ec.item(i);
            Element FCElement = (Element)node;
            
            if (FCElement == null){
                continue;
            }
            
            String  elementName  = FCElement.getAttribute(ATTR_NAME);
            String  elementColorString = FCElement.getAttribute(ATTR_COLOR);
            Color elementColor = OptionUtilities.string2Color(elementColorString);
            
            if (elementColor==null) continue;
            
            boolean validProperty = false;
            
            if (SettingsNames.TEXT_LIMIT_LINE_COLOR.equalsIgnoreCase(elementName)){
                if (propagate) base.setTextLimitLineColor(elementColor,false);
                validProperty = true;
            }
            
            else if (SettingsNames.CARET_COLOR_INSERT_MODE.equalsIgnoreCase(elementName)){
                if (propagate) base.setCaretColorInsertMode(elementColor, false);
                validProperty = true;
            }
            
            else if (SettingsNames.CARET_COLOR_OVERWRITE_MODE.equalsIgnoreCase(elementName)){
                if (propagate) base.setCaretColorOverwriteMode(elementColor, false);
                validProperty = true;
            }
            
            // add property to local map.
            if (validProperty) properties.put(elementName,elementColor);
        }
        if (propagate) setLoaded(true);
    }
    
    /** Save settings to XML file 
     *  @param changedProp the Map of settings to save */
    protected synchronized void updateSettings(Map changedProp){
        // put changed properties to local map
        properties.putAll(changedProp);
        
        // now we can save local map to XML file
        Document doc = XMLUtil.createDocument(TAG_ROOT, null, processor.getPublicID(), processor.getSystemID());
        Element rootElem = doc.getDocumentElement();
        
        Map elementColors = new HashMap();
        
        // save Colorings first
        for( Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (properties.get(key) instanceof Coloring){
                
                Coloring coloring = (Coloring)properties.get(key);
                
                Element fontColorElem = doc.createElement(TAG_FONTCOLOR);
                // syntax name
                fontColorElem.setAttribute(ATTR_SYNTAXNAME, key);
                
                // fore color
                if (coloring.getForeColor()!=null){
                    fontColorElem.setAttribute(ATTR_FORECOLOR, OptionUtilities.color2String(coloring.getForeColor()));
                }
                
                // bg color
                if (coloring.getBackColor()!=null){
                    fontColorElem.setAttribute(ATTR_BGCOLOR, OptionUtilities.color2String(coloring.getBackColor()));
                }
                
                Font font = coloring.getFont();
                
                if (font!=null){
                    Element fontElement = doc.createElement(TAG_FONT);
                    // font name
                    if (font.getName()!=null){
                        fontElement.setAttribute(ATTR_NAME,font.getName());
                    }
                    // size
                    fontElement.setAttribute(ATTR_SIZE,Integer.toString(font.getSize()));
                    //style
                    fontElement.setAttribute(ATTR_STYLE,OptionUtilities.style2String(font.getStyle()));
                    fontColorElem.appendChild(fontElement);
                }
                rootElem.appendChild(fontColorElem);
            }else if (properties.get(key) instanceof Color){
                // store for further processing
                elementColors.put(key, properties.get(key));
            }
        }
        
        // and now element colors
        for( Iterator i = elementColors.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Element elementColor = doc.createElement(TAG_ELEMENTCOLOR);
            elementColor.setAttribute(ATTR_NAME, key);
            elementColor.setAttribute(ATTR_COLOR, OptionUtilities.color2String((Color)elementColors.get(key)));
            rootElem.appendChild(elementColor);
        }
        
        doc.getDocumentElement().normalize();
        
        try{
            FileLock lock = processor.getXMLDataObject().getPrimaryFile().lock();
            try{
                OutputStream os = processor.getXMLDataObject().getPrimaryFile().getOutputStream(lock);
                try {
                    XMLUtil.write(doc, os, null);
                    os.flush();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    os.close();
                }
            }catch (IOException ioe){
                ioe.printStackTrace();
            }finally{
                lock.releaseLock();
            }
        }catch (IOException ioexc){
            ioexc.printStackTrace();
        }
    }
    
}
