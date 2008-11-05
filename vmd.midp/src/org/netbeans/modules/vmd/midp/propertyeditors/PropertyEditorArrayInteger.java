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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.GroupPropertyEditor;
import org.netbeans.modules.vmd.api.properties.GroupValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyEditorArrayInteger extends GroupPropertyEditor implements ExPropertyEditor {

    private static String ERROR_WARNING = NbBundle.getMessage(PropertyEditorArrayInteger.class, "MSG_ILLEGAL_FORMATING"); // NOI18N
    private WeakReference<DesignComponent> component;
    private Object parentTypeID;
    
    public static DesignPropertyEditor create() {
        return new PropertyEditorArrayInteger();
    }

    public static DesignPropertyEditor create(TypeID parentTypeID) {
        return new PropertyEditorArrayInteger(parentTypeID);
    }

    private PropertyEditorArrayInteger() {
    }

    private PropertyEditorArrayInteger(TypeID parentTypeID) {
        this.parentTypeID = parentTypeID;
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        this.component = null;
        parentTypeID = null;
    }

    @Override
    public boolean supportsCustomEditor() {
        return false;
    }

    @Override
    public String getAsText() {
        StringBuffer text = new StringBuffer();
        text.append('['); // NOI18N
        GroupValue values = getValue();
        for (Iterator<String> i = Arrays.asList(getValue().getPropertyNames()).iterator(); i.hasNext();) {
            PropertyValue value = (PropertyValue) values.getValue(i.next());
            text.append(value.getPrimitiveValue());
            if (i.hasNext()) {
                text.append(','); //NOI18N
            }
        }
        text.append(']'); //NOI18N
        return text.toString();
    }

    @Override
    public void setAsText(String text) {
        String newText = decodeValuesFromText(text);

        if (newText == null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERROR_WARNING + ' ' + text)); //NOI18N
        } else {
            GroupValue values = getValue();
            Iterator<String> propertyNamesIter = Arrays.asList(values.getPropertyNames()).iterator();
            for (String number : newText.split(",")) {
                //NOI18N
                values.putValue(propertyNamesIter.next(), MidpTypes.createIntegerValue(Integer.parseInt(number)));
            }
            setValue(values);
        }
    }

    private String decodeValuesFromText(String text) {
        text = text.trim().replaceAll(Pattern.compile("[\\[$\\]]").pattern(), ""); //NOI18N
        if (Pattern.compile("[^0123456789,]").matcher(text).find() || text.split(",").length != getValue().getPropertyNames().length) {
            //NOI18N
            return null;
        }
        return text;
    }

    @Override
    public Boolean canEditAsText() {
        return true;
    }

    @Override
    public boolean canWrite() {
        if (component.get() == null) {
            return super.canWrite();
        }
        final DesignComponent[] isEditable = new DesignComponent[1];
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                isEditable[0] = component.get().getParentComponent();
            }
        });
        if (parentTypeID != null && isEditable[0] != null && isEditable[0].getType().equals(parentTypeID)) {
            return false;
        }
        return super.canWrite();
    }

    @Override
    public void init(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
    }

    @Override
    public void customEditorResetToDefaultButtonPressed() {
         if (! (getValue() instanceof GroupValue))
            throw new IllegalArgumentException();
        GroupValue currentValue = (GroupValue) getValue();        
        for (String propertyName : currentValue.getPropertyNames()) {
            component.get().writeProperty(propertyName, MidpTypes.createIntegerValue(0));
        }
    }
    
}
