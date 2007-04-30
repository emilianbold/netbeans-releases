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
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class IdClassImpl implements IdClass {

    private final EntityImpl entity;
    private String class2;

    public IdClassImpl(EntityImpl entity) {
        this.entity = entity;
        TypeElement mainElement = entity.getSourceElement();
        if (mainElement == null) {
            // entity was removed, we should get an event soon
            // XXX log
            return;
        }
        AnnotationModelHelper helper = entity.getRoot().getHelper();
        AnnotationMirror idClassAnn = helper.getAnnotationsByType(mainElement.getAnnotationMirrors()).get("javax.persistence.IdClass"); // NOI18N
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectClass("value", null); // NOI18N
        class2 = parser.parse(idClassAnn).get("value", String.class); // NOI18N
    }

    public void setClass2(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getClass2() {
        return class2;
    }
}
