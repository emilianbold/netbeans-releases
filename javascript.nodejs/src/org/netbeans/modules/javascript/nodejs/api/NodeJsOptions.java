/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.api;

import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Node.js options (as set in IDE Options).
 * @since 0.14
 */
public final class NodeJsOptions {

    private static final NodeJsOptions INSTANCE = new NodeJsOptions();


    private NodeJsOptions() {
    }

    /**
     * Gets instance of node.js options.
     * @return instance of node.js options
     */
    public static NodeJsOptions getInstance() {
        return INSTANCE;
    }

    /**
     * Adds listener to node.js options.
     * @param listener listener to be added
     */
    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        getInternalOptions().addPreferenceChangeListener(listener);
    }

    /**
     * Removes listener from node.js options.
     * @param listener listener to be removed
     */
    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        getInternalOptions().removePreferenceChangeListener(listener);
    }

    /**
     * Gets file path representing <tt>node</tt> executable
     * or {@code null} if not set/found.
     * @return file path representing <tt>node</tt> executable, can be {@code null} if not set/found
     */
    @CheckForNull
    public String getNode() {
        return getInternalOptions().getNode();
    }

    /**
     * Gets file path representing <tt>npm</tt> executable
     * or {@code null} if not set/found.
     * @return file path representing <tt>npm</tt> executable, can be {@code null} if not set/found
     */
    @CheckForNull
    public String getNpm() {
        return getInternalOptions().getNpm();
    }

    private org.netbeans.modules.javascript.nodejs.options.NodeJsOptions getInternalOptions() {
        return org.netbeans.modules.javascript.nodejs.options.NodeJsOptions.getInstance();
    }

}
