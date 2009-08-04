
package org.netbeans.api.diff;

import org.netbeans.modules.diff.builtin.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.netbeans.modules.diff.builtin.ContextualPatch;
import org.netbeans.modules.diff.builtin.Patch;

/**
 * Utility class for patch application.
 * 
 * @author Tomas Stupka
 * @since 1.19
 */
public class PatchUtils {

    private PatchUtils() {}
    
    /**
     * Applies the patch file on the context file or folder. The patch file may be
     * in a context, normal or unified format.
     * 
     * @param patch the patch file
     * @param context the file or folder to be updated with the patch
     * @throws PatchException
     * @throws IOException - the patch is invalid or cannot be applied
     * @since 1.19
     */
    public static void applyPatch(File patch, File context) throws IOException {
        ContextualPatch cp = ContextualPatch.create(patch, context);
        cp.patch(false); // false - dry run
    }

    /**
     * Returns true only if the given file is a patch
     * in a format recognizable as a
     * <ul>
     *  <li>context</li>
     *  <li>normal</li>
     *  <li>unified</li>
     * </ul>
     * @param file patch file
     * @return true if the given input stream is a patch otherwise false
     * @throws IOException
     * @since 1.19
     */
    public static boolean isPatch(File patch) throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(patch)));
        try {
            return (Patch.parse(reader).length > 0);
        } finally {
            reader.close();
        }
    }
}
