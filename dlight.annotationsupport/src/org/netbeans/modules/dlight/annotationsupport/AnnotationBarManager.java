/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.annotationsupport;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.SideBarFactory;

/**
 *
 * @author ak119685
 */
public class AnnotationBarManager implements SideBarFactory {

  private static final Object BAR_KEY = new Object();

  static AnnotationBar hideAnnotationBar(JTextComponent target) {
    AnnotationBar ab = (AnnotationBar) target.getClientProperty(BAR_KEY);
    assert ab != null : "#58828 reappeared!"; // NOI18N
    ab.unAnnotate();
    return ab;
  }

  public JComponent createSideBar(JTextComponent target) {
    final AnnotationBar ab = new AnnotationBar(target);
    target.putClientProperty(BAR_KEY, ab);
    return ab;
  }

  public static AnnotationBar showAnnotationBar(JTextComponent target, FileAnnotationInfo fileAnnotationInfo) {
    AnnotationBar ab = (AnnotationBar) target.getClientProperty(BAR_KEY);
    assert ab != null : "#58828 reappeared!"; // NOI18N
    ab.annotate(fileAnnotationInfo);
    return ab;
  }
}
