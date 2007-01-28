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
package org.netbeans.modules.visualweb.faces.dt.data;

import java.beans.PropertyEditorSupport;
import java.sql.ResultSet;

public class HoldabilityPropertyEditor extends PropertyEditorSupport {

    private String[] tags = new String[] {
        "CLOSE_CURSORS_AT_COMMIT", "HOLD_CURSORS_OVER_COMMIT"}; //NOI18N
    private int[] tagVals = new int[] {
        ResultSet.CLOSE_CURSORS_AT_COMMIT,
        ResultSet.HOLD_CURSORS_OVER_COMMIT};

    public String[] getTags() {
        return tags;
    }

    public void setAsText(String text) {
        for (int i = 0; i < tags.length; i++) {
            if (text.equals(tags[i])) {
                setValue(new Integer(tagVals[i]));
                return;
            }
        }
    }

    public String getAsText() {
        for (int i = 0; i < tagVals.length; i++) {
            if (getValue().equals(new Integer(tagVals[i]))) {
                return tags[i];
            }
        }
        return null;
    }

    public String getJavaInitializationString() {
        return getAsText().equals("null") ? "java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT" : //NOI18N
            "java.sql.ResultSet." + getAsText(); //NOI18N
    }
}
