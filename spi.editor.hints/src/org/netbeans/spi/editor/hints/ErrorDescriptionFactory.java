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
package org.netbeans.spi.editor.hints;

import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.editor.hints.HintsControllerImpl;
import org.netbeans.modules.editor.hints.StaticFixList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorDescriptionFactory {

    /** Creates a new instance of ErrorDescriptionFactory */
    private ErrorDescriptionFactory() {
    }

    /**Should be called inside document read lock to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, Document doc, int lineNumber) {
        return createErrorDescription(severity, description, new StaticFixList(), doc, lineNumber);
    }
    
    /**Should be called inside document read lock to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, List<Fix> fixes, Document doc, int lineNumber) {
        return createErrorDescription(severity, description, new StaticFixList(fixes), doc, lineNumber);
    }
    
    /**Should be called inside document read lock to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, LazyFixList fixes, Document doc, int lineNumber) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        
        return new ErrorDescription(file, description, severity, fixes, HintsControllerImpl.fullLine(doc, lineNumber));
    }
    
    /**Acquires read lock on the provided document to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, Document doc, Position start, Position end) {
        return createErrorDescription(severity, description, new StaticFixList(), doc, start, end);
    }

    /**Acquires read lock on the provided document to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, List<Fix> fixes, Document doc, Position start, Position end) {
        return createErrorDescription(severity, description, new StaticFixList(fixes), doc, start, end);
    }
    
    /**Acquires read lock on the provided document to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, LazyFixList fixes, Document doc, Position start, Position end) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        
        return new ErrorDescription(file, description, severity, fixes, HintsControllerImpl.linePart(doc, start, end));
    }

    /**Should be called inside document read lock to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, FileObject file, int start, int end) {
        return createErrorDescription(severity, description, new StaticFixList(), file, start, end);
    }

    /**Should be called inside document read lock to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, List<Fix> fixes, FileObject file, int start, int end) {
        return createErrorDescription(severity, description, new StaticFixList(fixes), file, start, end);
    }
    
    /**Should be called inside document read lock to assure consistency
     */
    public static ErrorDescription createErrorDescription(Severity severity, String description, LazyFixList fixes, FileObject file, int start, int end) {
        return new ErrorDescription(file, description, severity, fixes, HintsControllerImpl.linePart(file, start, end));
    }

    public static LazyFixList lazyListForFixes(List<Fix> fixes) {
        return new StaticFixList(fixes);
    }
    
    public static LazyFixList lazyListForDelegates(List<LazyFixList> delegates) {
        return new HintsControllerImpl.CompoundLazyFixList(delegates);
    }
}
