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


package org.netbeans.modules.form;

import javax.swing.Action;

import org.openide.actions.OpenAction;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.java.JavaNode;

/** The DataNode for Forms.
 *
 * @author Ian Formanek
 * @version 1.00, Jul 21, 1998
 */
public class FormDataNode extends JavaNode {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 1795549004166402392L;

    /** Icon base for form data objects. */
    private static final String FORM_ICON_BASE = "org/netbeans/modules/form/resources/form"; // NOI18N

    /** Constructs a new FormDataObject for specified primary file */
    public FormDataNode(FormDataObject fdo) {
        super(fdo);
    }

    protected String getBareIconBase() {
        return FORM_ICON_BASE;
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }

}
