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

package org.netbeans.modules.cnd.navigation.overrides;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 */
public class OverrideAnnotationAction extends AbstractAction {

    private static final Set<String> COMBINED_TYPES = new HashSet<String>(Arrays.asList(
            "org-netbeans-modules-cnd-navigation-is_overridden_combined", // NOI18N
            "org-netbeans-modules-cnd-navigation-is_overridden_combined_pseudo", // NOI18N
            "org-netbeans-modules-cnd-navigation-is_overridden_pseudo", // NOI18N
            "org-netbeans-modules-cnd-navigation-overrides_pseudo" // NOI18N
    ));

    public OverrideAnnotationAction() {
        putValue(NAME, NbBundle.getMessage(OverrideAnnotationAction.class,
                                          "CTL_IsOverriddenAnnotationAction")); //NOI18N
        putValue("supported-annotation-types", new String[] { // NOI18N
            "org-netbeans-modules-cnd-navigation-is_overridden", // NOI18N
            "org-netbeans-modules-cnd-navigation-is_overridden_pseudo", // NOI18N
            "org-netbeans-modules-cnd-navigation-overrides", // NOI18N
            "org-netbeans-modules-cnd-navigation-overrides_pseudo", // NOI18N
            "org-netbeans-modules-cnd-navigation-is_overridden_combined", //NOI18N
            "org-netbeans-modules-cnd-navigation-is_overridden_combined_pseudo", //NOI18N
            "org-netbeans-modules-cnd-navigation-specializes", // NOI18N
            "org-netbeans-modules-cnd-navigation-is_specialized", // NOI18N
            "org-netbeans-modules-cnd-navigation-extended_specializes", // NOI18N
            "org-netbeans-modules-cnd-navigation-extended_is_specialized" // NOI18N
        });
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!invokeDefaultAction((JTextComponent) e.getSource())) {
            // sorry, don't know how to do without deprecated ImplementationProvider
            @SuppressWarnings("deprecation")
            Action actions[] = org.netbeans.editor.ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) e.getSource());
            if (actions == null) {
                return ;
            }
            int nextAction = 0;
            while (nextAction < actions.length && actions[nextAction] != this) {
                nextAction++;
            }
            nextAction++;
            if (actions.length > nextAction) {
                Action a = actions[nextAction];
                if (a!=null && a.isEnabled()){
                    a.actionPerformed(e);
                }
            }
        }
    }

    boolean invokeDefaultAction(final JTextComponent comp) {
        final Document doc = comp.getDocument();
        if (doc instanceof BaseDocument) {
            final int currentPosition = comp.getCaretPosition();
            final Annotations annotations = ((BaseDocument) doc).getAnnotations();
            //final Map<String, List<ElementDescription>> caption2Descriptions = new LinkedHashMap<String, List<ElementDescription>>();
            final AtomicReference<Point> point = new AtomicReference<Point>();
            final AtomicReference<BaseAnnotation> anno = new AtomicReference<BaseAnnotation>();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    try {
                        int line = LineDocumentUtils.getLineIndex((BaseDocument) doc, currentPosition);
                        int startOffset = LineDocumentUtils.getLineStartFromIndex((BaseDocument) doc, line);
                        int endOffset = LineDocumentUtils.getLineEnd((BaseDocument) doc, startOffset);
                        AnnotationDesc desc = annotations.getActiveAnnotation(line);
                        if (desc == null) {
                            return ;
                        }
                        Collection<BaseAnnotation> annos;

                        if (COMBINED_TYPES.contains(desc.getAnnotationType())) {
                            annos = findAnnotations(comp, startOffset, endOffset);
                        } else {
                            annos = Collections.singletonList(findAnnotation(comp, desc, startOffset, endOffset));
                        }

                        for (BaseAnnotation a : annos) {
                            if (a != null) {
                                anno.set(a);
                                point.set(comp.modelToView(startOffset).getLocation());
                                break;
                            }
                        }
                    }  catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            if (anno.get() != null) {
                anno.get().mouseClicked(comp, point.get());
                return true;
            }
        }
        return false;
    }

    private List<BaseAnnotation> findAnnotations(JTextComponent component, int startOffset, int endOffset) {
        DataObject dao = getDataObject(component);
        if (dao == null) {
            if (ErrorManager.getDefault().isLoggable(ErrorManager.WARNING)) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "component=" + component + " does not have a file specified in the document."); //NOI18N
            }
            return null;
        }

        AnnotationsHolder ah = AnnotationsHolder.get(dao);
        if (ah == null) {
            BaseAnnotation.LOGGER.log(Level.INFO, "component={0} does not have attached AnnotationsHolder", component); //NOI18N
            return null;
        }

        List<BaseAnnotation> annotations = new LinkedList<BaseAnnotation>();
        for(BaseAnnotation a : ah.getAttachedAnnotations()) {
            int offset = a.getPosition().getOffset();
            if (startOffset <= offset && offset <= endOffset) {
                annotations.add(a);
            }
        }
        return annotations;
    }

    private BaseAnnotation findAnnotation(JTextComponent component, AnnotationDesc desc, int startOffset, int endOffset) {
        DataObject dao = getDataObject(component);
        if (dao == null) {
            if (ErrorManager.getDefault().isLoggable(ErrorManager.WARNING)) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "component=" + component + " does not have a file specified in the document."); //NOI18N
            }
            return null;
        }

        AnnotationsHolder ah = AnnotationsHolder.get(dao);
        if (ah == null) {
            BaseAnnotation.LOGGER.log(Level.INFO, "component={0} does not have attached a IsOverriddenAnnotationHandler", component); //NOI18N
            return null;
        }

        for(BaseAnnotation a : ah.getAttachedAnnotations()) {
            int offset = a.getPosition().getOffset();
            if (startOffset <= offset && offset <= endOffset) {
                if (desc.getShortDescription().equals(a.getShortDescription())) {
                    return a;
                }
            }
        }
        return null;
    }

    private DataObject getDataObject(JTextComponent component) {
        Document doc = component.getDocument();
        return (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
    }


}
