/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2012 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.hints;

import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;

/**A base class for fixes that modify Java source code. Using this class
 * as a base class makes creating the fix somewhat simpler, but also supports
 * running the hint in the Inspect&Transform dialog. The fix can be converted
 * to {@link Fix} by means of the {@link #toEditorFix() } method.
 *
 * @see JavaFixUtilities for various predefined fixes.
 * @author Jan Lahoda
 */
public abstract class JavaFix {

    private final TreePathHandle handle;
    private final Map<String, String> options;

    /**Create JavaFix with the given base {@link TreePath}. The base {@link TreePath}
     * will be passed back to the real implementation of the fix.
     *
     * @param info a {@link CompilationInfo} from which the given {@link TreePath} originates
     * @param tp a {@link TreePath} that will be passed back to the
     *           {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) } method
     */
    protected JavaFix(CompilationInfo info, TreePath tp) {
        this(info, tp, Collections.<String, String>emptyMap());
    }

    JavaFix(CompilationInfo info, TreePath tp, Map<String, String> options) {
        this.handle = TreePathHandle.create(tp, info);
        this.options = Collections.unmodifiableMap(new HashMap<String, String>(options));
    }

    /**Create JavaFix with the given base {@link TreePathHandle}. The base {@link TreePathHandle}
     * will be resolved and passed back to the real implementation of the fix.
     *
     * @param handle a {@link TreePathHandle} that will be resolved and passed back to the
     *              {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) } method
     */
    protected JavaFix(TreePathHandle handle) {
        this(handle, Collections.<String, String>emptyMap());
    }

    JavaFix(TreePathHandle handle, Map<String, String> options) {
        this.handle = handle;
        this.options = Collections.unmodifiableMap(new HashMap<String, String>(options));
    }

    /**The display text of the fix.
     *
     * @return the display text of the fix.
     */
    protected abstract String getText();

    /**Do the transformations needed to implement the hint's function.
     *
     * @param ctx a context over which the fix should operate
     */
    protected abstract void performRewrite(TransformationContext ctx);

    /**Convert this {@link JavaFix} into the Editor Hints {@link Fix}.
     *
     * @return a {@link Fix}, that when invoked, will invoke {@link #performRewrite(org.netbeans.spi.java.hints.JavaFix.TransformationContext) }
     * method on this {@link JavaFix}.
     */
    public final Fix toEditorFix() {
        return new JavaFixImpl(this);
    }

    static {
        JavaFixImpl.Accessor.INSTANCE = new JavaFixImpl.Accessor() {
            @Override
            public String getText(JavaFix jf) {
                return jf.getText();
            }
            @Override
            public ChangeInfo process(JavaFix jf, WorkingCopy wc, boolean canShowUI) throws Exception {
                TreePath tp = jf.handle.resolve(wc);

                if (tp == null) {
                    Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", jf.handle);
                    return null;
                }

                jf.performRewrite(new TransformationContext(wc, tp, canShowUI));

                return null;
            }
            @Override
            public FileObject getFile(JavaFix jf) {
                return jf.handle.getFileObject();
            }
            @Override
            public Map<String, String> getOptions(JavaFix jf) {
                return jf.options;
            }

            @Override
            public Fix rewriteFix(CompilationInfo info, String displayName, TreePath what, String to, Map<String, TreePath> parameters, Map<String, Collection<? extends TreePath>> parametersMulti, Map<String, String> parameterNames, Map<String, TypeMirror> constraints, Map<String, String> options, String... imports) {
                return JavaFixUtilities.rewriteFix(info, displayName, what, to, parameters, parametersMulti, parameterNames, constraints, options, imports);
            }

            @Override
            public Fix createSuppressWarningsFix(CompilationInfo compilationInfo, TreePath treePath, String... keys) {
                return ErrorDescriptionFactory.createSuppressWarningsFix(compilationInfo, treePath, keys);
            }

            @Override
            public List<Fix> createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys) {
                return ErrorDescriptionFactory.createSuppressWarnings(compilationInfo, treePath, keys);
            }
        };
    }

    /**A context that contains a reference to a {@link WorkingCopy} through which
     * modifications of Java source code can be made.
     *
     */
    public static final class TransformationContext {
        private final WorkingCopy workingCopy;
        private final TreePath path;
        private final boolean canShowUI;
        TransformationContext(WorkingCopy workingCopy, TreePath path, boolean canShowUI) {
            this.workingCopy = workingCopy;
            this.path = path;
            this.canShowUI = canShowUI;
        }

        boolean isCanShowUI() {
            return canShowUI;
        }

        /**Returns the {@link TreePath} that was passed to a {@link JavaFix} constructor.
         *
         * @return the {@link TreePath} that was passed to a {@link JavaFix} constructor.
         */
        public TreePath getPath() {
            return path;
        }

        /**A {@link WorkingCopy} over which the transformation should operate.
         * @return {@link WorkingCopy} over which the transformation should operate.
         */
        public WorkingCopy getWorkingCopy() {
            return workingCopy;
        }
    }

}
