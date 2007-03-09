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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.util.NbBundle;

/**
 *
 * @author jsandusky
 */
public class LookAndFeelProperty extends BaseCasaProperty {
    
    public LookAndFeelProperty(CasaNode node)
    {
        super(
                node, 
                null, 
                null, 
                String.class, 
                "lookAndFeel", // NOI18N
                NbBundle.getMessage(LookAndFeelProperty.class, "LBL_LookAndFeel"),  // NOI18N
                NbBundle.getMessage(LookAndFeelProperty.class, "LBL_LookAndFeelDescription")); // NOI18N
    }

    
    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new LookAndFeelEditor(getDisplayName());
    }

    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return NbBundle.getMessage(getClass(), "LBL_EditLookAndFeel"); // NOI18N
    }

    public void setValue(Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    }
    
    public void restoreDefaultValue() {
        CasaFactory.getCasaCustomizer().restoreDefaults(true);
        CasaFactory.getCasaCustomizer().savePreferences();
    }
    public boolean supportsDefaultValue() {
        return true;
    }
}
