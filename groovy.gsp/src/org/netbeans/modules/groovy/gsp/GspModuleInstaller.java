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

package org.netbeans.modules.groovy.gsp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import org.netbeans.editor.Settings;
import org.openide.modules.ModuleInstall;


/**
 * 
 * @author mkleint
 * @author Martin Adamek
 */
public class GspModuleInstaller extends ModuleInstall {

    /**
     * screw friend dependency.
     * Hack to enable this module as friend to NB 6.0 modules
     */
    @Override
    public void validate() throws IllegalStateException {
        try {
            java.lang.Class main = java.lang.Class.forName("org.netbeans.core.startup.Main", false, //NOI18N
                    Thread.currentThread().getContextClassLoader());
            Method meth = main.getMethod("getModuleSystem", new Class[0]); //NOI18N
            Object moduleSystem = meth.invoke(null, new Object[0]);
            meth = moduleSystem.getClass().getMethod("getManager", new Class[0]); //NOI18N
            Object mm = meth.invoke(moduleSystem, new Object[0]);
            Method moduleMeth = mm.getClass().getMethod("get", new Class[]{String.class}); //NOI18N

            Object gsfapi = moduleMeth.invoke(mm, "org.netbeans.modules.gsf.api"); //NOI18N
            modifyFriends(gsfapi);

            Object gsf = moduleMeth.invoke(mm, "org.netbeans.modules.gsf"); //NOI18N
            modifyFriends(gsf);

            Object htmleditor = moduleMeth.invoke(mm, "org.netbeans.modules.html.editor"); //NOI18N
            modifyFriends(htmleditor);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            new IllegalStateException("Cannot fix dependencies for org.netbeans.modules.groovy.gsp."); //NOI18N
        }
    }

    void modifyFriends(Object input) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        if(input != null){
            Field frField = input.getClass().getSuperclass().getDeclaredField("friendNames"); //NOI18N
            frField.setAccessible(true);
            Set friends = (Set) frField.get(input);
            friends.add("org.netbeans.modules.groovy.gsp"); //NOI18N
        }
    }
    
    
    
    @Override
    public void restored() {
        Settings.addInitializer(new GspEditorSettings());
        Settings.reset();
    }
}
