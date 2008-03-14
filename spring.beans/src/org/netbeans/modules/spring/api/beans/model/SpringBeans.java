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

package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import java.util.List;

/**
 * Encapsulates the root of a Spring config model. It provides access to the
 * list of bean definitions and useful methods for retrieving beans
 * by id, etc.
 *
 * @author Andrei Badea
 */
public interface SpringBeans {

    // XXX aliases.

    /**
     * Finds a bean by its id or name.
     *
     * @param  name the bean id or name; never null.
     * @return the bean with the specified id or name; {@code null} if no such
     *         bean was found.
     */
    SpringBean findBean(String name);

    /**
     * Finds a bean by its id in the given beans config file.
     *
     * @param  name the bean id; never null.
     * @param  file the file to look in.
     * @return the bean with the specified id or {@code null} if no such
     *         bean could be found or {@code file} was not used
     *         to create the contents of this {@code SpringBeans}.
     */
    SpringBean findBean(File file, String id);

    /**
     * Returns the list of beans in the specified beans config file.
     *
     * @param  file the beans config file.
     * @return the list of beans or {@code null} if {@code file} was not used
     *         to create the contents of this {@code SpringBeans}.
     */
    List<SpringBean> getBeans(File file);

    /**
     * Returns the list of beans in the Spring config model.
     *
     * @return the list of beans; never {@code null}.
     */
    List<SpringBean> getBeans();
}
