/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.lib.html4parser;

import org.netbeans.modules.html.editor.lib.api.elements.Attribute;

/**
 *
 * @author marekfukala
 */
public class AstAttribute implements Attribute {
    
    private static final char NS_PREFIX_DELIMITER = ':';
    
    protected CharSequence name;
    protected CharSequence value;
    protected int nameOffset;
    protected int valueOffset;

    public AstAttribute(CharSequence name, CharSequence value, int nameOffset, int valueOffset) {
        this.name = name;
        this.value = value;
        this.nameOffset = nameOffset;
        this.valueOffset = valueOffset;
    }

    @Override
    public String name() {
        return name.toString();
    }

    public String namespacePrefix() {
        int delimIndex = name().indexOf(NS_PREFIX_DELIMITER);
        return delimIndex == -1 ? null : name().substring(0, delimIndex);
    }

    public String nameWithoutNamespacePrefix() {
        int delimIndex = name().indexOf(NS_PREFIX_DELIMITER);
        return delimIndex == -1 ? name() : name().substring(delimIndex + 1);
    }

    @Override
    public int nameOffset() {
        return nameOffset;
    }

    @Override
    public int valueOffset() {
        return valueOffset;
    }

    public int unqotedValueOffset() {
        return isValueQuoted() ? valueOffset + 1 : valueOffset;
    }

    @Override
    public String value() {
        return value.toString();
    }

    public String unquotedValue() {
        return isValueQuoted() ? value().substring(1, value().length() - 1) : value();
    }

    public boolean isValueQuoted() {
        if (value.length() < 2) {
            return false;
        } else {
            return (value.charAt(0) == '\'' || value.charAt(0) == '"') && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"');
        }
    }

    @Override
    public String toString() {
        return "Attr[" + name() + "(" + nameOffset() + ")=" + value + "(" + valueOffset() + ")]";
    }
    
}
