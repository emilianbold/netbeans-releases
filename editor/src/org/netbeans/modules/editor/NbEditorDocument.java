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

package org.netbeans.modules.editor;

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.AttributedCharacterIterator;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.PrintContainer;
import org.netbeans.editor.Utilities;
import org.openide.text.NbDocument;
import org.openide.text.AttributedCharacters;
import javax.swing.text.Position;
import org.openide.text.Annotation;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Dictionary;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.modules.editor.lib.SettingsConversions;

/**
* BaseDocument extension managing the readonly blocks of text
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorDocument extends GuardedDocument
implements NbDocument.PositionBiasable, NbDocument.WriteLockable,
NbDocument.Printable, NbDocument.CustomEditor, NbDocument.CustomToolbar, NbDocument.Annotatable {

    /** Indent engine for the given kitClass. */
    public static final String INDENT_ENGINE = "indentEngine"; // NOI18N

    /** Map of [Annotation, AnnotationDesc] */
    private HashMap annoMap;

    // #39718 hotfix
    private WeakHashMap annoBlackList;

    /**
     * Creates a new document.
     * 
     * @deprecated Use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
     */
    public NbEditorDocument(Class kitClass) {
        super(kitClass);
        init();
    }
    
    /**
     * Creates a new document.
     * 
     * @param mimeType The mime type for the new document.
     * 
     * @since 1.18
     */
    public NbEditorDocument(String mimeType) {
        super(mimeType);
        init();
    }
    
    private void init() {
        addStyleToLayerMapping(NbDocument.BREAKPOINT_STYLE_NAME,
                               NbDocument.BREAKPOINT_STYLE_NAME + "Layer:10"); // NOI18N
        addStyleToLayerMapping(NbDocument.ERROR_STYLE_NAME,
                               NbDocument.ERROR_STYLE_NAME + "Layer:20"); // NOI18N
        addStyleToLayerMapping(NbDocument.CURRENT_STYLE_NAME,
                               NbDocument.CURRENT_STYLE_NAME + "Layer:30"); // NOI18N
        setNormalStyleName(NbDocument.NORMAL_STYLE_NAME);
        
        annoMap = new HashMap(20);
        annoBlackList = new WeakHashMap();
        
        // Fill in the indentEngine property
        putProperty(INDENT_ENGINE, new BaseDocument.PropertyEvaluator() {
            public Object getValue() {
                MimePath mimePath = MimePath.parse((String) getProperty(MIME_TYPE_PROP));
                Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                String factoryRef = prefs.get(INDENT_ENGINE, null);
                if (factoryRef != null) {
                    return SettingsConversions.callFactory(factoryRef, mimePath);
                } else {
                    return null;
                }
            }
        });
    }

    public @Override void setCharacterAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        if (s != null) {
            Object val = s.getAttribute(NbDocument.GUARDED);
            if (val != null && val instanceof Boolean) {
                if (((Boolean)val).booleanValue() == true) { // want make guarded
                    super.setCharacterAttributes(offset, length, guardedSet, replace);
                } else { // want make unguarded
                    super.setCharacterAttributes(offset, length, unguardedSet, replace);
                }
            } else { // not special values, just pass
                super.setCharacterAttributes(offset, length, s, replace);
            }
        }
    }

    public java.text.AttributedCharacterIterator[] createPrintIterators() {
        NbPrintContainer npc = new NbPrintContainer();
        print(npc);
        return npc.getIterators();
    }

    public Component createEditor(JEditorPane j) {
        return Utilities.getEditorUI(j).getExtComponent();
    }

    public JToolBar createToolbar(JEditorPane j) {
        return Utilities.getEditorUI(j).getToolBarComponent();
    }
    
    /** Add annotation to the document. For annotation of whole line
     * the length parameter can be ignored (specify value -1).
     * @param startPos position which represent begining 
     * of the annotated text
     * @param length length of the annotated text. If -1 is specified 
     * the whole line will be annotated
     * @param annotation annotation which is attached to this text */
    public void addAnnotation(Position startPos, int length, Annotation annotation) {
        Integer count = (Integer)annoBlackList.get(annotation);
        if (count != null) {
            // #39718 hotfix - test whether the annotation was already removed; if so, just remove it from the black list and return
            if (count.intValue() == -1) {
                annoBlackList.remove(annotation);
                return;
            } else if (count.intValue() < -1) {
                annoBlackList.put(annotation, new Integer(count.intValue() + 1));
                return;
            }
        }
        // partial fix of #33165 - read-locking of the document added
        // BTW should only be invoked in EQ - see NbDocument.addAnnotation()
        readLock();
        try {
            // Recreate annotation's position to make sure it's in this doc at a valid offset
            int docLen = getLength();
            int offset = startPos.getOffset();
            offset = Math.min(offset, docLen);
            try {
                startPos = createPosition(offset);
            } catch (BadLocationException e) {
                startPos = null; // should never happen
            }
            
            AnnotationDescDelegate a = (AnnotationDescDelegate)annoMap.get(annotation);
            if (a != null) { // already added before
                // #39718 hotfix - remove the original annotation descriptor and put the annotation on the black list
                a.detachListeners();
                getAnnotations().removeAnnotation(a);
                annoMap.remove(annotation);
                annoBlackList.put(annotation, new Integer(count != null ? count.intValue() + 1 : 1));
            }
            if (annotation.getAnnotationType() != null) {
                a = new AnnotationDescDelegate(this, startPos, length, annotation);
                annoMap.put(annotation, a);
                getAnnotations().addAnnotation(a);
            }
        } finally {
            readUnlock();
        }
    }

    /** Removal of added annotation.
     * @param annotation annotation which is going to be removed */
    public void removeAnnotation(Annotation annotation) {
        if (annotation == null) { // issue 14803
            return; // can't do more as the rest of stacktrace is in openide and ant
        }

        Integer count = (Integer)annoBlackList.get(annotation);
        if (count != null) {
            // #39718 hotfix - test whether the annotation was already removed; if so, just remove it from the black list and return
            if (count.intValue() == 1) {
                annoBlackList.remove(annotation);
                return;
            } else if (count.intValue() > 1) {
                annoBlackList.put(annotation, new Integer(count.intValue() - 1));
                return;
            }
        }
        // partial fix of #33165 - read-locking of the document added
        // BTW should only be invoked in EQ - see NbDocument.removeAnnotation()
        readLock();
        try {
            if (annotation.getAnnotationType() != null) {
                AnnotationDescDelegate a = (AnnotationDescDelegate)annoMap.get(annotation);
                if (a == null) { // not added yet
                    // #39718 hotfix - put the annotation on the black list and return
                    annoBlackList.put(annotation, new Integer(count != null ? count.intValue() - 1 : -1));
                    return;
                }
                a.detachListeners();
                getAnnotations().removeAnnotation(a);
                annoMap.remove(annotation);
            }
        } finally {
            readUnlock();
        }
    }
    
    Map getAnnoMap(){
        return annoMap;
    }

    void addStreamDescriptionChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    
    void removeStreamDescriptionChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }
    
    private void fireStreamDescriptionChange() {
        ChangeEvent evt = new ChangeEvent(this);
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener)listeners[i + 1]).stateChanged(evt);
            }
        }
    }

    protected @Override Dictionary createDocumentProperties(Dictionary origDocumentProperties) {
        return new LazyPropertyMap(origDocumentProperties) {
            public @Override Object put(Object key, Object value) {
                Object origValue = super.put(key, value);
                if (Document.StreamDescriptionProperty.equals(key)) {
                    if (origValue == null || !origValue.equals(value)) {
                        fireStreamDescriptionChange();
                    }
                }
                
                return origValue;
            }
        };
    }

    /** Implementation of AnnotationDesc, which delegate to Annotation instance
     * defined in org.openide.text package.
     */
    static class AnnotationDescDelegate extends AnnotationDesc {
        
        private Annotation delegate;
        private PropertyChangeListener l;
        private Position pos;
        private BaseDocument doc;
        
        AnnotationDescDelegate(BaseDocument doc, Position pos, int length, Annotation anno) {
            super(pos.getOffset(),length);
            this.pos = pos;
            this.delegate = anno;
            this.doc = doc;
            
            // update AnnotationDesc.type member
            updateAnnotationType();
            
            // forward property changes to AnnotationDesc property changes
            l = new PropertyChangeListener() {
                public void propertyChange (PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == null || Annotation.PROP_SHORT_DESCRIPTION.equals(evt.getPropertyName())) {
                        firePropertyChange(AnnotationDesc.PROP_SHORT_DESCRIPTION, null, null);
                    }
                    if (evt.getPropertyName() == null || Annotation.PROP_MOVE_TO_FRONT.equals(evt.getPropertyName())) {
                        firePropertyChange(AnnotationDesc.PROP_MOVE_TO_FRONT, null, null);
                    }
                    if (evt.getPropertyName() == null || Annotation.PROP_ANNOTATION_TYPE.equals(evt.getPropertyName())) {
                        updateAnnotationType();
                        firePropertyChange(AnnotationDesc.PROP_ANNOTATION_TYPE, null, null);
                    }
                }
            };
            delegate.addPropertyChangeListener(l);
        }
        
        public String getAnnotationType() {
            return delegate.getAnnotationType();
        }
        
        public String getShortDescription() {
            return delegate.getShortDescription();
        }
        
        void detachListeners() {
            delegate.removePropertyChangeListener(l);
        }

        public int getOffset() {
            return pos.getOffset();
        }
        
        public int getLine() {
            try {
                return Utilities.getLineOffset(doc, pos.getOffset());
            } catch (BadLocationException e) {
                return 0;
            }
        }
        
    }
    
    
    class NbPrintContainer extends AttributedCharacters implements PrintContainer {

        ArrayList acl = new ArrayList();

        AttributedCharacters a;

        NbPrintContainer() {
            a = new AttributedCharacters();
        }

        public void add(char[] chars, Font font, Color foreColor, Color backColor) {
            a.append(chars, font, foreColor);
        }

        public void eol() {
            acl.add(a);
            a = new AttributedCharacters();
        }

        public boolean initEmptyLines() {
            return true;
        }

        public AttributedCharacterIterator[] getIterators() {
            int cnt = acl.size();
            AttributedCharacterIterator[] acis = new AttributedCharacterIterator[cnt];
            for (int i = 0; i < cnt; i++) {
                AttributedCharacters ac = (AttributedCharacters)acl.get(i);
                acis[i] = ac.iterator();
            }
            return acis;
        }

    }

}
