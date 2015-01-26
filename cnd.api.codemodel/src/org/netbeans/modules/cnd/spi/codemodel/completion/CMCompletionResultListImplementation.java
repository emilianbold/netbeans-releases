/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.spi.codemodel.completion;

import org.netbeans.modules.cnd.api.codemodel.CMCursorKind;
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.api.codemodel.completion.CMCompletionResult;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public interface CMCompletionResultListImplementation {
    /**
     * \brief Returns the cursor kind for the container for the current code
     * completion context. The container is only guaranteed to be set for
     * contexts where a container exists (i.e. member accesses or Objective-C
     * message sends); if there is not a container, this function will return
     * CXCursor_InvalidCode.
     *
     * @param Results the code completion results to query
     *
     * @param IsIncomplete on return, this value will be false if Clang has
     * complete information about the container. If Clang does not have complete
     * information, this value will be true.
     *
     * @return the container kind, or CXCursor_InvalidCode if there is not a
     * container
     */
    CMCursorKind getContextContainerKind();

    /**
     * \brief Returns the USR for the container for the current code completion
     * context. If there is not a container for the current context, this
     * function will return the empty string.
     *
     * @return the USR for the container
     */
    CMUnifiedSymbolResolution getContextContainerUSR();

    /**
     * 
     * @return 
     */
     Iterable<CMCompletionResultImplementation> getItems();
}
