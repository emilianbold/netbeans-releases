/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api;

/**
 * Describes a chunck of process information [column description in a process
 * list table]
 *
 * @author ak119685
 */
public final class ProcessInfoDescriptor {

    //ids are the same for all platforms
    //ID is used to understand all other staff
    public static final String UID_COLUMN_ID = "uid";
    public static final String PID_COLUMN_ID = "pid";
    public static final String PPID_COLUMN_ID = "ppid";
    public static final String COMMAND_COLUMN_ID = "command";
    public static final String STIME_COLUMN_ID = "stime";

    public final String id;
    public final String command;
    public final Class type;
    public final String header;
    public final String shortDescription;
    public final boolean isUserVisible;

    public ProcessInfoDescriptor(String id, String command, Class type,
            String header, String shortDescription) {
        this(true, id, command, type, header, shortDescription);
    }

    public ProcessInfoDescriptor(boolean isUserVisible, String id, String command,
            Class type, String header, String shortDescription) {
        this.isUserVisible = isUserVisible;
        this.id = id;
        this.command = command;
        this.type = type;
        this.header = header;
        this.shortDescription = shortDescription;
    }
}
