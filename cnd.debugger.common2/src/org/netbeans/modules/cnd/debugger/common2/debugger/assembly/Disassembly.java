/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpoint;
import org.netbeans.modules.cnd.support.ReadOnlySupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Egor Ushakov
 */
public abstract class Disassembly implements StateModel.Listener {
    private final NativeDebuggerImpl debugger;
    protected static boolean opened = false;
    private static boolean opening = false;
    private static final List<DebuggerAnnotation> bptAnnotations = new ArrayList<DebuggerAnnotation>();
    private final BreakpointModel breakpointModel;
    private int disLength = 0;
    private DisText disText;
    
    protected static enum RequestMode {FILE_SRC, FILE_NO_SRC, ADDRESS_SRC, ADDRESS_NO_SRC, NONE};
    protected RequestMode requestMode = RequestMode.FILE_SRC;
    
    private final BreakpointModel.Listener breakpointListener =
	new BreakpointModel.Listener() {
	    public void bptUpdated() {
                if (opened) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            updateAnnotations(false);
                        }
                    });
                }
	    }
	};

    protected Disassembly(NativeDebuggerImpl debugger, BreakpointModel breakpointModel) {
        this.debugger = debugger;
        this.breakpointModel = breakpointModel;
        breakpointModel.addListener(breakpointListener);
    }

    protected NativeDebuggerImpl getDebugger() {
        return debugger;
    }
    
    private void updateAnnotations(boolean andShow) {
        debugger.annotateDis(andShow);
        for (DebuggerAnnotation annotation : bptAnnotations) {
            annotation.detach();
        }
        bptAnnotations.clear();
        
        NativeBreakpoint[] bs = breakpointModel.getBreakpoints();
        for (NativeBreakpoint bpt : bs) {
            if (bpt instanceof InstructionBreakpoint) {
                InstructionBreakpoint ibpt = (InstructionBreakpoint)bpt;
                try {
                    // breakpoint has an annotation already
                    DebuggerAnnotation[] annotations = ibpt.annotations();
                    if (annotations.length == 0) {
                        continue;
                    }
                    int addressLine = getAddressLine(annotations[0].getAddr());
                    if (addressLine >= 0) {
                        Line line = getLine(addressLine);
                        bptAnnotations.add(new DebuggerAnnotation(null, ibpt.getAnnotationType(), line, 0, true, ibpt));
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
     static Disassembly getCurrent() {
        NativeDebugger currentDebugger = org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager.get().currentDebugger();
        if (currentDebugger != null) {
            return currentDebugger.getDisassembly();
        }
        return null;
    }
    
    public static boolean isInDisasm() {
        if (opened) {
            //TODO: optimize
            FileObject fobj = EditorContextDispatcher.getDefault().getCurrentFile();
            if (fobj == null) {
                fobj = EditorContextDispatcher.getDefault().getMostRecentFile();
            }
            if (fobj != null) {
                try {
                    return DataObject.find(fobj).equals(getDataObject());
                } catch(DataObjectNotFoundException doe) {
                    Exceptions.printStackTrace(doe);
                }
            }
        }
        return false;
    }
    
    public static boolean isDisasm(String url) {
        //TODO: optimize
        try {
            return getFileObject().getURL().toString().equals(url);
        } catch (FileStateInvalidException fsi) {
            Exceptions.printStackTrace(fsi);
        }
        return false;
    }
    
    public static void open() {
        try {
            ReadOnlySupport ro = getDataObject().getLookup().lookup(ReadOnlySupport.class);
            if (ro != null) {
                ro.setReadOnly(true);
            }
            getDataObject().getNodeDelegate().setDisplayName(NbBundle.getMessage(Disassembly.class, "LBL_Disassembly_Window")); // NOI18N
            final EditorCookie editorCookie = getDataObject().getCookie(EditorCookie.class);
            if (editorCookie instanceof EditorCookie.Observable) {
                ((EditorCookie.Observable)editorCookie).addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                            if (editorCookie.getOpenedPanes() == null) {
                                opened = false;
                                ((EditorCookie.Observable)editorCookie).removePropertyChangeListener(this);
                            }
                        }
                    }
                });
            }
            getDataObject().getCookie(OpenCookie.class).open();
            opening = true;
            opened = true;
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.debugger.registerDisassembly(dis);
                dis.reload();
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    protected abstract void reload();
    
    public static void close() {
        try {
            getDataObject().getCookie(CloseCookie.class).close();
            opened = false;
            // TODO: check for correct close on debug close
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.debugger.registerDisassembly(null);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public static boolean isOpened() {
        return opened;
    }
    
    public static FileObject getFileObject() {
        return FileObjectHolder.FOBJ;
    }
    
    private static class FileObjectHolder {
        static final FileObject FOBJ = createFileObject();
        
        private static FileObject createFileObject() {
            try {
                return FileUtil.createMemoryFileSystem().getRoot().createData("disasm", "s"); // NOI18N
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            return null;
        }
    }
    
    protected static DataObject getDataObject() {
        return DataObjectHolder.DOBJ;
    }
    
    private static class DataObjectHolder {
        static final DataObject DOBJ = createDataObject();
        
        private static DataObject createDataObject() {
            try {
                return DataObject.find(getFileObject());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
    
    static Line getLine(int lineNo) throws Exception {
        return EditorBridge.lineNumberToLine(getDataObject(), lineNo);
    }
    
    protected int getAddressLine(String address) {
        return getAddressLine(Address.parseAddr(address));
        // had problems with string comparison: 0x01 is not equal to 0x1
        //TODO : can use binary search
//        synchronized (lines) {
//            for (Line line : lines) {
//                if (line.address.equals(address)) {
//                    return line.idx;
//                }
//            }
//        }
//        return -1;
    }
    
    protected int getAddressLine(long address) {
        if (disText != null) {
            final List<DisLine> lines = disText.lines;
            //TODO : can use binary search
            synchronized (lines) {
                for (DisLine line : lines) {
                    try {
                        if (Address.parseAddr(line.getAddress()) == address) {
                            return line.getIdx();
                        }
                    } catch (NumberFormatException e) {
                        //do nothing
                    }
                }
            }
        }
        return -1;
    }
    
    String getLineAddress(int idx) {
        if (disText != null) {
            final List<DisLine> lines = disText.lines;
            //TODO : can use binary search
            synchronized (lines) {
                for (DisLine line : lines) {
                    if (line.getIdx() == idx) {
                        return line.getAddress();
                    }
                }
            }
        }
        return null;
    }
    
    protected static interface DisLine {
        String getAddress();
        int getIdx();
        void setIdx(int idx);
    }
    
    protected static class CommentLine implements DisLine {
        private final String text;
        
        public CommentLine(String text) {
            this.text = text;
        }

        public String getAddress() {
            return ""; //NOI18N
        }

        public int getIdx() {
            return -1;
        }

        public void setIdx(int idx) {}

        @Override
        public String toString() {
            return text;
        }
    }
    
    protected void attachUpdateListener() {
        DataObject dobj = getDataObject();
        Document doc = ((DataEditorSupport)dobj.getCookie(OpenCookie.class)).getDocument();
        if (doc != null) {
            doc.removeDocumentListener(updateListener);
            doc.addDocumentListener(updateListener);
        }
    }
    
    private final DocumentListener updateListener = new DocumentListener() {
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

        public void removeUpdate(DocumentEvent e) {
        }
    };
    
    protected final void setText(DisText text) {
        this.disText = text;
    }
    
    protected class DisText {
        private final List<DisLine> lines = new ArrayList<DisLine>();
        private final StringBuilder data = new StringBuilder();

        public DisText() {
        }

        public int size() {
            return lines.size();
        }
        
        public int getLength() {
            return data.length();
        }
        
        public void addLine(DisLine line) {
            lines.add(line);
            line.setIdx(lines.size());
            data.append(line.toString());
        }
        
        public void save() {
            disLength = getLength();
            try {
                Writer writer = new OutputStreamWriter(getFileObject().getOutputStream());
                writer.write(data.toString());
                writer.close();
            } catch (IOException ex) {
                //do nothing
            }
        }
        
        public boolean isEmpty() {
            return lines.isEmpty();
        }
    }
}
