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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.soa.ui;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;

/**
 * Contains the objects of the specific base class T.
 * The objects can be obtained by the particular class.
 * If there isn't the object with required class, then it is created
 * with the help of default constructor.
 * Hence, it's implied that the class has the default constructor.
 *
 * @author nk160297
 */
public class ClassBasedPool<T> {
    
    private Map<Class<T>, T> myPool = new HashMap<Class<T>, T>();
    
    public ClassBasedPool() {
    }
    
    public <P extends T> P getObjectByClass(Class<P> tClass) {
        P result = (P)myPool.get(tClass);
        //
        if (result == null) {
            Constructor<P> defaultConstructor = null;
            try {
                defaultConstructor = tClass.getConstructor();
            } catch (Exception ex) {
                // do nothing
            }
            if (defaultConstructor != null) {
                try {
                    result = defaultConstructor.newInstance();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                    // do nothing
                }
            }
            //
            if (result != null) {
                myPool.put((Class<T>)tClass, result);
            }
        }
        //
        return result;
    }
    
}
