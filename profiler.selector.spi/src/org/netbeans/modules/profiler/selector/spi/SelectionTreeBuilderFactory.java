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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.profiler.selector.spi;

import java.util.Collection;
import org.openide.util.Lookup;

/**
 * {@linkplain SelectionTreeBuilderFactory} provides an implementation
 * of {@linkplain SelectionTreeBuilder} for a certain context (an example of
 * the context would be a list of all opened {@linkplain org.netbeans.api.project.Project} instances.
 * In case the factory can not create {@linkplain SelectionTreeBuilder} instance
 * for the given context NULL is returned.
 *
 * There may be more {@linkplain SelectionTreeBuilderFactory} instances registered
 * in the system. The registration is done via {@linkplain org.openide.util.lookup.ServiceProvider} annotation.
 * 
 * @author Jaroslav Bachorik
 */
public interface SelectionTreeBuilderFactory {
    /**
     * Creates a {@linkplain SelectionTreeBuilder} implementation for the given
     * context
     * @param context The factory context
     * @return Returns a valid {@linkplain SelectionTreeBuilder} instance or NULL
     */
    Collection<SelectionTreeBuilder> createBuildersFor(Lookup context);
}
