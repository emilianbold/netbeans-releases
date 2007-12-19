/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient;

/**
 *
 * @author avk
 */
public interface FtpConstants_copied {
    public final static String DEFAULT_FTP_PORT = "21";


    // Access Control Commands

    /** User Name */
    public final static String USER = "USER";

    /** Password */
    public final static String PASS = "PASS";

    /** Account */
    public final static String ACCT = "ACCT";

    /** Change Working Directory */
    public final static String CWD = "CWD";

    /** Change to Parent Directory */
    public final static String CDUP = "CDUP";

    /** Structure Mount */
    public final static String SMNT = "SMNT";

    /** Reinitialize */
    public final static String REIN = "REIN";

    /** Logout */
    public final static String QUIT = "QUIT";

    // Transfer Parameter Commands

    /** Data Port */
    public final static String PORT = "PORT";

    /** Passive */
    public final static String PASV = "PASV";

    /** Representation Type (ASCII, EBCDIC, Image, Non-print, Telnet format effectors, Carriage Control, Local byte Byte size) */
    public final static String TYPE = "TYPE";

    /** File Structure (File, Record Structure, Page Structure) */
    public final static String STRU = "STRU";

    /** Transfer Mode (Stream, Block, Compressed) */
    public final static String MODE = "MODE";

    // FTP Service Commands

    /** Retrieve */
    public final static String RETR = "RETR";

    /** Store */
    public final static String STOR = "STOR";

    /** Store Unique */
    public final static String STOU = "STOU";

    /** Append (with create) */
    public final static String APPE = "APPE";

    /** Allocate */
    public final static String ALLO = "ALLO";

    /** Restart */
    public final static String REST = "REST";

    /** Rename From */
    public final static String RNFR = "RNFR";

    /** Rename To */
    public final static String RNTO = "RNTO";

    /** Abort */
    public final static String ABOR = "ABOR";

    /** Delete */
    public final static String DELE = "DELE";

    /** Remove Directory */
    public final static String RMD = "RMD";

    /** Make Directory */
    public final static String MKD = "MKD";

    /** Print Working Directory */
    public final static String PWD = "PWD";

    /** Name List */
    public final static String NLST = "NLST";

    /** Site Parameters */
    public final static String SITE = "SITE";

    /** System */
    public final static String SYST = "SYST";

    /** Status */
    public final static String STAT = "STAT";

    /** Help */
    public final static String HELP = "HELP";

    /** NOOP */
    public final static String NOOP = "NOOP";

    // The following commands were introduced in rfc 2228 FTP Security Extensions

    /** Authentication/Security Mechanism */
    public final static String AUTH = "AUTH";

    /** Authentication/Security Data */
    public final static String ADAT = "ADAT";

    /** Data Channel Protection Level */
    public final static String PROT = "PROT";

    /** Protection Buffer Size */
    public final static String PBSZ = "PBSZ";

    /** Clear Command Channel */
    public final static String CCC = "CCC";

    /** Integrity Protected Command */
    public final static String MIC = "MIC";

    /** Confidentiality Protected Command */
    public final static String CONF = "CONF";

    /** Privacy Protected Command */
    public final static String ENC = "ENC";

    // end of commands from rfc 2228
    // ftp reply codes
    // x0z Syntax
    // x1z Information
    // x2z Connections
    // x3z Authentication and accounting
    // x4z Unspecified
    // x5z File system
    // 1yz Positive Preliminary reply

    /** Restart marker reply */
    public final static String RC110 = "110";

    /** Service ready in nnn minutes */
    public final static String RC120 = "120";

    /** Data connection already open */
    public final static String RC125 = "125";

    /** File status okay; about to open data connection */
    public final static String RC150 = "150";

    // 2yz Positive Completion reply

    /** Command okay */
    public final static String FTP200_OK = "2";

    /** Command not implemented, superfluous at this site */
    public final static String RC202 = "202";

    /** System status, or system help reply */
    public final static String RC211 = "211";

    /** Directory status */
    public final static String RC212 = "212";

    /** File status */
    public final static String RC213 = "213";

    /** Help message */
    public final static String RC214 = "214";

    /** System type */
    public final static String FTP215_SYSTEM_TYPE = "215";

    /** Service ready for new user */
    public final static String FTP220_SERVICE_READY = "220";

    /** Service closing control connection */
    public final static String FTP221_SERVICE_CLOSING = "221";

    /** Data connection open; no transfer in progress */
    public final static String RC225 = "225";

    /** Closing data connection; requested file action successful */
    public final static String FTP226_CLOSING_DATA_REQUEST_SUCCESSFUL = "226";

    /** Entering Passive Mode */
    public final static String FTP227_ENTERING_PASSIVE_MODE = "227";

    /** User logged in, proceed */
    public final static String FTP230_LOGGED_IN = "2"; // 230

    /** Requested file action okay, completed */
    public final static String FTP250_COMPLETED = "250";

    /** "PATHNAME" created */
    public final static String FTP257_PATH_CREATED = "257";

    // 3yz Positive Intermediate reply

    /** User name okay, need password */
    public final static String FTP331_USER_OK_NEED_PASSWORD = "331";

    /** Need account for login */
    public final static String RC332 = "332";

    /** Requested file action pending further information */
    public final static String RC350 = "350";

    // 4yz Transient Negative Completion reply

    /** Service not available */
    public final static String RC421 = "421";

    /** Can't open data connection */
    public final static String RC425 = "425";

    /** Connection closed; transfer aborted */
    public final static String RC426 = "426";

    /** Requested file action not taken, file unavailable (file busy) */
    public final static String RC450 = "450";

    /** Requested action aborted: local error in processing */
    public final static String RC451 = "451";

    /** Requested action not taken, insufficient storage space in system */
    public final static String RC452 = "452";

    // 5yz Permanent Negative Completion reply

    /** Syntax error, command unrecognized */
    public final static String RC500 = "500";

    /** Syntax error in parameters or arguments */
    public final static String RC501 = "501";

    /** Command not implemented */
    public final static String RC502 = "502";

    /** Bad sequence of commands */
    public final static String RC503 = "503";

    /** Command not implemented for that parameter */
    public final static String RC504 = "504";

    /** Not logged in */
    public final static String RC530 = "530";

    /** Need account for storing files */
    public final static String RC532 = "532";

    /** Requested action not taken, file unavailable (file not found, no access) */
    public final static String RC550 = "550";

    /** Requested action aborted: page type unknown */
    public final static String RC551 = "551";

    /** Requested file action aborted, exceeded storage allocation */
    public final static String RC552 = "552";

    /** Requested action not taken, file name not allowed */
    public final static String RC553 = "553";

}
