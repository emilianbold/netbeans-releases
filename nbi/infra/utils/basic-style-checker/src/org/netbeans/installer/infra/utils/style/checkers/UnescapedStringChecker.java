package org.netbeans.installer.infra.utils.style.checkers;

import java.io.File;

/**
 *
 * @author ks152834
 */
public class UnescapedStringChecker implements Checker {
    public boolean accept(final File file) {
        return file.getName().endsWith(".java"); // NOI18N
    }

    public String check(final String line) {
        if ((line.indexOf("\"") > -1) && // NOI18N
                (line.indexOf("// NOI18N") == -1)) { // NOI18N
            return "Unescaped string constant."; // NOI18N
        }
        
        return null;
    }
}
