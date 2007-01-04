/*
 * FileEntry.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

/**
 *
 * @author ak119685
 */
public class FileEntry {
    public String fileName;
    public int dirIndex;
    public long modifiedTime;
    public long fileSize;
    
    /** Creates a new instance of FileEntry */
    public FileEntry(String fileName, int dirIndex, long modifiedTime, long fileSize) {
        this.fileName = fileName;
        this.dirIndex = dirIndex;
        this.modifiedTime = modifiedTime;
        this.fileSize = fileSize;
    }

}
