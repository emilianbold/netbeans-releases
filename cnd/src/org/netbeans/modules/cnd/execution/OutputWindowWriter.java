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
package org.netbeans.modules.cnd.execution;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
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

    private final ExecutionEnvironment execEnv;
    private final OutputWriter delegate;
    private final StringBuffer buffer;
    private final boolean parseOutputForErrors;
//    private FileObject relativeTo;
    
    private static final String LINE_SEPARATOR_QUOTED = System.getProperty("line.separator");  // NOI18N
    
    private final ErrorParser[] parsers;
    
    private static final Pattern SS_OF_1 = Pattern.compile("::\\(.*\\)");// NOI18N
    private static final Pattern SS_OF_2 = Pattern.compile(":\\(.*\\).*");// NOI18N
    private static final Pattern SS_OF_3 = Pattern.compile("\\(.*\\).*:");// NOI18N
    private static final Pattern[] SunStudioOutputFilters = new Pattern[] {SS_OF_1, SS_OF_2, SS_OF_3};
    
    public OutputWindowWriter(ExecutionEnvironment execEnv, OutputWriter delegate, FileObject relativeTo, boolean parseOutputForErrors) {
        this.execEnv = execEnv;
        this.delegate = delegate;
//        this.relativeTo = relativeTo;
        this.parseOutputForErrors = parseOutputForErrors;
        this.buffer = new StringBuffer();
        this.parsers = new ErrorParser[] {
            new GCCErrorParser(execEnv, relativeTo),
            new SUNErrorParser(execEnv, relativeTo),
            new MSVCErrorParser(execEnv, relativeTo),
            new CWErrorParser(execEnv, relativeTo),
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

    private static final int LENGTH_TRESHOLD = 2048;
    
    private void handleLine(String line) throws IOException {
        if (parseOutputForErrors && 
                line.length() < LENGTH_TRESHOLD) 
                // We can ignore strings which can't be compiler messages 
                // (their's length is capped by max(filename) + max(error desc)).
                // See IZ#124796 for details about perf issues with very long lines.
        {
            for (int cntr = 0; cntr < parsers.length; cntr++) {
                Pattern[] patterns = parsers[cntr].getPattern();

                for (int pi = 0; pi < patterns.length; pi++) {
                    Pattern p = patterns[pi];
                    Matcher m = p.matcher(line);
                    boolean found = m.find();

                    if (found && m.start() == 0) {
                        if (parsers[cntr].handleLine(delegate, line, m)) {
                            return ;
                        }
                    }
                }
            }
            
            // Remove lines extra lines from Sun Compiler output
            for (int i = 0; i < SunStudioOutputFilters.length; i++) {
                Matcher m = SunStudioOutputFilters[i].matcher(line);
                boolean found = m.find();
//                System.out.println("  " + found);
//                if (found)
//                    System.out.println("  " + m.start());
                if (found && m.start() == 0) {
                    return;
                }
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
            showLine(false);
        }
        
        public void outputLineAction(OutputEvent ev) {
            showLine(true);
        }
        
        public void outputLineCleared(OutputEvent ev) {
            ErrorAnnotation.getInstance().detach(null);
        }
        
        private void showLine(boolean openTab) {
            try {
                DataObject od = DataObject.find(file);
                LineCookie lc = od.getCookie(LineCookie.class);
                
                if (lc != null) {
                    try {
                        // TODO: IZ#119211
                        // Preprocessor supports #line directive => 
                        // line number can be out of scope
                        Line l = lc.getLineSet().getOriginal(line);

                        if (!l.isDeleted()) {
                            if (openTab) {
                                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            } else {
                                l.show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.NONE);
                            }
                            ErrorAnnotation.getInstance().attach(l);
                        }
                    } catch (IndexOutOfBoundsException ex) {
                       // something wrong with line number see IZ#118667
                    }
                }
            }  catch (DataObjectNotFoundException ex) {
                // project/file can be deleted 
                //ErrorManager.getDefault().notify(ex);
            }          
        }
    }
    
    private static abstract class ErrorParser {

        protected FileObject relativeTo;
        protected final ExecutionEnvironment execEnv;

        public ErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
            this.relativeTo = relativeTo;
            this.execEnv = execEnv;
        }
        
        public abstract boolean handleLine(OutputWriter delegate, String line, Matcher m) throws IOException;
        
        public abstract Pattern[] getPattern();
        
        protected FileObject resolveFile(String fileName) {
            if (Utilities.isWindows()) {
                //replace /cygdrive/<something> prefix with <something>:/ prefix:
                if (fileName.startsWith("/cygdrive/")) { // NOI18N
                    fileName = fileName.substring("/cygdrive/".length()); // NOI18N
                    fileName = "" + fileName.charAt(0) + ':' + fileName.substring(1); // NOI18N
                    fileName = fileName.replace('/', '\\');
                }
            }
            fileName = HostInfoProvider.getMapper(execEnv).getLocalPath(fileName,true);
            File file = CndFileUtils.normalizeFile(new File(fileName));
            return FileUtil.toFileObject(file);
        }

        protected FileObject resolveRelativePath(FileObject relativeDir, String relativePath) {
            if (IpeUtils.isPathAbsolute(relativePath)){ // NOI18N
                if (execEnv.isRemote() || Utilities.isWindows()) {
                    // See IZ 106841 for details.
                    // On Windows the file path for system header files comes in as /usr/lib/abc/def.h
                    // but the real path is something like D:/cygwin/lib/abc/def.h (for Cygwin installed
                    // on D: drive). We need the exact compiler that produced this output to safely
                    // convert the path but the compiler has been lost at this point. To work-around this problem
                    // iterate over all defined compiler sets and test whether the file existst in a set.
                    // If it does, convert it to a FileObject and return it.
                    // FIXUP: pass exact compiler used to this method (would require API changes we
                    // don't want to do now). Error/warning regular expressions should also be moved into
                    // the compiler(set) and the output should only be scanned for those patterns.
                    String absPath1 = relativePath;
                    String absPath2 = null;
                    if (absPath1.startsWith("/usr/lib")) { // NOI18N
                        absPath2 = absPath1.substring(4);
                    }
                    List<CompilerSet> compilerSets = CompilerSetManager.getDefault(execEnv).getCompilerSets();
                    for (CompilerSet set : compilerSets) {
                        Tool cCompiler = set.getTool(Tool.CCompiler);
                        if (cCompiler != null) {
                            String includePrefix = cCompiler.getIncludeFilePathPrefix();
                            File file = new File(includePrefix + absPath1);
                            if (!CndFileUtils.exists(file) && absPath2 != null) {
                                file = new File(includePrefix + absPath2);
                            }
                            if (CndFileUtils.exists(file)) {
                                FileObject fo = FileUtil.toFileObject( CndFileUtils.normalizeFile(file));
                                return fo;
                            }
                        }
                    }
                }
                FileObject myObj = resolveFile(relativePath);
                if (myObj != null) {
                    return myObj;
                }
                if (relativePath.startsWith(File.separator)){ // NOI18N
                    relativePath = relativePath.substring(1);
                }
                try {
                    FileSystem fs = relativeDir.getFileSystem();
                    myObj = fs.findResource(relativePath);
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
            String delims = Utilities.isWindows()? File.separator + '/' : File.separator; // NOI18N
            StringTokenizer st = new StringTokenizer(relativePath, delims);

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
    }

    private static final Pattern CW_ERROR_SCANNER = Pattern.compile("([^:\n]*):([0-9]+): .*"); // NOI18N
        
    private static final class CWErrorParser extends ErrorParser {
        
        private boolean failed;
        
        public CWErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo ) {
            super(execEnv, relativeTo);
        }
        
        public boolean handleLine( OutputWriter delegate, String line, Matcher m ) throws IOException {
            if( m.pattern() == CW_ERROR_SCANNER ) {
                try {                
                    String file = m.group( 1 );
                    Integer lineNumber = Integer.valueOf( m.group( 2 ));
                    FileObject fo = FileUtil.toFileObject( CndFileUtils.normalizeFile( new File( FileUtil.toFile( relativeTo ), file )));
                    
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

    private static final class MSVCErrorParser extends ErrorParser {

        private boolean failed;

        public MSVCErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo ) {
            super(execEnv, relativeTo);
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
    private static final Pattern GCC_ERROR_SCANNER_INTEL = Pattern.compile("([^\\(\n]*)\\(([0-9]+)\\): ([^:\n]*): ([^\n]*)"); // NOI18N
    private static final Pattern GCC_DIRECTORY_ENTER = Pattern.compile("[gd]?make(?:\\.exe)?(?:\\[([0-9]+)\\])?: Entering[\\w+\\s+]+`([^']*)'"); // NOI18N
    private static final Pattern GCC_DIRECTORY_LEAVE = Pattern.compile("[gd]?make(?:\\.exe)?(?:\\[([0-9]+)\\])?: Leaving[\\w+\\s+]+`([^']*)'"); // NOI18N
    private static final Pattern GCC_DIRECTORY_CD    = Pattern.compile("cd\\s+([\\S]+)[\\s;]");// NOI18N
    private static final Pattern GCC_STACK_HEADER = Pattern.compile("In file included from ([A-Z]:[^:\n]*|[^:\n]*):([^:^,]*)"); // NOI18N
    private static final Pattern GCC_STACK_NEXT =   Pattern.compile("                 from ([A-Z]:[^:\n]*|[^:\n]*):([^:^,]*)"); // NOI18N
    
    
    private static final class GCCErrorParser extends ErrorParser {
        
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
        
        private Stack<FileObject> relativesTo = new Stack<FileObject>();
        private Stack<Integer> relativesLevel = new Stack<Integer>();
        private ArrayList<StackIncludeItem> errorInludes =new ArrayList<StackIncludeItem>();
        private boolean failed;
        private boolean isEntered;
        
        public GCCErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo ) {
            super(execEnv, relativeTo);
            this.relativesTo.push(relativeTo);
            this.relativesLevel.push(0);
            this.isEntered = false;
        }
        
        // FIXUP IZ#115960 and all other about EmptyStackException
        // - make Stack.pop() and peek() safe.
        private void popPath(){
            if (relativesTo.size()>1) {
                relativesTo.pop();
            }
        }
        
        private void popLevel(){
           if (relativesLevel.size()>1) {
               relativesLevel.pop();
           }
        }
        
        public boolean handleLine(OutputWriter delegate, String line, Matcher m) throws IOException {
            
            if (m.pattern() == GCC_DIRECTORY_ENTER || m.pattern() == GCC_DIRECTORY_LEAVE) {
                String levelString = m.group(1);
                int level = levelString == null? 0 : Integer.valueOf(levelString);
                int baseLavel = relativesLevel.peek().intValue();
                String directory = m.group(2);
                
                if (level > baseLavel) {
                    isEntered = true;
                    relativesLevel.push(level);
                    isEntered = true;
                } else if (level == baseLavel) {
                    isEntered = !this.isEntered;
                } else {
                    isEntered = false;
                    popLevel();
                }
                
                if (isEntered) {
                    if (!IpeUtils.isPathAbsolute(directory)) { 
                        if (relativeTo != null) {
                            if (relativeTo.isFolder()) {
                                directory = relativeTo.getURL().getPath() + File.separator + directory;
                            }
                        }
                    }
                    
                    FileObject relativeDir = resolveFile(directory);

                    if (relativeDir != null) {
                        relativesTo.push(relativeDir);
                    }
                    return false;
                } else {
                    popPath();
                    return false;
                }
            }
            
            if (m.pattern() == GCC_DIRECTORY_CD) {
                String directory = m.group(1);
                if (!IpeUtils.isPathAbsolute(directory)) { 
                    if (relativeTo != null) {
                        if (relativeTo.isFolder()) {
                            directory = relativeTo.getURL().getPath() + File.separator + directory;
                        }
                    }
                }
                
                FileObject relativeDir = resolveFile(directory);
                if (relativeDir != null) {
                    relativesTo.push(relativeDir);
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
                    FileObject relativeDir = relativesTo.peek();
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
                    FileObject relativeDir = relativesTo.peek();
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
                (m.pattern() == GCC_ERROR_SCANNER_ANOTHER) ||
                (m.pattern() == GCC_ERROR_SCANNER_INTEL) ||
                (m.pattern() == MSVC_WARNING_SCANNER) ||
                (m.pattern() == MSVC_ERROR_SCANNER)){
                try {
                    String file = m.group(1);
                    Integer lineNumber = Integer.valueOf(m.group(2));
                    FileObject relativeDir = relativesTo.peek();
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
            return new Pattern[] {GCC_DIRECTORY_ENTER, GCC_DIRECTORY_LEAVE, GCC_DIRECTORY_CD, GCC_STACK_HEADER, GCC_STACK_NEXT, GCC_ERROR_SCANNER,
            GCC_ERROR_SCANNER_ANOTHER, GCC_ERROR_SCANNER_INTEL,
            MSVC_WARNING_SCANNER, MSVC_ERROR_SCANNER
            };
        }
        
    }

    private static final Pattern SUN_ERROR_SCANNER_CPP_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+): Error:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_CPP_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): Warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+):"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_ERROR = Pattern.compile("^\"(.*)\", Line = ([0-9]+),"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_WARNING = Pattern.compile("^\"(.*)\", Line = ([0-9]+), Column = ([0-9]+): WARNING:"); // NOI18N
    private static final Pattern SUN_DIRECTORY_ENTER = Pattern.compile("\\(([^)]*)\\)[^:]*:"); // NOI18N

    private static final class SUNErrorParser extends ErrorParser {
        
        public SUNErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo ) {
            super(execEnv, relativeTo);
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
