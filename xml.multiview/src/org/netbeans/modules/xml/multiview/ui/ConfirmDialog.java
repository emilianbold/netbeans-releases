/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;
import org.openide.DialogDescriptor;

import org.openide.util.NbBundle;

/** ConfirmDialog.java
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 */
public class ConfirmDialog extends DialogDescriptor {
    
    /** Creates a new instance of ConfirmDialog */
    public ConfirmDialog(String text, String title) {
        super (text,title,true,
              DialogDescriptor.YES_NO_OPTION,
              DialogDescriptor.NO_OPTION,
              DialogDescriptor.BOTTOM_ALIGN,
              null,
              null);
    }
   
    /** Creates a new instance of ConfirmDialog */
    public ConfirmDialog(String text) {
        this(text,NbBundle.getMessage(ConfirmDialog.class,"TTL_ConfirmationDialog"));
    }
}
