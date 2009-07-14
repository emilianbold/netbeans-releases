/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
 *
 * Author: Tomas Holy
 */

#include "nblauncher.h"
#include "../../../o.n.bootstrap/launcher/windows/utilsfuncs.h"
#include "../../../o.n.bootstrap/launcher/windows/argnames.h"
#include "../../../o.n.bootstrap/launcher/windows/nbexecloader.h"

using namespace std;

const char *NbLauncher::NBEXEC_FILE_PATH = "\\lib\\nbexec.dll";
const char *NbLauncher::OPT_NB_DEFAULT_USER_DIR = "netbeans_default_userdir=";
const char *NbLauncher::OPT_NB_DEFAULT_OPTIONS = "netbeans_default_options=";
const char *NbLauncher::OPT_NB_EXTRA_CLUSTERS = "netbeans_extraclusters=";
const char *NbLauncher::OPT_NB_JDK_HOME = "netbeans_jdkhome=";
const char *NbLauncher::ENV_USER_PROFILE = "USERPROFILE";
const char *NbLauncher::HOME_TOKEN = "${HOME}";
const char *NbLauncher::REG_SHELL_FOLDERS_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders";
const char *NbLauncher::REG_DESKTOP_NAME = "Desktop";

const char *NbLauncher::CON_ATTACH_MSG = 
"\n\nThe launcher has determined that the parent process has a console and will reuse it for its own console output. "
"Closing the console will result in termination of the running program.\n"
"Use '--console suppress' to suppress console output.\n"
"Use '--console new' to create a separate console window.\n";

const char *NbLauncher::staticOptions[] = {
    "-J-Dnetbeans.importclass=org.netbeans.upgrade.AutoUpgrade",
    "-J-Dnetbeans.accept_license_class=org.netbeans.license.AcceptLicense",
    "--branding",
    "nb"
};

NbLauncher::NbLauncher() {
}

NbLauncher::NbLauncher(const NbLauncher& orig) {
}

NbLauncher::~NbLauncher() {
}

int NbLauncher::start(char *cmdLine) {
    CmdArgs args(50);
    args.addCmdLine(cmdLine);
    return start(args.getCount(), args.getArgs());
}

int NbLauncher::start(int argc, char *argv[]) {
    SetErrorMode(SetErrorMode(0) | SEM_FAILCRITICALERRORS | SEM_NOOPENFILEERRORBOX);

    DWORD parentProcID = 0;
    if (!checkLoggingArg(argc, argv, true) || !setupProcess(argc, argv, parentProcID, CON_ATTACH_MSG) || !initBaseNames() || !readClusterFile()) {
        return -1;
    }

    parseConfigFile((baseDir + "\\etc\\" + getAppName() + ".conf").c_str());

    if (!parseArgs(argc, argv)) {
        return -1;
    }
    string oldUserDir = userDir;
    parseConfigFile((userDir + "\\etc\\" + getAppName() + ".conf").c_str());
    userDir = oldUserDir;

    adjustHeapSize();
    addExtraClusters();
    string nbexecPath;
    if (dirExists(platformDir.c_str())) {
        nbexecPath = platformDir;
    } else {
        nbexecPath = baseDir + '\\' + platformDir;
    }
    if (!dirExists(nbexecPath.c_str())) {
        logErr(false, true, "Could not find platform cluster:\n%s", nbexecPath.c_str());
        return false;
    }

    CmdArgs newArgs(argc + 20);
    addSpecificOptions(newArgs);

    if (!jdkHome.empty()) {
        newArgs.add(ARG_NAME_JDKHOME);
        newArgs.add(jdkHome.c_str());
    }
    if (!clusters.empty()) {
        newArgs.add(ARG_NAME_CLUSTERS);
        newArgs.add(clusters.c_str());
    }
    if (!userDir.empty()) {
        newArgs.add(ARG_NAME_USER_DIR);
        newArgs.add(userDir.c_str());
    }
    if (!nbOptions.empty()) {
        newArgs.addCmdLine(nbOptions.c_str());
    }
    for (int i = 0; i < argc; i++) {
        newArgs.add(argv[i]);
    }
    if (parentProcID) {
        newArgs.add(ARG_NAME_LA_PPID);
        char tmp[16] = "";
        newArgs.add(itoa(parentProcID, tmp, 10));
    }
    nbexecPath += NBEXEC_FILE_PATH;

    const char *curDir = getCurrentDir();
    if (curDir) {
        logMsg("Changing current directory to: \"%s\"", curDir);
        SetCurrentDirectory(curDir);
    }

    NBExecLoader loader;
    return loader.start(nbexecPath.c_str(), newArgs.getCount(), newArgs.getArgs());
}

bool NbLauncher::initBaseNames() {
    char path[MAX_PATH] = "";
    getCurrentModulePath(path, MAX_PATH);
    logMsg("Executable: %s", path);
    char *bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    appName = bslash + 1;
    appName.erase(appName.find('.'));
    logMsg("Application name: %s", appName.c_str());

    *bslash = '\0';
    bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    *bslash = '\0';

    baseDir = path;
    logMsg("Base dir: %s", baseDir.c_str());
    return true;
}

void NbLauncher::addCluster(const char *cluster) {

    class SetCurDir {
    public:
        SetCurDir(const char *dir) {
            oldCurDir[0] = '\0';
            DWORD rc = GetCurrentDirectory(MAX_PATH, oldCurDir);
            if (rc == 0) {
                logErr(true, false, "Failed to get current directory");
                return;
            }
            if (rc > MAX_PATH) {
                logMsg("Failed to get current directory, buffer is too small.");
                return;
            }
            if (!SetCurrentDirectory(dir)) {
                logErr(true, true, "Failed to set current directory to \"%s\"", dir);
                oldCurDir[0] = '\0';
            }
        }

        ~SetCurDir() {
            if (oldCurDir[0]) {
                if (!SetCurrentDirectory(oldCurDir)) {
                    logErr(true, true, "Failed to set current directory to \"%s\"", oldCurDir);
                }
            }
        }
    private:
        char oldCurDir[MAX_PATH];
    };

    logMsg("addCluster: %s", cluster);
    SetCurDir setCurDir(baseDir.c_str());
    char clusterPath[MAX_PATH + 1] = {0};
    strncpy(clusterPath, cluster, MAX_PATH);
    if (!normalizePath(clusterPath, MAX_PATH)) {
        logMsg("Invalid cluster path: %s", cluster);
        return;
    }
    if (!clusters.empty()) {
        clusters += ';';
    }
    logMsg("Adding cluster %s", clusterPath);
    clusters += clusterPath;
}

void NbLauncher::addExtraClusters() {
    logMsg("addExtraClusters()");
    const char delim = ';';
    string::size_type start = extraClusters.find_first_not_of(delim, 0);
    string::size_type end = extraClusters.find_first_of(delim, start);
    while (string::npos != end || string::npos != start) {
        string cluster = extraClusters.substr(start, end - start);
        addCluster(cluster.c_str());
        start = extraClusters.find_first_not_of(delim, end);
        end = extraClusters.find_first_of(delim, start);
    }
}

bool NbLauncher::readClusterFile() {
    clusters = "";
    string clusterFile = baseDir + "\\etc\\" + getAppName() + ".clusters";
    logMsg("readClusterFile() file: %s", clusterFile.c_str());

    FILE* file = fopen(clusterFile.c_str(), "r");
    if (!file) {
        logErr(true, true, "Cannot open file \"%s\" for reading.", clusterFile.c_str());
        return false;
    }

    char line[4096] = "";
    while (fgets(line, sizeof(line), file)) {
        char *str = skipWhitespaces(line);
        if (*str == '#' || *str == '\0') {
            continue;
        }
        char *pc = str;
        while (*pc != '\0' && *pc != '\t' && *pc != '\n' && *pc != '\r') {
            pc++;
        }
        *pc = '\0';

        if (platformDir.empty()) {
            char *slash = strrchr(str, '\\');
            if (!slash) {
                slash = strrchr(str, '/');
            }
            char *dir = slash ? slash + 1 : str;
            if (strncmp(dir, "platform", strlen("platform")) == 0) {
                platformDir = str;
            } else {
                addCluster(str);
            }
        } else {
            addCluster(str);
        }
    }
    bool ok = ferror(file) == 0;
    if (!ok) {
        logErr(true, true, "Error while reading file \"%s\".", clusterFile.c_str());
    }
    fclose(file);
    return ok;
}

bool NbLauncher::parseArgs(int argc, char *argv[]) {
#define CHECK_ARG \
    if (i+1 == argc) {\
        logErr(false, true, "Argument is missing for \"%s\" option.", argv[i]);\
        return false;\
    }

    logMsg("parseArgs():");
    for (int i = 0; i < argc; i++) {
        logMsg("\t%s", argv[i]);
    }
    for (int i = 0; i < argc; i++) {
        if (strcmp(ARG_NAME_USER_DIR, argv[i]) == 0) {
            CHECK_ARG;
            char tmp[MAX_PATH + 1] = {0};
            strncpy(tmp, argv[++i], MAX_PATH);
            if (!normalizePath(tmp, MAX_PATH)) {
                logErr(false, true, "User directory path \"%s\" is not valid.", argv[i]);
                return false;
            }
            userDir = tmp;
            logMsg("User dir: %s", userDir.c_str());
        }
    }
    logMsg("parseArgs() finished");
    return true;
}

bool NbLauncher::findUserDir(const char *str) {
    logMsg("NbLauncher::findUserDir()");
    if (strncmp(str, HOME_TOKEN, strlen(HOME_TOKEN)) == 0) {
        if (userHome.empty()) {
            char *userProfile = getenv(ENV_USER_PROFILE);
            if (userProfile) {
                userHome = userProfile;
            } else {

                if (!getStringFromRegistry(HKEY_CURRENT_USER, REG_SHELL_FOLDERS_KEY, REG_DESKTOP_NAME, userHome)) {
                    return false;
                }
                userHome.erase(userHome.rfind('\\'));
            }
            logMsg("User home: %s", userHome.c_str());
        }
        userDir = userHome + (str + strlen(HOME_TOKEN));
    } else {
        userDir = str;
    }
    return true;
}

bool NbLauncher::getOption(char *&str, const char *opt) {
    if (strncmp(str, opt, strlen(opt)) == 0) {
        str += strlen(opt);
        char *end = trimWhitespaces(str);
        if (*str == '"') {
            str++;
        }
        if (end >= str && *end == '"') {
            *end = '\0';
        }
        logMsg("Option found: %s%s", opt, str);
        return true;
    }
    return false;
}

bool NbLauncher::parseConfigFile(const char* path) {
    logMsg("parseConfigFile(%s)", path);
    FILE *file = fopen(path, "r");
    if (!file) {
        logErr(true, false, "Cannot open file \"%s\" for reading.", path);
        return false;
    }
    
    char line[4096] = "";
    while (fgets(line, sizeof(line), file)) {
        char *str = skipWhitespaces(line);
        if (*str == '#') {
            continue;
        }
        if (getOption(str, getDefUserDirOptName())) {
             findUserDir(str);
             logMsg("User dir: %s", userDir.c_str());
        } else if (getOption(str, getDefOptionsOptName())) {
            // replace \" by "
            int len = strlen(str);
            int k = 0;
            for (int i = 0; i < len; i++) {
                if (str[i] == '\\' && str[i+1] == '\"') {
                    continue;
                }
                str[k++] = str[i];
            }
            str[k] = '\0';
            nbOptions = str;
            logMsg("After replacement: %s", nbOptions.c_str());

        } else if (getOption(str, getExtraClustersOptName())) {
            extraClusters = str;
        } else if (getOption(str, getJdkHomeOptName())) {
            jdkHome = str;
        }
    }
    bool ok = ferror(file) == 0;
    if (!ok) {
        logErr(true, false, "Error while reading file \"%s\".", path);
    }
    fclose(file);
    return true;
}

// Search if -Xmx is specified in existing arguments
// If it isn't it adds it - 20% of available RAM but min is 96M and max 512M
void NbLauncher::adjustHeapSize() {
    if (nbOptions.find("-J-Xmx") == string::npos) {
        // find how much memory we have and add -Xmx
        MEMORYSTATUS ms = {0};
        GlobalMemoryStatus(&ms);
        int memory = (int)((ms.dwTotalPhys / 1024 / 1024) / 5);
        if (memory < 96) {
            memory = 96;
        }
        else if (memory > 512) {
            memory = 512;
        }
        char tmp[32];
        snprintf(tmp, 32, " -J-Xmx%dm", memory);
        nbOptions += tmp;
    }
}

const char * NbLauncher::getAppName() {
    return "netbeans";
}

void NbLauncher::addSpecificOptions(CmdArgs &args) {
    for (unsigned i = 0; i < sizeof (staticOptions) / sizeof (char*); i++) {
        args.add(staticOptions[i]);
    }
}

const char * NbLauncher::getDefUserDirOptName() {
    return OPT_NB_DEFAULT_USER_DIR;
}

const char * NbLauncher::getDefOptionsOptName() {
    return OPT_NB_DEFAULT_OPTIONS;
}

const char * NbLauncher::getExtraClustersOptName() {
    return OPT_NB_EXTRA_CLUSTERS;
}

const char * NbLauncher::getJdkHomeOptName() {
    return OPT_NB_JDK_HOME;
}

const char * NbLauncher::getCurrentDir() {
    return 0;
}
