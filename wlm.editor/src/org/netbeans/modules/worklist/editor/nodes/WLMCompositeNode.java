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
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public abstract class WLMCompositeNode<T extends WLMComponent> 
        extends WLMNode<T>
{
    private String baseName;
    private Boolean hasChildrenFlag = null;

    public WLMCompositeNode(T component, String nameKey, 
            Children children, Lookup lookup, boolean folder, String iconName)
    {
        super(component, children, lookup, folder, iconName);

        baseName = NbBundle.getMessage(getClass(), nameKey);

        updateDisplayName();
    }

    public abstract boolean hasChildren();

    @Override
    public void updateDisplayName() {
        boolean hasChildren = hasChildren();

        if (hasChildrenFlag == null
                || (hasChildren != hasChildrenFlag.booleanValue()))
        {
            DisplayNameBuilder builder = new DisplayNameBuilder();
            if (hasChildren) {
                builder.append(baseName);
            } else {
                builder.startColor("#888888"); // NOI18N
                builder.append(baseName);
                builder.endColor();
            }

            if (hasChildrenFlag == null) {
                setDisplayName(builder);
            } else {
                setDisplayName(""); // NOI18N
                setDisplayName(builder);
            }

            hasChildrenFlag = hasChildren;
        }
    }
}
