/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisassemblyService;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/**
 *
 * @author Egor Ushakov
 */
public class GdbDisassemblyService implements DisassemblyService {
    public int getAddressLine(String address) {
        Disassembly dis = Disassembly.getCurrent();
        if (dis == null) {
            return -1;
        }
        return dis.getAddressLine(address);
    }

//    public int getBreakpointLine(AddressBreakpoint b) {
//        int res = getAddressLine(b.getAddress());
//        if (res < 1) {
//            BreakpointImpl<?> bptImpl = GdbDebugger.getBreakpointImpl(b);
//            if (bptImpl != null) {
//                return Disassembly.getCurrent().getAddressLine(bptImpl.getAddress());
//            }
//        }
//        return res;
//    }

    public String getLineAddress(int lineNo) {
        Disassembly dis = Disassembly.getCurrent();
        if (dis == null) {
            return null;
        }
        return dis.getLineAddress(lineNo);
    }

    public boolean isDis(String url) {
        return Disassembly.isDisasm(url);
    }
    
    public boolean isInDis() {
        return Disassembly.isInDisasm();
    }

//    public boolean showBreakpoint(AddressBreakpoint b) {
//        return showLine(getBreakpointLine(b));
//    }

    private boolean showLine(int line) {
        if (line != -1) {
            FileObject fo = Disassembly.getFileObject();
            if (fo != null) {
//                try {
//                    return EditorContextBridge.getContext().showSource(DataObject.find(fo), line, null);
//                } catch (DataObjectNotFoundException dex) {
//                    // do nothing
//                }
            }
        } else {
            Disassembly.open();
        }
        return false;
    }

    public boolean showAddress(String address) {
        return showLine(getAddressLine(address));
    }

    public void movePC(long address, DebuggerAnnotation pcMarker) {
        Disassembly dis = Disassembly.getCurrent();
        if (dis == null) {
            return;
        }
        int line = dis.getAddressLine(address);
        if (line != -1) {
            FileObject fo = Disassembly.getFileObject();
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    Line disLine = EditorBridge.lineNumberToLine(dobj, line);
                    if (isInDis()) {
                        EditorBridge.showInEditor(disLine);
                    }
                    pcMarker.setLine(disLine, true);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public Annotation annotateAddress(String address, String annotationType) {
        Disassembly dis = Disassembly.getCurrent();
        if (dis == null) {
            return null;
        }
        int line = dis.getAddressLine(address);
        if (line != -1) {
            FileObject fo = Disassembly.getFileObject();
            if (fo != null) {
//                try {
//                    return EditorContextBridge.getContext().annotate(DataObject.find(fo), line, annotationType, null);
//                } catch (DataObjectNotFoundException dex) {
//                    // do nothing
//                }
            }
        }
        return null;
    }
}
