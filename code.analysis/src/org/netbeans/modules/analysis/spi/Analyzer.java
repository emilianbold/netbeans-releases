/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.analysis.spi;

import java.awt.Image;
import java.util.Collection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.analysis.SPIAccessor;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.lookup.ServiceProvider;

/**A static analyzer. Called by the infrastructure on a given {@link Scope} to perform
 * the analysis and return the found warnings as {@link ErrorDescription}s.
 *
 * It is intended to be installed in the global lookup, using e.g. {@link ServiceProvider}.
 *
 * @author lahvac
 */
public interface Analyzer {

    /**Perform the analysis over the {@link Scope} defined in the given {@code contextt}.
     *
     * @param context containing the required {@link Scope}
     * @return the found warnings
     */
    public Iterable<? extends ErrorDescription> analyze(Context context);

    /**If additional modules are required to run the analysis (for the given {@code context}),
     * return their description.
     *
     * @param context over which the analysis is going to be performed
     * @return descriptions of the missing plugins, if any
     */
    public Collection<? extends MissingPlugin> requiredPlugins(Context context);

    /**The name of this analyzer, in the format it should be shown to the user.
     *
     * @return the name of this analyzer
     */
    public String getDisplayName();

    /**The icon associated with this analyzer.
     *
     * @return the icon of this analyzer
     */
    public Image  getIcon();
    
    public WarningDescription getWarningDescription(String warningId);

    public static final class Context {
        private final Scope scope;
        private final ProgressHandle progress;
        private final int bucketStart;
        private final int bucketSize;
        private int totalWork;

        Context(Scope scope, ProgressHandle progress, int bucketStart, int bucketSize) {
            this.scope = scope;
            this.progress = progress;
            this.bucketStart = bucketStart;
            this.bucketSize = bucketSize;
        }

        public Scope getScope() {
            return scope;
        }

        public void start(int workunits) {
            totalWork = workunits;
        }

        public void progress(String message, int unit) {
            progress.progress(message, computeProgress(unit));
        }

        private int computeProgress(int unit) {
            return (int) (bucketStart + ((double) unit / totalWork) * bucketSize);
        }

        public void progress(String message) {
            progress.progress(message);
        }

        public void progress(int workunit) {
            progress.progress(computeProgress(workunit));
        }

        public void finish() {
            progress.progress(bucketStart + bucketSize);
        }

        static {
            SPIAccessor.ACCESSOR = new SPIAccessor() {
                @Override
                public Context createContext(Scope scope, ProgressHandle progress, int bucketStart, int bucketSize) {
                    return new Context(scope, progress, bucketStart, bucketSize);
                }

                @Override
                public String getDisplayName(MissingPlugin missing) {
                    return missing.displayName;
                }

                @Override
                public String getCNB(MissingPlugin missing) {
                    return missing.cnb;
                }

                @Override
                public String getWarningDisplayName(WarningDescription description) {
                    return description.warningDisplayName;
                }

                @Override
                public String getWarningCategoryId(WarningDescription description) {
                    return description.categoryId;
                }

                @Override
                public String getWarningCategoryDisplayName(WarningDescription description) {
                    return description.categoryDisplayName;
                }
            };
        }
    }

    public static final class MissingPlugin {
        private final String cnb;
        private final String displayName;
        public MissingPlugin(String cnb, String displayName) {
            this.cnb = cnb;
            this.displayName = displayName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MissingPlugin other = (MissingPlugin) obj;
            if ((this.cnb == null) ? (other.cnb != null) : !this.cnb.equals(other.cnb)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.cnb != null ? this.cnb.hashCode() : 0);
            return hash;
        }
        
    }

    public static final class WarningDescription {

        public static WarningDescription create(String warningDisplayName, String categoryId, String categoryDisplayName) {
            return new WarningDescription(warningDisplayName, categoryId, categoryDisplayName);
        }
        
        private final String warningDisplayName;
        private final String categoryId;
        private final String categoryDisplayName;

        private WarningDescription(String warningDisplayName, String categoryId, String categoryDisplayName) {
            this.warningDisplayName = warningDisplayName;
            this.categoryId = categoryId;
            this.categoryDisplayName = categoryDisplayName;
        }

    }

}
