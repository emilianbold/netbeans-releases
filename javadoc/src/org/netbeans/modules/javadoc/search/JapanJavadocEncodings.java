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
