/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.annotationsupport;

import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;

/**
 *
 * @author ak119685
 */
public class AnnotationMarkInstaller implements MarkProviderCreator {

  private static final Object PROVIDER_KEY = new Object();

  public MarkProvider createMarkProvider(JTextComponent pane) {
    AnnotationMarkProvider amp = new AnnotationMarkProvider();
    pane.putClientProperty(PROVIDER_KEY, amp);
    return amp;
  }

  public static AnnotationMarkProvider getMarkProvider(JTextComponent pane) {
    return (AnnotationMarkProvider) pane.getClientProperty(PROVIDER_KEY);
  }
}
