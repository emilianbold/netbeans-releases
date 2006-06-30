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
