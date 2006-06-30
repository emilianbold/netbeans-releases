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

package org.netbeans.modules.javadoc.search;

import java.beans.*;

/**
 *
 * @author  Petr Suchomel
 * @version
 */

public final class JapanJavadocEncodings extends PropertyEditorSupport {

    private static final String[] tags = { "JISAutoDetect", "SJIS", "EUC-JP", "ISO-2022-JP", "UTF-8"};     //NOI18N

    /** @return names of the supported encodings */
    public String[] getTags() {
        return tags;
    }

    /** @return text for the current value */
    public String getAsText () {
        return ((String)getValue());
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        setValue( text );
    }
}
