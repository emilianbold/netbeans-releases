/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import org.netbeans.modules.worklist.editor.utils.DisplayNameBuilder;
import org.netbeans.modules.worklist.editor.utils.StringUtils;
import org.netbeans.modules.wlm.model.api.TCopy;
import org.netbeans.modules.wlm.model.api.TFrom;
import org.netbeans.modules.wlm.model.api.TTo;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class CopyNode extends WLMNode<TCopy> {

    private String cachedFrom;
    private String cachedTo;

    public CopyNode(TCopy copy, Lookup lookup) {
        this(copy, Children.LEAF, lookup);
        updateDisplayName();
    }

    public CopyNode(TCopy copy, Children children, Lookup lookup) {
        super(copy, children, lookup, "copy.png"); // NOI18N
        updateDisplayName();
    }

    @Override
    public void updateDisplayName() {
        TCopy copy = getWLMComponent();
        
        TFrom fromElement = copy.getFrom();
        TTo toElement = copy.getTo();

        String from = (fromElement == null) ? "" : fromElement.getContent();
        String to = (toElement == null) ? "" : toElement.getContent();

        if (from == null) {
            from = "";
        } else {
            from = from.trim();
        }

        if (to == null) {
            to = "";
        } else {
            to = to.trim();
        }

        if (!StringUtils.equals(from, cachedFrom)
                || !StringUtils.equals(to, cachedTo))
        {
            cachedFrom = from;
            cachedTo = to;

            DisplayNameBuilder builder = new DisplayNameBuilder();

            if (to.length() > 30) {
                builder.append(to.substring(0, 27));
                builder.startColor("#888888");
                builder.append("...");
                builder.endColor();
                builder.append(" ");
            } else if (to.length() > 0) {
                builder.append(to);
                builder.append(" ");
            }

            builder.startColor("#3366CC");
            builder.append("=");
            builder.endColor();

            if (from.length() > 30) {
                builder.append(" ");
                builder.append(from.substring(0, 27));
                builder.startColor("#888888");
                builder.append("...");
                builder.endColor();
            } else if (from.length() > 0) {
                builder.append(" ");
                builder.append(from);
            }
            
            setDisplayName(builder);
        }
    }

    @Override
    public WLMNodeType getType() {
        return WLMNodeType.COPY;
    }
}
