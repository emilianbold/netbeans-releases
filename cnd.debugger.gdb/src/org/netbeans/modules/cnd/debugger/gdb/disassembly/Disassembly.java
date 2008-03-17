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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.AddressBreakpoint;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author eu155513
 */
public class Disassembly implements PropertyChangeListener, DocumentListener {
    private final GdbDebugger debugger;
    
    private final List<Line> lines = new ArrayList<Line>();
    private static String functionName = "";
    private CallStackFrame lastFrame = null;
    private boolean opened = false;
    
    private final Map<Integer,String> regNames = new HashMap<Integer,String>();
    private final Map<Integer,String> regValues = new HashMap<Integer,String>();
    private final Set<Integer> regModified = new  HashSet<Integer>();

    private static final String ADDRESS_HEADER="address"; // NOI18N
    private static final String FUNCTION_HEADER="func-name"; // NOI18N
    private static final String OFFSET_HEADER="offset"; // NOI18N
    private static final String INSTR_HEADER="inst"; // NOI18N
    private static final String LINE_HEADER="line"; // NOI18N
    private static final String FILE_HEADER="file"; // NOI18N
    private static final String NUMBER_HEADER="number"; // NOI18N
    private static final String VALUE_HEADER="value"; // NOI18N
    
    public static final String REGISTER_NAMES_HEADER="^done,register-names=["; // NOI18N
    public static final String REGISTER_VALUES_HEADER="^done,register-values=["; // NOI18N
    public static final String REGISTER_MODIFIED_HEADER="^done,changed-registers=["; // NOI18N
    public static final String RESPONSE_HEADER="^done,asm_insns=["; // NOI18N
    private static final String COMBINED_HEADER="src_and_asm_line={"; // NOI18N
    
    private static FileObject fo = null;
    
    public Disassembly(GdbDebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public void update(String msg) {
        assert msg.startsWith(RESPONSE_HEADER) : "Invalid asm response message"; // NOI18N
        boolean withSource = false;

        synchronized (lines) {
            lines.clear();
            int pos = RESPONSE_HEADER.length();

            CountingWriter writer = null;
            
            try {
                boolean nameSet = false;
                
                DataObject dobj = DataObject.find(getFileObject());
                Document doc = ((DataEditorSupport)dobj.getCookie(OpenCookie.class)).getDocument();
                if (doc != null) {
                    doc.removeDocumentListener(this);
                    doc.addDocumentListener(this);
                }

                writer = new CountingWriter(getFileObject().getOutputStream());

                for (;;) {
                    int combinedPos = msg.indexOf(COMBINED_HEADER, pos);
                    if (combinedPos != -1) {
                        withSource = true;
                    }
                    int addressPos = msg.indexOf(ADDRESS_HEADER, pos);
                    
                    if (addressPos == -1) {
                        break;
                    }
                    
                    if (combinedPos != -1 && combinedPos < addressPos) {
                        int lineIdx = Integer.valueOf(readValue(LINE_HEADER, msg, combinedPos));
                        String path = debugger.getRunDirectory();
                        String fileStr = readValue(FILE_HEADER, msg, combinedPos);
                        File file = new File(path, fileStr);
                        FileObject src_fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                        if (src_fo != null) {
                            writer.writeLine("//" + DataObject.find(src_fo).getCookie(LineCookie.class).getLineSet().getCurrent(lineIdx-1).getText()); // NOI18N
                        } else {
                            writer.writeLine("//" + NbBundle.getMessage(Disassembly.class, "MSG_Source_Not_Found", fileStr, lineIdx)); // NOI18N
                        }
                        pos = combinedPos+1;
                    } else {
                        // read instruction in this line
                        int idx = writer.getLineNo();
                        Line line = new Line(msg, addressPos, nameSet ? idx : idx+1);
                        if (!nameSet) {
                            functionName = line.function;
                            dobj.getNodeDelegate().setDisplayName(getHeader());
                            writer.writeLine(functionName + "()\n"); // NOI18N
                            nameSet = true;
                        }
                        if (functionName.equals(line.function)) {
                            lines.add(line);
                            writer.writeLine(line + "\n"); // NOI18N
                        }
                        pos = addressPos+1;
                    }
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
        }
        // If we got empty dis try to reload without source line info
        if (lines.isEmpty() && withSource) {
            reloadDis(false, true);
        }
    }
    
    public void updateRegNames(String msg) {
        assert msg.startsWith(REGISTER_NAMES_HEADER) : "Invalid asm response message"; // NOI18N
        regNames.clear();
        int idx = 0;
        int pos = REGISTER_NAMES_HEADER.length();
        while (pos != -1) {
            int end = msg.indexOf("\"", pos+1); // NOI18N
            if (end == -1) {
                break;
            }
            String value = msg.substring(pos+1, end);
            regNames.put(idx++, value);
            pos = msg.indexOf("\"", end+1); // NOI18N
        }
    }
    
    public void updateRegModified(String msg) {
        assert msg.startsWith(REGISTER_MODIFIED_HEADER) : "Invalid asm response message"; // NOI18N
        regModified.clear();
        int pos = REGISTER_MODIFIED_HEADER.length();
        while (pos != -1) {
            int end = msg.indexOf("\"", pos+1); // NOI18N
            if (end == -1) {
                break;
            }
            String index = msg.substring(pos+1, end);
            try {
                regModified.add(Integer.valueOf(index));
            } catch (NumberFormatException nfe) {
                //do nothing
            }
            pos = msg.indexOf("\"", end+1); // NOI18N
        }
        //RegisterValuesProvider.getInstance().fireRegisterValuesChanged();
    }
    
    public void updateRegValues(String msg) {
        assert msg.startsWith(REGISTER_VALUES_HEADER) : "Invalid asm response message"; // NOI18N
        regValues.clear();
        int pos = msg.indexOf(NUMBER_HEADER);
        while (pos != -1) {
            String idx = readValue(NUMBER_HEADER, msg, pos);
            String value = readValue(VALUE_HEADER, msg, pos);
            try {
                regValues.put(Integer.valueOf(idx), value);
            } catch (NumberFormatException nfe) {
                // do nothing
            }
            pos = msg.indexOf(NUMBER_HEADER, pos+1);
        }
        // Todo: we know that updated registers will fire the update, but better make it updated at one piece
        //RegisterValuesProvider.getInstance().fireRegisterValuesChanged();
    }

    public Collection<RegisterValue> getRegisterValues() {
        Collection<RegisterValue> res = new ArrayList<RegisterValue>();
        for (Integer idx : regValues.keySet()) {
            res.add(new RegisterValue(regNames.get(idx), regValues.get(idx), regModified.contains(idx)));
        }
        return res;
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateAnnotations(false);
            }
        });
    }
    
    private void updateAnnotations(boolean open) {
        debugger.fireDisUpdate(open);
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Breakpoint[] bs = dm.getBreakpoints();
        for (int i = 0; i < bs.length; i++) {
            if (bs[i] instanceof AddressBreakpoint) {
                ((AddressBreakpoint)bs[i]).refresh();
            }
        }
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(evt.getPropertyName())) {
            reloadDis(true, false);
        }
    }
    
    private void reloadDis(boolean withSource, boolean force) {
        if (!opened) {
            return;
        }
        // reload disassembler if needed
        // TODO: there may be functions with the same name called one from the other, we need to check that too
        CallStackFrame frame = debugger.getCurrentCallStackFrame();
        if (frame == null) {
            return;
        }
        if (force || lastFrame == null || !lastFrame.getFunctionName().equals(frame.getFunctionName())) {
            String filename = frame.getFileName();
            if (filename != null && filename.length() > 0) {
                debugger.getGdbProxy().data_disassemble(filename, frame.getLineNumber(), withSource);
            } else {
                // if filename is not known - just disassemble using address
                debugger.getGdbProxy().data_disassemble(1000, withSource);
            }
            lastFrame = frame;
        }
    }
    
    public static FileObject getFileObject() {
        if (fo == null) {
            try {
                fo = FileUtil.createMemoryFileSystem().getRoot().createData("disasm", "s"); // NOI18N
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return fo;
    }
    
    public String getLineAddress(int idx) {
        //TODO : can use binary search
        synchronized (lines) {
            for (Line line : lines) {
                if (line.idx == idx) {
                    return line.address;
                }
            }
            return "";
        }
    }
    
    public static Disassembly getCurrent() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        GdbDebugger debugger = currentEngine.lookupFirst(null, GdbDebugger.class);
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
        synchronized (lines) {
            for (Line line : lines) {
                if (line.address.equals(address)) {
                    return line.idx;
                }
            }
            return -1;
        }
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
        String paramHeader = name + "=\""; // NOI18N
        int start = msg.indexOf(paramHeader, pos);
        if (start != -1) {
            start += paramHeader.length();
            int end = msg.indexOf("\"", start + 1); // NOI18N
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
            int tmpoffset = 0;
            try {
                tmpoffset = Integer.valueOf(readValue(OFFSET_HEADER, msg, pos));
            } catch (Exception e) {
                //do nothing
            }
            this.offset = tmpoffset;
            this.instruction = readValue(INSTR_HEADER, msg, pos);
            this.idx = idx;
        }

        @Override
        public String toString() {
            //return function + "+" + offset + ": (" + address + ") " + instruction; // NOI18N
            return function + "+" + offset + ": " + instruction; // NOI18N
        }
    }
    
    public static boolean isInDisasm() {
        //TODO: optimize
        DataObject dobj = EditorContextBridge.getContext().getCurrentDataObject();
        if (dobj == null) {
            return false;
        }
        try {
            return dobj.equals(DataObject.find(getFileObject()));
        } catch(DataObjectNotFoundException doe) {
            doe.printStackTrace();
        }
        return false;
    }
    
    public static boolean isDisasm(String url) {
        //TODO: optimize
        try {
            return getFileObject().getURL().toString().equals(url);
        } catch (FileStateInvalidException fsi) {
            fsi.printStackTrace();
        }
        return false;
    }
    
    public static void open() {
        try {
            DataObject dobj = DataObject.find(getFileObject());
            dobj.getNodeDelegate().setDisplayName(NbBundle.getMessage(Disassembly.class, "LBL_Disassembly_Window")); // NOI18N
            dobj.getCookie(OpenCookie.class).open();
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.opened = true;
                dis.reloadDis(true, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getHeader() {
        String res = NbBundle.getMessage(Disassembly.class, "LBL_Disassembly_Window"); // NOI18N
        if (functionName.length() > 0) {
            res += "(" + functionName + ")"; // NOI18N
        }
        return res;
    }
    
    public static void close() {
        try {
            DataObject dobj = DataObject.find(getFileObject());
            dobj.getCookie(CloseCookie.class).close();
            // TODO: check for correct close on debug close
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.opened = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static class CountingWriter {
        private final OutputStreamWriter writer;
        private int lineNo = 1;

        public CountingWriter(OutputStream out) {
            this.writer = new OutputStreamWriter(out);
        }

        public int getLineNo() {
            return lineNo;
        }
        
        public void writeLine(String line) throws IOException {
            writer.write(line);
            lineNo++;
        }
        
        public void close() throws IOException {
            writer.close();
        }
    }
}
