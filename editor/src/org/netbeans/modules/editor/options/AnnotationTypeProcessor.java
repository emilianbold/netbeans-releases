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

import java.io.IOException;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.XMLDataObject;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileSystemCapability;
import org.netbeans.editor.AnnotationType;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import java.awt.Color;
import java.net.URL;
import java.util.MissingResourceException;
import java.net.MalformedURLException;

/** Processor of the XML file. The result of parsing is instance of AnnotationType
 * class.
 *
 * @author  David Konecny
 * @since 07/2001
*/
public class AnnotationTypeProcessor implements XMLDataObject.Processor, InstanceCookie {
    static final String DTD_PUBLIC_ID = "-//NetBeans//DTD annotation type 1.0//EN"; // NOI18N
    
    static final String TAG_TYPE = "type"; //NOI18N
    static final String ATTR_TYPE_NAME = "name"; // NOI18N
    static final String ATTR_TYPE_LOCALIZING_BUNDLE = "localizing_bundle"; // NOI18N
    static final String ATTR_TYPE_DESCRIPTION_KEY = "description_key"; // NOI18N
    static final String ATTR_TYPE_VISIBLE = "visible"; // NOI18N
    static final String ATTR_TYPE_GLYPH = "glyph"; // NOI18N
    static final String ATTR_TYPE_HIGHLIGHT = "highlight"; // NOI18N
    static final String ATTR_TYPE_FOREGROUND = "foreground"; // NOI18N
    static final String ATTR_TYPE_TYPE = "type"; // NOI18N
    static final String ATTR_TYPE_CONTENTTYPE = "contenttype"; // NOI18N
    static final String TAG_ACTIONS = "actions"; // NOI18N
    static final String TAG_ACTION = "action"; // NOI18N
    static final String ATTR_ACTION_NAME = "name"; // NOI18N
    static final String TAG_COMBINATIONS  = "combinations"; // NOI18N
    static final String ATTR_COMBINATIONS_TIPTEXT_KEY  = "tiptext_key"; // NOI18N
    static final String TAG_COMBINE  = "combine"; // NOI18N
    static final String ATTR_COMBINE_ANNOTATIONTYPE  = "annotationtype"; // NOI18N

    /** XML data object. */
    private XMLDataObject xmlDataObject;
    
    /**
     * Annotation type created from XML file.
     */
    private AnnotationType  annotationType;
    
    /** When the XMLDataObject creates new instance of the processor,
     * it uses this method to attach the processor to the data object.
     *
     * @param xmlDO XMLDataObject
     */
    public void attachTo(XMLDataObject xmlDO) {
        xmlDataObject = xmlDO;
    }
    
    /** Create an instance.
     * @return the instance of type {@link #instanceClass}
     * @exception IOException if an I/O error occured
     * @exception ClassNotFoundException if a class was not found
     */
    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        if (annotationType != null)
            return annotationType;

        try {
            loadLibrary( xmlDataObject.getDocument() );
            return annotationType;
        } catch (org.xml.sax.SAXException e) {
            IOException ex = new IOException ();
            org.openide.TopManager.getDefault ().getErrorManager ().copyAnnotation (ex, e);
            throw ex;
        }
    }
    
    /** The representation type that may be created as instances.
     * Can be used to test whether the instance is of an appropriate
     * class without actually creating it.
     *
     * @return the representation class of the instance
     * @exception IOException if an I/O error occurred
     * @exception ClassNotFoundException if a class was not found
     */
    public Class instanceClass() {
        return AnnotationType.class;
    }
    
    /** The bean name for the instance.
     * @return the name
     */
    public String instanceName() {
        return instanceClass().getName();
    }

    ////////////////////////////////////////////////////////////////////////

    /** Process one XML document */
    private void loadLibrary (Document doc) {
        Element rootElement = doc.getDocumentElement();
        if (!TAG_TYPE.equals(rootElement.getTagName())) {
            return;
        }
        annotationType = new AnnotationType();
        if (!readLibraryID(rootElement))
            annotationType = null;
    }
    
    /** Reads the TYPE tag and fill in AnnotationType members*/
    boolean readLibraryID(Element def) {
        
        // read all TYPE attributes
        annotationType.setName(def.getAttribute(ATTR_TYPE_NAME));
        annotationType.setContentType(def.getAttribute(ATTR_TYPE_CONTENTTYPE));
        annotationType.setVisible(def.getAttribute(ATTR_TYPE_VISIBLE));
        annotationType.setWholeLine(def.getAttribute(ATTR_TYPE_TYPE).equals("line"));
        try {
            if (def.getAttribute(ATTR_TYPE_HIGHLIGHT) != null && def.getAttribute(ATTR_TYPE_HIGHLIGHT).length() > 0) {
                annotationType.setHighlight(Color.decode(def.getAttribute(ATTR_TYPE_HIGHLIGHT)));
                annotationType.setUseHighlightColor(true);
            } else {
                annotationType.setUseHighlightColor(false);
            }
            
            if (def.getAttribute(ATTR_TYPE_FOREGROUND) != null && def.getAttribute(ATTR_TYPE_FOREGROUND).length() > 0) {
                annotationType.setForegroundColor(Color.decode(def.getAttribute(ATTR_TYPE_FOREGROUND)));
                annotationType.setInheritForegroundColor(false);
            } else {
                annotationType.setInheritForegroundColor(true);
            }
        } catch (NumberFormatException ex) {
            if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                ex.printStackTrace();
            return false;
        }
        try {
            if (def.getAttribute(ATTR_TYPE_GLYPH) != null && def.getAttribute(ATTR_TYPE_GLYPH).length() > 0)
                annotationType.setGlyph(new URL(def.getAttribute(ATTR_TYPE_GLYPH)));

        } catch (MalformedURLException ex) {
            if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                ex.printStackTrace();
            return false;
        }
        try {
            ResourceBundle bundle = NbBundle.getBundle(def.getAttribute(ATTR_TYPE_LOCALIZING_BUNDLE));
            annotationType.setDescription(bundle.getString(def.getAttribute(ATTR_TYPE_DESCRIPTION_KEY)));
        } catch (MissingResourceException ex) {
            if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                ex.printStackTrace();
            return false;
        }
        
        //System.err.println("Annotation="+annotationType);
        
        /*NodeList nL = def.getElementsByTagName (TAG_ACTIONS);
        if (nL.getLength() > 0 ) {
            Element actionsNode = (Element)nL.item (0);
            nL = actionsNode.getElementsByTagName (TAG_ACTION);
            int a=0;
            while (a<nL.getLength()) {
                Element actionNode = (Element)nL.item (a);
                System.err.println("Annotation's action="+actionNode.getAttribute(ATTR_ACTION_NAME));
                a++;
            }
        }*/
        return true;
    }
    
}
