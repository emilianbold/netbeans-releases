/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.admin;

import java.io.File;

/**
 * Offline asadmin command used to restore domain in junit tests.
 *
 * @author Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerAsadminRestoreDomain.class)
@RunnerRestClass(runner = RunnerAsadminRestoreDomain.class)
public class CommandRestoreDomain extends CommandJava {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for change administrator's password command. */
    private static final String COMMAND = "restore-domain";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Domain backup archive. */
    final File domainBackup;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server administration command entity
     * with specified server command, Java SE home and class path.
     * <p/>
     * @param javaHome Java SE home used to select JRE for GlassFish server.
     * @param domainBackup archive that contains domain restore.
     */
    public CommandRestoreDomain(final String javaHome,
            final File domainBackup) {
        super(COMMAND, javaHome);
        this.domainBackup = domainBackup;
    }
}
