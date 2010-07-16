/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.utils.system.cleaner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Lipin
 */
public class SystemPropertyOnExitCleanerHandler extends OnExitCleanerHandler {

    private static final String FILES_TO_DELETE_PROPERTY =
            "nbi.on.exit.delete.files";

    public SystemPropertyOnExitCleanerHandler() {
        if(!isSet()) {
            System.setProperty(FILES_TO_DELETE_PROPERTY, "");
        }
    }

    public static final boolean isSet() {
        return System.getProperty(FILES_TO_DELETE_PROPERTY) != null;
    }

    @Override
    public void run() {
    }

    @Override
    public void addDeleteOnExitFile(File file) {
        List <String> files = getFilesList();
        String path = file.getAbsolutePath();
        if(!files.contains(path)) {
            files.add(path);
            System.setProperty(FILES_TO_DELETE_PROPERTY,
                    StringUtils.asString(files, File.pathSeparator));
        }
    }
    protected List<String> getFilesList() {
        return new ArrayList<String> (StringUtils.asList(System.getProperty(
                FILES_TO_DELETE_PROPERTY,""), File.pathSeparator));
    }

    @Override
    public void removeDeleteOnExitFile(File file) {
        String path = file.getAbsolutePath();
        List <String> list  = getFilesList();
        if(list.contains(path)) {
            list.remove(path);
            System.setProperty(FILES_TO_DELETE_PROPERTY,
                    StringUtils.asString(list, File.pathSeparator));
        }
    }
}
