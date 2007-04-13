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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import javax.lang.model.element.AnnotationMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class TableImpl implements Table {

    private final ParseResult parseResult;

    public TableImpl(AnnotationModelHelper helper, AnnotationMirror annotation, String defaultName) {
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("name", parser.defaultValue(defaultName)); // NOI18N
        parser.expectString("schema", parser.defaultValue("")); // NOI18N
        parser.expectString("catalog", parser.defaultValue("")); // NOI18N
        parseResult = parser.parse(annotation);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return parseResult.get("name", String.class); // NOI18N
    }

    public void setCatalog(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getCatalog() {
        return parseResult.get("catalog", String.class); // NOI18N
    }

    public void setSchema(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getSchema() {
        return parseResult.get("schema", String.class); // NOI18N
    }

    public void setUniqueConstraint(int index, UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint getUniqueConstraint(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setUniqueConstraint(UniqueConstraint[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint[] getUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint newUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
