/*
 * The contents of this destFile are subject to the terms of the Common Development
 *
The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;

/**
 *
 * @author Vladimir Voskresensky
 */
public class FileElementsCollector {
    private final CsmFile destFile;
    private int startOffset;
    private int destOffset;
    private final CsmProject onlyInProject;

//    private final ProjectBase project;

    public FileElementsCollector(CsmFile file, int offset, CsmProject onlyInProject) {
        this.destFile = file;
//        this.project = (ProjectBase) file.getProject();
        this.destOffset = offset;
        this.startOffset = 0;
        this.onlyInProject = onlyInProject;
    }

    public synchronized void incrementOffset(int newOffset){
        if (mapsGathered) {
            startOffset = destOffset;
        }
        destOffset = newOffset;
        if (startOffset < destOffset) {
            mapsGathered = false;
            visibleUsedDeclarations = null;
            visibleNamespaces = null;
        } else if (startOffset > destOffset) {
            throw new IllegalArgumentException("Start offset "+startOffset+" > destination offset "+destOffset); // NOI18N
        }
    }


    private LinkedHashSet<CsmNamespace> directVisibleNamespaces = new LinkedHashSet<CsmNamespace>();
    private final LinkedHashSet<CsmUsingDirective> usingNamespaces = new LinkedHashSet<CsmUsingDirective>();
    private LinkedHashSet<CsmNamespaceAlias> namespaceAliases = new LinkedHashSet<CsmNamespaceAlias>()/*<String, CsmNamespace>*/;
    private final LinkedHashSet<CsmUsingDeclaration> usingDeclarations = new LinkedHashSet<CsmUsingDeclaration>()/*<String, CsmDeclaration>*/;
//    private LinkedHashSet<CsmNamespaceDefinition> directVisibleNamespaceDefinitions = new LinkedHashSet<CsmNamespaceDefinition>();

    public Collection<CsmUsingDeclaration> getUsingDeclarations() {
        initMaps();
        return Collections.unmodifiableCollection(usingDeclarations);
    }

    public Collection<CsmUsingDirective> getUsingDirectives() {
        initMaps();
        return Collections.unmodifiableCollection(usingNamespaces);
    }

    public Collection<CsmNamespaceAlias> getNamespaceAliases() {
        initMaps();
        return Collections.unmodifiableCollection(namespaceAliases);
    }

    private Collection<CsmDeclaration> visibleUsedDeclarations = null;
    public Collection<CsmDeclaration> getUsedDeclarations() {
        initMaps();
        return _getUsedDeclarations();
    }

    private synchronized Collection<CsmDeclaration> _getUsedDeclarations() {
        Collection<CsmDeclaration> res = visibleUsedDeclarations;
        if (res == null) {
            res = CsmUsingResolver.extractDeclarations(usingDeclarations);
            visibleUsedDeclarations = res;
        }
        return Collections.unmodifiableCollection(res);
    }

    private Collection<CsmNamespace> visibleNamespaces = null;
    public Collection<CsmNamespace> getVisibleNamespaces() {
        initMaps();
        return _getVisibleNamespaces();
    }

    public synchronized Collection<CsmNamespace> _getVisibleNamespaces() {
        Collection<CsmNamespace> res = visibleNamespaces;
        if (res == null) {
            res = CsmUsingResolver.extractNamespaces(usingNamespaces);
            // add scope's and unnamed visible namespaces
            res.addAll(directVisibleNamespaces);
            visibleNamespaces = res;
        }
        return Collections.unmodifiableCollection(res);
    }

//    public Collection<CsmNamespaceDefinition> getDirectVisibleNamespaceDefinitions() {
//        initMaps();
//        return Collections.unmodifiableCollection(directVisibleNamespaceDefinitions);
//    }

    private boolean mapsGathered = false;
    private synchronized void initMaps() {
        if( mapsGathered ) {
            return;
        }
        mapsGathered = true;
        gatherFileMaps(this.destFile);
    }

    protected void gatherFileMaps(CsmFile file) {
        gatherFileMaps(new HashSet<CsmFile>(), file, this.startOffset, this.destOffset);
    }

    protected void gatherFileMaps(Set<CsmFile> visitedFiles, CsmFile file, int startOffset, int endOffset) {
        if( visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        CsmFilter filter = CsmSelect.getDefault().getFilterBuilder().createOffsetFilter(startOffset, endOffset);
        Iterator<CsmInclude> iter = CsmSelect.getDefault().getIncludes(file, filter);
        while (iter.hasNext()){
            CsmInclude inc = iter.next();
            if (inc.getStartOffset() < startOffset) {
                continue;
            }
            // check that include is above the end offset
            if (inc.getEndOffset() < endOffset) {
                CsmFile incFile = inc.getIncludeFile();
                if( incFile != null && (onlyInProject == null || incFile.getProject() == onlyInProject)) {
                    // in includes look for everything
                    gatherFileMaps(visitedFiles, incFile, 0, Integer.MAX_VALUE);
                }
            }
        }
        // gather this file maps
        gatherDeclarationsMaps(CsmSelect.getDefault().getDeclarations(file, filter), startOffset, endOffset);
    }

    protected void gatherDeclarationsMaps(Iterable declarations, int startOffset, int endOffset) {
        gatherDeclarationsMaps(declarations.iterator(), startOffset, endOffset);
    }

    protected void gatherDeclarationsMaps(Iterator it, int startOffset, int endOffset) {
        while(it.hasNext()) {
            CsmOffsetable o = (CsmOffsetable) it.next();
            try {
                int start = o.getStartOffset();
                int end = o.getEndOffset();
                if (end < startOffset) {
                    continue;
                }
                if( start >= endOffset ) {
                    break;
                }
                //assert o instanceof CsmScopeElement;
                if(CsmKindUtilities.isScopeElement((CsmObject)o)) {
                    gatherScopeElementMaps((CsmScopeElement) o, end, endOffset);
                } else {
                    if( FileImpl.reportErrors ) {
                        System.err.println("Expected CsmScopeElement, got " + o);
                    }
                }
            } catch (NullPointerException ex) {
                if( FileImpl.reportErrors ) {
                    // FIXUP: do not crush on NPE
                    System.err.println("Unexpected NULL element in declarations collection");
                    DiagnosticExceptoins.register(ex);
                }
            }
        }
    }

    /**
     * It is quaranteed that element.getStartOffset < this.destOffset
     */
    protected void gatherScopeElementMaps(CsmScopeElement element, int end, int endOffset) {

        CsmDeclaration.Kind kind = CsmKindUtilities.isDeclaration(element) ? ((CsmDeclaration) element).getKind() : null;
        if( kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
            if (nsd.getName().length() == 0) {
                // this is unnamed namespace and it should be considered as
                // it declares using itself
                directVisibleNamespaces.add(nsd.getNamespace());
            }
            if( endOffset < end ) {
                // put in the end of list
                directVisibleNamespaces.remove(nsd.getNamespace());
                directVisibleNamespaces.add(nsd.getNamespace());
                //currentNamespace = nsd.getNamespace();
                gatherDeclarationsMaps(nsd.getDeclarations(), 0, endOffset);
            }
//            directVisibleNamespaceDefinitions.add(nsd);
//            gatherDeclarationsMaps(nsd.getDeclarations(), 0, endOffset);
        } else if( kind == CsmDeclaration.Kind.NAMESPACE_ALIAS ) {
            CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
            namespaceAliases.add(alias);
//            namespaceAliases.put(alias.getAlias(), alias.getReferencedNamespace());
        } else if( kind == CsmDeclaration.Kind.USING_DECLARATION ) {
            CsmUsingDeclaration udecl = (CsmUsingDeclaration) element;
            usingDeclarations.add(udecl);
        } else if( kind == CsmDeclaration.Kind.USING_DIRECTIVE ) {
            CsmUsingDirective udir = (CsmUsingDirective) element;
            usingNamespaces.add(udir);
//            directVisibleNamespaces.add(udir.getName()); // getReferencedNamespace()
        } else if( CsmKindUtilities.isDeclarationStatement(element) ) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if( ds.getStartOffset() < endOffset ) {
                gatherDeclarationsMaps( ((CsmDeclarationStatement) element).getDeclarators(), 0, endOffset);
            }
        } else if( CsmKindUtilities.isScope(element) ) {
            if( endOffset < end ) {
                gatherDeclarationsMaps( ((CsmScope) element).getScopeElements(), 0, endOffset);
            }
        }
    }
}
