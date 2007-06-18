package org.netbeans.installer.infra.utils.style.checkers;

import java.io.File;

/**
 *
 * @author ks152834
 */
public interface Checker {
    boolean accept(final File file);
    
    String check(final String line);
}
