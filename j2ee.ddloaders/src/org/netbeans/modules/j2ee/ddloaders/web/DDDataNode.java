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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import java.beans.*;

import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;

/** A node to represent this object.
 *
 * @author  mkuchtiak
 * @version 1.0
 */
public class DDDataNode extends DataNode {

    private DDDataObject dataObject;

    /** Name of property for spec version */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N

    /** Listener on dataobject */
    private PropertyChangeListener ddListener;
    
    public DDDataNode (DDDataObject obj) {
        this (obj, Children.LEAF);
    }

    public DDDataNode (DDDataObject obj, Children ch) {
        super (obj, ch);
        dataObject=obj;
        initListeners();
    }
    
    private static final java.awt.Image ERROR_BADGE = 
        org.openide.util.Utilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/web/resources/error-badge.gif" ); //NOI18N
    private static final java.awt.Image WEB_XML = 
        org.openide.util.Utilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif" ); //NOI18N
    
    public java.awt.Image getIcon(int type) {
        if (dataObject.getSaxError()==null)
            return WEB_XML;
        else 
            return org.openide.util.Utilities.mergeImages(WEB_XML, ERROR_BADGE, 6, 6);
    }
    
    public String getShortDescription() {
        org.xml.sax.SAXException saxError = dataObject.getSaxError();
        if (saxError==null) {
            return NbBundle.getBundle(DDDataNode.class).getString("HINT_web_dd");
        } else {
            return saxError.getMessage();
        }
    }

    void iconChanged() {
        fireIconChange();
    }
  
    /** Initialize listening on adding/removing server so it is 
     * possible to add/remove property sheets
     */
    private void initListeners(){
        ddListener = new PropertyChangeListener () {
            
            public void propertyChange (PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName ();
                Object newValue = evt.getNewValue ();
                Object oldValue = evt.getOldValue ();
                if (DDDataObject.PROP_DOCUMENT_DTD.equals (propertyName)) {
                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, oldValue, newValue);
                }
                if (DataObject.PROP_VALID.equals (propertyName)
                &&  Boolean.TRUE.equals (newValue)) {
                    removePropertyChangeListener (DDDataNode.this.ddListener);
                }
                if (Node.PROP_PROPERTY_SETS.equals (propertyName)) {
                    firePropertySetsChange(null,null);
                }
                if (XmlMultiViewDataObject.PROP_SAX_ERROR.equals(propertyName)) {
                    fireShortDescriptionChange((String) oldValue, (String) newValue);
                }
            }
            
        };
        getDataObject ().addPropertyChangeListener (ddListener);
    }

    protected Sheet createSheet () {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);

        Node.Property p = new PropertySupport.ReadOnly (
            PROPERTY_DOCUMENT_TYPE,
            String.class,
            NbBundle.getBundle(DDDataNode.class).getString("PROP_documentDTD"),
            NbBundle.getBundle(DDDataNode.class).getString("HINT_documentDTD")
        ) {
            public Object getValue () {
                return dataObject.getWebApp().getVersion();
            }
        };
        ss.put (p);
        s.put (ss);
        
        return s;
    }
    
}
