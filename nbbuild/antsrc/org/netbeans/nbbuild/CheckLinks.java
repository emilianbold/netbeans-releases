/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MatchingTask;

import org.apache.regexp.*;

// [PENDING] would be nice to have line numbers reported in output;
// not clear what the best way to do that is without introducing
// overhead; maybe wrapper InputStream that counts lines?
// [PENDING] checking external links needs to go thru HTTP proxy somehow

/** Task to check for broken links in HTML.
 * Note that this is a matching task and you must give it a list of things to match.
 * @author Jesse Glick
 */
public class CheckLinks extends MatchingTask {

    private File basedir;
    private boolean checkexternal = true;

    /** Set whether to check external links (absolute URLs).
     * Local relative links are always checked.
     * By default, external links are checked.
     */
    public void setCheckexternal (boolean ce) {
        checkexternal = ce;
    }

    /** Set the base directory from which to scan files.
     */
    public void setBasedir (File basedir) {
        this.basedir = basedir;
    }

    public void execute () throws BuildException {
        if (basedir == null) throw new BuildException ("Must specify the basedir attribute");
        FileScanner scanner = getDirectoryScanner (basedir);
        scanner.scan ();
        String message = "Scanning for broken links in " + basedir + " ...";
        if (! checkexternal) message += " (external URLs will be skipped)";
        log (message);
        String[] files = scanner.getIncludedFiles ();
        // Set of known-good URLs (including all anchored variants etc.).
        Set okurls = new HashSet (1000); // Set<URL>
        // Set of known-bad URLs.
        Set badurls = new HashSet (100); // Set<URL>
        // Set of base URLs known to be at least openable.
        Set openableurls = new HashSet(100); // Set<URL>
        for (int i = 0; i < files.length; i++) {
            File file = new File (basedir, files[i]);
            URL fileurl;
            try {
                fileurl = file.toURL ();
            } catch (MalformedURLException mfue) {
                throw new BuildException (mfue, location);
            }
            log ("Scanning " + file, Project.MSG_VERBOSE);
            try {
                scan(this, file.getAbsolutePath(), fileurl, openableurls, okurls, badurls, checkexternal, 1);
            } catch (IOException ioe) {
                throw new BuildException ("Could not scan " + file, ioe, location);
            }
        }
    }
    
    static RE hrefOrAnchor;
    static {
        try {
            hrefOrAnchor = new RE("<a\\s+(href|name)=\"([^\"#]+)(#[^\"]+)?\">");
        } catch (RESyntaxException rese) {
            throw new Error (rese.toString());
        }
        hrefOrAnchor.setMatchFlags (RE.MATCH_CASEINDEPENDENT);
    }
    
    // recurse:
    // 0 - just check that it can be opened
    // 1 - check also that any links from it can be opened
    // 2 - recurse
    public static void scan(Task task, String referrer, URL u, Set openableurls, Set okurls, Set badurls, boolean checkexternal, int recurse) throws IOException {
        if (okurls.contains(u)) {
            // Already checked, done.
            return;
        }
        URL base = new URL(u, "");
        String frag = u.getRef();
        if (badurls.contains(u) || badurls.contains(base)) {
            task.log(referrer + ": broken link (already reported): " + u, Project.MSG_WARN);
            return;
        }
        if (openableurls.contains(base) && recurse == 0 && frag == null) {
            // We are just checking opening, done.
            return;
        }
        if (! checkexternal && ! "file".equals(u.getProtocol())) {
            task.log("Skipping external link: " + base, Project.MSG_VERBOSE);
            openableurls.add(base);
            okurls.add(base);
            okurls.add(u);
            return;
        }
        task.log("Checking " + u, Project.MSG_VERBOSE);
        InputStream rd;
        String mimeType;
        try {
            URLConnection conn = base.openConnection ();
            conn.connect ();
            mimeType = conn.getContentType ();
            rd = conn.getInputStream ();
        } catch (IOException ioe) {
            task.log(referrer + ": broken link: " + base, Project.MSG_WARN);
            badurls.add(u);
            return;
        }
        Set others = new HashSet(100); // Set<URL>
        try {
            openableurls.add(base);
            if (recurse == 0 && frag == null) {
                // That is all we wanted to check.
                return;
            }
            okurls.add(base);
            if ("text/html".equals(mimeType)) {
                task.log("Parsing " + base, Project.MSG_VERBOSE);
                CharacterIterator it = new StreamCharacterIterator (rd);
                int idx = 0;
                while (hrefOrAnchor.match (it, idx)) {
                    // Advance match position past end of expression:
                    idx = hrefOrAnchor.getParenEnd (0);
                    // Get the stuff involved:
                    String type = hrefOrAnchor.getParen(1);
                    if (type.equalsIgnoreCase("name")) {
                        // We have an anchor, therefore refs to it are valid.
                        String name = hrefOrAnchor.getParen(2);
                        okurls.add(new URL(base, "#" + name));
                    } else if (type.equalsIgnoreCase("href")) {
                        // A link to some other document.
                        if (recurse > 0) {
                            String otherbase = hrefOrAnchor.getParen (2);
                            String otheranchor = hrefOrAnchor.getParen (3);
                            others.add(new URL(base, (otheranchor == null) ? otherbase : otherbase + otheranchor));
                        } // else we are only checking that this one has right anchors
                    } else {
                        throw new IllegalStateException(type);
                    }
                }
            } else {
                task.log("Not checking contents of " + base, Project.MSG_VERBOSE);
            }
        } finally {
            rd.close();
        }
        if (! okurls.contains(u)) {
            task.log(referrer + ": broken link: " + u, Project.MSG_WARN);
        }
        Iterator it = others.iterator();
        while (it.hasNext()) {
            URL other = (URL)it.next();
            scan(task, u.getPath(), other, openableurls, okurls, badurls, checkexternal, recurse == 1 ? 0 : 2);
        }
    }
    
}
