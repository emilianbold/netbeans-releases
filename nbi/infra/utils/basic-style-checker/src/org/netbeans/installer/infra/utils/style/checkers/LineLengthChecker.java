package org.netbeans.installer.infra.utils.style.checkers;

import java.io.File;

/**
 *
 * @author ks152834
 */
public class LineLengthChecker implements Checker {
    public boolean accept(final File file) {
        return file.getName().endsWith(".java");
    }
    
    public String check(final String line) {
        if (line.length() > 85) {
            return "Line length exceeeds 85 characters.";
        }
        
        return null;
    }
}
