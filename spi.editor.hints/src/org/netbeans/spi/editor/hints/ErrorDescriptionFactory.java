/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.spi.editor.hints;

import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.hints.HintsControllerImpl;
import org.netbeans.modules.editor.hints.StaticFixList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;

/**
 * Factory class with static methods that allow creation of ErrorDescription.
 * @author Jan Lahoda
 */
public class ErrorDescriptionFactory {

    /** No instances of this class are needed - all the API methods are static. */
    private ErrorDescriptionFactory() {
    }

    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull Document doc, int lineNumber) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("doc", doc);
        return createErrorDescription(severity, description, new StaticFixList(), doc, lineNumber);
    }
    
    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull List<Fix> fixes, @NonNull Document doc, int lineNumber) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        return createErrorDescription(severity, description, new StaticFixList(fixes), doc, lineNumber);
    }
    
    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull LazyFixList fixes, @NonNull Document doc, int lineNumber) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        
        return new ErrorDescription(file, description, severity, fixes, HintsControllerImpl.fullLine(doc, lineNumber));
    }
    
    /**
     * Acquires read lock on the provided document to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("doc", doc);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        return createErrorDescription(severity, description, new StaticFixList(), doc, start, end);
    }

    /**
     * Acquires read lock on the provided document to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull List<Fix> fixes, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        return createErrorDescription(severity, description, new StaticFixList(fixes), doc, start, end);
    }
    
    /**
     * Acquires read lock on the provided document to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull LazyFixList fixes, @NonNull Document doc, @NonNull Position start, @NonNull Position end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("doc", doc);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        FileObject file = od != null ? od.getPrimaryFile() : null;
        
        return new ErrorDescription(file, description, severity, fixes, HintsControllerImpl.linePart(doc, start, end));
    }

    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull FileObject file, int start, int end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("file", file);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        return createErrorDescription(severity, description, new StaticFixList(), file, start, end);
    }

    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull List<Fix> fixes, @NonNull FileObject file, int start, int end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("file", file);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        return createErrorDescription(severity, description, new StaticFixList(fixes), file, start, end);
    }
    
    /**
     * Should be called inside document read lock to assure consistency
     */
    public static @NonNull ErrorDescription createErrorDescription(@NonNull Severity severity, @NonNull String description, @NonNull LazyFixList fixes, @NonNull FileObject file, int start, int end) {
        Parameters.notNull("severity", severity);
        Parameters.notNull("description", description);
        Parameters.notNull("fixes", fixes);
        Parameters.notNull("file", file);
        Parameters.notNull("start", start);
        Parameters.notNull("end", end);
        
        return new ErrorDescription(file, description, severity, fixes, HintsControllerImpl.linePart(file, start, end));
    }

    /**
     * Converts "normal" list of {@link Fix}es into {@link LazyFixList}
     * @param fixes
     * @return lazy
     */
    public static @NonNull LazyFixList lazyListForFixes(@NonNull List<Fix> fixes) {
        Parameters.notNull("fixes", fixes);
        
        return new StaticFixList(fixes);
    }

    /**
     * Concatenates several {@link LazyFixList}s into one.
     * @param delegates the lists to be delegated to
     * @return one list to contain them all
     */
    public static @NonNull LazyFixList lazyListForDelegates(@NonNull List<LazyFixList> delegates) {
        Parameters.notNull("delegates", delegates);
        
        return new HintsControllerImpl.CompoundLazyFixList(delegates);
    }

    /**Attach given sub-fixes to the given fix. The sub-fixes may be shown as a
     * sub-menu for the given fix. Only one level of sub-fixes is currently supported
     * (attaching sub-fixes to any of the sub-fix will not have any effect). The sub-fixes
     * are held in memory as long as the given fix exists.
     *
     * @param to fix to which should be the sub-fixes attached
     * @param subfixes the sub-fixes to attach
     * @return the given fix
     * @since 1.13
     */
    public static @NonNull Fix attachSubfixes(@NonNull Fix to, @NonNull Iterable<? extends Fix> subfixes) {
        Parameters.notNull("to", to);
        Parameters.notNull("subfixes", subfixes);
        
        HintsControllerImpl.attachSubfixes(to, subfixes);
        return to;
    }
}
