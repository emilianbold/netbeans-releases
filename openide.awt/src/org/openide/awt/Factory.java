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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.openide.awt;

import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/** Factory methods to simplify various types of actions. These actions can either
 * be created directly by calling one of the factory methods, or they can be 
 * registered in layer file to create the necessary instance declaratively:
 * <pre>
&lt;file name="org-yourpkg-YourActionName.instance"&gt;
    &lt;attr name='instanceCreate' methodvalue='org.netbeans.spi.actions.support.Factory."oneOfFactoryMethods"'/&gt;
    &lt;attr name='SystemFileSystem.localizingBundle' stringvalue='org/yourpkg/Bundle'/&gt;
    &lt;attr name='iconBase' stringvalue='org/yourpkg/yourIcon.png'/&gt;
    &lt;attr name='noIconInMenu' boolvalue="true"/&gt;
&lt;/file&gt;
 * </pre>
 * Some of the attributes are shared between all types of factory methods. 
 * Others are useful only for individual individual factory methods, those are documented
 * bellow.
 *
 * @author Jaroslav Tulach
 */
final class Factory {
    /** No instances 
     */
    private Factory() {
    }

    /** TBD: Nice javadoc here!
     */
    public static Action delegate(ActionListener l) {
        return null;
    }
    
    /** Called from XML filesystems */
    private static Action delegate(Map fo) {
        return GeneralAction.alwaysEnabled(fo);
    }
    
    /** TBD: Nice javadoc here!
     */
    public static ContextAwareAction callback(String key, Action defaultDelegate, Lookup context, boolean surviveFocusChange) {
        return GeneralAction.callback(key, defaultDelegate, context, surviveFocusChange);
    }
    
    private static ContextAwareAction callback(Map fo) {
        return GeneralAction.callback(fo);
    }
    
    /** TBD: Nice javadoc here!
     */
    public static <T> ContextAwareAction context(
        ContextActionPerformer<? super T> handler,
        ContextSelection selectionType,
        Lookup context,
        Class<T> dataType
    ) {
        return GeneralAction.context(handler, null, selectionType, context, dataType);
    }
    
    /** TBD: Nice javadoc here!
     */
    public static <T> ContextAwareAction context(
        ContextActionPerformer<? super T> handler,
        ContextActionEnabler<? super T> enabler,
        ContextSelection selectionType,
        Lookup context,
        Class<T> dataType
    ) {
        return GeneralAction.context(handler, enabler, selectionType, context, dataType);
    }

    
    private static ContextAwareAction context(Map fo) {
        return GeneralAction.context(fo);
    }

}
