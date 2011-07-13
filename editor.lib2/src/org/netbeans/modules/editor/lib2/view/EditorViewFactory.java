/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.editor.lib2.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.ListenerList;

/**
 * SPI class allowing to produce views.
 * <br/>
 * There are two main factories: for folds and for highlights (the default one).
 * The factories have a priority and factory with highest priority
 * will always "win" in terms that its view will surely be created as desired.
 * Factories at lower levels will receive a limitOffset into createView()
 * being a start offset of the view produced by a higher level factory.
 * The factory may decide whether it will create view with limiting offset or not.
 * <br/>
 * Factory generally operates in two modes:<ul>
 * <li>Regular mode when the factory produces views</li>
 * <li>Offset mode when the factory only returns bounds of the produced views
 *   but it does not create them. This helps the view hierarchy infrastructure
 *   to do estimates (e.g. how many lines a fold view will span etc.).</li>
 * </ul>
 *
 * @author Miloslav Metelka
 */

public abstract class EditorViewFactory {

    private static List<Factory> viewFactoryFactories = new ArrayList<Factory>(3);

    public static void registerFactory(Factory factory) {
        viewFactoryFactories.add(factory);
        Collections.sort(viewFactoryFactories, new Comparator<Factory>() {
            public int compare(Factory f0, Factory f1) {
                return f0.importance() - f1.importance();
            }
        });
    }

    public static List<Factory> factories() {
        return Collections.unmodifiableList(viewFactoryFactories);
    }
    
    private final DocumentView docView;
    
    private final JTextComponent component;

    private ViewBuilder viewBuilder;

    private final ListenerList<EditorViewFactoryListener> listenerList = new ListenerList<EditorViewFactoryListener>();

    protected EditorViewFactory(View documentView) {
        assert (documentView instanceof DocumentView) : "documentView=" + documentView + // NOI18N
                " is not instance of " + DocumentView.class.getName(); // NOI18N
        // Remember component explicitly (it may be null-ed in DocView.setParent(null))
        this.docView = (DocumentView) documentView;
        this.component = docView.getTextComponent();
    }

    /**
     * Text component for which this view factory was constructed.
     *
     * @return non-null text component.
     */
    protected final JTextComponent textComponent() {
        return component;
    }
    
    /**
     * Document for which this view factory was constructed.
     * <b>Note</b>: Do not use <code>textComponent().getDocument()</code> since
     * it may differ from <code>document()</code> result at certain points
     * and it could lead to incorrect behavior.
     *
     * @return non-null document for which the view hierarchy was constructed.
     */
    protected final Document document() {
        return docView.getDocument();
    }

    /**
     * Restart this view factory to start producing views.
     *
     * @param startOffset first offset from which the views will be produced.
     * @param matchOffset offset where the view creation should end (original views
     *  should match with the new views at that offset).
     *  However during the views creation it may be found out that this offset
     *  will be exceeded.
     */
    public abstract void restart(int startOffset, int matchOffset);

    /**
     * Return starting offset of the next view to be produced by this view factory.
     * <br/>
     * This method gets called after restarting of this view factory
     * (with a <code>startOffset</code> parameter passed to {@link #restart(int)})
     * and also after any of the registered view factories created a view
     * (with end offset of the created view).
     *
     * @param offset offset at which (or after which) a possible new view should be created.
     * @return start offset of the new view to be created or Integer.MAX_VALUE to indicate that
     *  no more views would be produced by this factory over the given offset.
     */
    public abstract int nextViewStartOffset(int offset);

    /**
     * Create a view at the given offset. The view factory must determine
     * the appropriate end offset of the produced view and set its length
     * returned by {@link EditorView#getLength()} appropriately.
     * <br/>
     * This method is only called if the factory is in view-producing mode
     * (its {@link #viewEndOffset(startOffset, limitOffset)} is not called).
     *
     * @param startOffset start offset at which the view must start
     *  (it was previously returned from {@link #nextViewStartOffset(int)} by this factory
     *   and {@link EditorView#getStartOffset()} must return it).
     * @param limitOffset maximum end offset that the created view can have.
     * @return EditorView instance or null if limitOffset is too limiting
     *  for the view that would be otherwise created.
     */
    public abstract EditorView createView(int startOffset, int limitOffset);

    /**
     * Return to-be-created view's end offset.
     * <br/>
     * This method is only called in offset-mode when only view boundaries
     * are being determined.
     *
     * @param startOffset start offset at which the view would start
     *  (it was previously returned from {@link #nextViewStartOffset(int)} by this factory).
     * @param limitOffset maximum end offset that the created view can have.
     * @param end offset of the view to be created or -1 if view's creation is refused by the factory.
     */
    public abstract int viewEndOffset(int startOffset, int limitOffset);

    /**
     * Finish this round of views creation.
     * <br/>
     * {@link #restart(int) } may be called subsequently to init a new round
     * of views creation.
     */
    public abstract void finishCreation();

    public void addEditorViewFactoryListener(EditorViewFactoryListener listener) {
        listenerList.add(listener);
    }

    public void removeEditorViewFactoryListener(EditorViewFactoryListener listener) {
        listenerList.remove(listener);
    }

    protected void fireEvent(List<Change> changes) {
        fireEvent(changes, 0);
    }

    protected void fireEvent(List<Change> changes, int priority) {
        EditorViewFactoryEvent evt = new EditorViewFactoryEvent(this, changes, priority);
        for (EditorViewFactoryListener listener : listenerList.getListeners()) {
            listener.viewFactoryChanged(evt);
        }
    }

    public static Change createChange(int startOffset, int endOffset) {
        return new Change(startOffset, endOffset);
    }

    public static List<Change> createSingleChange(int startOffset, int endOffset) {
        return Collections.singletonList(createChange(startOffset, endOffset));
    }

    /**
     * Schedule repaint request on the view hierarchy.
     * <br/>
     * Document must be read-locked prior calling this method.
     *
     * @param startOffset
     * @param endOffset 
     */
    public void offsetRepaint(int startOffset, int endOffset) {
        docView.offsetRepaint(startOffset, endOffset);
    }

    /**
     *  Signal that this view factory is no longer able to produce
     *  valid views due to some serious changes that it processes
     *  (for example highlights change for HighlightsViewFactory).
     *  <br/>
     *  View creation may be stopped immediately by the caller and restarted to get
     *  the correct views. However if it would fail periodically the caller may decide
     *  to continue the creation to have at least some views. In both cases
     *  the view factory should be able to continue working normally.
     *  <br/>
     *  This method can be called from any thread.
     */
    protected final void notifyStaleCreation() {
        if (viewBuilder != null) {
            viewBuilder.notifyStaleCreation();
        }
    }
    
    void setViewBuilder(ViewBuilder viewBuilder) {
        this.viewBuilder = viewBuilder;
    }

    /**
     * Change that occurred in a view factory either due to insert/remove in a document
     * or due to some other cause.
     * For example when a fold gets collapsed the fold view factory fires an event with the change.
     */
    public static final class Change {

        private int startOffset;

        private int endOffset;

        Change(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public String toString() {
            return "<" + getStartOffset() + "," + getEndOffset() + ">";
        }

    }

    /**
     * Factory for producing editor view factories.
     */
    public static interface Factory {

        EditorViewFactory createEditorViewFactory(View documentView);

        /**
         * A higher importance factory wins when wishing to create view
         * in the same offset area.
         *
         * @return id &gt;0. A default factory for creating basic views has importance 0.
         */
        int importance();

    }

}
