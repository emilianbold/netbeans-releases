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
import org.apache.tools.ant.taskdefs.MatchingTask;

import org.apache.regexp.*;

// [PENDING] would be nice to have line numbers reported in output;
// not clear what the best way to do that is without introducing
// overhead; maybe wrapper InputStream that counts lines?
// [PENDING] checking external links needs to go thru HTTP proxy somehow

/** Task to check for broken links in HTML.
 * Note that this is a matching task and you must give it a list of things to match.
 * It will assume that any matching file is in HTML format.
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
        RE hrefs, anchors;
        try {
            hrefs = new RE ("<a\\s+href=\"([^\"#]+)(#[^\"]+)?\">");
            anchors = new RE ("<a\\s+name=\"([^\"]+)\">");
        } catch (RESyntaxException rese) {
            throw new BuildException (rese, location);
        }
        hrefs.setMatchFlags (RE.MATCH_CASEINDEPENDENT);
        anchors.setMatchFlags (RE.MATCH_CASEINDEPENDENT);
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
                // Intentionally using InputStream, not Reader, for speed:
                InputStream rd = new FileInputStream (file);
                try {
                    CharacterIterator it = new StreamCharacterIterator (rd);
                    int idx = 0;
                    while (hrefs.match (it, idx)) {
                        // Advance match position past end of expression:
                        idx = hrefs.getParenEnd (0);
                        // Get the URL involved:
                        String base = hrefs.getParen (1);
                        String anchor = hrefs.getParen (2);
                        String baseAnchor = ((anchor == null) ? base : base + anchor);
                        URL u1 = new URL (fileurl, base);
                        URL u2 = new URL (fileurl, baseAnchor);
                        if (! checkexternal && ! "file".equals (u2.getProtocol ())) {
                            log ("" + file + ": skipping external link: " + baseAnchor, Project.MSG_VERBOSE);
                            continue;
                        }
                        // Now categorize the URL according to whether it is good/bad, known/unknown, ...
                        if (badurls.contains (u1) || badurls.contains (u2)) {
                            log ("" + file + ": " + baseAnchor + " again bad", Project.MSG_WARN);
                            continue;
                        }
                        if (okurls.contains (u2)) {
                            continue;
                        }
                        if (okurls.contains (u1)) {
                            log ("" + file + ": " + base + " exists but it has no anchor " + anchor.substring (1), Project.MSG_WARN);
                            badurls.add (u2);
                            continue;
                        }
                        InputStream is;
                        String mimeType;
                        try {
                            URLConnection conn = u1.openConnection ();
                            conn.connect ();
                            mimeType = conn.getContentType ();
                            is = conn.getInputStream ();
                        } catch (IOException ioe) {
                            badurls.add (u1);
                            // u2 will automatically be considered bad too
                            log ("" + file + ": " + base + " does not exist", Project.MSG_WARN);
                            continue;
                        }
                        try {
                            okurls.add (u1);
                            if ("text/html".equals (mimeType)) {
                                CharacterIterator it2 = new StreamCharacterIterator (is);
                                int idx2 = 0;
                                while (anchors.match (it2, idx2)) {
                                    idx2 = anchors.getParenEnd (0);
                                    String name = anchors.getParen (1);
                                    okurls.add (new URL (u1.toString () + '#' + name));
                                }
                                if (! okurls.contains (u2)) {
                                    log ("" + file + ": " + base + " exists but it has no anchor " + anchor.substring (1), Project.MSG_WARN);
                                    badurls.add (u2);
                                }
                            } else if (! u1.equals (u2)) {
                                log ("" + file + ": " + baseAnchor + " pointed to resource with type " + mimeType + "; not checking anchors");
                                badurls.add (u2);
                            }
                        } finally {
                            is.close ();
                        }
                    }
                } finally {
                    rd.close ();
                }
            } catch (IOException ioe) {
                throw new BuildException ("Could not scan " + file, ioe, location);
            }
        }
    }

}
