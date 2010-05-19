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

package org.netbeans.modules.bpel.core.compatibility;

import org.netbeans.modules.bpel.model.ext.Extensions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author nikita krjukov
 */
public class CompatUtils {

    public static final class CompatibilityResult {

        private boolean mBpelEditorExt;
        private boolean mBpelEditor2Ext;

        CompatibilityResult() {
            mBpelEditorExt = false;
            mBpelEditor2Ext = false;
        }

        void setBpelEditorExtCompatible() {
            mBpelEditorExt = true;
        }

        void setBpelEditor2ExtCompatible() {
            mBpelEditor2Ext = true;
        }

        public boolean containsBpelEditorExt() {
            return mBpelEditorExt;
        }

        public boolean containsBpelEditor2Ext() {
            return mBpelEditor2Ext;
        }
    }

    public static CompatibilityResult checkCompatibility(Element root) {
        CompatibilityResult result = new CompatibilityResult();
        checkCompatibilityImpl(root, result);
        return result;
    }
    
    private static void checkCompatibilityImpl(Element root, CompatibilityResult result) {
        //
        if (root == null) {
            return;
        }
        //
        String nsUri = root.getNamespaceURI();
        if (Extensions.EDITOR_EXT_URI.equals(nsUri)) {
            result.setBpelEditorExtCompatible();
        }
        if (Extensions.EDITOR2_EXT_URI.equals(nsUri)) {
            result.setBpelEditor2ExtCompatible();
        }
        //
        NodeList nl = root.getChildNodes();
        if (nl != null) {
            for (int index = 0; index < nl.getLength(); index++) {
                Node node = nl.item(index);
                if (node instanceof Element) {
                    checkCompatibilityImpl(Element.class.cast(node), result);
                }
            }
        }
    }

    public static boolean findFirstOldComponent(Element root) {
        if (root == null) {
            return false;
        }
        //
        String nsUri = root.getNamespaceURI();
        if (Extensions.EDITOR_EXT_URI.equals(nsUri)) {
            return true;
        }
        //
        NodeList nl = root.getChildNodes();
        if (nl != null) {
            for (int index = 0; index < nl.getLength(); index++) {
                Node node = nl.item(index);
                if (node instanceof Element) {
                    boolean found = findFirstOldComponent(
                            Element.class.cast(node));
                    if (found) {
                        return true;
                    }
                }
            }
        }
        //
        return false;
    }
}
