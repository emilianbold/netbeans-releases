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

package org.netbeans.modules.worklist.editor.nodes;

import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.worklist.editor.utils.DisplayNameBuilder;
import org.netbeans.modules.worklist.editor.utils.StringUtils;
import org.netbeans.modules.wlm.model.api.TPriority;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class TitleNode extends WLMNode<TTask> {

    private String cachedXPath;
    private Boolean cachedExists;

    public TitleNode(TTask component, Lookup lookup) {
        this(component, Children.LEAF, lookup);
    }

    public TitleNode(TTask component, Children children, Lookup lookup) {
        super(component, children, lookup);
        updateDisplayName();
    }

    @Override
    public void updateDisplayName() {
        TTask task = getWLMComponent();

        TTitle title = task.getTitle();

        String exprString = (title == null) ? null
                : title.getContent();

        Boolean exists = Boolean.valueOf(title != null);

        String xpath = TextFieldEditor.xPathToText(exprString);

        if ((cachedExists != exists)
                || !StringUtils.equals(cachedXPath, xpath))
        {
            cachedExists = exists;
            cachedXPath = xpath;

            DisplayNameBuilder builder = new DisplayNameBuilder();
            if (!exists) {
                builder.startColor("#888888");
                builder.append("Title");
                builder.endColor();
            } else if (cachedXPath.length() == 0) {
                builder.append("Title [");
                builder.startColor("#888888");
                builder.append("Undefined expression");
                builder.endColor();
                builder.append("]");
            } else {
                builder.append("Title [");
                if (xpath.length() > TRUNC_LENGTH + 3) {
                    builder.append(xpath.substring(0, TRUNC_LENGTH));
                    builder.startColor("#888888");
                    builder.append("...");
                    builder.endColor();
                } else {
                    builder.append(xpath);
                }
                builder.append("]");
            }

            setDisplayName(builder);
        }
    }

    @Override
    public WLMNodeType getType() {
        return WLMNodeType.TITLE;
    }

    @Override
    public WLMComponent getGoToSourceWLMComponent() {
        TTask task = getWLMComponent();
        TTitle title = task.getTitle();
        return (title != null) ? title : task;
    }
}
