/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.spring.beans.refactoring;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.WriteContext;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;

/**
 *
 * @author Andrei Badea
 */
public class JavaElementRefFinder {

    private final WriteContext context;
    private final XMLSyntaxSupport syntaxSupport;

    public JavaElementRefFinder(WriteContext context) {
        this.context = context;
        BaseDocument document = (BaseDocument)context.getDocument();
        syntaxSupport = (XMLSyntaxSupport)document.getSyntaxSupport();
    }

    public void addOccurrences(Matcher matcher, List<Occurrence> result) throws BadLocationException {
        List<SpringBean> beans = context.getSpringBeans().getBeans(context.getFile());
        for (SpringBean bean : beans) {
            String className = bean.getClassName();
            if (className != null) {
                String matched = matcher.accept(className);
                if (matched == null) {
                    continue;
                }
                Occurrence occurrence = createOccurrence(matched, bean, context.getFileObject());
                if (occurrence != null) {
                    result.add(occurrence);
                }
            }
        }
    }

    private Occurrence createOccurrence(String matched, SpringBean bean, FileObject fo) throws BadLocationException {
        Location loc = bean.getLocation();
        if (loc == null) {
            return null;
        }
        int startOffset = loc.getOffset();
        if (startOffset == -1) {
            return null;
        }
        AttributeValueFinder finder = new AttributeValueFinder(syntaxSupport, "class", startOffset); // NOI18N
        if (!finder.find()) {
            return null;
        }
        int foundOffset = finder.getFoundOffset();
        String foundValue = finder.getFoundValue();
        int index = foundValue.indexOf(matched);
        if (index == -1) {
            return null;
        }
        PositionRef startRef = context.createPositionRef(foundOffset + index, Bias.Forward);
        PositionRef endRef = context.createPositionRef(foundOffset + index + matched.length(), Bias.Backward);
        return new JavaElementRefOccurrence(matched, fo, new PositionBounds(startRef, endRef));
    }

    public static interface Matcher {

        String accept(String beanClassName);
    }

    private static final class JavaElementRefOccurrence extends Occurrence {

        private final String className;

        JavaElementRefOccurrence(String className, FileObject fo, PositionBounds bounds) {
            super(fo, bounds);
            this.className = className;
        }

        @Override
        public String getDisplayText() {
            StringBuffer stringBuffer = new StringBuffer();
            // XXX temporary.
            stringBuffer.append("&lt;bean class=" + className);
            return stringBuffer.toString();
        }
    }
}
