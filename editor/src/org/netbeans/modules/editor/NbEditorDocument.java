/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.AttributedCharacterIterator;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.PrintContainer;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.Utilities;
import org.openide.text.NbDocument;
import org.openide.text.AttributedCharacters;
import org.openide.text.IndentEngine;
import javax.swing.text.Position;
import org.openide.text.Annotation;
import org.netbeans.editor.Mark;
import org.netbeans.editor.Annotations;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.modules.editor.options.AllOptionsFolder;

/**
* BaseDocument extension managing the readonly blocks of text
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorDocument extends GuardedDocument
implements NbDocument.PositionBiasable, NbDocument.WriteLockable,
NbDocument.Printable, NbDocument.CustomEditor, NbDocument.Annotatable {

    /** Name of the formatter setting. */
    public static final String FORMATTER = "formatter";

    /** Mime type of the document. The name of this property corresponds
     * to the property that is filled in the document by CloneableEditorSupport.
     */
    public static final String MIME_TYPE_PROP = "mimeType";

    /** Indent engine for the given kitClass. */
    public static final String INDENT_ENGINE = "indentEngine";

    /** Formatter being used. */
    private Formatter formatter;

    /** Map of [Annotation, AnnotationDesc] */
    private HashMap annoMap;

    public NbEditorDocument(Class kitClass) {
        super(kitClass);
        addStyleToLayerMapping(NbDocument.BREAKPOINT_STYLE_NAME,
                               NbDocument.BREAKPOINT_STYLE_NAME + "Layer:10"); // NOI18N
        addStyleToLayerMapping(NbDocument.ERROR_STYLE_NAME,
                               NbDocument.ERROR_STYLE_NAME + "Layer:20"); // NOI18N
        addStyleToLayerMapping(NbDocument.CURRENT_STYLE_NAME,
                               NbDocument.CURRENT_STYLE_NAME + "Layer:30"); // NOI18N
        setNormalStyleName(NbDocument.NORMAL_STYLE_NAME);
        
        annoMap = new HashMap(20);
        
        // lazy init of MIME options
        AllOptionsFolder.getDefault().loadMIMEOption(kitClass);
    }

    public void settingsChange(SettingsChangeEvent evt) {
        super.settingsChange(evt);

        // Check whether the mimeType is set
        Object o = getProperty(MIME_TYPE_PROP);
        if (!(o instanceof String)) {
            BaseKit kit = BaseKit.getKit(getKitClass());
            putProperty(MIME_TYPE_PROP, kit.getContentType());
        }

        // Fill in the indentEngine property
        putProperty(INDENT_ENGINE,
            new BaseDocument.PropertyEvaluator() {

                private Object cached;

                public Object getValue() {
                    if (cached == null) {
                        cached = Settings.getValue(getKitClass(), INDENT_ENGINE);
                    }
                    
                    return cached;
                }
            }
        );

        // Refresh formatter
        formatter = null;

    }

    public void setCharacterAttributes(int offset, int length, AttributeSet s,
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

    public Formatter getFormatter() {
        Formatter f = formatter;
        if (f == null) {
            formatter = (Formatter)Settings.getValue(getKitClass(), FORMATTER);
            f = formatter;
        }

        return (f != null) ? f : super.getFormatter();
    }


    /** Add annotation to the document. For annotation of whole line
     * the length parameter can be ignored (specify value -1).
     * @param startPos position which represent begining 
     * of the annotated text
     * @param length length of the annotated text. If -1 is specified 
     * the whole line will be annotated
     * @param annotation annotation which is attached to this text */
    public void addAnnotation(Position startPos, int length, Annotation annotation) {
        if (annotation.getAnnotationType() != null) {
            AnnotationDesc a = new AnnotationDescDelegate(this, startPos, length, annotation);
            annoMap.put(annotation, a);
            getAnnotations().addAnnotation(a);
        }
    }

    /** Removal of added annotation.
     * @param annotation annotation which is going to be removed */
    public void removeAnnotation(Annotation annotation) {
        if (annotation.getAnnotationType() != null) {
            AnnotationDescDelegate a = (AnnotationDescDelegate)annoMap.get(annotation);
            a.detachListeners();
            getAnnotations().removeAnnotation(a);
        }
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
                    if (evt.getPropertyName() == Annotation.PROP_SHORT_DESCRIPTION)
                        firePropertyChange(AnnotationDesc.PROP_SHORT_DESCRIPTION, null, null);
                    if (evt.getPropertyName() == Annotation.PROP_MOVE_TO_FRONT)
                        firePropertyChange(AnnotationDesc.PROP_MOVE_TO_FRONT, null, null);
                    if (evt.getPropertyName() == Annotation.PROP_ANNOTATION_TYPE) {
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
