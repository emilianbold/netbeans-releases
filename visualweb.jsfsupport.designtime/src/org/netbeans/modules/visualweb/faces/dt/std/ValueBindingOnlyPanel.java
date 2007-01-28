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
/*
 * Created on Jun 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.visualweb.faces.dt.std;

import javax.swing.JOptionPane;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ValueBindingOnlyPanel extends ValueBindingPanel {

    /**
     * @param vbpe
     * @param lp
     */
    public ValueBindingOnlyPanel(ValueBindingOnlyPropertyEditor vbpe, DesignProperty lp) {

        super(vbpe, lp);
    }

    public Object getPropertyValue() throws IllegalStateException {

        try {
            String result = (String)super.getPropertyValue();
            ((ValueBindingOnlyPropertyEditor)vbpe).verifyString(result);
            return result;
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(
                this,
                t.getMessage(),
                ValueBindingOnlyPropertyEditor.bundle.getMessage("vbOnlyPanelErroDialogTitle"), //NOI18N
                JOptionPane.WARNING_MESSAGE);
            throw new IllegalStateException(t.getMessage());
        }
    }

}
