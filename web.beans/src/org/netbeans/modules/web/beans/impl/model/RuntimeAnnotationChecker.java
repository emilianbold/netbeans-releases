/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.beans.impl.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;


/**
 * @author ads
 *
 */
public abstract class RuntimeAnnotationChecker {
    
    protected static final String VALUE = "value";                // NOI18N

    public void init( TypeElement element, AnnotationModelHelper helper ) {
        myHelper = helper;
        myElement = element;
    }
    
    public boolean check() {
        List<? extends AnnotationMirror> annotations = getElement()
                .getAnnotationMirrors();
        boolean hasAnnotation = getHelper().hasAnnotation(annotations,
                getAnnotation());
        boolean hasRequiredRetention = getHelper().hasAnnotation(annotations,
                Retention.class.getCanonicalName());
        boolean hasRequiredTarget = getHelper().hasAnnotation(annotations,
                Target.class.getCanonicalName());

        if (!hasAnnotation) {
            // this is not qualifier , just return false
            return false;
        }

        if (!hasRequiredRetention) {
            getLogger().log(Level.WARNING, "Annotation "
                    + getElement().getQualifiedName()
                    + "declared as " +getAnnotation()+" but has no Retention");// NOI18N
            return false;
        }

        if (!hasRequiredTarget) {
            getLogger().log(Level.WARNING, "Annotation "
                    + getElement().getQualifiedName()
                    + "declared as " +getAnnotation()+" but has no Target");// NOI18N
            return false;
        }

        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectEnumConstant(VALUE, getHelper().resolveType(
                RetentionPolicy.class.getCanonicalName()), null);
        Map<String, ? extends AnnotationMirror> types = getHelper()
                .getAnnotationsByType(annotations);
        AnnotationMirror retention = types.get(Retention.class
                .getCanonicalName()); // NOI18N
        String retentionPolicy = parser.parse(retention).get(VALUE,
                String.class);
        hasRequiredRetention = retentionPolicy.equals(RetentionPolicy.RUNTIME
                .toString());
        if (!hasRequiredRetention) {
            getLogger().log(Level.WARNING, "Annotation "
                    + getElement().getQualifiedName()
                    + " declared as " +getAnnotation()+" but has wrong retention policy."
                    + " Correct retention policy is "
                    + RetentionPolicy.RUNTIME.toString());// NOI18N
            return false;
        }
        hasRequiredTarget = checkTarget(types);

        return hasRequiredTarget;
    }
    
    protected abstract Logger getLogger();
    
    protected abstract String getAnnotation();
    
    protected abstract boolean checkTarget(
            Map<String, ? extends AnnotationMirror> types);
    
    protected TypeElement getElement(){
        return myElement;
    }
    
    protected AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private AnnotationModelHelper myHelper;
    private TypeElement myElement;
}
