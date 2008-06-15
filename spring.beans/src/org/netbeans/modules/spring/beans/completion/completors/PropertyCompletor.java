/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.QueryProgress;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.MatchType;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;
import org.netbeans.modules.xml.text.syntax.dom.Tag;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PropertyCompletor extends Completor {

    public PropertyCompletor() {
    }

    @Override
    protected void computeCompletionItems(final CompletionContext context, QueryProgress progress) throws IOException {
        final String propertyPrefix = context.getTypedPrefix();
        final JavaSource js = JavaUtils.getJavaSource(context.getFileObject());
        if (js == null) {
            return;
        }

        // traverse the properties
        final int dotIndex = propertyPrefix.lastIndexOf("."); // NOI18N

        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws Exception {
                Tag beanTag = (Tag) SpringXMLConfigEditorUtils.getBean(context.getTag());
                String className = new BeanClassFinder(
                        SpringXMLConfigEditorUtils.getTagAttributes(beanTag),
                        context.getFileObject()).findImplementationClass();
                if (className == null) {
                    return;
                }
                TypeElement te = JavaUtils.findClassElementByBinaryName(className, cc);
                if (te == null) {
                    return;
                }
                TypeMirror startType = te.asType();
                ElementUtilities eu = cc.getElementUtilities();

                // property chain
                if (dotIndex != -1) {
                    String getterChain = propertyPrefix.substring(0, dotIndex);
                    StringTokenizer tokenizer = new StringTokenizer(getterChain, "."); // NOI18N

                    while (tokenizer.hasMoreTokens() && startType != null) {
                        String propertyName = tokenizer.nextToken();
                        Property[] props = new PropertyFinder(startType, propertyName, eu, MatchType.PREFIX).findProperties();

                        // no matching element found
                        if (props.length == 0 || props[0].getGetter() == null) {
                            startType = null;
                            break;
                        }

                        TypeMirror retType = props[0].getGetter().getReturnType();
                        if (retType.getKind() == TypeKind.DECLARED) {
                            startType = retType;
                        } else {
                            startType = null;
                        }
                    }
                }

                if (startType == null) {
                    return;
                }

                String setterPrefix = "";
                if (dotIndex != propertyPrefix.length() - 1) {
                    setterPrefix = propertyPrefix.substring(dotIndex + 1);
                }

                Property[] props = new PropertyFinder(startType, setterPrefix, eu, MatchType.PREFIX).findProperties();
                int substitutionOffset = context.getCurrentToken().getOffset() + 1;
                if (dotIndex != -1) {
                    substitutionOffset += dotIndex + 1;
                }

                for (Property prop : props) {
                    if (prop.getSetter() == null) {
                        continue;
                    }
                    addItem(SpringXMLConfigCompletionItem.createPropertyItem(substitutionOffset, prop));
                }

                setAnchorOffset(substitutionOffset);
            }
        }, false);
    }
}
