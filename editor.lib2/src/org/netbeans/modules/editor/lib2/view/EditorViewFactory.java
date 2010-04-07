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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.util.ListenerList;

/**
 * SPI class allowing to produce views.
 * <br/>
 * There are two main factories: for folds and for highlights (the default one).
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

    private final JTextComponent component;

    private ListenerList<EditorViewFactoryListener> listenerList = new ListenerList<EditorViewFactoryListener>();

    protected EditorViewFactory(JTextComponent component) {
        assert (component != null) : "Null component prohibited"; // NOI18N
        this.component = component;
    }

    protected final JTextComponent textComponent() {
        return component;
    }

    /**
     * Init this factory by taking extra properties into account.
     *
     * This method gets called when {@link #getComponent()} returns a valid component already.
     *
     * @param properties non-null properties.
     */
    protected void initProperties(Map<String,Object> properties) {

    }

    /**
     * Restart this view factory to start producing views.
     *
     * @param startOffset first offset from which the views will be produced.
     */
    public abstract void restart(int startOffset);

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
     * the appropriate end offset of the produced view and create a position for it
     * and return it from {@link EditorView#getEndOffset()}.
     *
     * @param startOffset start offset at which the view must start
     *  (it was previously returned from {@link #nextViewStartOffset(int)}
     *   and {@link EditorView#getStartOffset()} must return it).
     * @param limitOffset suggested maximum end offset of the created view.
     *  The view factory may however return view with higher end offset if necessary
     *  - for example a collapsed fold view may insist on a fold's end offset.
     * @return non-null EditorView instance.
     */
    public abstract EditorView createView(int startOffset, int limitOffset);

    /**
     * Finish this round of views creation.
     * <br/>
     * {@link #restart(int) } may be called subsequently to init a new round
     * of views creation.
     */
    public abstract void finish();

    /**
     * Inform the view factory that the document insert occurred.
     *
     * @param evt non-null document event describing the modification.
     * @return change in case there is a potentially new view(s) to be created
     *  or destroyed as an effect of the modification. If there is no change
     *  (including the case when existing views' boundaries are updated according
     *   to positions movement) then <code>null</code> should be returned.
     */
    public abstract Change insertUpdate(DocumentEvent evt);

    /**
     * Inform the view factory that the document remove occurred.
     *
     * @param evt non-null document event describing the modification.
     * @return change in case there is a potentially new view(s) to be created
     *  or destroyed as an effect of the modification. If there is no change
     *  (including the case when existing views' boundaries are updated according
     *   to positions movement) then <code>null</code> should be returned.
     *  The change's end offset should be in the new coordinate space (reflecting the removed length).
     */
    public abstract Change removeUpdate(DocumentEvent evt);

    /**
     * Inform the view factory that the document change occurred.
     *
     * @param evt non-null document event describing the modification.
     * @return change in case there is a potentially new view(s) to be created
     *  or destroyed as an effect of the modification. If there is no change
     *  (including the case when existing views' boundaries are updated according
     *   to positions movement) then <code>null</code> should be returned.
     */
    public Change changedUpdate(DocumentEvent evt) {
        return null; // Suppose there are no updates from changedUpdate() by default
    }

    public void addEditorViewFactoryListener(EditorViewFactoryListener listener) {
        listenerList.add(listener);
    }

    public void removeEditorViewFactoryListener(EditorViewFactoryListener listener) {
        listenerList.remove(listener);
    }

    protected void fireEvent(List<Change> changes) {
        EditorViewFactoryEvent evt = new EditorViewFactoryEvent(this, changes);
        for (EditorViewFactoryListener listener : listenerList.getListeners()) {
            listener.viewFactoryChanged(evt);
        }
    }

    public Change createChange(int startOffset, int endOffset) {
        return new Change(startOffset, endOffset);
    }

    /**
     * Change that occurred in a view factory either due to insert/remove in a document
     * or due to some other cause.
     * For example when a fold gets collapsed the fold view factory fires an event with the change.
     */
    public static final class Change {

        private int startOffset;

        private int endOffset;

        public Change(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

    }

    /**
     * Factory for producing editor view factories.
     */
    public static interface Factory {

        EditorViewFactory createEditorViewFactory(JTextComponent component);

        /**
         * A higher importance factory wins when wishing to create view
         * in the same offset area.
         *
         * @return id &gt;0. A default factory for creating basic views has importance 0.
         */
        int importance();

    }

}
