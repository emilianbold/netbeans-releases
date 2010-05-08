/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.api.model.syntaxerr;

import java.util.Iterator;
import org.netbeans.modules.cnd.modelutil.NamedEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.NamedEntityOptions;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * An abstract error provider.
 * @author Vladimir Kvashin
 */
public abstract class CsmErrorProvider implements NamedEntity {

    //
    // Interface part
    //

    /** Represents a request for getting errors for a particular file   */
    public interface Request {

        /** A file to process */
        CsmFile getFile();

        /** Determines whether the caller wants to cancel the processing of the request */
        boolean isCancelled();

        Document getDocument();
    }

    /** Response for adding errors for a particular file */
    public interface Response {

        /** Is called for each error */
        void addError(CsmErrorInfo errorInfo);

        /** Is called once the processing is done */
        void done();
    }

    public final void getErrors(Request request, Response response) {
        if (validate(request)) {
            doGetErrors(request, response);
        }
        response.done();
    }

    protected boolean validate(CsmErrorProvider.Request request) {
        return NamedEntityOptions.instance().isEnabled(this) && !request.isCancelled();
    }

    public boolean isEnabledByDefault() {
        return true;
    }
    
    protected abstract void doGetErrors(Request request, Response response);

    public static boolean disableAsLibraryHeaderFile(CsmFile file) {
        // in release mode we skip library files, because it's very irritating
        // for user to see errors in system libraries
        return CndUtils.isReleaseMode() && (file != null) && file.isHeaderFile() && 
                (file.getProject() != null) && file.getProject().isArtificial();
    }
    //
    // Implementation part
    //

    private static final boolean ENABLE = CndUtils.getBoolean("cnd.csm.errors", true); //NOI18N
    private static final boolean ASYNC = CndUtils.getBoolean("cnd.csm.errors.async", true); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("ErrorsProvider", CndUtils.getConcurrencyLevel()*2); // NOI18N
    private static abstract class BaseMerger extends CsmErrorProvider {

        protected final Lookup.Result<CsmErrorProvider> res;

        public BaseMerger() {
            res = Lookup.getDefault().lookupResult(CsmErrorProvider.class);
        }

        protected abstract void getErrorsImpl(Request request, Response response);

        @Override
        protected boolean validate(CsmErrorProvider.Request request) {
            // all real providers should call super
            return ENABLE;
        }

        @Override
        public void doGetErrors(Request request, Response response) {
            if (! isPartial(request.getFile(), new HashSet<CsmFile>())) {
                getErrorsImpl(request, response);
            }
        }

        public String getName() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }
   }

    private static class SynchronousMerger extends BaseMerger {
        
        @Override
        public void getErrorsImpl(Request request, Response response) {
            for( CsmErrorProvider provider : res.allInstances() ) {
                if (request.isCancelled()) {
                    break;
                }
                provider.getErrors(request, response);
            }
        }
    }

    private static class AsynchronousMerger extends BaseMerger {

        @Override
        public void getErrorsImpl(final Request request, final Response response) {
            final Collection<RequestProcessor.Task> tasks = new ArrayList<RequestProcessor.Task>();
            for( final CsmErrorProvider provider : res.allInstances() ) {
                if (request.isCancelled()) {
                    break;
                }
                RequestProcessor.Task task = RP.post(new Runnable() {
                    public void run() {
                        if (!request.isCancelled()){
                            try {
                                provider.getErrors(request, response);
                            } catch (AssertionError ex) {
                                ex.printStackTrace();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                tasks.add(task);
            }
            for (RequestProcessor.Task task : tasks) {
                task.waitFinished();
            }
        }
    }
    
    /** default instance */
    private static CsmErrorProvider DEFAULT = ASYNC ? new AsynchronousMerger() : new SynchronousMerger();
    
    public static final synchronized  CsmErrorProvider getDefault() {
        return DEFAULT;
    }

    /**
     * Determines whether this file contains part of some declaration,
     * i.e. whether it was included in the middle of some other declaration
     */
    private static boolean isPartial(CsmFile isIncluded, Set<CsmFile> antiLoop) {
        if (antiLoop.contains(isIncluded)) {
            return false;
        }
        antiLoop.add(isIncluded);
        //Collection<CsmFile> files = CsmIncludeHierarchyResolver.getDefault().getFiles(isIncluded);
        Collection<CsmReference> directives = CsmIncludeHierarchyResolver.getDefault().getIncludes(isIncluded);
        for (CsmReference directive : directives) {
            if (directive != null  ) {
                int offset = directive.getStartOffset();
                CsmFile containingFile = directive.getContainingFile();
                if (containingFile != null) {
                    if (CsmSelect.hasDeclarations(containingFile)) {
                        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(offset);
                        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(containingFile, filter);
                        if (declarations.hasNext()) {
                            return true;
                        }
                    } else {
                        if (isPartial(containingFile, antiLoop)) {
                            return true;
                        }
                    }
                }
            }
        }
	return false;
    }

}
