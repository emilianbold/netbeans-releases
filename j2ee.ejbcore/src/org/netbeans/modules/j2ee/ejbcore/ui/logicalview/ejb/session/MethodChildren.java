/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodModel;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodViewStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class MethodChildren extends ComponentMethodModel {

    private ComponentMethodViewStrategy mvs;
    private final SessionMethodController controller;
    private final MethodsNode.ViewType viewType;
    
    public MethodChildren(ClasspathInfo cpInfo, SessionMethodController smc, Collection<String> interfaces, MethodsNode.ViewType viewType) {
        super(cpInfo, smc.getBeanClass(), interfaces, viewType == viewType.NO_INTERFACE? null:viewType == viewType.LOCAL ? smc.getLocalHome() : smc.getHome());
        controller = smc;
        this.viewType = viewType;
        mvs = new SessionStrategy();
    }

    protected Collection<String> getInterfaces() {
        switch (viewType){
            case LOCAL: return controller.getLocalInterfaces();
            case REMOTE: return controller.getRemoteInterfaces();
            case NO_INTERFACE: return Arrays.asList(controller.getBeanClass());
            default: return Collections.EMPTY_LIST;
        }
    }
    
    public ComponentMethodViewStrategy createViewStrategy() {
        return mvs;
    }

    private class SessionStrategy implements ComponentMethodViewStrategy {
        
        public void deleteImplMethod(MethodModel me, String implClass, FileObject implClassFO, Collection interfaces) throws IOException {
            switch (viewType){
                case NO_INTERFACE:{
                    controller.delete(me);
                    break;
                }
                case LOCAL:
                case REMOTE:{
                    controller.delete(me, viewType == viewType.LOCAL);
                    break;
                }
            }
        }

        public Image getBadge(MethodModel me, Collection interfaces) {
            return null;
        }

        public Image getIcon(MethodModel me, Collection interfaces) {
            IconVisitor iv = new IconVisitor();
            return ImageUtilities.loadImage(iv.getIconUrl(controller.getMethodTypeFromInterface(me)));
        }

        public void openMethod(final MethodModel me, final String implClass, FileObject implClassFO, Collection interfaces) {
            final List<ElementHandle<ExecutableElement>> methodHandle = new ArrayList<ElementHandle<ExecutableElement>>();
            try {
                JavaSource javaSource = JavaSource.forFileObject(implClassFO);
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElement = controller.getElements().getTypeElement(implClass);
                        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                            if (MethodModelSupport.isSameMethod(controller, executableElement, me)) {
                                methodHandle.add(ElementHandle.create(executableElement));
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            if (methodHandle.size() > 0) {
                ElementOpen.open(implClassFO, methodHandle.get(0));
            }
        }

    }

}
