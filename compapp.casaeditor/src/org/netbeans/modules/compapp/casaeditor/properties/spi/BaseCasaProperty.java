/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.casaeditor.properties.spi;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.Node;

/**
 *
 * @author Josh
 */
public abstract class BaseCasaProperty<T> extends Node.Property<T> {
    
    protected CasaNode mNode;
    private String mPropertyType;
    private CasaComponent mComponent;
    private T mDefaultValue;

    // TODO: full support of default value
//    public BaseCasaProperty(
//            CasaNode node,
//            CasaComponent component,
//            String propertyType,
//            Class<T> valueType,
//            String propertyName,
//            String propertyDisplayName,
//            String propertyDescription,
//            T defaultValue)
//    {
//        this(node, component, propertyType, valueType, propertyName,
//                propertyDisplayName, propertyDescription);
//
//        mDefaultValue = defaultValue;
//    }

    public BaseCasaProperty(
            CasaNode node,
            CasaComponent component,
            String propertyType,
            Class<T> valueType,
            String propertyName,
            String propertyDisplayName,
            String propertyDescription)
    {
        super(valueType);

        mNode = node;
        mComponent = component;
        mPropertyType = propertyType;

        super.setName(propertyName);
        super.setDisplayName(propertyDisplayName);
        super.setShortDescription(propertyDescription);
    }
    
    public boolean canRead() {
        return true;
    }
    
    @Override
    public boolean canWrite() {
        try {            
            CasaDataEditorSupport editorSupport = mNode.getDataObject().getEditorSupport();
            if (editorSupport == null || !editorSupport.isDocumentLoaded()) {
                // Ensure the document is loaded, otherwise writes will surely fail.
                // A document may not be loaded if the user closed the editor.
                return false;
            }
            
            Model model = mComponent.getModel();
            return XAMUtils.isWritable(model) && mNode.isEditable(mPropertyType);
        } catch (Throwable t) {
            // At this point we may be inside property rendering.
            // We cannot throw up an error dialog, instead we exit quietly.
            // Log the error.
            t.printStackTrace(System.err);
            return false;
        }
    }
    
    @Override
    public boolean isDefaultValue () {
        try {
            return getValue() == null;
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        } catch (IllegalAccessException ex) {
        }
        return false;
    }

    @Override
    public boolean supportsDefaultValue () {
        return canWrite();
    }

    @Override
    public void restoreDefaultValue()
            throws IllegalAccessException, InvocationTargetException {
        setValue(mDefaultValue);
    }
    
    protected CasaWrapperModel getModel() {
        return mNode.getModel();
    }
    
    protected CasaComponent getComponent() {
        return mComponent;
    }

    protected Node getNode() {
        return mNode;
    }
}
