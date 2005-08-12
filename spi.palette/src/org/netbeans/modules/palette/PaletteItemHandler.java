/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.palette;
import java.util.LinkedList;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Libor Kotouc
 */
public final class PaletteItemHandler extends DefaultHandler {

    private static final String XML_ROOT = "editor_palette_item"; // NOI18N
    private static final String ATTR_VERSION = "version"; // NOI18N
//    private static final String TAG_ATTRIBUTES = "attributes"; // NOI18N
//    private static final String TAG_ATTRIBUTE = "attribute"; // NOI18N
//    private static final String ATTR_ATTRIBNAME = "name"; // NOI18N
//    private static final String ATTR_ATTRIBVALUE = "value"; // NOI18N
    private static final String TAG_BODY = "body"; // NOI18N
    private static final String TAG_CLASS = "class"; // NOI18N
    private static final String ATTR_CLASSNAME = "name"; // NOI18N
    private static final String TAG_CUSTOMIZER = "customizer"; // NOI18N
    private static final String ATTR_CUSTNAME = "name"; // NOI18N
    private static final String TAG_ICON16 = "icon16"; // NOI18N
    private static final String ATTR_URL = "urlvalue"; // NOI18N
    private static final String TAG_ICON32 = "icon32"; // NOI18N
    private static final String TAG_DESCRIPTION = "description"; // NOI18N
    private static final String ATTR_BUNDLE = "localizing-bundle"; // NOI18N
    private static final String ATTR_DISPLAY_NAME_KEY = "display-name-key"; // NOI18N
    private static final String ATTR_TOOLTIP_KEY = "tooltip-key"; // NOI18N

    private LinkedList/*<String>*/ bodyLines;
    private boolean insideBody = false;
    
    //raw data read from the file
//    private HashMap attributeMap;
    private String body;
    private String className;
    private String customizerName;
    
    private String icon16URL;
    private String icon32URL;
    private String bundleName;
    private String displayNameKey;
    private String tooltipKey;
    
//    public Map getAttributes() { return (attributeMap == null ? new HashMap() : attributeMap); }
    public String getBody() { return body; }
    public String getClassName() { return className; }
    
    public String getIcon16URL() { return icon16URL; }
    public String getIcon32URL() { return icon32URL; }
    public String getBundleName() { return bundleName; }
    public String getDisplayNameKey() { return displayNameKey; }
    public String getTooltipKey() { return tooltipKey; }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) 
        throws SAXException 
    {
        if (XML_ROOT.equals(qName)) {
            String version = attributes.getValue(ATTR_VERSION);
            if (version == null) {
                String message = NbBundle.getBundle(PaletteItemHandler.class)
                    .getString("MSG_UnknownEditorPaletteItemVersion"); // NOI18N
                throw new SAXException(message);
            } else if (!version.equals("1.0")) { // NOI18N
                String message = NbBundle.getBundle(PaletteItemHandler.class)
                    .getString("MSG_UnsupportedEditorPaletteItemVersion"); // NOI18N
                throw new SAXException(message);
            }
//        } else if (TAG_ATTRIBUTES.equals(qName)) {
//            attributeMap = new HashMap();
//        } else if (TAG_ATTRIBUTE.equals(qName)) {
//            String name = attributes.getValue(ATTR_ATTRIBNAME);
//            String value = attributes.getValue(ATTR_ATTRIBVALUE);
//            attributeMap.put(name, value);
        } else if (TAG_BODY.equals(qName)) {
            bodyLines = new LinkedList();
            insideBody = true;
        } else if (TAG_CLASS.equals(qName)) {
            className = attributes.getValue(ATTR_CLASSNAME);
        } else if (TAG_ICON16.equals(qName)) {
            icon16URL = attributes.getValue(ATTR_URL);
            // TODO support also class resource name for icons
        } else if (TAG_ICON32.equals(qName)) {
            icon32URL = attributes.getValue(ATTR_URL);
            // TODO support also class resource name for icons
        } else if (TAG_DESCRIPTION.equals(qName)) {
            bundleName = attributes.getValue(ATTR_BUNDLE);
            displayNameKey = attributes.getValue(ATTR_DISPLAY_NAME_KEY);
            tooltipKey = attributes.getValue(ATTR_TOOLTIP_KEY);
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (TAG_BODY.equals(qName)) {
            insideBody = false;
            body = trimSurroundingLines(bodyLines);
        }
    }
    
    public void characters(char buf[], int offset, int len)
        throws SAXException
    {
        if (insideBody) {
            String chars = new String(buf, offset, len).trim();
            bodyLines.add(chars + "\n");
        }
    }

    /**
     * Trims empty lines from the beginning and the end of the line list
     */
    private String trimSurroundingLines(LinkedList/*<String>*/ lines) {
        
        int nlines = lines.size();
        
        int firstNonEmpty = nlines;

        //going from the beginning and skipping empty lines until the first nonempty line occurs
        for (int i = 0; i < firstNonEmpty; i++) {
            String line = (String)lines.get(i);
            if (line.trim().length() != 0)
                firstNonEmpty = i;
        }
            
        int lastNonEmpty = -1;
        
        //going from the end and skipping empty lines until the first nonempty line occurs
        for (int i = nlines - 1; i > lastNonEmpty; i--) {
            String line = (String)lines.get(i);
            if (line.trim().length() != 0)
                lastNonEmpty = i;
        }

        StringBuffer sb = new StringBuffer();
        for (int i = firstNonEmpty; i <= lastNonEmpty; i++)
            sb.append((String)lines.get(i));
        
        String body = sb.toString();
        if (body.length() > 0  && body.charAt(body.length() - 1) == '\n') // cut trailing new-line
            body = body.substring(0, body.length() - 1); 
        
        return body;
    }
}

