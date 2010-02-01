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

package org.netbeans.modules.cnd.makefile.navigator;

import javax.swing.Action;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.makefile.model.MakefileRule;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Alexey Vladykin
 */
public final class MakefileTargetNode extends AbstractNode {

    private final MakefileRule rule;

    public MakefileTargetNode(MakefileRule rule, String targetName) {
        super(Children.LEAF);
        this.rule = rule;
        setName(targetName);
        setIconBaseWithExtension("org/netbeans/modules/cnd/script/resources/TargetIcon.gif"); // NOI18N
        getCookieSet().add(new TargetOpenCookie());
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }

    @Override
    public SystemAction[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(PropertiesAction.class)
        };
    }

    private class TargetOpenCookie implements OpenCookie {

        public void open() {
            FileObject fobj = rule.getContainingFile();
            try {
                DataObject dobj = DataObject.find(fobj);
                EditorCookie editorCookie = dobj.getCookie(EditorCookie.class);
                LineCookie lineCookie = dobj.getCookie(LineCookie.class);
                if (editorCookie != null && lineCookie != null) {
                    StyledDocument doc = editorCookie.openDocument();
                    int line = Utilities.getLineOffset((BaseDocument) doc, rule.getStartOffset());
                    lineCookie.getLineSet().getCurrent(line).show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
