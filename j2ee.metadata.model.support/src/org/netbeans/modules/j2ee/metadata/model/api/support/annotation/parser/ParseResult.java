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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser;

import java.util.Map;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.*;
import org.openide.util.Parameters;

/**
 * Encapsulates a parsed annotation.
 *
 * @see AnnotationParser
 *
 * @author Andrei Badea
 */
public final class ParseResult {

    private final Map<String, Object> resultMap;

    ParseResult(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

    /**
     * Returns the value for the <code>name</code> key if that
     * value is of the type specified by <code>asType</code>.
     */
    public <T> T get(String name, Class<T> asType) {
        Parameters.notNull("name", name); //NOI18N
        Parameters.notNull("asType", asType); //NOI18N
        Object value = resultMap.get(name);
        if (asType.isInstance(value)) {
            @SuppressWarnings("unchecked") //NOI18N
            T typedValue = (T)value;
            return typedValue;
        }
        return null;
    }
}
