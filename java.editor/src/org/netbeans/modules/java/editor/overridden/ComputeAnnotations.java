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
package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.Tree;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class ComputeAnnotations extends JavaParserResultTask<Result> {

    private final AtomicBoolean cancel = new AtomicBoolean();
    
    public ComputeAnnotations() {
        super(Phase.RESOLVED);
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        cancel.set(false);
        
        CompilationInfo info = CompilationInfo.get(result);

        if (info.getChangedTree() != null) {
            //XXX: currently only method bodies are rebuilt.
            return ;
        }
        
        long start = System.currentTimeMillis();
        StyledDocument doc = (StyledDocument) result.getSnapshot().getSource().getDocument(false);

        if (doc == null) {
            return ;
        }
        
        List<IsOverriddenAnnotation> annotations = computeAnnotations(info, doc);

        if (cancel.get()) return ;
        
        AnnotationsHolder.get(info.getFileObject()).setNewAnnotations(annotations);

        long end = System.currentTimeMillis();

        Logger.getLogger("TIMER").log(Level.FINE, "Is Overridden Annotations", new Object[] {info.getFileObject(), end - start});
    }

    List<IsOverriddenAnnotation> computeAnnotations(CompilationInfo info, StyledDocument doc) {
        List<IsOverriddenAnnotation> annotations = new LinkedList<IsOverriddenAnnotation>();

        createAnnotations(info, doc, new ComputeOverriding(cancel).process(info), false, annotations);
        createAnnotations(info, doc, new ComputeOverriders(cancel).process(info, null, null, false), true, annotations);
        
        return annotations;
    }

    private void createAnnotations(CompilationInfo info, StyledDocument doc, Map<ElementHandle<? extends Element>, List<ElementDescription>> descriptions, boolean overridden, List<IsOverriddenAnnotation> annotations) {
        if (descriptions != null) {
            for (Entry<ElementHandle<? extends Element>, List<ElementDescription>> e : descriptions.entrySet()) {
                Element ee = e.getKey().resolve(info);
                Tree t = info.getTrees().getTree(ee);

                if (t == null) {
                    //XXX: log
                    continue;
                }

                AnnotationType type;
                String dn;

                if (overridden) {
                    if (ee.getEnclosingElement().getKind().isInterface() || ee.getKind().isInterface()) {
                        type = AnnotationType.HAS_IMPLEMENTATION;
                        dn = NbBundle.getMessage(ComputeAnnotations.class, "TP_HasImplementations");
                    } else {
                        type = AnnotationType.IS_OVERRIDDEN;
                        dn = NbBundle.getMessage(ComputeAnnotations.class, "TP_IsOverridden");
                    }
                } else {
                    StringBuffer tooltip = new StringBuffer();
                    boolean wasOverrides = false;

                    boolean newline = false;

                    for (ElementDescription ed : e.getValue()) {
                        if (newline) {
                            tooltip.append("\n"); //NOI18N
                        }

                        newline = true;

                        if (ed.getModifiers().contains(Modifier.ABSTRACT)) {
                            tooltip.append(NbBundle.getMessage(ComputeAnnotations.class, "TP_Implements", ed.getDisplayName()));
                        } else {
                            tooltip.append(NbBundle.getMessage(ComputeAnnotations.class, "TP_Overrides", ed.getDisplayName()));
                            wasOverrides = true;
                        }
                    }
                    
                    if (wasOverrides) {
                        type = AnnotationType.OVERRIDES;
                    } else {
                        type = AnnotationType.IMPLEMENTS;
                    }

                    dn = tooltip.toString();
                }

                Position pos = getPosition(doc, (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t));

                if (pos == null) {
                    //#179304: possibly the position is outside document bounds (i.e. <0 or >doc.getLenght())
                    continue;
                }
                
                annotations.add(new IsOverriddenAnnotation(doc, pos, type, dn, e.getValue()));
            }
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancel.set(true);
    }
    
    private static Position getPosition(final StyledDocument doc, final int offset) {
        class Impl implements Runnable {
            private Position pos;
            public void run() {
                if (offset < 0 || offset >= doc.getLength())
                    return ;

                try {
                    pos = doc.createPosition(offset - NbDocument.findLineColumn(doc, offset));
                } catch (BadLocationException ex) {
                    //should not happen?
                    Logger.getLogger(ComputeAnnotations.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        Impl i = new Impl();

        doc.render(i);

        return i.pos;
    }

    public static final class FactoryImpl extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new ComputeAnnotations());
        }
        
    }
}
