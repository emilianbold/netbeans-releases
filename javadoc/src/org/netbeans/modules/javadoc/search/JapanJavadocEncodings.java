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

/* Base class providing search for JDK1.2/1.3 documentation
 * Jdk12SearchType.java
 *
 * Created on 19. ?or 2001, 17:14
 * @author Petr Hrebejk, Petr Suchomel
 */

/*
 * JapanJavadocEncodings.java
 *
 * Created on March 7, 2001, 5:37 PM
 */

package org.netbeans.modules.javadoc.search;

import java.beans.*;

/**
 *
 * @author  Petr Suchomel
 * @version 
 */

public class JapanJavadocEncodings extends PropertyEditorSupport {
    
    private static final String[] tags = { "JISAutoDetect", "SJIS", "EUC-JP", "ISO-2022-JP", "UTF-8"};
    
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
