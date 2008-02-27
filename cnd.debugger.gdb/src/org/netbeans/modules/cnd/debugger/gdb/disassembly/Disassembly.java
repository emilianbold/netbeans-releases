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

package org.netbeans.modules.cnd.debugger.gdb.disassembly;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.BreakpointAnnotationListener;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;

/**
 *
 * @author eu155513
 */
public class Disassembly implements PropertyChangeListener, DocumentListener {
    private final List<Line> lines = new ArrayList<Line>();
    private static String functionName = "";
    private final GdbDebugger debugger;
    private String lastFilename = null;

    private static final String ADDRESS_HEADER="address"; // NOI18N
    private static final String FUNCTION_HEADER="func-name"; // NOI18N
    private static final String OFFSET_HEADER="offset"; // NOI18N
    private static final String INSTR_HEADER="inst"; // NOI18N
    private static final String LINE_HEADER="line"; // NOI18N
    private static final String FILE_HEADER="file"; // NOI18N
    
    public static final String RESPONSE_HEADER="^done,asm_insns=["; // NOI18N
    private static final String COMBINED_HEADER="src_and_asm_line={"; // NOI18N
    
    private static File file = null;
    
    private BreakpointAnnotationListener breakAnnotationListener = null;
    
    public Disassembly(GdbDebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public void update(String msg) {
        assert msg.startsWith(RESPONSE_HEADER) : "Invalid asm response message"; // NOI18N
        lines.clear();
        int pos = RESPONSE_HEADER.length();
        try {
            DataObject dobj = DataObject.find(getFileObject());
            functionName = debugger.getCurrentCallStackFrame().getFunctionName();
            dobj.getNodeDelegate().setDisplayName(getHeader());
            Document doc = ((DataEditorSupport)dobj.getCookie(OpenCookie.class)).getDocument();
            if (doc != null) {
                doc.removeDocumentListener(this);
                doc.addDocumentListener(this);
            }
            
            OutputStreamWriter writer = new OutputStreamWriter(getFileObject().getOutputStream());

            // Dis is opened - write to document
            //if (doc != null) {
                //doc.remove(0, doc.getLength());
                //doc.insertString(doc.getLength(), debugger.getCurrentCallStackFrame().getFunctionName() + "()\n", null);
                writer.write(functionName + "()\n");
                int idx = 2;
                
                /*int combinedPos = msg.indexOf(COMBINED_HEADER, pos);
                while (combinedPos != -1) {
                    int lineIdx = Integer.valueOf(readValue(LINE_HEADER, msg, pos));
                    String fileStr = readValue(FILE_HEADER, msg, pos);
                    doc.insertString(doc.getLength(), "// file:" + fileStr + ", line " + lineIdx + "\n", null);
                    idx++;
                    /*FileObject fobj = URLMapper.findFileObject(new URL(fileStr));
                    DataObject srcdobj = DataObject.find(fobj);
                    org.openide.text.Line srcLine = srcdobj.getCookie(LineCookie.class).getLineSet().getOriginal(lineIdx);
                    doc.insertString(doc.getLength(), "//" + srcLine.getText() + "\n", null);*/
                    
                    //combinedPos = msg.indexOf(COMBINED_HEADER, combinedPos + COMBINED_HEADER.length());
                    int combinedPos = -1;
                    
                    // read instructions in this line
                    int start = msg.indexOf(ADDRESS_HEADER, pos);
                    while (start != -1 && (combinedPos == -1 || start < combinedPos)) {
                        pos = start;
                        Line line = new Line(msg, start, idx++);
                        lines.add(line);
                        writer.write(line + "\n");
                        //doc.insertString(doc.getLength(), line.toString() + "\n", null);
                        start = msg.indexOf(ADDRESS_HEADER, start+1);
                    }
                //}
            writer.close();
        //}
             /*else {
                // Dis is not opened - write to file
                OutputStreamWriter writer = new OutputStreamWriter(getFileObject().getOutputStream());
                int start = msg.indexOf(ONLY_HEADER, pos);
                while (start != -1) {
                    Line line = new Line(msg, start);
                    lines.add(line);
                    writer.write(line + "\n");
                    start = msg.indexOf(ONLY_HEADER, start+1);
                }
                writer.close();
            }*/
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        updateAnnotations();
    }
    
    private void updateAnnotations() {
        debugger.fireDisUpdate();
        /*DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Breakpoint[] bs = dm.getBreakpoints();
        for (int i = 0; i < bs.length; i++) {
            if (bs[i] instanceof AddressBreakpoint) {
                ((AddressBreakpoint)bs[i]).refresh();
            }
        }*/
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // stack is updated, reload disassembler if needed
        String filename = debugger.getCurrentCallStackFrame().getFileName();
        if (lastFilename == null || !lastFilename.equals(filename)) {
            int line = debugger.getCurrentCallStackFrame().getLineNumber();
            debugger.getGdbProxy().data_disassemble(filename, line);
            lastFilename = filename;
        }
    }
    
    public static File getFile() {
        if (file == null) {
            try {
                file = FileUtil.normalizeFile(File.createTempFile("disasm", ".s")); // NOI18N
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return file;
    }
    
    public static FileObject getFileObject() {
        return FileUtil.toFileObject(getFile());
    }
    
    public String getLineAddress(int idx) {
        //TODO : can use binary search
        for (Line line : lines) {
            if (line.idx == idx) {
                return line.address;
            }
        }
        return "";
    }
    
    public static Disassembly getCurrent() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        GdbDebugger debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
        if (debugger == null) {
            return null;
        }
        return debugger.getDisassembly();
    }
    
    public static String getLineAddress(Disassembly dis, int idx) {
        if (dis != null) {
            return dis.getLineAddress(idx);
        } else {
            return "";
        }
    }
    
    public int getAddressLine(String address) {
        //TODO : can use binary search
        for (Line line : lines) {
            if (line.address.equals(address)) {
                return line.idx;
            }
        }
        return -1;
    }
    
    public static int getAddressLine(Disassembly dis, String address) {
        if (dis != null) {
            return dis.getAddressLine(address);
        } else {
            return -1;
        }
    }
    
    /*
     * Reads expressions like param="value"
     * in this case readValue("param") will return "value"
     */
    private static String readValue(String name, String msg, int pos) {
        String paramHeader = name + "=\"";
        int start = msg.indexOf(paramHeader, pos);
        if (start != -1) {
            start += paramHeader.length();
            int end = msg.indexOf("\"", start + 1);
            if (end != -1) {
                return msg.substring(start, end);
            }
        }
        return "";
    }
    
    private static class Line {
        private final String address;
        private final String function;
        private final int offset;
        private final String instruction;
        private final int idx;

        public Line(String msg, int pos, int idx) {
            this.address = readValue(ADDRESS_HEADER, msg, pos);
            this.function = readValue(FUNCTION_HEADER, msg, pos);
            this.offset = Integer.valueOf(readValue(OFFSET_HEADER, msg, pos));
            this.instruction = readValue(INSTR_HEADER, msg, pos);
            this.idx = idx;
        }

        @Override
        public String toString() {
            //return function + "+" + offset + ": (" + address + ") " + instruction; // NOI18N
            return function + "+" + offset + ": 00 00 " + instruction; // NOI18N
        }
    }
    
    public static boolean isInDisasm() {
        //TODO: check that we are only in disassembly, not in any asm file
        return "text/x-asm".equals(EditorContextBridge.getContext().getCurrentMIMEType());
    }
    
    public static void open() {
        getFileObject();
        try {
            DataObject dobj = DataObject.find(getFileObject());
            dobj.getNodeDelegate().setDisplayName(getHeader());
            dobj.getCookie(OpenCookie.class).open();
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.updateAnnotations();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getHeader() {
        String res = "Disassembly";
        if (functionName.length() > 0) {
            res += "(" + functionName + ")";
        }
        return res;
    }
    
    public static void close() {
        getFileObject();
        try {
            DataObject dobj = DataObject.find(getFileObject());
            dobj.getCookie(CloseCookie.class).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
