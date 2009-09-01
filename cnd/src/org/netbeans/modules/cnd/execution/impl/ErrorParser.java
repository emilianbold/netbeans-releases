package org.netbeans.modules.cnd.execution.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.windows.OutputListener;

public abstract class ErrorParser {

    protected FileObject relativeTo;
    protected final ExecutionEnvironment execEnv;

    public ErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
        super();
        this.relativeTo = relativeTo;
        this.execEnv = execEnv;
    }

    public abstract Result handleLine(String line, Matcher m) throws IOException;

    public abstract Pattern[] getPattern();

    protected FileObject resolveFile(String fileName) {
        if (Utilities.isWindows()) {
            //replace /cygdrive/<something> prefix with <something>:/ prefix:
            if (fileName.startsWith("/cygdrive/")) {
                fileName = fileName.substring("/cygdrive/".length());
                fileName = "" + fileName.charAt(0) + ':' + fileName.substring(1);
            } else if (fileName.length() > 3 && fileName.charAt(0) == '/' && fileName.charAt(2) == '/') {
                // NOI18N
                fileName = "" + fileName.charAt(1) + ':' + fileName.substring(2);
            }
            if (fileName.startsWith("/") || fileName.startsWith(".")) {
                // NOI18N
                return null;
            }
            fileName = fileName.replace('/', '\\');
        }
        fileName = HostInfoProvider.getMapper(execEnv).getLocalPath(fileName, true);
        File file = CndFileUtils.normalizeFile(new File(fileName));
        return FileUtil.toFileObject(file);
    }

    protected FileObject resolveRelativePath(FileObject relativeDir, String relativePath) {
        if (IpeUtils.isPathAbsolute(relativePath)) {
            // NOI18N
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
                if (absPath1.startsWith("/usr/lib")) {
                    // NOI18N
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
                            FileObject fo = FileUtil.toFileObject(CndFileUtils.normalizeFile(file));
                            return fo;
                        }
                    }
                }
            }
            FileObject myObj = resolveFile(relativePath);
            if (myObj != null) {
                return myObj;
            }
            if (relativePath.startsWith(File.separator)) {
                // NOI18N
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
            }
        }
        FileObject myObj = relativeDir;
        String delims = Utilities.isWindows() ? File.separator + '/' : File.separator;
        // NOI18N
        StringTokenizer st = new StringTokenizer(relativePath, delims);
        while ((myObj != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            if ("..".equals(nameExt)) {
                myObj = myObj.getParent();
            } else if (".".equals(nameExt)) {
            } else {
                myObj = myObj.getFileObject(nameExt, null);
            }
        }
        return myObj;
    }
    protected static final Result NO_RESULT = new NoResult();
    protected static final Result REMOVE_LINE = new RemoveLine();

    public static abstract class Result {
        public abstract boolean result();
        public abstract List<ConvertedLine> converted();
    }

    private static final class NoResult extends Result {
        @Override
        public boolean result() {
            return false;
        }
        @Override
        public List<ConvertedLine> converted() {
            return Collections.<ConvertedLine>emptyList();
        }
    }

    private static final class RemoveLine extends Result {
        @Override
        public boolean result() {
            return true;
        }
        @Override
        public List<ConvertedLine> converted() {
            return Collections.<ConvertedLine>emptyList();
        }
    }

    protected static final class Results extends Result {
        List<ConvertedLine> result = new ArrayList<ConvertedLine>(1);
        public Results(){
        }
        public Results(String line, OutputListener listener, boolean important){
            result.add(ConvertedLine.forText(line, listener));
        }
        public void add(String line, OutputListener listener, boolean important) {
            result.add(ConvertedLine.forText(line, listener));
        }
        @Override
        public boolean result() {
            return true;
        }
        @Override
        public List<ConvertedLine> converted() {
            return result;
        }
    }
}
