/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.collab.channel.filesharing;

import org.openide.util.NbBundle;
import org.netbeans.modules.collab.channel.filesharing.ui.SharedProjectNode;


/**
 *
 * @author  Owner
 */
public interface FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // General constants
    ////////////////////////////////////////////////////////////////////////////
    //make sure both FILE_SEPERATOR & FILE_SEPERATOR_CHAR are in sync
    public static final String FILE_SEPERATOR = "/";
    public static final char FILE_SEPERATOR_CHAR = '/';

    ////////////////////////////////////////////////////////////////////////////
    // Filesharing channel/context constants
    ////////////////////////////////////////////////////////////////////////////

    /* protocol version */
    public static final String COLLAB_VERSION = "1.1"; //NoI18n
    public static final String FILESHARING_NAMESPACE = "http://www.netbeans.org/ns/filesharing/" +
        COLLAB_VERSION.replaceAll(".", "_"); //NoI18n
    public static final String COLLAB_CHANNEL_TYPE = "x-channel"; //NoI18n
    public static final String FILESHARING_CONTENT_TYPE_HEADER = "x-display-content-type";
    public static final int STATE_UNKNOWN = -1;
    public static final int STATE_JOINBEGIN = 1;
    public static final int STATE_JOINEND = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_SENDFILE = 4;
    public static final int STATE_RESUME = 5;
    public static final int STATE_LOCK = 6;
    public static final int STATE_SENDCHANGE = 7;
    public static final int STATE_UNLOCK = 8;
    public static final int STATE_LEAVE = 9;
    public static final int STATE_RECEIVEDJOINBEGIN = 11;
    public static final int STATE_RECEIVEDJOINEND = 12;
    public static final int STATE_RECEIVEDPAUSE = 13;
    public static final int STATE_RECEIVEDSENDFILE = 14;
    public static final int STATE_RECEIVEDRESUME = 15;
    public static final int STATE_RECEIVEDLOCK = 16;
    public static final int STATE_RECEIVEDSENDCHANGE = 17;
    public static final int STATE_RECEIVEDUNLOCK = 18;
    public static final int STATE_RECEIVEDLEAVE = 19;

    ////////////////////////////////////////////////////////////////////////////
    // FilesharingTimerTask Constants
    ////////////////////////////////////////////////////////////////////////////
    public final static String SEND_RESUME_TIMER_TASK = "sendResumeMessageTimerTask"; //NOI18n
    public final static String SEND_PAUSE_TIMER_TASK = "sendPauseMessageTimerTask"; //NOI18n
    public final static String SEND_SENDFILE_TIMER_TASK = "sendFileMessageTimerTask"; //NOI18n
    public final static String SEND_JOINBEGIN_TIMER_TASK = "sendJoinBeginMessageTimerTask"; //NOI18n
    public final static String SEND_JOINEND_TIMER_TASK = "sendJoinEndMessageTimerTask"; //NOI18n
    public final static String SEND_FILECHANGE_TIMER_TASK = "sendFileChangeTimerTask"; //NOI18n
    public final static String SEND_UNLOCK_TIMER_TASK = "sendUnlockRegionTimerTask"; //NOI18n
    public final static String COOKIE_LISTENER_TIMER_TASK = "cookieListener"; //NOI18n
    public final static String SEND_PROJECTACTIONLIST_TIMER_TASK = "sendProjectActionListTimerTask"; //NOI18n
    public final static String SEND_PROJECTPERFORMACTION_TIMER_TASK = "sendProjectPerformActionTimerTask"; //NOI18n

    //time constants
    public final static long INITIAL_DELAY = 2000; //millis
    public final static long PERIOD = 1000; //millis
    public final static long INTER_DELAY = 100; //millis
    public final static long JOIN_BEGIN_DELAY = PERIOD; //millis
    public final static long JOIN_END_DELAY = PERIOD * 10; //millis
    public final static long PAUSE_DELAY = PERIOD * 10; //millis		
    public final static long RESUME_DELAY = PAUSE_DELAY; //millis
    public final static long SENDFILE_DELAY = PAUSE_DELAY + RESUME_DELAY + (PERIOD * 120); //millis(~2 min)

    ////////////////////////////////////////////////////////////////////////////
    // CollabFileHandler Constants
    ////////////////////////////////////////////////////////////////////////////
    public static final long CREATELOCK_TIMER_RATE = 500; //milliseconds
    public static final long CREATELOCK_TIMER_START_DELAY = 1000; //milliseconds
    public static final long CREATELOCK_NEWITEM_INCREMENT_DELAY = 1000; //milliseconds	

    //public static final long CREATELOCK_MAX_DELAY = 15000;//milliseconds
    ////////////////////////////////////////////////////////////////////////////
    // Project Constants
    ////////////////////////////////////////////////////////////////////////////	
    public static final String COLLAB_NON_PROJECT_FOLDER_NAME = NbBundle.getMessage(
            SharedProjectNode.class, "LBL_ProjectsRootNode_NonProjectFolder"
        ); //"Shared Common"
    public static final String SRC_FOLDER_NAME = NbBundle.getMessage(
            SharedProjectNode.class, "LBL_ProjectsRootNode_SrcFolder"
        ); //"Source Packages"
    public static final String TEST_FOLDER_NAME = NbBundle.getMessage(
            SharedProjectNode.class, "LBL_ProjectsRootNode_TestFolder"
        ); //"Test Packages"
    public static final String JAVAHELP_FOLDER_NAME = NbBundle.getMessage(
            SharedProjectNode.class, "LBL_ProjectsRootNode_JavaHelpFolder"
        ); //"JavaHelp Packages"	
    public static final String WEB_FOLDER_NAME = NbBundle.getMessage(
            SharedProjectNode.class, "LBL_ProjectsRootNode_WebFolder"
        ); //"Web Packages"
    public static final String CONF_FOLDER_NAME = NbBundle.getMessage(
            SharedProjectNode.class, "LBL_ProjectsRootNode_ConfFolder"
        ); //"Configuration Files"	
    public static final String COLLAB_ARCHIVE_FOLDER_NAME = NbBundle.getMessage(
            SharedProjectNode.class, "LBL_ProjectsRootNode_ArchiveFolder"
        ); //"Original Files Archive"
    public static final String SRC_NODE = "${src.dir}"; //NoI18n
    public static final String TEST_NODE = "${test.src.dir}"; //NoI18n
    public static final String JAVAHELP_NODE = "${javahelp.src.dir}"; //NoI18n	
    public static final String WEB_NODE = "web"; //NoI18n	
    public static final String LIBRARY_NODE = "Libraries"; //NoI18n
    public static final String TEST_LIBRARY_NODE = "Test Libraries"; //NoI18n
    public static final String CONF_NODE = "conf"; //NoI18n
    public static final String JAVA_PKG = "java"; //NoI18n
    public static final String DEFAULT_PKG = "<default package>"; //NoI18n
    public static final String SRC_DIR = "src"; //NoI18n
    public static final String CONF_DIR = SRC_DIR + FILE_SEPERATOR + "conf"; // src/conf //NoI18n
    public static final String TEST_DIR = "test"; //NoI18n
    public static final String WEB_DIR = "web"; //NoI18n
    public static final String JAVAHELP_DIR = "javahelp"; //NoI18n
    public static final String ARCHIVE_DIR = "collab_archive"; //NoI18n
    public static final String SHARED_COMMON_DIR = "shared_common"; //NoI18n

    ////////////////////////////////////////////////////////////////////////////
    // Other Constants
    ////////////////////////////////////////////////////////////////////////////
    public static final long SHAREABLE_LIMIT = 1000000; //1MB
    public static final String FILE_COUNT_CHANGED = "FILE_COUNT_CHANGED";
    public static final String FS_STATUS_CHANGE = "FS_STATUS_CHANGE";
}
