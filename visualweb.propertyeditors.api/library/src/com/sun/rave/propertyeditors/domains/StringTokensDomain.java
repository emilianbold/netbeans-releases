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
package com.sun.rave.propertyeditors.domains;

import com.sun.rave.designtime.DesignProperty;
import java.beans.PropertyDescriptor;

/**
 * Domain that contains string tokens supplied as part of the property's metadata,
 * in the property descriptor. Each string token will constitute one element in the
 * domain, and will represent both the element's label and value. Tokens are
 * supplied as the comma-delimited value of the property descriptor attribute
 * <code>StringToken.TOKEN_STRING</code>.
 *
 * An empty token is treated as an "unset" or "null" value. An empty token is
 * a token of length 0, or a token that consists of only white space. When the
 * user selects an empty token, the value returned is null.
 *
 */
public class StringTokensDomain extends AttachedDomain {

    /**
     * Name of the property descriptor attribute whose value must contain the
     * comma-delimited string of tokens used to initialize this domain.
     */
    public final static String TOKEN_STRING =
            "com.sun.rave.propertyeditors.domains.TOKEN_STRING";

    public final static String TOKEN_DELIMITER = ",";

    Element[] elements;

    /**
     * Creates a new instance of StringTokenDomain.
     */
    public StringTokensDomain() {
        this.elements = Element.EMPTY_ARRAY;
    }

    /** Returns the elements for this domain. Initially, this will be an empty
     * array. The elements will be initialized after <code>setDesignProperty()</code>
     * has been called.
     */
    public Element[] getElements() {
        return this.elements;
    }

    public void setDesignProperty(DesignProperty designProperty) {
        super.setDesignProperty(designProperty);
        // Retrieve tokens from the property descriptor
        PropertyDescriptor descriptor = designProperty.getPropertyDescriptor();
        String tokenString = (String) descriptor.getValue(this.TOKEN_STRING);
        if (tokenString != null && tokenString.length() > 0) {
            String[] tokens = tokenString.split(",");
            this.elements = new Element[tokens.length];
            for (int i = 0; i < tokens.length; i++ ) {
                this.elements[i] = new Element(tokens[i]);
            }
        }
    }

}
