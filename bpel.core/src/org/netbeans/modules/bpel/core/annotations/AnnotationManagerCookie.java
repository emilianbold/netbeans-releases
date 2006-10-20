package org.netbeans.modules.bpel.core.annotations;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Zgursky
 */
public interface AnnotationManagerCookie extends Node.Cookie {
    boolean addAnnotation(DiagramAnnotation annotation);
    boolean removeAnnotation(DiagramAnnotation annotation);
    DiagramAnnotation[] getAnnotations(UniqueId bpelEntityId);
    void addAnnotationListener(AnnotationListener listener);
    void removeAnnotationListener(AnnotationListener listener);
}
