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
 * Ftp commands and replies described in rfc 959.
 * Only commands that are not automated in sun.net.FtpClient and
 * are necessary for us are added here.
 * @author avk
 */
public interface FtpConstants {

    public static final String DEFAULT_FTP_PORT = "21";

    // Commands
    // FTP Service Commands
    /** Delete */
    public static final String COMMAND_DELE = "DELE";

    /** Remove Directory */
    public static final String COMMAND_RMD = "RMD";

    /** Make Directory */
    public static final String COMMAND_MKD = "MKD";

    // Replies from rfc 959
    
    // ftp reply codes
    // x0z Syntax
    // x1z Information
    // x2z Connections
    // x3z Authentication and accounting
    // x4z Unspecified
    // x5z File system

    // 1yz   Positive Preliminary reply
    // 2yz   Positive Completion reply
    // 3yz   Positive Intermediate reply
    // 4yz   Transient Negative Completion reply
    // 5yz   Permanent Negative Completion reply
    
    public static final String POSITIVE_PRELIMINARY = "1";
    public static final String POSITIVE_COMPLETION = "2";
    public static final String POSITIVE_INTERMEDIATE = "3";
    public static final String NEGATIVE_TRANSIENT = "4";
    public static final String NEGATIVE_PERMANENT = "5";
}
