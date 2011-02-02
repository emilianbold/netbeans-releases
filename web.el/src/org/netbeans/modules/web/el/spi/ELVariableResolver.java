/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.el.spi;

import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

public interface ELVariableResolver {

    /**
     * Gets the bean name of the given {@code clazz}.
     *
     * @param clazz the FQN of the class
     * @return the bean name of of the class or {@code null}.
     */
    String getBeanName(String clazz, FileObject context);

    /**
     * Gets the class of the bean identified by the given {@code beanName}.
     * 
     * @param beanName the bean name
     * @param context
     * @return the FQN of the bean or {@code null}.
     */
    String getBeanClass(String beanName, FileObject context);

//    /**
//     * Gets the expression referred by the variable at the given {@code offset}.
//     * @param snapshot
//     * @param offset
//     * @return the referred expression or {@code null}.
//     */
//    String getReferredExpression(Snapshot snapshot, int offset);

    /**
     * Gets the names of managed beans and variables.
     * @return a list of bean infos; never {@code null}.
     */
    List<VariableInfo> getManagedBeans(FileObject context);

    /**
     * Gets all the variables available at the given offset.
     * 
     * @param snapshot
     * @param offset
     * @return a list of variable infos; never {@code null}.
     */
    List<VariableInfo> getVariables(Snapshot snapshot, int offset);

    /**
     * Gets the managed beans in the given {@code scope}.
     * @param scope the scope to search, e.g. {@code "session"} or {@code "application"}.
     * @param context
     * @return a list of bean infos; never {@code null}.
     */
    List<VariableInfo> getBeansInScope(String scope, Snapshot snapshot);

    List<VariableInfo> getRawObjectProperties(String name, Snapshot snapshot);

    
    public static final class VariableInfo {

        public final String name;
        public final String clazz;
        public final String expression;

        public static VariableInfo createResolvedVariable(String name, String clazz) {
            Parameters.notNull("name", name); //NOI18N
            Parameters.notNull("clazz", clazz); //NOI18N
            
            return new VariableInfo(name, clazz, null);
        }
        
        public static VariableInfo createUnresolvedVariable(String name, String expression) {
            Parameters.notNull("name", name); //NOI18N
            Parameters.notNull("expression", expression); //NOI18N

            return new VariableInfo(name, null, expression);
        }

        public static VariableInfo createVariable(String name) {
            Parameters.notNull("name", name); //NOI18N

            return new VariableInfo(name, null, null);
        }

        private VariableInfo(String name, String clazz, String expression) {
            this.name = name;
            this.clazz = clazz;
            this.expression = expression;
        }
        
    }
}
