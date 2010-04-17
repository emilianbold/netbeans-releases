/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.css.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;

import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 * 
 * @author Marek Fukala
 */
public final class CssCaretAwareSourceTask extends ParserResultTask<CssParserResult> {

    private static final String CSS_MIMETYPE = "text/x-css"; //NOI18N

    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            String mimeType = snapshot.getMimeType();
            String sourceMimeType = snapshot.getSource().getMimeType();

            //allow to run only on .css files
            if(sourceMimeType.equals(CSS_MIMETYPE) && mimeType.equals(CSS_MIMETYPE)) { //NOI18N
                return Collections.singletonList(new CssCaretAwareSourceTask());
            } else {
                return Collections.EMPTY_LIST;
            }
        }
    }

//    private static final String SOURCE_DOCUMENT_PROPERTY_NAME = Source.class.getName();

//    public static synchronized Source forDocument(Document doc) {
//        Source source = (Source) doc.getProperty(SOURCE_DOCUMENT_PROPERTY_NAME);
//        if (source == null) {
//            source = new Source();
//            doc.putProperty(SOURCE_DOCUMENT_PROPERTY_NAME, source);
//        }
//        return source;
//    }

    @Override
    public int getPriority() {
        return 100; //todo use reasonable number
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        //xxx cancel???
    }

    @Override
    public void run(CssParserResult result, SchedulerEvent event) {
        //xxx: how come I can get null event here? parsing api bug?
        if(event == null) {
            return ;
        }

        if(!(event instanceof CursorMovedSchedulerEvent)) {
            return ;
        }

        int caretOffset = ((CursorMovedSchedulerEvent)event).getCaretOffset();

        SimpleNode root = result.root();
        if(root != null) {
            //find the rule scope and check if there is an error inside it
            SimpleNode leaf = SimpleNodeUtil.findDescendant(root, caretOffset);
            if(leaf != null) {
                SimpleNode ruleNode = leaf.kind() == CssParserTreeConstants.JJTSTYLERULE ?
                    leaf :
                    SimpleNodeUtil.getAncestorByType(leaf, CssParserTreeConstants.JJTSTYLERULE);
                if(ruleNode != null) {
                    //filter out warnings
                    List<? extends Error> errors = result.getDiagnostics();
                    for(Error e : errors) {

                        if(e.getSeverity() == Severity.ERROR) {
                            if(ruleNode.startOffset() <= e.getStartPosition() &&
                                    ruleNode.endOffset() >= e.getEndPosition()) {
                                //there is an error in the selected rule
                                CssEditorSupport.getDefault().parsedWithError(result);
                                return ;
                            }
                        }
                    }

                    //no errors found in the node
                    CssEditorSupport.getDefault().parsed(result, ((CursorMovedSchedulerEvent)event).getCaretOffset());
                    return ;
                }
            }
        }

        //some error
        CssEditorSupport.getDefault().parsedWithError(result);

    }

//    public static class Source {
//
//        private Vector<SourceListener> listeners = new Vector<SourceListener>();
//
//        protected void parsed(Result ci, SchedulerEvent event) {
//            //distribute to clients
//            for (SourceListener listener : listeners) {
//                listener.parsed(ci, event);
//            }
//        }
//
//        public void addChangeListener(SourceListener l) {
//            listeners.add(l);
//        }
//
//        public void removeChangeListener(SourceListener l) {
//            listeners.remove(l);
//        }
//    }
//
//    public static interface SourceListener {
//
//        public void parsed(Result info, SchedulerEvent event);
//    }
}

