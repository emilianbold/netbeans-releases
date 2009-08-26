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

package org.netbeans.modules.cnd.builds;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/** OutputStream for wrapping writes to the IDE.
 * Handles hyperlinks and so on.
 * @deprecated this class will be removed from open package, because not used anywhere in cnd cluster
 */
@Deprecated
public class OutputWindowOutputStream extends OutputStream {
    private static final ErrorManager errMgr = ErrorManager.getDefault().getInstance("org.netbeans.modules.cnd"); // NOI18N

    private OutputWriter writer;
    
    /** buffer which will be used for the next line */
    private StringBuffer buffer = new StringBuffer(1000);
    
    /** have we printed any lines yet? used to prevent initial blank line */
    private boolean hadFirst = false;
    
    private HyperlinkFactory factory = new HyperlinkFactory();
    
    //static public PrintWriter dbout;		    // XXX - Debug
    
    public OutputWindowOutputStream(OutputWriter writer) {
        this.writer = writer;
        
        //try {					    // XXX - Debug code
        //dbout = new PrintWriter(new FileWriter("/dev/tty"), true);
        //} catch (IOException ex) {};
    }
    
    @Override
    public void close() throws IOException {
        flush();
        writer.close();
        //dbout.close();
    }
    
    @Override
    public void flush() throws IOException {
        flushLines();
        if (buffer.length() > 0) {
            // No chance to hyperlink individual lines at this point:
            writer.print(buffer.toString());
            buffer.setLength(0);
        }
        writer.flush();
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    
    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        buffer.append(new String(b, offset, length));
        // Will usually contain at least one newline:
        flushLines();
    }
    
    public void write(int b) throws IOException {
        buffer.append((char) b);
        if ((char) b == '\n') {
            flushLines();
        }
    }
    
    private void flushLines() throws IOException {
        // Not as efficient as it could be, surely, but keep it simple for now:
        //System.err.println("flushLines: buffer=" + buffer);
        MAIN:
            while (true) {
            int len = buffer.length();
            for (int i = 0; i < len; i++) {
                if (buffer.charAt(i) == '\n') {
                    //System.err.println("flushing; i=" + i);
                    // For Windows:
                    int end = i;
                    if (end > 0 && buffer.charAt(end - 1) == '\r') {
                        end--;
                    }
                    flushLine(buffer.substring(0, end));
                    buffer.delete(0, i + 1);
                    continue MAIN;
                }
            }
            //System.err.println("not found");
            break MAIN;
            }
    }
    
    private void flushLine(String l) throws IOException {
        //System.err.println("flushing: " + l);
        if (! hadFirst) {
            hadFirst = true;
            // Do not print an initial blank line.
            if (l.trim().length() == 0) {
                return;
            }
        }
        Hyperlink link = factory.create(l);
        if (link != null) {
            //dbout.println("flushLine: (link) " + l);
            writer.println(link.getMessage(), link);
        } else {
            //dbout.println("flushLine: (asis) " + l);
            writer.println(l);
        }
    }
    
    // #14804: detach everything before uninstalling module.
    private static final Set<Hyperlink> hyperlinks = new WeakSet<Hyperlink>(); // Set<Hyperlink>
    public static void detachAllAnnotations() {
        synchronized (hyperlinks) {
            Iterator<Hyperlink> it = hyperlinks.iterator();
            while (it.hasNext()) {
                (it.next()).destroy();
            }
        }
    }
    
    /**
     *  Represents a linkable line (appears in red in Output Window...).
     *  Its functionality could be replaced in the future by ErrorEvent's from the
     *  Compiler API (though something like the HyperlinkFactory would have to remain).
     *  This would also handle the proper error highlighting and so on.
     */
    private static final class Hyperlink
            extends Annotation implements OutputListener, PropertyChangeListener {
        private FileObject file; // file to jump to
        private int line1, col1, line2, col2; // line/col number to jump to, 0-based, -1 for unspecified
        private String message; // message it is saying, or null
        private boolean dead = false; // whether it has been destroyed
        
        Hyperlink(FileObject file, int line1, int col1, int line2, int col2, String message) {
            this.file = file;
            this.line1 = line1;
            this.col1 = col1;
            this.line2 = line2;
            this.col2 = col2;
            this.message = message;
            synchronized (hyperlinks) {
                hyperlinks.add(this);
            }
        }
        void destroy() {
            doDetach();
            dead = true;
        }
        
        
        /** Get the message suitable for printing in the Output Window. */
        public String getMessage() {
            String fname = file.getPath();
            String m = (message != null ? message :
                NbBundle.getMessage(OutputWindowOutputStream.class, "ERR_unknown")); // NOI18N
            if (line1 == -1) {
                return NbBundle.getMessage(OutputWindowOutputStream.class,
                        "MSG_err", fname, m); // NOI18N
            } else {
                if (col1 == -1) {
                    return NbBundle.getMessage
                            (OutputWindowOutputStream.class, "MSG_err_line", fname, m, // NOI18N
                            Integer.valueOf(line1 + 1));
                } else {
                    if (line2 == -1 || col2 == -1 || (line1 == line2 && col1 == col2)) {
                        return NbBundle.getMessage
                                (OutputWindowOutputStream.class, "MSG_err_line_col", // NOI18N
                                new Object[] { fname, m, Integer.valueOf(line1 + 1), Integer.valueOf(col1 + 1) });
                    } else {
                        if (line1 == line2) {
                            return NbBundle.getMessage
                                    (OutputWindowOutputStream.class, "MSG_err_line_col_col", // NOI18N
                                    new Object[] { fname, m, Integer.valueOf(line1 + 1),
                                    Integer.valueOf(col1 + 1), Integer.valueOf(col2 + 1) });
                        } else {
                            return NbBundle.getMessage
                                    (OutputWindowOutputStream.class, "MSG_err_line_col_line_col", // NOI18N
                                    new Object[] { fname, m, Integer.valueOf(line1 + 1),
                                    Integer.valueOf(col1 + 1), Integer.valueOf(line2 + 1), Integer.valueOf(col2 + 1) });
                        }
                    }
                }
            }
        }
        
        
        // OutputListener:
        public void outputLineAction(OutputEvent ev) {
            System.err.println("outputLineAction: " + ev.getLine()); // NOI18N
            if (dead) {
                return;
            }
            if (! file.isValid()) { // #13115
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            if (message != null) {
                StatusDisplayer.getDefault().setStatusText(message);
            }
            try {
                DataObject dob = DataObject.find(file);
                EditorCookie ed = dob.getCookie(EditorCookie.class);
                if (ed != null) {
                    if (line1 == -1) {
                        // OK, just open it.
                        ed.open();
                    } else {
                        ed.openDocument(); // XXX getLineSet does not do it for you!
                        errMgr.log("opened document for " + file); // NOI18N
                        Line l = ed.getLineSet().getOriginal(line1);
                        if (! l.isDeleted()) {
                            attachAsNeeded(l, ed);
                            if (col1 == -1) {
                                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            } else {
                                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, col1);
                            }
                        }
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } catch (DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, donfe);
            } catch (IndexOutOfBoundsException iobe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, iobe);
            } catch (IOException ioe) {
                // XXX see above, should not be necessary to call openDocument at all
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
            }
        }
        
        
        public void outputLineSelected(OutputEvent ev) {
            System.err.println("outputLineSelected: " + ev.getLine()); // NOI18N
            if (dead) {
                return;
            }
            if (! file.isValid()) {
                return;
            }
            try {
                DataObject dob = DataObject.find(file);
                EditorCookie ed = dob.getCookie(EditorCookie.class);
                if (ed != null) {
                    if (ed.getDocument() == null) {
                        // The document is not opened, don't bother with it.
                        // The Line.Set will be corrupt anyway, currently.
                        errMgr.log("no document for " + file); // NOI18N
                        return;
                    }
                    errMgr.log("got document for " + file); // NOI18N
                    if (line1 != -1) {
                        Line l = ed.getLineSet().getOriginal(line1);
                        if (! l.isDeleted()) {
                            attachAsNeeded(l, ed);
                            if (col1 == -1) {
                                l.show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.NONE);
                            } else {
                                l.show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.NONE, col1);
                            }
                        }
                    }
                }
            } catch (DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, donfe);
            } catch (IndexOutOfBoundsException iobe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, iobe);
            }
        }
        
        
        private synchronized void attachAsNeeded(Line l, EditorCookie ed) {
            
            if (getAttachedAnnotatable() == null) {
                boolean log = errMgr.isLoggable(ErrorManager.UNKNOWN);
                Annotatable ann;
                // Text of the line, incl. trailing newline.
                String text = l.getText();
                System.err.println("attachAsNeeded: " + text); // NOI18N
                if (log) {errMgr.log("Attaching to line " + l.getDisplayName() + " text=`" + text + "' line1=" + line1 + " line2=" + line2 + " col1=" + col1 + " col2=" + col2);} // NOI18N
                if (text != null && (line2 == -1 || line1 == line2) && col1 != -1) {
                    if (log) {errMgr.log("\tfits on one line");} // NOI18N
                    if (col2 != -1 && col2 >= col1 && col2 < text.length()) {
                        if (log) {errMgr.log("\tspecified section of the line");} // NOI18N
                        ann = l.createPart(col1, col2 - col1 + 1);
                    } else if (col1 < text.length()) {
                        if (log) {errMgr.log("\tspecified column to end of line");} // NOI18N
                        ann = l.createPart(col1, text.length() - col1);
                    } else {
                        if (log) {errMgr.log("\tcolumn numbers are bogus");} // NOI18N
                        ann = l;
                    }
                } else {
                    if (log) {errMgr.log("\tmultiple lines, something wrong with line, or no column given");} // NOI18N
                    ann = l;
                }
                attach(ann);
                ann.addPropertyChangeListener(this);
            }
        }
        
        
        private synchronized void doDetach() {
            Annotatable ann = getAttachedAnnotatable();
            if (ann != null) {
                if (errMgr.isLoggable(ErrorManager.UNKNOWN)) {
                    errMgr.log("Detaching from " + ann + " `" + ann.getText() + "'"); // NOI18N
                }
                ann.removePropertyChangeListener(this);
                detach();
            }
        }
        
        
        public void outputLineCleared(OutputEvent ev) {
            doDetach();
        }
        
        
        public void propertyChange(PropertyChangeEvent ev) {
            if (dead) {
                return;
            }
            String prop = ev.getPropertyName();
            if (prop == null ||
                    prop.equals(Annotatable.PROP_TEXT) ||
                    prop.equals(Annotatable.PROP_DELETED)) {
                // Affected line has changed.
                // Assume user has edited & corrected the error.
                if (errMgr.isLoggable(ErrorManager.UNKNOWN)) {
                    errMgr.log("Received Annotatable property change: " + prop); // NOI18N
                }
                doDetach();
            }
        }
        
        
        // Annotation:
        public String getAnnotationType() {
            return "org-apache-tools-ant-module-error"; // NOI18N
        }
        
        
        public String getShortDescription() {
            return message;
        }
        
        
        // Debugging:
        @Override
        public String toString() {
            return "Hyperlink[" + file + ":" + line1 + ":" + col1 + ":" + line2 + ":" + col2 + "]"; // NOI18N
        }
    }
    
    private static final class HyperlinkFactory implements Comparator<String> {
        
        /** used only in constructor */
        private Map<String,FileSystem> fss0; // Map
        
        /** list of filesystem prefixes, akin to a classpath, mapped to filesystems */
        private SortedMap<String,FileSystem> fss; // SortedMap<String,FileSystem>
        
        public HyperlinkFactory() {
            fss0 = new HashMap<String,FileSystem>();
            fss = new TreeMap<String,FileSystem>(this);
            FileObject root = FileUtil.getConfigRoot();
            FileSystem fs = null;
            try {
                fs = root.getFileSystem();
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            File path = FileUtil.toFile(root);
            if (path != null) {
                String prefix = path.getAbsolutePath();
                if (! prefix.endsWith(File.separator)) {
                    prefix += File.separator;
                }
                if (path != null) {
                    fss0.put(prefix, fs);
                    fss.put(prefix, fs);
                }
            }
            fss0 = null;
        }
        
        
        /** returns a hyperlink for the line, or null if not apropos */
        public Hyperlink create(String line) {
            //dbout.println("HlF.create: line = \"" + line + "\"");
            // Look through the list of filesystem prefixes in order...
            Iterator it = fss.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String prefix = (String) entry.getKey();
                
                if (line.startsWith(prefix) ||
                        // #11513 - Jikes may use wrong sep on Win
                        line.replace('/', File.separatorChar).startsWith(prefix)) {
                    int colon1 = line.indexOf(':', prefix.length());
                    if (colon1 != -1) {
                        String filename = line.substring(prefix.length(), colon1)
                        .replace(File.separatorChar, '/');
                        FileSystem fs = (FileSystem) entry.getValue();
                        // XXX consider using FileUtil.fromFile for this at some point...
                        FileObject fo = fs.findResource(filename);
                        if (fo != null) {
                            int line1 = -1, col1 = -1, line2 = -1, col2 = -1;
                            int start = colon1 + 1; // start of message
                            int colon2 = line.indexOf(':', colon1 + 1);
                            if (colon2 != -1) {
                                try {
                                    line1 = Integer.parseInt(line.substring(colon1 + 1, colon2).trim()) - 1;
                                    start = colon2 + 1;
                                    int colon3 = line.indexOf(':', colon2 + 1);
                                    if (colon3 != -1) {
                                        col1 = Integer.parseInt(line.substring(colon2 + 1, colon3).trim()) - 1;
                                        start = colon3 + 1;
                                        int colon4 = line.indexOf(':', colon3 + 1);
                                        if (colon4 != -1) {
                                            line2 = Integer.parseInt(line.substring(colon3 + 1, colon4).trim()) - 1;
                                            start = colon4 + 1;
                                            int colon5 = line.indexOf(':', colon4 + 1);
                                            if (colon5 != -1) {
                                                col2 = Integer.parseInt(line.substring(colon4 + 1, colon5).trim()) - 1;
                                                start = colon5 + 1;
                                            }
                                        }
                                    }
                                } catch (NumberFormatException nfe) {
                                    // Fine, rest is part of the message.
                                }
                            }
                            String message = line.substring(start).trim();
                            if (message.length() == 0) {
                                message = null;
                            }
                            Hyperlink h = new Hyperlink(fo, line1, col1, line2, col2, message);
                            //debug ("Got " + h + " for: " + line); // NOI18N
                            return h;
                        } else {
                            //debug ("no fo for: " + line + " fs=" + fs.getDisplayName () + " prefix=" + prefix + " filename=" + filename); // NOI18N
                        }
                    } else {
                        //debug ("colon1 not found for: " + line); // NOI18N
                    }
                }
            }
            //debug ("nothing for: " + line); // NOI18N
            return null;
        }
        
        
        private static PrintWriter debugwriter = null;
        
        private static void debug(String s) {
            if (debugwriter == null) {
                try {
                    debugwriter = new PrintWriter(new FileWriter("/dev/tty")); // NOI18N
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                    return;
                }
            }
            debugwriter.println("[owos]: " + s); // NOI18N
            debugwriter.flush();
        }
        /*
         */
        
        
        /** compare prefixes */
        public int compare(String o1, String o2) {
            FileSystem f1 = fss0.get(o1);
            FileSystem f2 = fss0.get(o2);
            // The same; compare length of prefixes. Longer prefixes
            // are more specific, thus preference (earlier in list).
            int comp = o2.length() - o1.length();
            //debug ("comp=" + comp); // NOI18N
            if (comp != 0) {
                return comp;
            }
            return System.identityHashCode(f1) - System.identityHashCode(f2);
        }
    }
    
}
