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
package org.netbeans.modules.java.editor.overridden;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ImplementationProvider;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotatable;
import org.openide.text.Line;

/**
 *
 * @author Jan Lahoda
 */
public final class IsOverriddenAnnotationAction extends AbstractAction {
    
    public IsOverriddenAnnotationAction() {
        setEnabled(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (!invokeDefaultAction((JTextComponent) e.getSource())) {
            Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) e.getSource());
            
            if (actions == null)
                return ;
            
            int nextAction = 0;
            
            while (nextAction < actions.length && actions[nextAction] != this)
                nextAction++;
            
            nextAction++;
            
            if (actions.length > nextAction) {
                Action a = actions[nextAction];
                if (a!=null && a.isEnabled()){
                    a.actionPerformed(e);
                }
            }
        }
    }
    
    private FileObject getFile(JTextComponent component) {
        Document doc = component.getDocument();
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null) {
            return null;
        }
        
        return od.getPrimaryFile();
    }
    
    private IsOverriddenAnnotation findAnnotation(JTextComponent component, AnnotationDesc desc, int lineNum) {
        FileObject file = getFile(component);
        
        if (file == null) {
            if (ErrorManager.getDefault().isLoggable(ErrorManager.WARNING)) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "component=" + component + " does not have a file specified in the file.");
            }
            return null;
        }
        
        Reference<IsOverriddenAnnotationHandler> handlerRef = IsOverriddenAnnotationHandler.file2Annotations.get(file);
        IsOverriddenAnnotationHandler handler = handlerRef != null ? handlerRef.get() : null;

        if (handler == null) {
            if (ErrorManager.getDefault().isLoggable(ErrorManager.WARNING)) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "component=" + component + " does not have attached a IsOverriddenAnnotationHandler");
            }

            return null;
        }

        for(IsOverriddenAnnotation a : handler.annotations) {
            Annotatable at = a.getAttachedAnnotatable();
            
            if (at instanceof Line && lineNum == ((Line) at).getLineNumber()
                         && desc.getShortDescription().equals(a.getShortDescription())
                         && a instanceof IsOverriddenAnnotation) {
                return (IsOverriddenAnnotation) a;
            }
        }
        
        return null;
    }
    
    boolean invokeDefaultAction(JTextComponent comp) {
        Document doc = comp.getDocument();
        
        if (doc instanceof BaseDocument) {
            Annotations annotations = ((BaseDocument) doc).getAnnotations();
            
            try {
                int line = Utilities.getLineOffset((BaseDocument) doc, comp.getCaretPosition());
                AnnotationDesc desc = annotations.getActiveAnnotation(line);
                Point p = comp.modelToView(Utilities.getRowStartFromLineOffset((BaseDocument) doc, line)).getLocation();
                IsOverriddenAnnotation annotation = findAnnotation(comp, desc, line);
                
                if (annotation == null)
                    return false;
                
                annotation.mouseClicked(comp, p);
                
                return true;
            }  catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        return false;
    }
    
}