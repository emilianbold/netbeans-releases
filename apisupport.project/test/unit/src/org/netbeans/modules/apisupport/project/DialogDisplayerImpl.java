/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.awt.Dialog;
import junit.framework.TestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/** Test ready implementation of DialogDisplayer.
 *
 * @author Jaroslav Tulach
 */
public class DialogDisplayerImpl extends DialogDisplayer {
    private static Object toReturn;
    
    
    /** Creates a new instance of DialogDisplayerImpl */
    public DialogDisplayerImpl() {
    }
    
    public static void returnFromNotify(Object value) {
        Object o = DialogDisplayer.getDefault();
        TestCase.assertEquals("My class", DialogDisplayerImpl.class, o.getClass());
        
        TestCase.assertNull("No previous value", toReturn);
        TestCase.assertNotNull("Cannot have null return value", value);
        toReturn = value;
    }

    public Object notify(NotifyDescriptor descriptor) {
        Object r = toReturn;
        toReturn = null;
        
        TestCase.assertNotNull("We are supposed to return a value", r);
        return r;
    }

    public Dialog createDialog(DialogDescriptor descriptor) {
        TestCase.fail("Not implemented");
        return null;
    }
    
}
