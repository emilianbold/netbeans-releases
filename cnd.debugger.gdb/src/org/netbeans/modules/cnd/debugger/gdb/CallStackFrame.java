/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.gdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.deep.CsmForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;

/**
 * Represents one stack frame.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackFrame {
    private final GdbDebugger debugger;
    private final int lineNumber;
    private final String func;
    private final String file;
    private final String fullname;
    private final int frameNumber;
    private final String address;
    private final String from;
    
    private LocalVariable[] cachedLocalVariables = null;
    private LocalVariable[] cachedAutos = null;

    private Collection<GdbVariable> arguments = null;
    private StyledDocument document = null;
    private int offset = -1;

    //private Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    public CallStackFrame(GdbDebugger debugger, String func, String file, String fullname, String lnum, String address, int frameNumber, String from) {
        this.debugger = debugger;
        this.func = func;
        this.file = file;
        this.fullname = fullname;
        this.address = address;
        this.frameNumber = frameNumber;
        this.from = from;
        int lNumber = -1;
        if (lnum != null) {
            try {
                lNumber = Integer.parseInt(lnum);
            } catch (NumberFormatException ex) {
                lNumber = 1; // shouldn't happen
            }
        } else {
            lNumber = -1;
        }
        this.lineNumber = lNumber;
    }
    
    /**
     *  Get frame number.
     *
     *  @return Frame nunmber in Call Stack ("0" means top)
     */
    public int getFrameNumber() {
        return frameNumber;
    }
    
    /**
     * Returns line number associated with this stack frame.
     *
     * @return line number associated with this this stack frame
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Returns method name associated with this stack frame.
     *
     * @return method name associated with this stack frame
     */
    public String getFunctionName() {
        return func;
    }

    /**
     * Returns from value
     * @return from value
     */
    public String getFrom() {
        return from;
    }
    
    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public String getFileName() {
        return file;
    }
    
    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public String getFullname() {
        // PathMap.getLocalPath throws NPE when argument is null
        return fullname == null? null : debugger.getPathMap().getLocalPath(debugger.checkCygwinLibs(fullname));
    }

    public String getOriginalFullName() {
        return fullname;
    }
    
    /**
     * @return address this stack frame is stopped in
     */
    public String getAddr() {
        return address;
    }
    
    /** Sets this frame current */
    public void makeCurrent() {
        debugger.setCurrentCallStackFrame(this);
    }

    public boolean isValid() {
        return getFileName() != null && getFullname() != null && getFunctionName() != null;
    }
    
    /** UNCOMMENT WHEN THIS METHOD IS NEEDED. IT'S ALREADY IMPLEMENTED IN THE IMPL. CLASS.
     * Determine, if this stack frame can be poped off the stack.
     *
     * @return <code>true</code> if this frame can be poped
     *
     * public abstract boolean canPop();
     */
    
    /**
     * Pop stack frames. All frames up to and including the frame
     * are popped off the stack. The frame previous to the parameter
     * frame will become the current frame.
     */
    public void popFrame() {
        debugger.getGdbProxy().exec_finish();
    }

    public Collection<GdbVariable> getArguments() {
        return arguments;
    }

    public void setArguments(Collection<GdbVariable> arguments) {
        this.arguments = arguments;
    }

    public StyledDocument getDocument() {
        if (document == null) {
            if (fullname != null && fullname.length() > 0) {
                File docFile = new File(fullname);
                if (docFile.exists()) {
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(docFile));
                    document = (StyledDocument) CsmUtilities.getDocument(fo);
                }
            }
        }
        return document;
    }

    public int getOffset() {
        if (offset < 0 && lineNumber >= 0 && getDocument() != null) {
            offset = NbDocument.findLineOffset(document, lineNumber-1);
        }
        return offset;
    }
    
    /**
     * Returns local variables.
     * If local variables are not available returns empty array.
     *
     * NOTE: This method should <b>not</b> be called from GdbReaderRP as it can block
     * waiting for type information to be returned on that thread.
     *
     * @return local variables
     */
    public LocalVariable[] getLocalVariables() {
        assert !(Thread.currentThread().getName().equals("GdbReaderRP"));
        assert !(SwingUtilities.isEventDispatchThread()); 

        if (cachedLocalVariables == null) {
            List<GdbVariable> list = debugger.getLocalVariables();
            int n = list.size();

            LocalVariable[] locals = new LocalVariable[n];
            for (int i = 0; i < n; i++) {
                locals[i] = new AbstractVariable(list.get(i));
            }
            cachedLocalVariables = locals;
            return locals;
        } else {
            return cachedLocalVariables;
        }
    }

    public LocalVariable[] getAutos() {
        if (cachedAutos == null) {
            if (getDocument() == null) {
                return null;
            }
            CsmFile csmFile = CsmUtilities.getCsmFile(getDocument(), false);
            if (csmFile == null || !csmFile.isParsed()) {
                return null;
            }
            CsmContext context = CsmOffsetResolver.findContext(csmFile, getOffset(), null);
            CsmScope scope = context.getLastScope();
            if (scope != null) {
                CsmOffsetable previous = null;
                final List<int[]> spans = new ArrayList<int[]>();
                for (CsmScopeElement csmScopeElement : scope.getScopeElements()) {
                    if (CsmKindUtilities.isOffsetable(csmScopeElement)) {
                        CsmOffsetable offs = (CsmOffsetable) csmScopeElement;
                        if (offs.getEndOffset() >= getOffset()) {
                            if (previous != null) {
                                spans.add(getInterestedStatementOffsets(previous));
                            }
                            spans.add(getInterestedStatementOffsets(offs));
                            break;
                        } else {
                            previous = offs;
                        }
                    }
                }
                final Set<String> autos = new HashSet<String>();
                if (!spans.isEmpty()) {
                    CsmFileReferences.getDefault().accept(scope, new CsmFileReferences.Visitor() {
                        public void visit(CsmReferenceContext context) {
                            CsmReference reference = context.getReference();
                            for (int[] span : spans) {
                                if (span[0] <= reference.getStartOffset() && reference.getEndOffset() <= span[1]) {
                                    CsmObject referencedObject = reference.getReferencedObject();
                                    if (CsmKindUtilities.isVariable(referencedObject) || CsmKindUtilities.isMacro(referencedObject)) {
                                        autos.add(((CsmNamedElement)referencedObject).getName().toString());
                                    }
                                }
                            }
                        }
                    });
                }
                cachedAutos = new LocalVariable[autos.size()];
                int i = 0;
                for (String name : autos) {
                    cachedAutos[i++] = new AbstractVariable(name);
                }
            }
        }
        return cachedAutos;
    }

    @Override
    public int hashCode() {
        // currently default hash code and equals are the optimal ones,
        // because CallStackFrames can not be equal if they are not the same object
        return super.hashCode();
    }

    private static int[] getInterestedStatementOffsets(CsmOffsetable offs) {
        if (CsmKindUtilities.isStatement(offs)) {
            switch (((CsmStatement)offs).getKind()) {
                case IF:
                    offs = ((CsmIfStatement)offs).getCondition();
                    break;
                case SWITCH:
                    offs = ((CsmSwitchStatement)offs).getCondition();
                    break;
                case WHILE:
                case DO_WHILE:
                    offs = ((CsmLoopStatement)offs).getCondition();
                    break;
                case FOR:
                    offs = ((CsmForStatement)offs).getCondition();
                    break;
            }
        }
        return new int[]{offs.getStartOffset(), offs.getEndOffset()};
    }
}
