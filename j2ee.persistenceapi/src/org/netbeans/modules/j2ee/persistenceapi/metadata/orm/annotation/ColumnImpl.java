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

public class ColumnImpl implements Column {

    private final ParseResult parseResult;

    public ColumnImpl(AnnotationModelHelper helper, AnnotationMirror annotation, String defaultName) {
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("name", parser.defaultValue(defaultName)); // NOI18N
        parser.expectPrimitive("length", Integer.class, parser.defaultValue(255));  // NOI18N
        parser.expectPrimitive("nullable", Boolean.class, parser.defaultValue(true));  // NOI18N
        parseResult = parser.parse(annotation);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return parseResult.get("name", String.class); // NOI18N
    }

    public void setUnique(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isUnique() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNullable(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isNullable() {
        return parseResult.get("nullable", Boolean.class); // NOI18N
    }

    public void setInsertable(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isInsertable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setUpdatable(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isUpdatable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setColumnDefinition(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getColumnDefinition() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTable(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getTable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setLength(int value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int getLength() {
        return parseResult.get("length", Integer.class); // NOI18N
    }

    public void setPrecision(int value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int getPrecision() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setScale(int value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int getScale() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
