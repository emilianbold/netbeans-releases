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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.visualizers.GotoSourceActionProvider;
import org.netbeans.modules.dlight.visualizers.GotoSourceActionProvider.GotoSourceAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
public final class FunctionCallNode extends AbstractNode {

    private static final boolean useHtmlFormat = "true".equalsIgnoreCase(System.getProperty("FunctionsListViewVisualizer.usehtml", "true")); // NOI18N
    private final FunctionCallWithMetric functionCall;
    private final List<Column> metrics;
    private final PropertyChangeListener pcl;
    private final GotoSourceAction goToSourceAction;
    private String plainDisplayName = null;
    private String htmlDisplayName = null;
    private String functionName = null;

    public FunctionCallNode(GotoSourceActionProvider actionsProvider, FunctionCallWithMetric f, List<Column> metrics) {
        super(Children.LEAF);
        this.functionCall = f;
        this.metrics = metrics;

        // Listener on changes in action
        pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateNames();
                fireDisplayNameChange(null, getDisplayName());
            }
        };

        goToSourceAction = actionsProvider.getAction(functionCall);
        goToSourceAction.addPropertyChangeListener(pcl);

        updateNames();
    }

    @Override
    protected Sheet createSheet() {
        Sheet result = new Sheet();
        Sheet.Set set = new Sheet.Set();

        for (final Column metric : metrics) {
            @SuppressWarnings("unchecked")
            PropertySupport.ReadOnly property = new PropertySupport.ReadOnly(
                    metric.getColumnName(), metric.getColumnClass(),
                    metric.getColumnUName(), metric.getColumnLongUName()) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return !functionCall.hasMetric(metric.getColumnName())
                            ? NbBundle.getMessage(FunctionCallNode.class, "NotDefined") // NOI18N
                            : functionCall.getMetricValue(metric.getColumnName());
                }
            };

            set.put(property);
        }

        result.put(set);
        return result;
    }

    @Override
    public Image getIcon(int type) {
        return null;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return null;
    }

    @Override
    public synchronized Action getPreferredAction() {
        return goToSourceAction;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{goToSourceAction};
    }

    @Override
    public synchronized String getName() {
        return functionName;
    }

    @Override
    public String toString() {
        return getClass().getName().concat(": ").concat(getName()); // NOI18N
    }

    public synchronized GotoSourceAction getGoToSourceAction() {
        return goToSourceAction;
    }

    @Override
    public synchronized String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    @Override
    public synchronized String getDisplayName() {
        return useHtmlFormat ? htmlDisplayName : plainDisplayName;
    }

    private synchronized void updateNames() {
        plainDisplayName = functionCall.getDisplayedName();

        String name = functionCall.getFunction().getName();
        String funcName = functionCall.getFunction().getQuilifiedName();
        int idx1 = name.indexOf(funcName);

        int idx2 = funcName.lastIndexOf(':');
        if (idx2 > 0) {
            idx1 += idx2 + 1;
            funcName = funcName.substring(idx2 + 1);
        }

        this.functionName = funcName;

        String prefix = name.substring(0, idx1);
        String suffix = name.substring(idx1 + funcName.length());

        prefix = toHtml(prefix);
        funcName = toHtml(funcName);
        suffix = toHtml(suffix);
        funcName = "<b>" + funcName + "</b>"; // NOI18N

        String dispName = prefix + funcName + suffix + "&nbsp;"; // NOI18N

        final GotoSourceAction action = getGoToSourceAction();
        StringBuilder result = new StringBuilder("<html>"); // NOI18N

        String infoSuffix = null;

        if (action != null && action.isEnabled()) {
            SourceFileInfo sourceInfo = action.getSourceInfo();
            if (sourceInfo != null && sourceInfo.isSourceKnown()) {
                String fname = new File(sourceInfo.getFileName()).getName();
                int line = sourceInfo.getLine();

                if (line > 0) {
                    result.append("<font color='#000000'>").append(dispName).append("</font>"); // NOI18N
                } else {
                    result.append("<font color='#808080'>").append(dispName).append("</font>"); // NOI18N
                }

                String infoPrefix = line > 0
                        ? NbBundle.getMessage(FunctionCallNode.class, "FunctionCallNode.prefix.withLine") // NOI18N
                        : NbBundle.getMessage(FunctionCallNode.class, "FunctionCallNode.prefix.withoutLine"); // NOI18N

                infoSuffix = infoPrefix + "&nbsp;" + fname + (line > 0 ? ":" + line : ""); // NOI18N
                result.append("<font color='#808080'>").append(infoSuffix).append("</font>"); // NOI18N
            }
        } else {
            result.append("<font color='#808080'>").append(dispName).append("</font>"); // NOI18N
        }

        result.append("</html>"); // NOI18N

        htmlDisplayName = result.toString();
    }

    private String toHtml(String plain) {
        plain = plain.replace("&", "&amp;"); // NOI18N
        plain = plain.replace("<", "&lt;"); // NOI18N
        plain = plain.replace(">", "&gt;"); // NOI18N
        plain = plain.replace(" ", "&nbsp;"); // NOI18N
        return plain;
    }
}
