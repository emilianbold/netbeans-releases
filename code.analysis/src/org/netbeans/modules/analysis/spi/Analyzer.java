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

/**
 *
 * @author lahvac
 */
public interface Analyzer {

    public Iterable<? extends ErrorDescription> analyze(Context context);
    public Collection<? extends MissingPlugin> requiredPlugins(Context context);
    public String getDisplayName();
    public String getDisplayName4Id(String id);
    public String getCategoryId4WarningId(String id);
    public Image  getIcon();

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

}
