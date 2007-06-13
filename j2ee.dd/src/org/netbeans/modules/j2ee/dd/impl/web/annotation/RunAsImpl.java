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

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;

/**
 *
 * @author Andrei Badea
 */
public class RunAsImpl implements RunAs {
    
    private final ParseResult parseResult;

    public RunAsImpl(AnnotationModelHelper helper, AnnotationMirror annotation) {
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("value", null); // NOI18N
        parseResult = parser.parse(annotation);
    }
    
    public String getRoleName() {
        return parseResult.get("value", String.class); // NOI18N
    }

    public void setRoleName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Object clone() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getDefaultDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setDescription(String description) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }
}
