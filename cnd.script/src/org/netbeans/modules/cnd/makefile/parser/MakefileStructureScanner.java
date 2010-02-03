/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makefile.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.netbeans.modules.cnd.makefile.model.AbstractMakefileElement;
import org.netbeans.modules.cnd.makefile.model.MakefileRule;
import org.netbeans.modules.cnd.makefile.model.MakefileUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Alexey Vladykin
 */
public final class MakefileStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(ParserResult parseResult) {
        if (parseResult instanceof MakefileParseResult) {
            MakefileParseResult makefileParseResult = (MakefileParseResult) parseResult;
            List<MakefileRuleItem> list = new ArrayList<MakefileRuleItem>();
            for (AbstractMakefileElement element : makefileParseResult.getElements()) {
                if (element.getKind() == ElementKind.RULE) {
                    MakefileRule rule = (MakefileRule) element;
                    list.add(new MakefileRuleItem(rule, makefileParseResult));
                }
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        return Collections.emptyMap();
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }


    private static class MakefileRuleItem implements StructureItem {

        private static ImageIcon ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/script/resources/TargetIcon.gif", false); // NOI18N
        private final MakefileRule rule;
        private final MakefileParseResult parseResult;

        public MakefileRuleItem(MakefileRule rule, MakefileParseResult parseResult) {
            this.rule = rule;
            this.parseResult = parseResult;
        }

        @Override
        public String getName() {
            return rule.getName();
        }

        @Override
        public String getSortText() {
            return rule.getName();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            final String name = getName();
            final boolean bold = MakefileUtils.isPreferredTarget(name);
            final boolean shaded = !MakefileUtils.isRunnableTarget(name);
            if (bold) {
                formatter.emphasis(true);
            }
            if (shaded) {
                formatter.appendHtml("<font color=\""); // NOI18N
                formatter.appendHtml(getShadedColor());
                formatter.appendHtml("\">"); // NOI18N
            }
            formatter.appendText(getName());
            if (shaded) {
                formatter.appendHtml("</font>"); // NOI18N
            }
            if (bold) {
                formatter.emphasis(false);
            }
            return formatter.getText();
        }

        @Override
        public ElementHandle getElementHandle() {
            return rule;
        }

        @Override
        public ElementKind getKind() {
            return rule.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return rule.getModifiers();
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return Collections.emptyList();
        }

        @Override
        public long getPosition() {
            return rule.getOffsetRange(parseResult).getStart();
        }

        @Override
        public long getEndPosition() {
            return rule.getOffsetRange(parseResult).getEnd();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return ICON;
        }
    }

    // Copied from org.apache.tools.ant.module.nodes.AntTargetNode
    // Looks like there is some wisdom behind
    private static String shadedColor;
    private static synchronized String getShadedColor() {
        if (shadedColor == null) {
            if (UIManager.getDefaults().getColor("Tree.selectionBackground").equals(UIManager.getDefaults().getColor("controlShadow"))) { // NOI18N
                shadedColor = "!Tree.selectionBorderColor"; // NOI18N
            } else {
                shadedColor = "!controlShadow"; // NOI18N
            }
        }
        return shadedColor;
    }
}
