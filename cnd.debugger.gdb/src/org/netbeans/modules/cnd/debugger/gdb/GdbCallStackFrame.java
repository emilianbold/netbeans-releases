/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.debugger.common.CallStackFrame;
import org.netbeans.modules.cnd.debugger.common.utils.Autos;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbLocalVariable;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
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
public class GdbCallStackFrame extends CallStackFrame {
    public static boolean enableMacros = Boolean.getBoolean("gdb.autos.macros");

    private final GdbDebugger debugger;
    private final int lineNumber;
    private final String func;
    private final String file;
    private final String fullname;
    private final String resolvedName;
    private final int frameNumber;
    private final String address;
    private final String from;
    
    private AbstractVariable[] cachedLocalVariables = null;
    private AbstractVariable[] cachedAutos = null;

    private final String LOCALS_LOCK = new String("Locals lock"); //NOI18N
    private final String AUTOS_LOCK = new String("Autos lock"); //NOI18N

    private volatile boolean destroyed = false;

    private Collection<GdbVariable> arguments = null;
    private StyledDocument document = null;
    private int offset = -1;

    //private Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    public GdbCallStackFrame(GdbDebugger debugger, String func, String file, String fullname, String lnum, String address, int frameNumber, String from) {
        this.debugger = debugger;
        this.func = func;
        this.file = file;
        this.fullname = fullname;
        if (fullname != null) {
            resolvedName = debugger.getPathMap().getLocalPath(debugger.getOSPath(fullname));
        } else {
            resolvedName = null;
        }

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
    @Override
    public int getFrameNumber() {
        return frameNumber;
    }
    
    /**
     * Returns line number associated with this stack frame.
     *
     * @return line number associated with this this stack frame
     */
    @Override
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
    @Override
    public String getFullname() {
        return resolvedName;
    }

    public String getOriginalFullName() {
        return fullname;
    }
    
    /**
     * @return address this stack frame is stopped in
     */
    @Override
    public String getAddr() {
        return address;
    }
    
    /** Sets this frame current */
    @Override
    public void makeCurrent() {
        debugger.setCurrentCallStackFrame(this);
    }

    @Override
    public boolean isValid() {
        return getFileName() != null && getOriginalFullName() != null && getFunctionName() != null;
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
            if (resolvedName != null && resolvedName.length() > 0) {
                File docFile = new File(resolvedName);
                if (docFile.exists()) {
                    FileObject fo = FileUtil.toFileObject(CndFileUtils.normalizeFile(docFile));
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

    private final LocalsBuilder localsBuilder = new LocalsBuilder();
    /**
     * Returns local variables.
     * If local variables are not available returns empty array.
     *
     * NOTE: This method should <b>not</b> be called from GdbReaderRP as it can block
     * waiting for type information to be returned on that thread.
     *
     * @return local variables
     */
    public AbstractVariable[] getLocalVariables() {
        assert !(Thread.currentThread().getName().equals("GdbReaderRP"));
        assert !(SwingUtilities.isEventDispatchThread()); 

        if (destroyed) {
            return new AbstractVariable[0];
        }
        AbstractVariable[] curLocals = localsBuilder.get();
        synchronized (LOCALS_LOCK) {
            if (destroyed) {
                destroyVars(curLocals);
                cachedLocalVariables = null;
                return new AbstractVariable[0];
            } else {
                cachedLocalVariables = curLocals;
            }
        }
        return cachedLocalVariables;
    }

    private final class LocalsBuilder {
        private AbstractVariable[] locals = null;

        public synchronized AbstractVariable[] get() {
            if (locals == null) {
                List<GdbVariable> list = debugger.getLocalVariables();
                locals = new AbstractVariable[list.size()];
                int i = 0;
                for (GdbVariable var : list) {
                    if (destroyed) {
                        break;
                    }
                    locals[i++] = new GdbLocalVariable(debugger, var);
                }
            }
            return locals;
        }
    }

    private final AutosBuilder autosBuilder = new AutosBuilder();
    public AbstractVariable[] getAutos() {
        if (destroyed) {
            return new AbstractVariable[0];
        }
        AbstractVariable[] curAutos = autosBuilder.get();
        synchronized (AUTOS_LOCK) {
            if (destroyed) {
                destroyVars(curAutos);
                cachedAutos = null;
                return new AbstractVariable[0];
            } else {
                cachedAutos = curAutos;
            }
        }
        return cachedAutos;
    }

    private final class AutosBuilder {
        private AbstractVariable[] autos = null;
        
        public synchronized AbstractVariable[] get() {
            if (autos == null) {
                Set<String> res = Autos.get(getDocument(), lineNumber-1);
                autos = new AbstractVariable[res.size()];
                int i = 0;
                for (String name : res) {
                    if (destroyed) {
                        break;
                    }
                    autos[i++] = new GdbLocalVariable(debugger, name);
                }
            }
            return autos;
        }
    }

    public void destroy() {
        destroyed = true;
        AbstractVariable[] curLocals;
        synchronized (LOCALS_LOCK) {
            curLocals = cachedLocalVariables;
            cachedLocalVariables = null;
        }
        destroyVars(curLocals);

        AbstractVariable[] curAutos;
        synchronized (AUTOS_LOCK) {
            curAutos = cachedAutos;
            cachedAutos = null;
        }
        destroyVars(curAutos);
    }

    private void destroyVars(AbstractVariable[] cache) {
        if (cache != null) {
            for (AbstractVariable var : cache) {
                if (var != null) {
                    var.destroy();
                }
            }
        }
    }

    @Override
    public int hashCode() {
        // currently default hash code and equals are the optimal ones,
        // because CallStackFrames can not be equal if they are not the same object
        return super.hashCode();
    }
}
