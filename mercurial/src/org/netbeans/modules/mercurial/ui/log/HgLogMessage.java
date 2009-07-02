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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.ui.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author jr140578
 */
public class HgLogMessage {
    public static char HgModStatus = 'M';
    public static char HgAddStatus = 'A';
    public static char HgDelStatus = 'R';
    public static char HgCopyStatus = 'C';

    private List<HgLogMessageChangedPath> paths;
    private String rev;
    private String author;
    private String desc;
    private Date date;
    private String id;
    private String timeZoneOffset;

    private String parentOneRev;
    private String parentTwoRev;
    private boolean bMerged;
    private String rootURL;
    private OutputLogger logger;

    public HgLogMessage(String changeset){
    }

    private void updatePaths(List<String> pathsStrings, String path, List<String> filesShortPaths, char status) {
        if (filesShortPaths.isEmpty()) {
            paths.add(new HgLogMessageChangedPath(path, status));
            if(pathsStrings != null){
                pathsStrings.add(path);
            }
        } else {
            paths.add(new HgLogMessageChangedPath(path, status));
            if(pathsStrings != null){
                pathsStrings.add(path);
            }
        }
    }

    public HgLogMessage(String rootURL, List<String> filesShortPaths, String rev, String auth, String desc, String date, String id,
            String parents, String fm, String fa, String fd, String fc) {

        this.rootURL = rootURL;
        this.rev = rev;
        this.author = auth;
        this.desc = desc;
        this.id = id;
        this.date = new Date(Long.parseLong(date.split(" ")[0]) * 1000); // UTC in miliseconds
        String[] parentSplits;
        parentSplits = parents != null ? parents.split(" ") : null;
        if ((parentSplits != null) && (parentSplits.length == 2)) {
        String[] ps1 = parentSplits[0].split(":"); // NOI18N
        this.parentOneRev = ps1 != null && ps1.length >= 1 ? ps1[0] : null;
        String[] ps2 = parentSplits[1].split(":"); // NOI18N
        this.parentTwoRev = ps2 != null && ps2.length >= 1 ? ps2[0] : null;
        }
        this.bMerged = this.parentOneRev != null && this.parentTwoRev != null && !this.parentOneRev.equals("-1") && !this.parentTwoRev.equals("-1") ? true : false;

        this.paths = new ArrayList<HgLogMessageChangedPath>();
        List<String> apathsStrings = new ArrayList<String>();
        List<String> dpathsStrings = new ArrayList<String>();
        List<String> cpathsStrings = new ArrayList<String>();

        // Mercurial Bug: Currently not seeing any file_copies coming back from Mercurial
        if (fc != null && !fc.equals("")) {
            for (String s : fc.split("\t")) {
                updatePaths(cpathsStrings, s, filesShortPaths, HgCopyStatus);
            }
        }
        if (fa != null && !fa.equals("")) {
            for (String s : fa.split("\t")) {
                if(!cpathsStrings.contains(s)){
                    updatePaths(apathsStrings, s, filesShortPaths, HgAddStatus);
                }
            }
        }
        if (fd != null && !fd.equals("")) {
            for (String s : fd.split("\t")) {
                updatePaths(dpathsStrings, s, filesShortPaths, HgDelStatus);
            }
        }
        if (fm != null && !fm.equals("")) {
            for (String s : fm.split("\t")) {
                //#132743, incorrectly reporting files as added/modified, deleted/modified in same changeset
                if (!apathsStrings.contains(s) && !dpathsStrings.contains(s) && !cpathsStrings.contains(s)) {
                    updatePaths(null, s, filesShortPaths, HgModStatus);
                }
            }
        }
        if(fa == null && fc == null && fd == null && fm == null) {
            for (String fileSP : filesShortPaths) {
                paths.add(new HgLogMessageChangedPath(fileSP, ' '));
            }
        }
    }

    HgLogMessageChangedPath [] getChangedPaths(){
        return paths.toArray(new HgLogMessageChangedPath[paths.size()]);
    }

    public String getRevision() {
        return rev;
    }

    public long getRevisionAsLong() {
        long revLong;
        try{
            revLong = Long.parseLong(rev);
        }catch(NumberFormatException ex){
            // Ignore number format errors
            return 0;
        }
        return revLong;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getCSetShortID() {
        return id;
    }

    public  String getAncestor() {
        if(bMerged){
            try{
                return HgCommand.getCommonAncestor(rootURL, parentOneRev, parentTwoRev, getLogger());
            } catch (HgException ex) {
                return null;
            }
        }
        int revInt = -1;
        try{
            revInt = Integer.parseInt(rev);
        }catch(NumberFormatException ex){
        }
        revInt = revInt > -1? revInt -1: -1;
        return Integer.toString(revInt);
    }

    private OutputLogger getLogger() {
        if (logger == null) {
            logger = Mercurial.getInstance().getLogger(rootURL);
        }
        return logger;
    }

    public String getMessage() {
        return desc;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("rev: ");
        sb.append(this.rev);
        sb.append("\nauthor: ");
        sb.append(this.author);
        sb.append("\ndesc: ");
        sb.append(this.desc);
        sb.append("\ndate: ");
        sb.append(this.date);
        sb.append("\nid: ");
        sb.append(this.id);
        sb.append("\npaths: ");
        sb.append(this.paths);
        return sb.toString();
    }
}
