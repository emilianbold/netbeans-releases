/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.core.syntax.spi.ErrorAnnotation.LineSetAnnotation;
import org.netbeans.modules.web.frameworks.facelets.loaders.FaceletDataObject;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.text.Annotation;
import org.openide.text.Line;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsAnnotationManager {
    
    final private FaceletDataObject faceletsDO;
    
    private ArrayList <Annotation> annotations;
    
    /** Creates a new instance of FaceletsErrorAnnotation */
    public FaceletsAnnotationManager(FaceletDataObject faceletsDO) {
        this.faceletsDO = faceletsDO;
        annotations = new ArrayList();
    }
    
    /** This method is here mainly due to testing
     **/
    public Collection <Annotation> getAnnotations(){
        return annotations;
    }
    
    /** Adds annotation for the errors. If the error is already annotated, does nothing. If there are
     *  annotated erros, which are not in the input array, then these annotations are deleted.
     */
    public void annotate(FaceletsEditorErrors.Error[] errors){
        ArrayList <Annotation> added, removed, unchanged;
        Collection <Annotation> newAnnotations;
        
        EditorCookie editor = faceletsDO.getCookie(EditorCookie.class);
        if (editor == null)
            return;
        StyledDocument document = editor.getDocument();
        if (document == null)
            return;
        
        // Fix issue #59568
        if(editor.getOpenedPanes()==null)
            return;
        
        // The approriate JText component
        JTextComponent component = editor.getOpenedPanes()[0];
        if (component != null){
            if (errors != null && errors.length > 0){
                // Place the first error in the status bar
                org.netbeans.editor.Utilities.setStatusBoldText(component , " " + errors[0].getText()); //NOI18N
            } else{
                // clear status bar
                org.netbeans.editor.Utilities.clearStatusText(component);
            }
        }
        
        // create annotations from errors
        newAnnotations = getAnnotations(errors, document);
        // which annotations are really new
        added=new ArrayList(newAnnotations);
        added.removeAll(annotations);
        // which annotations were here before
        unchanged=new ArrayList(annotations);
        unchanged.retainAll(newAnnotations);
        // which annotations are obsolete
        removed = annotations;
        removed.removeAll(newAnnotations);
        detachAnnotations(removed);
        
        // are there new annotations?
        if (!added.isEmpty()) {
            final ArrayList <Annotation> finalAdded = added;
            Runnable docRenderer = new Runnable() {
                public void run() {
                    LineCookie cookie = (LineCookie)faceletsDO.getCookie(LineCookie.class);
                    Line.Set lines = cookie.getLineSet();
                    
                    for (Iterator i=finalAdded.iterator();i.hasNext();) {
                        LineSetAnnotation ann=(LineSetAnnotation)i.next();
                        ann.attachToLineSet(lines);
                    }
                }
            };
            
            if (document != null) {
                document.render(docRenderer);
            } else {
                docRenderer.run();
            }
        }
        
        // remember current annotations
        annotations=unchanged;
        annotations.addAll(added);
        
    }
    
    /** Transforms FaceletsEditorError to Annotation
     */
    private Collection <Annotation> getAnnotations(FaceletsEditorErrors.Error[] errors, StyledDocument document) {
        HashMap map = new HashMap(errors.length);
        for (int i = 0; i < errors.length; i ++) {
            FaceletsEditorErrors.Error err = errors[i];
            int line = err.getLine();
            
            if (line<0)
                continue; // When error is outside the file, don't annotate it
            
            LineSetAnnotation ann = err.getErrorAnotation((NbEditorDocument)document);
            
            // This is trying to ensure that annotations on the same
            // line are "chained" (so we get a single annotation for
            // multiple errors on a line).
            // If we knew the errors were sorted by file & line number,
            // this would be easy (and we wouldn't need to do the hashmap
            // "sort"
            Integer lineInt = Integer.valueOf(line);
            map.put(lineInt, ann);
        }
        return map.values();
    }
    
    /** Removes obsolete annotations
     */
    private static void detachAnnotations(Collection <Annotation> anns) {
        for (Annotation annotation : anns) {
            if (annotation.getAttachedAnnotatable() != null)
                annotation.detach();
        }
    }
    
    public abstract static class LineSetAnnotation extends Annotation {
        public abstract void attachToLineSet(Line.Set lines);
    }
}
