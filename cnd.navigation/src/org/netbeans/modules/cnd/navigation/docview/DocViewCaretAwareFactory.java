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
package org.netbeans.modules.cnd.navigation.docview;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.model.tasks.CaretAwareCsmFileTaskFactory;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmDocProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory.class, position = 12)
public class DocViewCaretAwareFactory extends CaretAwareCsmFileTaskFactory {

    @Override
    protected PhaseRunner createTask(final FileObject fo) {
        return new PhaseRunner() {
            private final AtomicBoolean isCanceled = new AtomicBoolean(false);

            @Override
            public void run(Phase phase) {
                if (phase != Phase.PARSED) {
                    return;
                }
                isCanceled.set(false);
                if (!isDocViewActive()) {
                    return;
                }
                Document doc = CsmUtilities.getDocument(fo);
                if (doc == null) {
                    return;
                }
                if (isCanceled.get()) {
                    return;
                }
                CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
                if (csmFile == null) {
                    return;
                }
                if (isCanceled.get()) {
                    return;
                }
                updateDoc(doc, fo, csmFile, isCanceled);
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public void cancel() {
                isCanceled.set(true);
            }

            @Override
            public boolean isHighPriority() {
                return false;
            }

            @Override
            public String toString() {
                return "DocViewCaretAwareFactory runner"; //NOI18N
            }
        };
    }

    private static boolean isDocViewActive() {
        DocViewTopComponent instance = DocViewTopComponent.getInstance();
        return instance != null && instance.isActivated();
    }

    private static void updateDoc(Document doc, FileObject fo, CsmFile csmFile, AtomicBoolean isCanceled) {
        CsmReference ref = CsmReferenceResolver.getDefault().findReference(doc, CaretAwareCsmFileTaskFactory.getLastPosition(fo));
        if (ref == null) {
            return;
        }
        if (isCanceled.get()) {
            return;
        }
        CsmObject csmObject = ref.getReferencedObject();
        if (csmObject == null) {
            return;
        }
        if (isCanceled.get()) {
            return;
        }
        CsmDocProvider p = Lookup.getDefault().lookup(CsmDocProvider.class);
        if (p == null) {
            return;
        }
        CharSequence documentation = p.getDocumentation(csmObject, csmFile);
        if (documentation == null) {
            return;
        }
        if (isCanceled.get()) {
            return;
        }
        CharSequence selfDoc = CsmDisplayUtilities.getTooltipText(csmObject);
        if (selfDoc != null) {
            documentation = selfDoc.toString() + documentation.toString();
        }
        if (isCanceled.get()) {
            return;
        }
        final CharSequence toShow = documentation;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                DocViewTopComponent topComponent = DocViewTopComponent.findInstance();
                if (topComponent != null && topComponent.isOpened()) {
                    topComponent.setDoc(toShow);
                }
            }
        });
    }

    @Override
    protected int taskDelay() {
        return 500;
    }

    @Override
    protected int rescheduleDelay() {
        return 500;
    }
}
