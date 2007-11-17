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

package org.netbeans.editor;

import java.lang.ref.WeakReference;
import javax.swing.text.Position;

/**
* Various marks are located here
*
* @author Miloslav Metelka
* @version 1.00
*/

public class MarkFactory {

    private MarkFactory() {
        // no instantiation
    }

    /** Syntax mark holds info about scan state of syntax scanner.
     * This helps in redraws because reparsing after insert/delete is done
     * only from nearest left syntax mark. Moreover rescaning is done only
     * until there are marks with different scan state. As soon as mark
     * is found with same parsing info as rescanning scanner has, parsing
     * ends.
     * @deprecated syntax marks are no longer used to hold lexer states.
     */
    public static class SyntaxMark extends Mark {

        /** Syntax mark state info */
        private Syntax.StateInfo stateInfo;

        private TokenItem tokenItem;

        /** Get state info of this mark */
        public Syntax.StateInfo getStateInfo() {
            return stateInfo;
        }

        public void updateStateInfo(Syntax syntax) {
            if (stateInfo == null) {
                stateInfo = syntax.createStateInfo();
            }
            syntax.storeState(stateInfo);
        }

        void setStateInfo(Syntax.StateInfo stateInfo) {
          this.stateInfo = stateInfo;
        }

        public TokenItem getTokenItem() {
            return tokenItem;
        }

        void setTokenItem(TokenItem tokenItem) {
            this.tokenItem = tokenItem;
        }

        /** When removal occurs */
        protected void removeUpdateAction(int pos, int len) {
            try {
                remove();
            } catch (InvalidMarkException e) {
                // shouldn't happen
            }
        }

    }

    /** Mark that can have its position updated by where it's located */
    public static class ContextMark extends Mark {

        public ContextMark(boolean stayBOL) {
            this(false, stayBOL);
        }

        public ContextMark(boolean insertAfter, boolean stayBOL) {
            this(insertAfter ? Position.Bias.Backward : Position.Bias.Forward, stayBOL);
        }

        public ContextMark(Position.Bias bias, boolean stayBOL) {
            super(bias);
        }

    }

    /** Activation mark for particular layer. When layer is not active
    * its updateContext() method is not called.
    */
    public static class DrawMark extends ContextMark {

        /** Activation flag means either activate layer or deactivate it */
        protected boolean activateLayer;

        /** Reference to draw layer this mark belogns to */
        String layerName;

        /** Reference to extended UI if this draw mark is info-specific or
        * null if it's document-wide.
        */
        WeakReference editorUIRef;

        public DrawMark(String layerName, EditorUI editorUI) {
            this(layerName, editorUI, Position.Bias.Forward);
        }
        
        public DrawMark(String layerName, EditorUI editorUI, Position.Bias bias) {
            super(bias, false);
            this.layerName = layerName;
            setEditorUI(editorUI);
        }

        public boolean isDocumentMark() {
            return (editorUIRef == null);
        }

        public EditorUI getEditorUI() {
            if (editorUIRef != null) {
                return (EditorUI)editorUIRef.get();
            }
            return null;
        }

        public void setEditorUI(EditorUI editorUI) {
            this.editorUIRef = (editorUI != null) ? new WeakReference(editorUI) : null;
        }

        public boolean isValidUI() {
            return !(editorUIRef != null && editorUIRef.get() == null);
        }

        public void setActivateLayer(boolean activateLayer) {
            this.activateLayer = activateLayer;
        }

        public boolean getActivateLayer() {
            return activateLayer;
        }

        public boolean removeInvalid() {
            if (!isValidUI() && isValid()) {
                try {
                    this.remove();
                } catch (InvalidMarkException e) {
                    throw new IllegalStateException(e.toString());
                }
                return true; // invalid and removed
            }
            return false; // valid
        }

        public String toString() {
            try {
                return "pos=" + getOffset() + ", line=" + getLine(); // NOI18N
            } catch (InvalidMarkException e) {
                return "mark not valid"; // NOI18N
            }
        }

    }

    /** Support for draw marks chained in double linked list */
    public static class ChainDrawMark extends DrawMark {

        /** Next mark in chain */
        protected ChainDrawMark next;

        /** Previous mark in chain */
        protected ChainDrawMark prev;

        public ChainDrawMark(String layerName, EditorUI editorUI) {
            this(layerName, editorUI, Position.Bias.Forward);
        }
        
        public ChainDrawMark(String layerName, EditorUI editorUI, Position.Bias bias) {
            super(layerName, editorUI, bias);
        }

        public final ChainDrawMark getNext() {
            return next;
        }

        public final void setNext(ChainDrawMark mark) {
            next = mark;
        }

        /** Set next mark in chain */
        public void setNextChain(ChainDrawMark mark) {
            this.next = mark;
            if (mark != null) {
                mark.prev = this;
            }
        }

        public final ChainDrawMark getPrev() {
            return prev;
        }

        public final void setPrev(ChainDrawMark mark) {
            prev = mark;
        }

        /** Set previous mark in chain */
        public void setPrevChain(ChainDrawMark mark) {
            this.prev = mark;
            if (mark != null) {
                mark.next = this;
            }
        }

        /** Insert mark before this one in chain
        * @return inserted mark
        */
        public ChainDrawMark insertChain(ChainDrawMark mark) {
            ChainDrawMark thisPrev = this.prev;
            mark.prev = thisPrev;
            mark.next = this;
            if (thisPrev != null) {
                thisPrev.next = mark;
            }
            this.prev = mark;
            return mark;
        }

        /** Remove this mark from the chain
        * @return next chain member or null for end of chain
        */
        public ChainDrawMark removeChain() {
            ChainDrawMark thisNext = this.next;
            ChainDrawMark thisPrev = this.prev;
            if (thisPrev != null) { // not the first
                thisPrev.next = thisNext;
                this.prev = null;
            }
            if (thisNext != null) { // not the last
                thisNext.prev = thisPrev;
                this.next = null;
            }
            try {
                this.remove(); // remove the mark from DocMarks
            } catch (InvalidMarkException e) {
                // already removed
            }
            return thisNext;
        }

        public String toStringChain() {
            return toString() + (next != null ? "\n" + next.toStringChain() : ""); // NOI18N
        }

        public String toString() {
            return super.toString() + ", " // NOI18N
                   + ((prev != null) ? ((next != null) ? "chain member" // NOI18N
                            : "last member") : ((next != null) ? "first member" // NOI18N
                                                            : "standalone member")); // NOI18N
        }

    }
}
