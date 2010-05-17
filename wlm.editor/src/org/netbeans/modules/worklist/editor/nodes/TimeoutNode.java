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

import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.worklist.editor.utils.DisplayNameBuilder;
import org.netbeans.modules.worklist.editor.utils.StringUtils;
import org.netbeans.modules.wlm.model.api.DeadlineOrDuration;
import org.netbeans.modules.wlm.model.api.TDeadlineExpr;
import org.netbeans.modules.wlm.model.api.TDurationExpr;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class TimeoutNode extends WLMNode<DeadlineOrDuration> {

    private Type cachedType = null;
    private String cachedXPath = null;

    public TimeoutNode(DeadlineOrDuration component, Lookup lookup) {
        this(component, Children.LEAF, lookup);
    }

    public TimeoutNode(DeadlineOrDuration component, Children children, 
            Lookup lookup)
    {
        super(component, children, lookup);
        updateDisplayName();
    }

    @Override
    public void updateDisplayName() {
        DeadlineOrDuration deadlineOrDuration = getWLMComponent();

        TDeadlineExpr deadline = deadlineOrDuration.getDeadline();
        TDurationExpr duration = deadlineOrDuration.getDuration();

        Type type;
        String xpath;
        if (deadline != null) {
            type = Type.DEADLINE;
            xpath = deadline.getContent();
        } else if (duration != null) {
            type = Type.DURATION;
            xpath = duration.getContent();
        } else {
            type = Type.UNDEFINED;
            xpath = null;
        }

        xpath = TextFieldEditor.xPathToText(xpath);

        if (cachedType != type || !StringUtils.equals(xpath, cachedXPath)) {
            cachedType = type;
            cachedXPath = xpath;

            DisplayNameBuilder builder = new DisplayNameBuilder();

            if (type == Type.DEADLINE) {
                builder.append("Deadline [");
            } else if (type == Type.DURATION) {
                builder.append("Duration [");
            } else {
                builder.startColor("#888888");
                builder.append("Timeout");
                builder.endColor();
            }

            if (type != Type.UNDEFINED) {
                if (xpath.length() == 0) {
                    builder.startColor("#888888");
                    builder.append("Undefined Expresison");
                    builder.endColor();
                } else if (xpath.length() > TRUNC_LENGTH + 3) {
                    builder.append(xpath.substring(0, TRUNC_LENGTH).trim());
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
        return WLMNodeType.TIMEOUT;
    }

    @Override
    public WLMComponent getGoToSourceWLMComponent() {
        DeadlineOrDuration deadlineOrDuration = getWLMComponent();

        TDeadlineExpr deadline = deadlineOrDuration.getDeadline();
        TDurationExpr duration = deadlineOrDuration.getDuration();

        if (deadline != null) {
            return deadline;
        }

        if (duration != null) {
            return duration;
        }

        return deadlineOrDuration;
    }

    private static enum Type {
        DEADLINE,
        DURATION,
        UNDEFINED;
    }
}
