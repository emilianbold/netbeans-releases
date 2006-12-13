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
package org.netbeans.modules.print.impl.ui;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.02.03
 */
final class Editor {

  Editor (Class clazz, String title, Object value) {
    myEditor = PropertyEditorManager.findEditor(clazz);
    myEditor.setValue(value);
    myDescriptor = new DialogDescriptor(myEditor.getCustomEditor(), title);
    DialogDisplayer.getDefault().createDialog(myDescriptor).setVisible(true);
  }

  Object getValue() {
    if (myDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
        return myEditor.getValue();
    }
    return null;
  }

  private PropertyEditor myEditor;
  private DialogDescriptor myDescriptor;
}
