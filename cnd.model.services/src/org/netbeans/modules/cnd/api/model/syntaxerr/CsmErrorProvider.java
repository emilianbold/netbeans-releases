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

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * An abstract error provider.
 * @author Vladimir Kvashin
 */
public abstract class CsmErrorProvider {

    //
    // Interface part
    //

    /** Represents a request for getting errors for a particular file   */
    public interface Request {

        /** A file to process */
        CsmFile getFile();

        /** Determines whether the caller wants to cancel the processing of the request */
        boolean isCancelled();
    }

    /** Response for adding errors for a particular file */
    public interface Response {

        /** Is called for each error */
        void addError(CsmErrorInfo errorInfo);

        /** Is called once the processing is done */
        void done();
    }

    public abstract void getErrors(Request request, Response response);

    //
    // Implementation part
    //

    private static final boolean ENABLE = getBoolean("cnd.csm.errors", true);
    private static final boolean ASYNC = getBoolean("cnd.csm.errors.async", true);

    private static abstract class BaseMerger extends CsmErrorProvider {

        protected final Lookup.Result<CsmErrorProvider> res;

        public BaseMerger() {
            res = Lookup.getDefault().lookupResult(CsmErrorProvider.class);
        }

        protected abstract void getErrorsImpl(Request request, Response response);

        @Override
        public void getErrors(Request request, Response response) {
            if (ENABLE) {
                if (! isPartial(request.getFile())) {
                    getErrorsImpl(request, response);
                }
            }
            response.done();
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
                RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        provider.getErrors(request, response);
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

    private static boolean getBoolean(String name, boolean result) {
        String value = System.getProperty(name);
        if (value != null) {
            result = Boolean.parseBoolean(value);
        }
        return result;
    }

    /**
     * Determines whether this file contains part of some declaration,
     * i.e. whether it was included in the middle of some other declaration
     */
    private static boolean isPartial(CsmFile isIncluded) {
        //Collection<CsmFile> files = CsmIncludeHierarchyResolver.getDefault().getFiles(isIncluded);
        Collection<CsmReference> directives = CsmIncludeHierarchyResolver.getDefault().getIncludes(isIncluded);
        for (CsmReference directive : directives) {
            if (directive != null  ) {
                int offset = directive.getStartOffset();
                CsmFile containingFile = directive.getContainingFile();
                if (containingFile != null) {
                    for (CsmOffsetableDeclaration decl : containingFile.getDeclarations()) {
                        if (decl.getStartOffset() <= offset && offset < decl.getEndOffset()) {
                            return true;
                        }
                    }
                }
            }
        }
	return false;
    }
}
