/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.api.schema;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public class Element extends RepeatableSchemaConstruct {
    
    private boolean nillable;
    private Type elementType ;

    public Element() {
        super(SchemaConstruct.ConstructType.ELEMENT);

        setMinOccurs(1);
        setMaxOccurs(1);
        nillable = false;
    }

    public Element(QName name) {
        super(SchemaConstruct.ConstructType.ELEMENT, name);
        setMinOccurs(1);
        setMaxOccurs(1);
        nillable = false;

        setName(name);
    }

    public Element(QName name, Type elementType) {
        this(name);
        this.elementType = elementType;
    }

    public Element(QName name, Type elementType, int minOccurs, int maxOccurs) {
        this(name, elementType);
        nillable = false;

        setMinOccurs(minOccurs);
        setMaxOccurs(maxOccurs);
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setType(Type elementType) {
        this.elementType = elementType;
    }

    public Type getType() {
        return elementType;
    }

    @Override
    public String getJavaName() {
        String javaName = getJavaName();
        if (javaName != null) {
            return javaName;
        }
        return getName().getLocalPart() + (getMaxOccurs() > 1 ? "[]" : ""); // NOI18N
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("element"); // NOI18N
        if (getName() != null) {
            sb.append(" name='"); // NOI18N
            sb.append(getName());
            sb.append("'"); // NOI18N
        }
        sb.append(" minOccurs='"); // NOI18N
        sb.append(getMinOccurs());
        sb.append("'"); // NOI18N
        if (getMaxOccurs() == RepeatableSchemaConstruct.UNBOUNDED) {
            sb.append(" maxOccurs='unbounded'"); // NOI18N
        } else {
            sb.append(" maxOccurs='"); // NOI18N
            sb.append(getMaxOccurs());
            sb.append("'"); // NOI18N
        }
        sb.append(" nillable='"); // NOI18N
        sb.append(nillable);
        sb.append("'"); // NOI18N
        sb.append('\n');

        return sb.toString();
    }

}
