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

package org.netbeans.modules.ruby.debugger;

import java.awt.Dialog;
import junit.framework.Assert;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/** 
 * Copy-pasted from APISupport. Test ready implementation of DialogDisplayer.
 *
 * @author Jaroslav Tulach
 */
public class DialogDisplayerImpl extends DialogDisplayer {
    
    private static Object toReturn;
    private NotifyDescriptor lastNotifyDescriptor;
    private Dialog dialog;
    
    public static void returnFromNotify(Object value) {
        Object o = DialogDisplayer.getDefault();
        Assert.assertEquals("My class", DialogDisplayerImpl.class, o.getClass());
        
        Assert.assertNull("No previous value", toReturn);
        toReturn = value;
    }
    
    public Object notify(NotifyDescriptor descriptor) {
        lastNotifyDescriptor = descriptor;
        Object r = toReturn;
        toReturn = null;
        
        Assert.assertNotNull("We are supposed to return a value", r);
        return r;
    }
    
    public Dialog createDialog(DialogDescriptor descriptor) {
        if (dialog == null) {
            Assert.fail("Not implemented");
        }
        return dialog;
    }
    
    public NotifyDescriptor getLastNotifyDescriptor() {
        return lastNotifyDescriptor;
    }
    
    public void reset() {
        this.lastNotifyDescriptor = null;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }
    
}
