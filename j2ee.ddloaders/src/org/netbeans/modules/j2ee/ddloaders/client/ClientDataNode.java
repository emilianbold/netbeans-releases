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

package org.netbeans.modules.j2ee.ddloaders.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;

/** A node to represent this ejb-jar.xml object.
 *
 * @author  Ludovic Champenois
 * @version 1.0
 */
public class ClientDataNode extends DataNode {
    
    private static final String DEPLOYMENT = "deployment"; // NOI18N
    private static final java.awt.Image ERROR_BADGE = 
        org.openide.util.Utilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/client/error-badge.gif" ); //NOI18N
    private static final java.awt.Image CLIETN_XML = 
        org.openide.util.Utilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/client/DDDataIcon.gif" ); //NOI18N
 
    private ClientDataObject dataObject;
   
    /** Name of property for spec version */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N
    
    /** Listener on dataobject */
    private PropertyChangeListener ddListener;
    
    public ClientDataNode (ClientDataObject obj) {
        this (obj, Children.LEAF);
    }

    public ClientDataNode (ClientDataObject obj, Children ch) {
        super (obj, ch);
        dataObject=obj;
        initListeners();
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
                if (ClientDataObject.PROP_DOCUMENT_DTD.equals (propertyName)) {
                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, oldValue, newValue);
                }
                if (ClientDataObject.PROP_VALID.equals (propertyName)
                &&  Boolean.TRUE.equals (newValue)) {
                    removePropertyChangeListener (ClientDataNode.this.ddListener);
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
    
    private ClientDataObject getDDDataObject () {
        return (ClientDataObject) getDataObject ();
    }
   
    protected Sheet createSheet () {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);

        Node.Property p = new PropertySupport.ReadOnly (
            PROPERTY_DOCUMENT_TYPE,
            String.class,
            NbBundle.getBundle(ClientDataNode.class).getString("PROP_documentDTD"),
            NbBundle.getBundle(ClientDataNode.class).getString("HINT_documentDTD")
        ) {
            public Object getValue () {
                return dataObject.getAppClient().getVersion();
            }
        };
        ss.put (p);
        s.put (ss);
        
        return s;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("TBD application-client.xml file");//NOI18N
    }

    void iconChanged() {
        fireIconChange();
    }

    public java.awt.Image getIcon(int type) {
        if (dataObject.getSaxError()==null)
            return CLIETN_XML;
        else 
            return org.openide.util.Utilities.mergeImages(CLIETN_XML, ERROR_BADGE, 6, 6);
    }
    
    public String getShortDescription() {
        org.xml.sax.SAXException saxError = dataObject.getSaxError();
        if (saxError==null) {
            return NbBundle.getBundle(ClientDataNode.class).getString("HINT_web_dd");
        } else {
            return saxError.getMessage();
        }
    }

}
