/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.annotationsupport;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;

/**
 *
 * @author ak119685
 */
public class AnnotationMarkProvider extends MarkProvider {

  private List marks = Collections.emptyList();

  public void setMarks(List<AnnotationMark> marks) {
    List old = this.marks;
    this.marks = marks;
    firePropertyChange(PROP_MARKS, old, marks);
  }

  @SuppressWarnings("unchecked")
  public synchronized List<Mark> getMarks() {
    return marks;
  }
}
