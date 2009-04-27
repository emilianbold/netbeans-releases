/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorFixesFakeHint extends AbstractHint {

    private FixKind kind;

    private ErrorFixesFakeHint(FixKind kind) {
        super(true, false, null);
        this.kind = kind;
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(ErrorFixesFakeHint.class, "DESC_ErrorFixesFakeHint" + kind.name());
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.noneOf(Kind.class);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        return null;//should not be called
    }

    public String getId() {
        return ErrorFixesFakeHint.class.getName() + kind.name();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ErrorFixesFakeHint.class, "DN_ErrorFixesFakeHint" + kind.name());
    }
    
    public void cancel() {}

    @Override
    public JComponent getCustomizer(Preferences node) {
        switch (kind) {
            case CREATE_LOCAL_VARIABLE:
                return new LocalVariableFixCustomizer(node);
            case SURROUND_WITH_TRY_CATCH:
                return new SurroundWithTryCatchLog(node);
            case CREATE_FINAL_FIELD_CTOR:
                return new FinalFieldsFromCtorCustomiser(node);
        }
        return super.getCustomizer(node);
    }

    public static enum FixKind {
        SURROUND_WITH_TRY_CATCH,
        CREATE_LOCAL_VARIABLE,
        CREATE_FINAL_FIELD_CTOR;
    }
    
    private static Map<FixKind, ErrorFixesFakeHint> kind2Hint = new  EnumMap<FixKind, ErrorFixesFakeHint>(FixKind.class);
    
    private static synchronized ErrorFixesFakeHint getHint(FixKind kind) {
        ErrorFixesFakeHint h = kind2Hint.get(kind);
        
        if (h == null) {
            kind2Hint.put(kind, h = new ErrorFixesFakeHint(kind));
        }
        
        return h;
    }
    
    public static boolean enabled(FixKind kind) {
        return getHint(kind).isEnabled();
    }

    public static boolean isCreateFinalFieldsForCtor() {
        return getHint(FixKind.CREATE_FINAL_FIELD_CTOR).getPreferences(null).getBoolean(FINAL_FIELDS_FROM_CTOR, true);
    }

    public static void setCreateFinalFieldsForCtor(Preferences p, boolean v) {
        p.putBoolean(FINAL_FIELDS_FROM_CTOR, v);
    }

    public static void setCreateFinalFieldsForCtor(boolean v) {
        setCreateFinalFieldsForCtor(getHint(FixKind.CREATE_FINAL_FIELD_CTOR).getPreferences(null), v);
    }

    public static boolean isCreateLocalVariableInPlace() {
        return getHint(FixKind.CREATE_LOCAL_VARIABLE).getPreferences(null).getBoolean(LOCAL_VARIABLES_INPLACE, true);
    }
    
    public static void setCreateLocalVariableInPlace(boolean v) {
        setCreateLocalVariableInPlace(getHint(FixKind.CREATE_LOCAL_VARIABLE).getPreferences(null), v);
    }
    
    public static void setCreateLocalVariableInPlace(Preferences p, boolean v) {
        p.putBoolean(LOCAL_VARIABLES_INPLACE, v);
    }
    
    public static boolean isUseExceptions() {
        return getHint(FixKind.SURROUND_WITH_TRY_CATCH).getPreferences(null).getBoolean(SURROUND_USE_EXCEPTIONS, true);
    }
    
    public static void setUseExceptions(boolean v) {
        setUseExceptions(getHint(FixKind.SURROUND_WITH_TRY_CATCH).getPreferences(null), v);
    }
    
    public static void setUseExceptions(Preferences p, boolean v) {
        p.putBoolean(SURROUND_USE_EXCEPTIONS, v);
    }
    
    public static boolean isUseLogger() {
        return getHint(FixKind.SURROUND_WITH_TRY_CATCH).getPreferences(null).getBoolean(SURROUND_USE_JAVA_LOGGER, true);
    }
    
    public static void setUseLogger(boolean v) {
        setUseLogger(getHint(FixKind.SURROUND_WITH_TRY_CATCH).getPreferences(null), v);
    }
    
    public static void setUseLogger(Preferences p, boolean v) {
        p.putBoolean(SURROUND_USE_JAVA_LOGGER, v);
    }
    
    public static final String LOCAL_VARIABLES_INPLACE = "create-local-variables-in-place"; // NOI18N
    public static final String SURROUND_USE_EXCEPTIONS = "surround-try-catch-org-openide-util-Exceptions"; // NOI18N
    public static final String SURROUND_USE_JAVA_LOGGER = "surround-try-catch-java-util-logging-Logger"; // NOI18N
    public static final String FINAL_FIELDS_FROM_CTOR = "create-final-fields-from-ctor"; // NOI18N

    public static ErrorFixesFakeHint create(FileObject file) {
        if (file.getName().endsWith("surround")) {
            return getHint(FixKind.SURROUND_WITH_TRY_CATCH);
        }
        if (file.getName().endsWith("local")) {
            return getHint(FixKind.CREATE_LOCAL_VARIABLE);
        }
        if (file.getName().endsWith("finalfield")) {
            return getHint(FixKind.CREATE_FINAL_FIELD_CTOR);
        }
        
        throw new IllegalArgumentException();
    }
}
