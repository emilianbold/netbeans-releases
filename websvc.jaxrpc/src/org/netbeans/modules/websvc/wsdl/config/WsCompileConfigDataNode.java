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

package org.netbeans.modules.websvc.wsdl.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataNode;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.wsdl.xmlutils.XMLJ2eeDataObject;

/** Node for WsCompile configuration files in the explorer views.
 *
 * @author Peter Williams
 */
public class WsCompileConfigDataNode extends DataNode {

    private WsCompileConfigDataObject dataObject;
    private PropertyChangeListener ddListener;

    public WsCompileConfigDataNode(WsCompileConfigDataObject obj) {
        super(obj, Children.LEAF);

        this.dataObject = obj;
        setIconBase(dataObject.getIconBaseForValidDocument());
        setShortDescription(null);
        addListeners();
    }

    public void destroy() throws IOException {
        super.destroy();
        removeListeners();
    }

//    public String getShortDescription() {
//            // !PW FIXME This logic shouldn't be encoded like this, but it for now, it is.
//            //   getStringForInvalidDocument() will return null if there is no error
//            //   which implies the document is parsable (and thus valid).
//            //
//            String description = dataObject.getStringForInvalidDocument();
//            if(description == null) {
//                description = dataObject.getStringForValidDocument();
//            }
//
//            return description;
//    }

    private void addListeners() {
        ddListener = new PropertyChangeListener() {
            public void propertyChange (PropertyChangeEvent evt) {
//                if (WsCompileConfigDataObject.PROP_DOCUMENT_DTD.equals (evt.getPropertyName ())) {
//                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, evt.getOldValue (), evt.getNewValue ());
//                }
                if (DataObject.PROP_VALID.equals (evt.getPropertyName ())
                    && Boolean.TRUE.equals (evt.getNewValue ())) {
                    removePropertyChangeListener (WsCompileConfigDataNode.this.ddListener);
                }
                if (WsCompileConfigDataObject.PROP_DOC_VALID.equals (evt.getPropertyName ())) {
                    if (Boolean.TRUE.equals (evt.getNewValue ())) {
                        setIconBase (dataObject.getIconBaseForValidDocument ());
                    } else {
                        setIconBase (dataObject.getIconBaseForInvalidDocument ());
                    }
                }
                if (Node.PROP_PROPERTY_SETS.equals (evt.getPropertyName ())) {
                    firePropertySetsChange(null,null);
                }
            }
        };

        dataObject.addPropertyChangeListener(ddListener);
    }

    private void removeListeners() {
        dataObject.removePropertyChangeListener(ddListener);
    }

    protected org.openide.nodes.Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set ss = new Sheet.Set();

        ss.setName("service"); // NOI18N
        ss.setDisplayName(NbBundle.getBundle(WsCompileConfigDataNode.class).getString("LBL_WsConfigPropertiesName")); // NOI18N
        ss.setShortDescription(NbBundle.getBundle(WsCompileConfigDataNode.class).getString("LBL_WsConfigPropertiesDescription")); // NOI18N
        ss.put(new PackageProperty());

        sheet.put(ss);
        return sheet;
    }

    /** Web Service Client files are generated into a particular java package
     *  that is specified in the WsCompile configuration file.  This class
     *  represents that package as a property on the config file node.
     */
    private final class PackageProperty extends PropertySupport.ReadWrite {

        public PackageProperty() {
            super("Package" /*NOI18N*/, String.class,
                NbBundle.getBundle(WsCompileConfigDataNode.class).getString("LBL_WsConfigPackagePropertyName"), // NOI18N
                NbBundle.getBundle(WsCompileConfigDataNode.class).getString("LBL_WsConfigPackagePropertyDescription")); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return dataObject.getServicePackageName();
        }

        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if(val instanceof String) {
                dataObject.setServicePackageName((String) val);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
