/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.caret;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Describes the operation that initiated the caret navigation. The core implementation
 * supports operation type (token) as a key and original intended movement direction as a hint
 * for possible filtering. Future API versions may define more details.
 * <p/>
 * The description is used so that {@link NavigationFilter}s can identify the original operation
 * so they can selectively modify the caret movements. The value can be also used to register
 * {@link NavigationFilters} for just certain actions.
 * <p/>
 * When caret move operation is performed, key parts (currently: just the operation type token)
 * are used to select appropriate NavigationFilter to call. The hint attributes (currently: direction)
 * are used just as additional info for the filter.
 */
public final class MoveCaretsOrigin {
    /**
     * Actions, which are defined as moving or setting the caret. Do not user for actions
     * like search (moves caret to the found string), goto type (moves to the definition) etc.
     */
    public static final String DIRECT_NAVIGATION = "navigation.action"; // NOI18N
    /**
     * Undefined action type. Use this description when registering for all possible
     * caret movements.
     */
    public static final MoveCaretsOrigin DEFAULT = new MoveCaretsOrigin("default", 0); // NOI18N
    private final String actionType;
    private final int direction;

    public MoveCaretsOrigin(@NonNull String actionType, int direction) {
        Parameters.notNull("actionType", actionType); // NOI18N
        this.actionType = actionType;
        this.direction = direction;
    }

    /**
     * Returns the type of the action which originated the caret movement.
     * @return the action type.
     */
    @NonNull
    public String getActionType() {
        return actionType;
    }

    /**
     * Specifies the desired movement direction. Use {@link SwingConstants}
     * compass constants to specify the direction. 0 means the direction
     * is unspecified.
     *
     * @return The initial direction of movemnet
     */
    public int getDirection() {
        return direction;
    }
    
}
