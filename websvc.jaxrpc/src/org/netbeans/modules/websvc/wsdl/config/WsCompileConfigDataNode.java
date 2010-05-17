/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
