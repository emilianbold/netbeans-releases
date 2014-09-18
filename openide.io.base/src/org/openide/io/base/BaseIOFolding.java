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
package org.openide.io.base;

import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Folding of group of lines in Output Window.
 * <p>
 * Client usage:
 * </p>
 * <pre>
 *  BaseInputOutput io = ...;
 *  if (!BaseIOFolding.isSupported(io)) {
 *    throw new Exception("Folding is not supported");
 *  }
 *  io.getOut().println("First Line - start of fold");
 *  BaseFoldHandle fold = BaseIOFolding.startFold(io, true);
 *  io.getOut().println("  Fold Content 1");
 *  io.getOut().println("  The first line of nested fold");
 *  BaseFoldHandle nestedFold = fold.startFold(true);
 *  io.getOut().println("     Nested fold content 1");
 *  nestedFold.finish();
 *  io.getOut().println("  Fold Content 2");
 *  fold.finish();
 *  io.getOut().println("Text outside of the fold.");
 * </pre>
 * <p>
 * How to support {@link BaseIOFolding} in own {@link BaseIOProvider} implementation:
 * </p>
 * <ul>
 * <li> Implement some {@link Provider} of BaseIOFolding</li>
 * <li> Extend {@link FoldHandleDefinition}</li>
 * <li> Place instance of {@link Provider} to {@link Lookup} provided by
 * {@link BaseInputOutput}</li>
 * </ul>
 *
 * @author jhavlin
 * @since openide.io/1.38
 */
public final class BaseIOFolding {

    private BaseIOFolding() {}

    /**
     * Check whether an {@link BaseInputOutput} supports folding.
     *
     * @param io The {@link BaseInputOutput} to check.
     * @return True if {@link #startFold(BaseInputOutput, boolean)} can be used
     * with {@code io}, false otherwise.
     */
    public static boolean isSupported(@NonNull BaseInputOutput io) {
        Parameters.notNull("parent", io);                               //NOI18N
        return ExtrasHelper.isSupported(io, BaseIOFolding.Provider.class);
    }

    public interface Provider {

        /**
         * Create a fold handle definition for the current last line in the
         * output window.
         *
         * @param expanded Initial state of the fold.
         * @return FoldHandleDefinition for the fold handle. Never null.
         *
         * @throws IllegalStateException if the last fold hasn't been finished
         * yet.
         */
        @NonNull
        FoldHandleDefinition startFold(boolean expanded);
    }

    /**
     * Create a fold handle for the current last line in the output window.
     *
     * @param io {@link BaseInputOutput} to create the fold in.
     * @param expanded Initial state of the fold.
     * @return The fold handle that can be used to finish the fold or to create
     * nested folds.
     * @throws IllegalStateException if the last fold hasn't been finished yet.
     * @throws UnsupportedOperationException if folding is not supported by the
     * {@link BaseInputOutput} object.
     */
    @CheckReturnValue
    @NonNull
    public static BaseFoldHandle startFold(
            @NonNull BaseInputOutput io, boolean expanded) {

        Parameters.notNull("io", io);                                   //NOI18N
        BaseIOFolding.Provider folding = ExtrasHelper.getExtras(io,
                BaseIOFolding.Provider.class);

        return new BaseFoldHandle(folding.startFold(expanded));
    }

    /**
     * An SPI for creating custom FoldHandle implementations.
     */
    public interface FoldHandleDefinition {

        /**
         * Finish the fold at the current last line. Ensure that nested folds
         * are finished correctly.
         *
         * @throws IllegalStateException if parent fold has been already
         * finished, or if there is an unfinished nested fold.
         */
        public void finish();

        /**
         * Start a new fold at the current last line. Ensure that the parent
         * fold hasn't been finished yet.
         *
         * @param expanded If false, the fold will be collapsed by default,
         * otherwise it will be expanded.
         * @return FoldHandleDefinition of handle for the newly created fold.
         * @throws IllegalStateException if the fold has been already finished,
         * or if the last nested fold hasn't been finished yet.
         */
        public FoldHandleDefinition startFold(boolean expanded);

        /**
         * Set state of the fold.
         *
         * If a nested fold is expanded, expand all parent folds too.
         *
         * @param expanded True to expand the fold, false to collapse it.
         */
        public void setExpanded(boolean expanded);
    }
}
