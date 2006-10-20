package org.netbeans.modules.bpel.core.annotations;

/**
 *
 * @author Alexander Zgursky
 */
public interface AnnotationListener {
    void annotationAdded(DiagramAnnotation annotation);
    void annotationRemoved(DiagramAnnotation annotation);
}
