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
package org.netbeans.modules.html.api;

import org.netbeans.modules.html.HtmlDataObject;
import org.netbeans.modules.html.HtmlEditorSupport;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * Allows to perform some modifications to instances of {@link HtmlEditorSupport}.
 *
 * @author marekfukala
 */
public interface HtmlEditorSupportControl {

    /**
     * Sets a {@link Node} to the HtmlEditorSupport's lookup.
     *
     * The lookup is propagated to the editor top component.
     *
     * @param node an instance of {@link Node} you want to be current for the
     * opened html editor. Use null to remove the actual node from the lookup.
     */
    public void setNode(Node node);

    /**
     * Provides ability to obtain an instance of
     * {@link HtmlEditorSupportControl} for an html DataObject.
     */
    public static class Query {

        /**
         * Gets an instance of {@link HtmlEditorSupportControl} for a
         * DataObject.
         *
         * The given {@link DataObject} must of an instance of
         * {@link HtmlDataObject}.
         *
         * @param dataObject An instance of {@link HtmlDataObject}
         *
         * @return an instance of {@link HtmlEditorSupportControl} if the given
         * dataObject argument is instance of {@link HtmlDataObject}, null
         * otherwise.
         */
        public static HtmlEditorSupportControl get(DataObject dataObject) {
            if (!(dataObject instanceof HtmlDataObject)) {
                return null;
            }

            HtmlEditorSupport editorSupport = dataObject.getLookup().lookup(HtmlEditorSupport.class);
            assert editorSupport != null;

            return editorSupport;

        }
        
    }
}
