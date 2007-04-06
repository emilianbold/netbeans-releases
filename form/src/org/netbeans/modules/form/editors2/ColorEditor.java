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

package org.netbeans.modules.form.editors2;

import java.awt.Color;
import java.beans.PropertyEditorManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.ResourceWrapperEditor;

/**
 * A wrapper of a default property editor for colors allowing to define the
 * colors as resources.
 * 
 * @author Tomas Pavek
 */
public class ColorEditor extends ResourceWrapperEditor implements NamedPropertyEditor {
    
    public ColorEditor() {
        super(PropertyEditorManager.findEditor(Color.class));
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ColorEditor.class, "ColorEditor_DisplayName"); // NOI18N
    }
}
