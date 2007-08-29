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

package org.netbeans.modules.web.core.palette.items;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;


/**
 *
 * @author Libor Kotouc
 */
public class SetProperty extends GetProperty {

    private String value = "";

    public SetProperty() {
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        allBeans = initAllBeans(targetComponent);
        SetPropertyCustomizer c = new SetPropertyCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                JSPPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }

        return accept;
    }

    private String createBody() {

        String strBean = " name=\"\""; // NOI18N
        if (getBeanIndex() == -1) {
            strBean = " name=\"" + getBean() + "\""; // NOI18N
        } else {
            strBean = " name=\"" + allBeans.get(getBeanIndex()).getId() + "\""; // NOI18N
        }
        String strProperty = " property=\"" + getProperty() + "\""; // NOI18N
        String strValue = " value=\"" + getValue() + "\""; // NOI18N
        String sp = "<jsp:setProperty" + strBean + strProperty + strValue + " />"; // NOI18N
        return sp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}