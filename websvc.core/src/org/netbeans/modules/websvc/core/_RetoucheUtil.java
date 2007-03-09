/*
 * _RetoucheUtil.java
 *
 * Created on December 3, 2006, 3:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core;

import java.io.IOException;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class _RetoucheUtil {
    
    private _RetoucheUtil() {}
    
    /** never call this from javac task */
    public static String getMainClassName(final FileObject classFO) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(classFO);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                result[0] = sourceUtils.getTypeElement().getQualifiedName().toString();
            }
        }, true);
        return result[0];
    }

}
