package org.netbeans.modules.cnd.execution.impl;

import org.netbeans.modules.cnd.execution.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 * Implements Annotation
 */
public final class ErrorAnnotation extends Annotation implements PropertyChangeListener {

    private static ErrorAnnotation instance;
    private Line currentLine;

    public static ErrorAnnotation getInstance() {
        if (instance == null) {
            instance = new ErrorAnnotation();
        }
        return instance;
    }

    /** Returns name of the file which describes the annotation type.
     * The file must be defined in module installation layer in the
     * directory "Editors/AnnotationTypes"
     * @return  name of the anotation type */
    public String getAnnotationType() {
        return "org-netbeans-modules-cnd-error"; // NOI18N
    }

    /** Returns the tooltip text for this annotation.
     * @return  tooltip for this annotation */
    public String getShortDescription() {
        return NbBundle.getMessage(OutputWindowWriter.class, "HINT_CompilerError"); // NOI18N
    }

    public void attach(Line line) {
        if (currentLine != null) {
            detach(currentLine);
        }
        currentLine = line;
        super.attach(line);
        line.addPropertyChangeListener(this);
    }

    public void detach(Line line) {
        if (line == currentLine || line == null) {
            currentLine = null;
            Annotatable at = getAttachedAnnotatable();
            if (at != null) {
                at.removePropertyChangeListener(this);
            }
            detach();
        }
    }

    public void propertyChange(PropertyChangeEvent ev) {
        if (Annotatable.PROP_TEXT.equals(ev.getPropertyName())) {
            detach(null);
        }
    }
}
