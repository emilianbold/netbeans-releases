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
package org.netbeans.modules.css.visual;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.css.visual.actions.OpenLocationAction;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a style sheet.
 *
 * @author Jan Stola
 */
public class StyleSheetNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/css/visual/resources/style_sheet_16.png"; // NOI18N
    
    private final FileObject styleSheet;

    /**
     * Creates a new {@code StyleSheetNode}.
     *
     * @param project owning project of the inspected page.
     * @param css CSS domain of the corresponding WebKit debugging.
     * @param header header of the represented stylesheet.
     * @param filter filter for the subtree of the node.
     */
    StyleSheetNode(DocumentViewModel model, FileObject stylesheet, Filter filter) {
        super(Children.create(new StyleSheetChildFactory(model, stylesheet, filter), true), Lookups.fixed(new Location(stylesheet)));
        this.styleSheet = stylesheet;
        updateDisplayName();
        setIconBaseWithExtension(ICON_BASE);
    }

    /**
     * Updates the display name of the node.
     */
    private void updateDisplayName() {
        String path;
        FileObject webRoot = ProjectWebRootQuery.getWebRoot(styleSheet);
        if(webRoot == null) {
            path = styleSheet.getNameExt();
        } else {
            path = FileUtil.getRelativePath(webRoot, styleSheet);
        }
        setDisplayName(path);
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenLocationAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenLocationAction.class)
        };
    }

    /**
     * Factory for children of {@code StyleSheetNode}.
     */
    static class StyleSheetChildFactory extends ChildFactory<RuleHandle> {
        
        private DocumentViewModel model;
        private FileObject stylesheet;
        
        /** Filter of the subtree of the node. */
        private Filter filter;

        StyleSheetChildFactory(DocumentViewModel model, FileObject stylesheet, Filter filter) {
            this.model = model;
            this.stylesheet = stylesheet;
            this.filter = filter;
            filter.addPropertyChangeListener(createListener());
        }

        /**
         * Creates a property change listener on the changes of the filter.
         *
         * @return property change listener on the changes of the filter.
         */
        private PropertyChangeListener createListener() {
            return new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String propertyName = evt.getPropertyName();
                    if (Filter.PROPERTY_PATTERN.equals(propertyName)) {
                        refresh(false);
                    }
                }
            };
        }

        @Override
        protected boolean createKeys(List<RuleHandle> toPopulate) {
            for(RuleHandle handle : model.getFilesToRulesMap().get(stylesheet)) {
                if(includeKey(handle)) {
                    toPopulate.add(handle);
                }
            }
            return true;
        }

        /**
         * Determines whether the specified rule should be included among keys.
         *
         * @param rule rule to check.
         * @return {@code true} when the rule should be included among keys,
         * returns {@code false} otherwise.
         */
        private boolean includeKey(RuleHandle rule) {
            boolean include = true;
            String pattern = filter.getPattern();
            if (pattern != null) {
                String selector = rule.getDisplayName();
                include = (selector.indexOf(pattern) != -1);
            }
            return include;
        }
        
        @Override
        protected Node createNodeForKey(RuleHandle key) {
            return new RuleNode(key);
        }

    }

}
