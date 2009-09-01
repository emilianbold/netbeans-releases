package org.netbeans.modules.cnd.execution.impl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

public final class MSVCErrorParser extends ErrorParser {

    static final Pattern MSVC_WARNING_SCANNER = Pattern.compile( "([a-zA-Z0-0\\\\._]+)\\(([0-9]+)\\) : warning ([a-zA-Z0-9]+): .*" ); // NOI18N
    static final Pattern MSVC_ERROR_SCANNER = Pattern.compile( "([a-zA-Z0-0\\\\._]+)\\(([0-9]+)\\) : error ([a-zA-Z0-9]+): .*" ); // NOI18N
    private static final Pattern[] patterns = new Pattern[]{MSVC_WARNING_SCANNER, MSVC_ERROR_SCANNER};

    public MSVCErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(execEnv, relativeTo);
    }

    public Result handleLine(String line) throws IOException {
        for (int pi = 0; pi < patterns.length; pi++) {
            Pattern p = patterns[pi];
            Matcher m = p.matcher(line);
            boolean found = m.find();
            if (found && m.start() == 0) {
                return handleLine(line, m);
            }
        }
        return null;
    }

    private Result handleLine(String line, Matcher m) throws IOException {
        if (m.pattern() == MSVC_ERROR_SCANNER || m.pattern() == MSVC_WARNING_SCANNER) {
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                FileObject fo = relativeTo.getFileSystem().getRoot().getFileObject(file);
                if (fo == null) {
                    return NO_RESULT;
                }
                boolean important = m.pattern() == MSVC_ERROR_SCANNER;
                if (fo != null) {
                    return new Results(line, new OutputListenerImpl(fo, lineNumber.intValue() - 1), important);
                }
            } catch (NumberFormatException e) {
            }
            return NO_RESULT;
        }
        throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
    }
}
