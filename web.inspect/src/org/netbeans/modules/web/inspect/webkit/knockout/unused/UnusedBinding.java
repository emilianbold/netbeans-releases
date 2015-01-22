/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.inspect.webkit.knockout.unused;

/**
 * An unused binding.
 *
 * @author Jan Stola
 */
public class UnusedBinding {
    /** ID of the binding. */
    private final int id;
    /** Name of the binding. */
    private final String name;
    /** Tag name of the owner of this binding. */
    private final String nodeTagName;
    /** ID of the owner of this binding. */
    private final String nodeId;
    /** Classes (value if the {@code class} attribute) of the owner of this binding. */
    private final String nodeClasses;

    /**
     * Creates a new {@code UnusedBinding}.
     * 
     * @param id ID of the binding.
     * @param name name of the binding.
     * @param nodeTagName tag name of the owner of the binding.
     * @param nodeId ID of the owner of the binding.
     * @param nodeClasses classes (value of the {@code class} attribute)
     * of the owner of the binding.
     */
    public UnusedBinding(int id, String name, String nodeTagName, String nodeId, String nodeClasses) {
        this.id = id;
        this.name = name;
        this.nodeTagName = nodeTagName;
        this.nodeId = nodeId;
        this.nodeClasses = nodeClasses;
    }

    /**
     * Returns the ID of this binding.
     * 
     * @return ID of this binding.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of this binding.
     * 
     * @return name of this binding.
     */
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        // PENDING
        return nodeTagName;
    }

}
