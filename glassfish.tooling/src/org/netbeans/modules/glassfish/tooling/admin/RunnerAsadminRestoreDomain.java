/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.admin;

import static org.netbeans.modules.glassfish.tooling.admin.RunnerJava.PARAM_ASSIGN_VALUE;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;

/**
 *
 * @author Peter Benedikovic
 */
public class RunnerAsadminRestoreDomain extends RunnerAsadmin {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(RunnerAsadminRestoreDomain.class);

    /** Specifies the domain dir. */
    private static final String DOMAIN_DIR_PARAM = "--domaindir";
    
    /** Specifies the directory where the backup archive is stored. */
    private static final String BACKUP_DIR_PARAM = "--backupdir";
    
    /** Specifies the name of the backup archive. */
    private static final String BACKUP_FILE_PARAM = "--filename";
    
    /** Specifies the force param needed to restore from non-standard location. */
    private static final String FORCE_PARAM = "--force";
   

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds <code>change-admin-password</code> command query string.
     */
    private static String query(final GlassFishServer server,
            final Command command) {
        final String METHOD = "query";
        CommandRestoreDomain restoreCommand;
        String domainsFolder = OsUtils.escapeString(server.getDomainsFolder());
        String domainName = OsUtils.escapeString(server.getDomainName());
        if (domainName == null || domainsFolder == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "nullValue"));
        }
        if (command instanceof CommandRestoreDomain) {
            restoreCommand = (CommandRestoreDomain)command;
        } else {
            throw new CommandException(
                    LOGGER.excMsg(METHOD, "illegalInstance"));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(DOMAIN_DIR_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(domainsFolder);
        sb.append(PARAM_SEPARATOR);
        sb.append(FORCE_PARAM);
        sb.append(PARAM_SEPARATOR);
        sb.append(BACKUP_FILE_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(restoreCommand.domainBackup.getAbsolutePath());
        sb.append(PARAM_SEPARATOR);
        sb.append(domainName);
        System.out.println("Restore command params: " + sb.toString());
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandRestoreDomain command;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * command line asadmin interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerAsadminRestoreDomain(final GlassFishServer server,
            final Command command) {
        super(server, command, query(server, command));
        final String METHOD = "init";
        if (command instanceof CommandRestoreDomain) {
            this.command = (CommandRestoreDomain)command;
        } else {
            throw new CommandException(
                    LOGGER.excMsg(METHOD, "illegalInstance"));
        }
    }
 
    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create internal <code>ProcessIOContent</code> object corresponding
     * to command execution IO.
     */
    @Override
    protected ProcessIOContent createProcessIOContent() {
        ProcessIOContent processIOContent = new ProcessIOContent();
        processIOContent.addOutput(
                new String[] {"Command", "executed successfully"},
                new String[] {"Command restore-domain failed"});
        return processIOContent;
    }
}
