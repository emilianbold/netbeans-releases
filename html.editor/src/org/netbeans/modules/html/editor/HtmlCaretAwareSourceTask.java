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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.html.editor;

import java.util.Vector;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source.Priority;
import org.openide.filesystems.FileObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.DataLoadersBridge;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.support.CaretAwareSourceTaskFactory;

/**
 * 
 * @author Marek Fukala
 */
public final class HtmlCaretAwareSourceTask implements CancellableTask<CompilationInfo> {

    private static final String SOURCE_DOCUMENT_PROPERTY_NAME = Source.class.getName();
    
    private FileObject file;

    HtmlCaretAwareSourceTask(FileObject file) {
        this.file = file;
    }

    public Document getDocument() {
        return DataLoadersBridge.getDefault().getDocument(file);
    }
    private boolean cancel;

    synchronized boolean isCanceled() {
        return cancel;
    }

    public synchronized void cancel() {
        cancel = true;
    }

    synchronized void resume() {
        cancel = false;
    }

    public void run(CompilationInfo info) {

        System.out.println("parsed; caret on pos " + CaretAwareSourceTaskFactory.getLastPosition(info.getFileObject()));

        resume();

        Document doc = getDocument();

        if (doc == null) {
            Logger.getLogger(HtmlCaretAwareSourceTask.class.getName()).log(Level.INFO, "Cannot get document!");
            return;
        }

        Source source = HtmlCaretAwareSourceTask.forDocument(doc);
        System.out.println("source = " + source);
        source.parsed(info);

    }

    public static class HtmlCaretAwareSourceTaskFactory extends CaretAwareSourceTaskFactory {

        public HtmlCaretAwareSourceTaskFactory() {
            super(Phase.PARSED, Priority.BELOW_NORMAL);
        }

        public CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new HtmlCaretAwareSourceTask(file);
        }
    }

    
    public static synchronized Source forDocument(Document doc) {
        Source source = (Source) doc.getProperty(SOURCE_DOCUMENT_PROPERTY_NAME);
        if (source == null) {
            source = new Source();
            doc.putProperty(SOURCE_DOCUMENT_PROPERTY_NAME, source);
        }
        return source;
    }

    public static class Source {

        private Vector<SourceListener> listeners = new Vector<SourceListener>();

        protected void parsed(CompilationInfo ci) {
            //distribute to clients
            for (SourceListener listener : listeners) {
                System.out.println("Source(" + this + ").parsed(" + listener + ")");
                listener.parsed(ci);
            }
        }

        public void addChangeListener(SourceListener l) {
            System.out.println("Source(" + this + ").addChangeListener(" + l + ")");
            listeners.add(l);
        }

        public void removeChangeListener(SourceListener l) {
            listeners.remove(l);
        }
        
    }

    public static interface SourceListener {

        public void parsed(CompilationInfo info);
    }
}

