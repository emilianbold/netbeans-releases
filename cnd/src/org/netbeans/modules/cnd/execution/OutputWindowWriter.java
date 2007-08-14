/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.execution;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.cookies.LineCookie;
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
import org.openide.util.Utilities;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


public class OutputWindowWriter extends Writer {
    
    private OutputWriter delegate;
    private StringBuffer buffer;
    private boolean parseOutputForErrors;
//    private FileObject relativeTo;
    
    private static final String LINE_SEPARATOR_QUOTED = System.getProperty("line.separator");  // NOI18N
    
    private final ErrorParser[] parsers;
    
    public OutputWindowWriter(OutputWriter delegate, FileObject relativeTo, boolean parseOutputForErrors) {
        this.delegate = delegate;
//        this.relativeTo = relativeTo;
        this.parseOutputForErrors = parseOutputForErrors;
        this.buffer = new StringBuffer();
        this.parsers = new ErrorParser[] {
            new GCCErrorParser(relativeTo),
            new SUNErrorParser(relativeTo),
            new MSVCErrorParser(relativeTo),
            new CWErrorParser( relativeTo ),
        };
        
        ErrorAnnotation.getInstance().detach(null);
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        buffer.append(new String(cbuf, off, len).replaceAll(LINE_SEPARATOR_QUOTED, "\n")); // NOI18N
        
        int eolIndex;
        
        while ((eolIndex = buffer.indexOf("\n")) != (-1)) {  // NOI18N
            handleLine(buffer.substring(0, eolIndex));
            buffer.delete(0, eolIndex + "\n".length() + 1);  // NOI18N
        }
    }
    
    public void flush() throws IOException {
        //ignored.
    }
    
    public void close() throws IOException {
        delegate.close();
    }

    private static FileObject resolveFile(String fileName) {
        if (Utilities.isWindows()) {
            //replace /cygdrive/<something> prefix with <something>:/ prefix:
            if (fileName.startsWith("/cygdrive/")) { // NOI18N
                fileName = fileName.substring("/cygdrive/".length()); // NOI18N
                fileName = "" + fileName.charAt(0) + ':' + fileName.substring(1); // NOI18N
                fileName = fileName.replace('/', File.separatorChar);
            }
        }
        
	File directory = FileUtil.normalizeFile(new File(fileName));
        
        return FileUtil.toFileObject(directory);
    }

    private static FileObject resolveRelativePath(FileObject relativeDir, String relativePath) {
        if (IpeUtils.isPathAbsolute(relativePath)){ // NOI18N
            if (relativePath.startsWith(File.separator)){ // NOI18N
                relativePath = relativePath.substring(1);
            }
            try {
                FileSystem fs = relativeDir.getFileSystem();
                FileObject myObj = fs.findResource(relativePath);
                if (myObj != null) {
                    return myObj;
                }
                myObj = fs.getRoot();
                if (myObj != null) {
                    relativeDir = myObj;
                }
            } catch (FileStateInvalidException ex) {
                //ex.printStackTrace();
            }
        }
        
        FileObject myObj = relativeDir;
        StringTokenizer st = new StringTokenizer(relativePath, File.separator); // NOI18N
        
        while ((myObj != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            if ("..".equals(nameExt)){ // NOI18N
                myObj = myObj.getParent();
            } else if (".".equals(nameExt)){ // NOI18N
                // current
            } else {
                myObj = myObj.getFileObject(nameExt, null);
            }
        }
        
        return myObj;
    }
    
    private void handleLine(String line) throws IOException {
        if (parseOutputForErrors) {
            for (int cntr = 0; cntr < parsers.length; cntr++) {
                Pattern[] patterns = parsers[cntr].getPattern();

                for (int pi = 0; pi < patterns.length; pi++) {
                    Pattern p = patterns[pi];
                    Matcher m = p.matcher(line);
                    boolean found = m.find();

                    if (found && m.start() == 0) {
                        if (parsers[cntr].handleLine(delegate, line, m))
                            return ;
                    }
                }
            }
            // Remove lines extra lines from Sun Compiler output
            if (line.equals("::(build)")) { // NOI18N
                return;
            }
        }
        
        delegate.println(line);
    }
    
    private static final class OutputListenerImpl implements OutputListener {
        
        private FileObject file;
        private int line;
        
        public OutputListenerImpl(FileObject file, int line) {
            this.file = file;
            this.line = line;
        }
        
        public void outputLineSelected(OutputEvent ev) {
            //next action:
            try {
                DataObject od = DataObject.find(file);
                LineCookie lc = (LineCookie) od.getCookie(LineCookie.class);
                
                if (lc != null) {
                    Line l = lc.getLineSet().getOriginal(line);
                    
                    if (!l.isDeleted()) {
                        l.show(Line.SHOW_GOTO);
                        ErrorAnnotation.getInstance().attach(l);
                    }
                }
            }  catch (DataObjectNotFoundException ex) {
                // project/file can be deleted 
                //ErrorManager.getDefault().notify(ex);
            }
        }
        
        public void outputLineAction(OutputEvent ev) {
            //goto:
            try {
                DataObject od = DataObject.find(file);
                LineCookie lc = (LineCookie) od.getCookie(LineCookie.class);
                
                if (lc != null) {
                    Line l = lc.getLineSet().getOriginal(line);
                    
                    if (!l.isDeleted()) {
                        l.show(Line.SHOW_GOTO);
                        ErrorAnnotation.getInstance().attach(l);
                    }
                }
            }  catch (DataObjectNotFoundException ex) {
                // project/file can be deleted 
                //ErrorManager.getDefault().notify(ex);
            }
        }
        
        public void outputLineCleared(OutputEvent ev) {
            ErrorAnnotation.getInstance().detach(null);
        }
    }
    
    private static interface ErrorParser {
        
        public boolean handleLine(OutputWriter delegate, String line, Matcher m) throws IOException;
        
        public Pattern[] getPattern();
        
    }

    private static final Pattern CW_ERROR_SCANNER = Pattern.compile("([^:\n]*):([0-9]+): .*"); // NOI18N
        
    private static final class CWErrorParser implements ErrorParser {
        
        private FileObject relativeTo;
        private boolean failed;
        
        public CWErrorParser( FileObject relativeTo ) {
            this.relativeTo = relativeTo;
        }
        
        public boolean handleLine( OutputWriter delegate, String line, Matcher m ) throws IOException {
            if( m.pattern() == CW_ERROR_SCANNER ) {
                try {                
                    String file = m.group( 1 );
                    Integer lineNumber = Integer.valueOf( m.group( 2 ));
                    FileObject fo = FileUtil.toFileObject( FileUtil.normalizeFile( new File( FileUtil.toFile( relativeTo ), file )));
                    
                    if( fo == null ) {
                        return false;
                    }
                    
                    delegate.println( line, new OutputListenerImpl( fo, lineNumber.intValue() - 1 ), true );
                    
                    if( !failed ) {
//                        relativeTo.createData( ".fail" ); // NOI18N
                        failed = true;
                    }
                    
                    return true;
                } catch( NumberFormatException e ) {
                    //ignore.
                }                     
            }
            return false;
        }

        public Pattern[] getPattern() {
            return new Pattern[] { CW_ERROR_SCANNER };
        }
        
    }
    
    private static final Pattern MSVC_WARNING_SCANNER = Pattern.compile( "([a-zA-Z0-0\\\\._]+)\\(([0-9]+)\\) : warning ([a-zA-Z0-9]+): .*" ); // NOI18N
    private static final Pattern MSVC_ERROR_SCANNER = Pattern.compile( "([a-zA-Z0-0\\\\._]+)\\(([0-9]+)\\) : error ([a-zA-Z0-9]+): .*" ); // NOI18N

    private static final class MSVCErrorParser implements ErrorParser {

        private FileObject relativeTo;
        private boolean failed;

        public MSVCErrorParser( FileObject relativeTo ) {
            this.relativeTo = relativeTo;
        }

        public boolean handleLine(OutputWriter delegate, String line, Matcher m) throws IOException {
            if( m.pattern() == MSVC_ERROR_SCANNER ||
                m.pattern() == MSVC_WARNING_SCANNER )
            {
                try {
                    String file = m.group( 1 );
                    Integer lineNumber = Integer.valueOf( m.group( 2 ));
                    FileObject fo = relativeTo.getFileSystem().getRoot().getFileObject(file);
                    
                    if( fo == null ) {
                        return false;
                    }
                    
                    boolean important = m.pattern() == MSVC_ERROR_SCANNER;

                    if( fo != null ) {
                        delegate.println( line, new OutputListenerImpl( fo, lineNumber.intValue() - 1 ), important );
                        if( !failed ) {
//                            relativeTo.createData( ".fail" ); // NOI18N
                            failed = true;
                        }
                        return true;
                    }
                } catch( NumberFormatException e ) {
                    //ignore.
                } 
                return false;
            }
            
            throw new IllegalArgumentException( "Unknown pattern: " + m.pattern().pattern()); // NOI18N
        }

        public Pattern[] getPattern() {
            return new Pattern[] {
                MSVC_WARNING_SCANNER, MSVC_ERROR_SCANNER
            };
        }
    }

    private static final Pattern GCC_ERROR_SCANNER = Pattern.compile("([a-zA-Z]:[^:\n]*|[^:\n]*):([^:\n]*):([^:\n]*):([^\n]*)"); // NOI18N
    private static final Pattern GCC_ERROR_SCANNER_ANOTHER = Pattern.compile("([^:\n]*):([0-9]+): ([a-zA-Z]*):*.*"); // NOI18N    
    private static final Pattern GCC_DIRECTORY_ENTER = Pattern.compile("[gd]?make\\[([0-9]+)\\]: Entering[\\w+\\s+]+`([^']*)'"); // NOI18N
    private static final Pattern GCC_DIRECTORY_LEAVE = Pattern.compile("[gd]?make\\[([0-9]+)\\]: Leaving[\\w+\\s+]+`([^']*)'"); // NOI18N
    private static final Pattern GCC_DIRECTORY_CD    = Pattern.compile("cd\\s+([\\S]+)[\\s;]");// NOI18N
    private static final Pattern GCC_STACK_HEADER = Pattern.compile("In file included from ([A-Z]:[^:\n]*|[^:\n]*):([^:^,]*)"); // NOI18N
    private static final Pattern GCC_STACK_NEXT =   Pattern.compile("                 from ([A-Z]:[^:\n]*|[^:\n]*):([^:^,]*)"); // NOI18N
    
    
    private static final class GCCErrorParser implements ErrorParser {
        
        private static class StackIncludeItem {
            private FileObject fo;
            private String line;
            private int lineNumber;
            
            private StackIncludeItem(FileObject fo, String line, int lineNumber){
                this.fo = fo;
                this.line = line;
                this.lineNumber= lineNumber;
            }
        }
        
        private Stack/*<FileObject>*/   relativeTo;
        private Stack/*int*/            relativeLevel;
        private ArrayList/*<StackIncludeItem>*/ errorInludes;
        private FileObject relativeToFO;
        private boolean failed;
        private boolean isEntered;
        
        public GCCErrorParser(FileObject relativeTo) {
            this.relativeToFO = relativeTo;
            this.relativeTo = new Stack();
            this.errorInludes = new ArrayList();
            this.relativeLevel = new Stack();
            
            this.relativeTo.push(relativeTo);
            this.relativeLevel.push(0);
            this.isEntered = false;
        }
        
        public boolean handleLine(OutputWriter delegate, String line, Matcher m) throws IOException {
            
            if (m.pattern() == GCC_DIRECTORY_ENTER || m.pattern() == GCC_DIRECTORY_LEAVE) {
                int level = Integer.valueOf((m.group(1)));
                int baseLavel = Integer.valueOf(this.relativeLevel.peek() + "");
                String directory = m.group(2);
                
                if (level > baseLavel) {
                    this.isEntered = true;
                    this.relativeLevel.push(level);
                    this.isEntered = true;
                } else if (level == baseLavel) {
                    this.isEntered = !this.isEntered;
                } else {
                    this.isEntered = false;
                    this.relativeLevel.pop();
                }
                
                if (this.isEntered) {
                    if (!IpeUtils.isPathAbsolute(directory)) { 
                        if (this.relativeToFO != null) {
                            if (this.relativeToFO.isFolder()) {
                                directory = this.relativeToFO.getURL().getPath() + File.separator + directory;
                            }
                        }
                    }
                    
                    FileObject relativeDir = resolveFile(directory);

                    if (relativeDir != null) {
                        relativeTo.push(relativeDir);
                    }
                    
                    return false;
                    
                } else {
                    relativeTo.pop();
                    return false;
                }
                
                
            }
            
            if (m.pattern() == GCC_DIRECTORY_CD) {
                String directory = m.group(1);
                if (!IpeUtils.isPathAbsolute(directory)) { 
                    if (this.relativeToFO != null) {
                        if (this.relativeToFO.isFolder()) {
                            directory = this.relativeToFO.getURL().getPath() + File.separator + directory;
                        }
                    }
                }
                
                FileObject relativeDir = resolveFile(directory);
                if (relativeDir != null) {
                    
                    relativeTo.push(relativeDir);
                }
                
                return false;
            }
            

            if (m.pattern() == GCC_STACK_HEADER) {
                for(Iterator it = errorInludes.iterator(); it.hasNext();){
                    StackIncludeItem item = (StackIncludeItem)it.next();
                    delegate.println(item.line);
                }
                errorInludes.clear();
                try {
                    String file = m.group(1);
                    Integer lineNumber = Integer.valueOf(m.group(2));
                    FileObject relativeDir = (FileObject) relativeTo.peek();
                    if (relativeDir != null) {
                        FileObject fo = resolveRelativePath(relativeDir, file);
                        if (fo != null) {
                            errorInludes.add(new StackIncludeItem(fo, line, lineNumber.intValue() - 1));
                            return true;
                        }
                    }
                }  catch (NumberFormatException e) {
                    //ignore.
                }
                errorInludes.add(new StackIncludeItem(null, line, 0));
                return true;
            }

            if (m.pattern() == GCC_STACK_NEXT) {
                try {
                    String file = m.group(1);
                    Integer lineNumber = Integer.valueOf(m.group(2));
                    FileObject relativeDir = (FileObject) relativeTo.peek();
                    if (relativeDir != null) {
                        FileObject fo = resolveRelativePath(relativeDir, file);
                        if (fo != null) {
                            errorInludes.add(new StackIncludeItem(fo, line, lineNumber.intValue() - 1));
                            return true;
                        }
                    }
                }  catch (NumberFormatException e) {
                    //ignore.
                }
                errorInludes.add(new StackIncludeItem(null, line, 0));
                return true;
            }
            
            if ((m.pattern() == GCC_ERROR_SCANNER) ||
                (m.pattern() == GCC_ERROR_SCANNER_ANOTHER)){
                try {
                    String file = m.group(1);
                    Integer lineNumber = Integer.valueOf(m.group(2));
                    FileObject relativeDir = (FileObject) relativeTo.peek();
                    
                    if (relativeDir != null){
                        //FileObject fo = relativeDir.getFileObject(file);
                        FileObject fo = resolveRelativePath(relativeDir, file);

                        boolean important = m.group(3).indexOf("error") != (-1); // NOI18N
                        
                        if (fo != null) {
                            for(Iterator it = errorInludes.iterator(); it.hasNext();){
                                StackIncludeItem item = (StackIncludeItem)it.next();
                                if (item.fo != null) {
                                    delegate.println(item.line, new OutputListenerImpl(item.fo, item.lineNumber), important);
                                } else {
                                    delegate.println(item.line);
                                }
                            }
                            errorInludes.clear();
                            delegate.println(line, new OutputListenerImpl(fo, lineNumber.intValue() - 1), important);
                            if( !failed ) {
//                            relativeToFO.createData( ".fail" ); // NOI18N
                                failed = true;
                            }
                            return true;
                        }
                    }
                }  catch (NumberFormatException e) {
                    //ignore.
                }
                for(Iterator it = errorInludes.iterator(); it.hasNext();){
                    StackIncludeItem item = (StackIncludeItem)it.next();
                    delegate.println(item.line);
                }
                errorInludes.clear();
                
                return false;
            }
            
            throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
        }

        
        public Pattern[] getPattern() {
            return new Pattern[] {GCC_DIRECTORY_ENTER, GCC_DIRECTORY_LEAVE, GCC_DIRECTORY_CD, GCC_STACK_HEADER, GCC_STACK_NEXT, GCC_ERROR_SCANNER, GCC_ERROR_SCANNER_ANOTHER};
        }
        
    }

    private static final Pattern SUN_ERROR_SCANNER_CPP_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+): Error:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_CPP_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): Warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+):"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_ERROR = Pattern.compile("^\"(.*)\", Line = ([0-9]+),"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_WARNING = Pattern.compile("^\"(.*)\", Line = ([0-9]+), Column = ([0-9]+): WARNING:"); // NOI18N
    private static final Pattern SUN_DIRECTORY_ENTER = Pattern.compile("\\(([^)]*)\\)[^:]*:"); // NOI18N

    private static final class SUNErrorParser implements ErrorParser {
        
        private FileObject relativeTo;
        
        public SUNErrorParser(FileObject relativeTo) {
            this.relativeTo = relativeTo;
        }
        
        public boolean handleLine(OutputWriter delegate, String line, Matcher m) throws IOException {
            if (m.pattern() == SUN_DIRECTORY_ENTER) {
                relativeTo = resolveFile(m.group(1));
                return false;
            }
            if (    m.pattern() == SUN_ERROR_SCANNER_CPP_ERROR
                 || m.pattern() == SUN_ERROR_SCANNER_CPP_WARNING
                 || m.pattern() == SUN_ERROR_SCANNER_C_ERROR
                 || m.pattern() == SUN_ERROR_SCANNER_C_WARNING
                 || m.pattern() == SUN_ERROR_SCANNER_FORTRAN_ERROR
                 || m.pattern() == SUN_ERROR_SCANNER_FORTRAN_WARNING) {
                try {
                    String file = m.group(1);
                    Integer lineNumber = Integer.valueOf(m.group(2));
                    //FileObject fo = relativeTo.getFileObject(file);
                    FileObject fo = resolveRelativePath(relativeTo, file);
                    
                    boolean important = m.pattern() == SUN_ERROR_SCANNER_CPP_ERROR || m.pattern() == SUN_ERROR_SCANNER_C_ERROR || m.pattern() == SUN_ERROR_SCANNER_FORTRAN_ERROR;
                    
                    if (fo != null) {
                        delegate.println(line, new OutputListenerImpl(fo, lineNumber.intValue() - 1), important);
                        return true;
                    }
                }  catch (NumberFormatException e) {
                    //ignore.
                }
                
                return false;
            }
            
            throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
        }

        public Pattern[] getPattern() {
            return new Pattern[] {
                SUN_ERROR_SCANNER_CPP_ERROR,
                SUN_ERROR_SCANNER_CPP_WARNING,
                SUN_ERROR_SCANNER_FORTRAN_WARNING,
                SUN_ERROR_SCANNER_FORTRAN_ERROR,
                SUN_ERROR_SCANNER_C_WARNING,
                SUN_ERROR_SCANNER_C_ERROR/*keep this one at the end of the error patterns, the order is important*/,
                SUN_DIRECTORY_ENTER
            };
        }
        
    }

    /** Implements Annotation */
    private static class ErrorAnnotation extends Annotation implements PropertyChangeListener {
        private static ErrorAnnotation instance;
        private Line currentLine;
        
        public static ErrorAnnotation getInstance() {
            if (instance == null) {
                instance = new ErrorAnnotation();
            }
            
            return instance;
        }
        
        /** Returns name of the file which describes the annotation type.
         * The file must be defined in module installation layer in the
         * directory "Editors/AnnotationTypes"
         * @return  name of the anotation type */
        public String getAnnotationType() {
            return "org-netbeans-modules-cnd-error"; // NOI18N
        }
        
        /** Returns the tooltip text for this annotation.
         * @return  tooltip for this annotation */
        public String getShortDescription() {
            return NbBundle.getMessage(OutputWindowWriter.class, "HINT_CompilerError"); // NOI18N
        }
        
        public void attach(Line line) {
            if (currentLine != null) {
                detach(currentLine);
            }
            currentLine = line;
            super.attach(line);
            line.addPropertyChangeListener(this);
        }
        
        public void detach(Line line) {
            if (line == currentLine || line == null) {
                currentLine = null;
                Annotatable at = getAttachedAnnotatable();
                if (at != null) {
                    at.removePropertyChangeListener(this);
                }
                detach();
            }
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            if (Annotatable.PROP_TEXT.equals(ev.getPropertyName())) {
                detach(null);
            }
        }
    }
    
}
