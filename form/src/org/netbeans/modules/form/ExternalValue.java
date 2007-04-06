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

package org.netbeans.modules.form;

/**
 * Interface for a property value stored externally, e.g. in a properties file,
 * not generated into the source code. The value is identified by a key.
 * 
 * @author Tomas Pavek
 */
public interface ExternalValue {
    /**
     * Special key representing a request to provide a valid key by form editor.
     * Can be used when a copy of the value is created that should have a new
     * key corresponding e.g. to the component and property names. Form editor
     * will compute and set the key automatically.
     */
    String COMPUTE_AUTO_KEY = "#auto"; // NOI18N

    /**
     * Returns the key identifying the value.
     * @return String key of the represented value
     */
    String getKey();

    /**
     * Returns the represented value.
     * @return represented value
     */
    Object getValue();
}
