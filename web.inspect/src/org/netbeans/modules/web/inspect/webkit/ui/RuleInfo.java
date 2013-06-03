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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.util.HashSet;
import java.util.Set;

/**
 * Additional information about a rule.
 *
 * @author Jan Stola
 */
public class RuleInfo {
    /** Names of properties that are overridden by other rules. */
    private final Set<String> overridenProperties = new HashSet<String>();
    /**
     * Determines whether the rules matches the selected element or whether
     * it matches some parent of the selected element (i.e., is inherited).
     */
    private boolean inherited;
    /** Meta-source file of the rule. */
    private String metaSourceFile;
    /** Line number of the rule in the meta-source file. */
    private int metaSourceLine = -1;

    /**
     * Marks the specified property as overridden by other rules.
     *
     * @param propertyName name of the overridden property.
     */
    void markAsOverriden(String propertyName) {
        overridenProperties.add(propertyName);
    }

    /**
     * Determines whether the specified property is overridden by other rules.
     *
     * @param propertyName name of the property to check.
     * @return {@code true} when the property is overridden,
     * returns {@code false} otherwise.
     */
    public boolean isOverriden(String propertyName) {
        return overridenProperties.contains(propertyName);
    }

    /**
     * Sets whether the rule is inherited or not.
     * 
     * @param inherited determines whether the rule matches the selected
     * element or whether it matches some parent of the selected element
     * (i.e., is inherited).
     */
    void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    /**
     * Determines whether the rules matches the selected element or whether
     * it matches some parent of the selected element (i.e., is inherited).
     * 
     * @return {@code true} when the rule comes from some parent,
     * returns {@code false} otherwise.
     */
    public boolean isInherited() {
        return inherited;
    }

    /**
     * Sets the meta-source file of the rule.
     * 
     * @param metaSourceFile meta-source file of the rule.
     */
    void setMetaSourceFile(String metaSourceFile) {
        this.metaSourceFile = metaSourceFile;
    }

    /**
     * Returns the meta-source file of the rule.
     * 
     * @return meta-source file of the rule.
     */
    public String getMetaSourceFile() {
        return metaSourceFile;
    }

    /**
     * Sets the line number of the rule in the meta-source file.
     * 
     * @param metaSourceLine line number of the rule.
     */
    void setMetaSourceLine(int metaSourceLine) {
        this.metaSourceLine = metaSourceLine;
    }

    /**
     * Returns the line number of the rule in the meta-source file.
     * 
     * @return line number of the rule in the meta-source file.
     */
    public int getMetaSourceLine() {
        return metaSourceLine;
    }

}
