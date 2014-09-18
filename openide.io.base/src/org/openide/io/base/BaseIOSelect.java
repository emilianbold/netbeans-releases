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

package org.openide.io.base;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Capability of an {@link BaseInputOutput} of fine grained selection of a
 * component.
 *
 * @author ivan, Jaroslav Havlin
 */
public final class BaseIOSelect {

    private BaseIOSelect() {}

    /**
     * Additional operations to perform when selecting the output tab.
     * @author ivan
     */
    public static enum AdditionalOperation {
        /**
         * Additionally issue open() on the TopComponent containing the
         * {@link BaseInputOutput}.
         */
	OPEN,

	/**
	 * Additionally issue requestVisible() on the TopComponent containing
         * the {@link BaseInputOutput}.
	 */
	REQUEST_VISIBLE,

	/**
	 * Additionally issue requestActive() on the TopComponent containing
         * the {@link BaseInputOutput}.
	 */
	REQUEST_ACTIVE
    }

    /**
     * With an empty 'extraOps' simply selects this io
     * without involving it's containing TopComponent.
     * <p>
     * For example:
     * </p>
     * <pre>
     * if (BaseIOSelect.isSupported(io) {
     *     BaseIOSelect.select(io, EnumSet.noneOf(IOSelect.AdditionalOperation.class));
     * }
     * </pre>
     * <p>
     * If this capability is not supported then no operation will be performed.
     * </p>
     * @param io {@link BaseInputOutput} to operate on.
     * @param extraOps Additional operations to apply to the containing
     * TopComponent.
     */
    public static void select(BaseInputOutput io, Set<AdditionalOperation> extraOps) {
	Parameters.notNull("extraOps", extraOps);	// NOI18N
	Provider ios = ExtrasHelper.find(io, Provider.class);
	if (ios != null) {
            ios.select(extraOps);
        }
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(BaseInputOutput io) {
        return ExtrasHelper.isSupported(io, Provider.class);
    }

    public interface Provider {

        /**
         * With an empty 'extraOps' simply selects this io without involving
         * it's containing TopComponent.
         *
         * @param extraOps Additional operations to apply to the containing
         * TopComponent.
         */
        void select(@NonNull Set<AdditionalOperation> extraOps);
    }
}
