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

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.GdbCallStackFrame;
import org.netbeans.modules.cnd.debugger.common.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.common.breakpoints.AddressBreakpoint;
import org.netbeans.modules.cnd.debugger.common.disassembly.DisProgressPanel;
import org.netbeans.modules.cnd.support.ReadOnlySupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
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
    private static String fileName = "";
    private String address = "";
    private boolean withSource = true;
    private boolean opened = false;
    private boolean opening = false;
    private int disLength = 0;
    
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
    
    public static final String REGISTER_NAMES_HEADER="^done,register-names="; // NOI18N
    public static final String REGISTER_VALUES_HEADER="^done,register-values="; // NOI18N
    public static final String REGISTER_MODIFIED_HEADER="^done,changed-registers="; // NOI18N
    public static final String RESPONSE_HEADER="^done,asm_insns="; // NOI18N
    private static final String COMBINED_HEADER="src_and_asm_line={"; // NOI18N

    private static final String COMMENT_PREFIX="!"; // NOI18N
    
    private static FileObject fo = null;
    
    private static final Logger log = Logger.getLogger("gdb.logger"); // NOI18N

    private boolean cancelled = false;

    private static enum RequestMode {FILE, ADDRESS, NONE};

    private RequestMode requestMode = RequestMode.FILE;

    public Disassembly(GdbDebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }

    protected void cancel() {
        cancelled = true;
    }

    public void update(String msg) {
        assert msg.startsWith(RESPONSE_HEADER) : "Invalid asm response message"; // NOI18N
        cancelled = false;

        Dialog dialog = null;

        GdbCallStackFrame frame = debugger.getCurrentCallStackFrame();
        if (frame == null) {
            return;
        }

        String currentAddr = debugger.getCurrentCallStackFrame().getAddr();

        synchronized (lines) {
            lines.clear();
            disLength = 0;

            DataObject dobj;
            try {
                dobj = DataObject.find(getFileObject());
            } catch (DataObjectNotFoundException doe) {
                // we failed, no need to do anything else
                doe.printStackTrace();
                return;
            }
            Document doc = ((DataEditorSupport)dobj.getCookie(OpenCookie.class)).getDocument();
            if (doc != null) {
                doc.removeDocumentListener(this);
                doc.addDocumentListener(this);
            }

            DisText text = new DisText();

            int pos = RESPONSE_HEADER.length();
            boolean nameSet = false;

            File srcFile = null;
            if (fileName != null) {
                srcFile = new File(fileName);
            }

            long start = System.currentTimeMillis();
            boolean dialogOpened = false;
            DisProgressPanel panel = null;

            for (;!cancelled;) {
                int combinedPos = msg.indexOf(COMBINED_HEADER, pos);
                int addressPos = msg.indexOf(ADDRESS_HEADER, pos);

                try {
                    if (panel != null) {
                        panel.setProgress(pos*100/msg.length());
                    }
                    if (!cancelled && !dialogOpened && System.currentTimeMillis() - start > 2000) {
                        dialogOpened = true;
                        panel = new DisProgressPanel();
                        final DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(Disassembly.class, "DIS_PROGRESS_TITLE")); // NOI18N
                        dd.setOptions(new Object[]{DialogDescriptor.CANCEL_OPTION});
                        dialog = DialogDisplayer.getDefault().createDialog(dd);
                        final Dialog dlg = dialog;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                dlg.setVisible(true);
                                if (dd.getValue() == DialogDescriptor.CANCEL_OPTION) {
                                    cancel();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (addressPos == -1) {
                    break;
                }

                if (combinedPos != -1 && combinedPos < addressPos) {
                    int lineIdx = Integer.valueOf(readValue(LINE_HEADER, msg, combinedPos));
                    if (lineIdx > 0) {
                        //String path = debugger.getRunDirectory();
                        String fileStr = readValue(FILE_HEADER, msg, combinedPos);
                        if (srcFile != null && srcFile.getName().equals(fileStr)) {
                            FileObject src_fo = FileUtil.toFileObject(CndFileUtils.normalizeFile(srcFile));
                            if (src_fo != null) {
                                try {
                                    String lineText = DataObject.find(src_fo).getCookie(LineCookie.class).getLineSet().getCurrent(lineIdx-1).getText();
                                    if (lineText != null && lineText.length() > 0) {
                                        text.addLine(COMMENT_PREFIX + lineText); // NOI18N
                                    }
                                } catch (Exception ex) {
                                    // do nothing
                                }
                            } else {
                                text.addLine(COMMENT_PREFIX + NbBundle.getMessage(Disassembly.class, "MSG_Source_Not_Found", fileStr, lineIdx)); // NOI18N
                            }
                        }
                    }
                    pos = combinedPos+1;
                } else {
                    // read instruction in this line
                    int idx = text.getLineNo();
                    Line line = new Line(msg, addressPos, nameSet ? idx : idx+1);
                    if (!nameSet && currentAddr.equals(line.address)) {
                        functionName = line.function;
                        dobj.getNodeDelegate().setDisplayName(getHeader());
                        text.addLine(functionName + "()\n"); // NOI18N
                        nameSet = true;
                    }
                    if (!nameSet || functionName.equals(line.function)) {
                        lines.add(line);
                        text.addLine(line + "\n"); // NOI18N
                    }
                    pos = addressPos+1;
                }
            }
            if (!cancelled) {
                disLength = text.getLength();
                try {
                    text.save(getFileObject().getOutputStream());
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }

        if (dialog != null) {
            final Dialog dlg = dialog;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dlg.setVisible(false);
                    dlg.dispose();
                }
            });
        }

        if (cancelled) {
            close();
            return;
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
        int pos = msg.indexOf("\"", REGISTER_NAMES_HEADER.length()); // NOI18N
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
        int pos = msg.indexOf("\"", REGISTER_MODIFIED_HEADER.length()); // NOI18N
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

    public List<org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue> getRegisterValues() {
        List<org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue> res = new ArrayList<org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue>();
        for (Integer idx : regValues.keySet()) {
            String name = regNames.get(idx);
            if (name == null) {
                log.severe("Unknown register: " + idx); // NOI18N
                name = String.valueOf(idx);
            }
            res.add(new org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue(name, regValues.get(idx), regModified.contains(idx)));
        }
        return res;
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        // update anything on full load only
        if (e.getOffset() + e.getLength() >= disLength) {
            final boolean dis = opening;
            opening = false;
            if (opened) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateAnnotations(dis);
                    }
                });
            }
        }
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
        this.withSource = withSource;
        if (!opened) {
            return;
        }
        GdbCallStackFrame frame = debugger.getCurrentCallStackFrame();
        if (frame == null) {
            return;
        }
        String curAddress = frame.getAddr();
        if (curAddress == null || curAddress.length() == 0) {
            return;
        }

        if (!curAddress.equals(address)) {
            requestMode = RequestMode.FILE;
        }

        if (requestMode == RequestMode.NONE) {
            return;
        }

        if (force || getAddressLine(curAddress) == -1) {
            fileName = frame.getOriginalFullName();
            if ((fileName == null || fileName.length() == 0) && requestMode == RequestMode.FILE) {
                requestMode = RequestMode.ADDRESS;
            }
            switch (requestMode) {
                case FILE:
                    debugger.getGdbProxy().data_disassemble(fileName, frame.getLineNumber(), withSource);
                    requestMode = RequestMode.ADDRESS;
                    break;
                case ADDRESS:
                    debugger.getGdbProxy().data_disassemble(1000, withSource);
                    requestMode = RequestMode.NONE;
                    break;
            }
        }

        address = curAddress;
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
        GdbDebugger debugger = GdbDebugger.getGdbDebugger();
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
    
    /*public String getNextAddress(String address) {
        //TODO : can use binary search
        synchronized (lines) {
            for (Iterator<Line> iter = lines.iterator(); iter.hasNext();) {
                Line line = iter.next();
                if (line.address.equals(address)) {
                    // Fix for IZ:131372 (Step Over doesn't work in Disasm)
                    // return next address only for call instructions
                    if (line.instruction.startsWith("call") && iter.hasNext()) { // NOI18N
                        return iter.next().address;
                    }
                    return "";
                }
            }
            return "";
        }
    }*/
    
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
            dobj = EditorContextBridge.getContext().getMostRecentDataObject();
        }
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
            ReadOnlySupport ro = dobj.getLookup().lookup(ReadOnlySupport.class);
            if (ro != null) {
                ro.setReadOnly(true);
            }
            dobj.getNodeDelegate().setDisplayName(NbBundle.getMessage(Disassembly.class, "LBL_Disassembly_Window")); // NOI18N
            final EditorCookie editorCookie = dobj.getCookie(EditorCookie.class);
            if (editorCookie instanceof EditorCookie.Observable) {
                ((EditorCookie.Observable)editorCookie).addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                            if (editorCookie.getOpenedPanes() == null) {
                                Disassembly dis = getCurrent();
                                if (dis != null) {
                                    dis.opened = false;
                                }
                                ((EditorCookie.Observable)editorCookie).removePropertyChangeListener(this);
                            }
                        }
                    }
                });
            }
            dobj.getCookie(OpenCookie.class).open();
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.opening = true;
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
    
    private static class DisText {
        private int lineNo = 1;
        private int length = 0;
        private final StringBuilder data = new StringBuilder();

        public int getLineNo() {
            return lineNo;
        }
        
        public int getLength() {
            return length;
        }
        
        public void addLine(String line) {
            data.append(line);
            lineNo++;
            length += line.length();
        }
        
        public void save(OutputStream out) throws IOException {
            Writer writer = new OutputStreamWriter(out);
            writer.write(data.toString());
            writer.close();
        }
    }
}
