/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 *
 * @author marekfukala
 */
public final class TagImpl implements Tag {

    private static final String ID_ATTR_NAME = "id"; //NOI18N
    private String name;
    private String description;
    private Map<String, Attribute> attrs;

    public TagImpl(String name, String description, Map<String, Attribute> attrs) {
        this.name = name;
        this.description = description;
        this.attrs = attrs;
        //add the default ID attribute
        if (getAttribute(ID_ATTR_NAME) == null) {
            attrs.put(ID_ATTR_NAME, new Attribute.DefaultAttribute(ID_ATTR_NAME, "", false));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasNonGenenericAttributes() {
        return getAttributes().size() > 1; //the ID attribute is the default generic one
    }

    @Override
    public Collection<Attribute> getAttributes() {
        return attrs.values();
    }

    @Override
    public Attribute getAttribute(String name) {
        return attrs.get(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tag[name=").append(getName()).append(", attributes={"); //NOI18N
        for (Attribute attr : getAttributes()) {
            sb.append(attr.toString()).append(",");
        }
        sb.append("}]");
        return sb.toString();
    }
}
