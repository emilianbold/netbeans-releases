/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MatchingTask;

import org.apache.tools.ant.types.Mapper;

// XXX in Ant 1.6, permit <xmlcatalog> entries to make checking of "external" links
// work better in the case of cross-links between APIs

/** Task to check for broken links in HTML.
 * Note that this is a matching task and you must give it a list of things to match.
 * The Java VM's configured HTTP proxy will be used (${http.proxyHost} and ${http.proxyPort}).
 * @author Jesse Glick
 */
public class CheckLinks extends MatchingTask {

    private File basedir;
    private boolean checkexternal = true;
    private boolean checkspaces = true;
    private boolean checkforbidden = true;
    private List<Mapper> mappers = new LinkedList<Mapper>();
    private boolean failOnError;
    private List<Filter> filters = new ArrayList<Filter>();

    /** Set whether to check external links (absolute URLs).
     * Local relative links are always checked.
     * By default, external links are checked.
     */
    public void setCheckexternal (boolean ce) {
        checkexternal = ce;
    }
    
    /** False if spaces in URLs shall not be reported. Default to true.
     */
    public void setCheckspaces (boolean s) {
        checkspaces = s;
    }

    /** Allows to disable check for forbidden links.
     */
    public void setCheckforbidden(boolean s) {
        checkforbidden = s;
    }
    
    /** Set to true, if you want the build to fail if a url is wrong.
     */
    public void setFailOnError (boolean f) {
        failOnError = f;
    }

    /** Set the base directory from which to scan files.
     */
    public void setBasedir (File basedir) {
        this.basedir = basedir;
    }
    
    public Filter createFilter () {
        Filter f = new Filter ();
        filters.add (f);
        return f;
    }

    /**
     * Add a mapper to translate file names to the "originals".
     */
    public Mapper createMapper() {
        Mapper m = new Mapper(getProject());
        mappers.add(m);
        return m;
    }

    public void execute () throws BuildException {
        if (basedir == null) throw new BuildException ("Must specify the basedir attribute");
        FileScanner scanner = getDirectoryScanner (basedir);
        scanner.scan ();
        String message = "Scanning for broken links in " + basedir + " ...";
        if (! checkexternal) message += " (external URLs will be skipped)";
        log (message);
        String[] files = scanner.getIncludedFiles ();
        Set<URI> okurls = new HashSet<URI>(1000);
        Set<URI> badurls = new HashSet<URI>(100);
        Set<URI> cleanurls = new HashSet<URI>(100);
        for (int i = 0; i < files.length; i++) {
            File file = new File (basedir, files[i]);
            URI fileurl = file.toURI();
            log ("Scanning " + file, Project.MSG_VERBOSE);
            try {
                scan(this, getLocation().toString(), "", fileurl, okurls, badurls, cleanurls, checkexternal, checkspaces, checkforbidden, 1, mappers, filters);
            } catch (IOException ioe) {
                throw new BuildException("Could not scan " + file + ": " + ioe, ioe, getLocation());
            }
        }
        
        if (failOnError && !badurls.isEmpty ()) {
            throw new BuildException ("There were broken links");
        }
    }
    
    private static Pattern hrefOrAnchor = Pattern.compile("<(a|img)(\\s+shape=\"rect\")?\\s+(href|name|src)=\"([^\"#]*)(#[^\"]+)?\"(\\s+shape=\"rect\")?\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static Pattern lineBreak = Pattern.compile("^", Pattern.MULTILINE);
    
    /**
     * Scan for broken links.
     * @param task an Ant task to associate with this
     * @param referrer the referrer file path (or full URL if not file:)
     * @param referrerLocation the location in the referrer, e.g. ":38:12", or "" if unavailable
     * @param u the URI to check
     * @param okurls a set of URIs known to be fully checked (including all anchored variants etc.)
     * @param badurls a set of URIs known to be bogus
     * @param cleanurls a set of (base) URIs known to have had their contents checked
     * @param checkexternal if true, check external links (all protocols besides file:)
     * @param recurse one of:
     *                0 - just check that it can be opened;
     *                1 - check also that any links from it can be opened;
     *                2 - recurse
     * @param mappers a list of Mappers to apply to get source files from HTML files
     */
    public static void scan(Task task, String referrer, String referrerLocation, URI u, Set<URI> okurls, Set<URI> badurls, Set<URI> cleanurls, boolean checkexternal, boolean checkspaces, boolean checkforbidden, int recurse, List<Mapper> mappers) throws IOException {
        scan (task, referrer, referrerLocation, u, okurls, badurls, cleanurls, checkexternal, checkspaces, checkforbidden, recurse, mappers, Collections.<Filter>emptyList());
    }
    
    private static void scan(Task task, String referrer, String referrerLocation, URI u, Set<URI> okurls, Set<URI> badurls, Set<URI> cleanurls, boolean checkexternal, boolean checkspaces, boolean checkforbidden, int recurse, List<Mapper> mappers, List<Filter> filters) throws IOException {
        //task.log("scan: u=" + u + " referrer=" + referrer + " okurls=" + okurls + " badurls=" + badurls + " cleanurls=" + cleanurls + " recurse=" + recurse, Project.MSG_DEBUG);
        if (okurls.contains(u) && recurse == 0) {
            // Yes it is OK.
            return;
        }
        String b = u.toString();
        int i = b.lastIndexOf('#');
        if (i != -1) {
            b = b.substring(0, i);
        }
        URI base;
        try {
            base = new URI(u.getScheme(), u.getUserInfo(), u.getHost(), u.getPort(), u.getPath(), u.getQuery(), /*fragment*/null);
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
        String frag = u.getFragment();
        String basepath = base.toString();
        if ("file".equals(base.getScheme())) {
            try {
                basepath = new File(base).getAbsolutePath();
            } catch (IllegalArgumentException e) {
                task.log(normalize(referrer, mappers) + referrerLocation + ": malformed URL: " + base + " (" + e.getLocalizedMessage() + ")", Project.MSG_WARN);
            }
        }
        //task.log("scan: base=" + base + " frag=" + frag, Project.MSG_DEBUG);
        if (badurls.contains(u) || badurls.contains(base)) {
            task.log(normalize(referrer, mappers) + referrerLocation + ": broken link (already reported): " + u, Project.MSG_WARN);
            return;
        }

        if (checkforbidden) {
            for (Filter f : filters) {
                Boolean decision = f.isOk (u);
                if (Boolean.TRUE.equals (decision)) {
                    break;
                }
                if (Boolean.FALSE.equals (decision)) {
                    task.log(normalize(referrer, mappers) + referrerLocation + ": forbidden link: " + base, Project.MSG_WARN);
                    badurls.add(base);
                    badurls.add(u);
                    return;
                }
            }
        }
        
        if (! checkexternal && ! "file".equals(u.getScheme())) {
            task.log("Skipping external link: " + base, Project.MSG_VERBOSE);
            cleanurls.add(base);
            okurls.add(base);
            okurls.add(u);
            return;
        }
        
        task.log("Checking " + u + " (recursion level " + recurse + ")", Project.MSG_VERBOSE);
        String content;
        String mimeType;
        try {
            // XXX for protocol 'file', could more efficiently use a memmapped char buffer
            URLConnection conn = base.toURL().openConnection ();
            conn.connect ();
            mimeType = conn.getContentType ();
            InputStream is = conn.getInputStream ();
            String enc = conn.getContentEncoding();
            if (enc == null) {
                enc = "UTF-8";
            }
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int read;
                byte[] buf = new byte[4096];
                while ((read = is.read(buf)) != -1) {
                    baos.write(buf, 0, read);
                }
                content = baos.toString(enc);
            } finally {
                is.close();
            }
        } catch (IOException ioe) {
            task.log(normalize(referrer, mappers) + referrerLocation + ": broken link: " + base, Project.MSG_WARN);
            task.log("Error: " + ioe, Project.MSG_VERBOSE);
            badurls.add(base);
            badurls.add(u);
            return;
        }
        okurls.add(base);
        // map from other URIs (hrefs) to line/col info where they occur in this file (format: ":1:2")
        Map<URI,String> others = null;
        if (recurse > 0 && cleanurls.add(base)) {
            others = new HashMap<URI,String>(100);
        }
            if (recurse == 0 && frag == null) {
                // That is all we wanted to check.
                return;
            }
            if ("text/html".equals(mimeType)) {
                task.log("Parsing " + base, Project.MSG_VERBOSE);
                Matcher m = hrefOrAnchor.matcher(content);
                Set<String> names = new HashSet<String>(100);
                while (m.find()) {
                    // Get the stuff involved:
                    String type = m.group(3);
                    if (type.equalsIgnoreCase("name")) {
                        // We have an anchor, therefore refs to it are valid.
                        String name = unescape(m.group(4));
                        if (names.add(name)) {
                            try {
                                okurls.add(new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), base.getPath(), base.getQuery(), /*fragment*/name));
                            } catch (URISyntaxException e) {
                                task.log(normalize(basepath, mappers) + findLocation(content, m.start(4)) + ": bad anchor name: " + e.getMessage(), Project.MSG_WARN);
                            }
                        } else if (recurse == 1) {
                            task.log(normalize(basepath, mappers) + findLocation(content, m.start(4)) + ": duplicate anchor name: " + name, Project.MSG_WARN);
                        }
                    } else {
                        // A link to some other document: href=, src=.
                        
                        // check whether this URL is not commented out
                        int previousCommentStart = content.lastIndexOf ("<!--", m.start (0));
                        int previousCommentEnd = content.lastIndexOf ("-->", m.start (0));
                        boolean commentedOut = false;
                        if (previousCommentEnd < previousCommentStart) {
                            // comment start is there and end is before it
                            commentedOut = true;
                        }
                        
                        if (others != null && !commentedOut) {
                            String otherbase = unescape(m.group(4));
                            String otheranchor = unescape(m.group(5));
                            String uri = (otheranchor == null) ? otherbase : otherbase + otheranchor;
                            String location = findLocation(content, m.start(4));
                            String fixedUri;
                            if (uri.indexOf(' ') != -1) {
                                fixedUri = uri.replaceAll(" ", "%20");
                                if (checkspaces) {
                                    task.log(normalize(basepath, mappers) + location + ": spaces in URIs should be encoded as \"%20\": " + uri, Project.MSG_WARN);
                                }
                            } else {
                                fixedUri = uri;
                            }
                            try {
                                URI relUri = new URI(fixedUri);
                                if (!relUri.isOpaque()) {
                                    URI o = base.resolve(relUri).normalize();
                                    //task.log("href: " + o);
                                    if (!others.containsKey(o)) {
                                        // Only keep location info for first reference.
                                        others.put(o, location);
                                    }
                                } // else mailto: or similar
                            } catch (URISyntaxException e) {
                                // Message should contain the URI.
                                task.log(normalize(basepath, mappers) + location + ": bad relative URI: " + e.getMessage(), Project.MSG_WARN);
                            }
                        } // else we are only checking that this one has right anchors
                    }
                }
            } else {
                task.log("Not checking contents of " + base, Project.MSG_VERBOSE);
            }
        if (! okurls.contains(u)) {
            task.log(normalize(referrer, mappers) + referrerLocation + ": broken link: " + u, Project.MSG_WARN);
            badurls.add(u); // #97784
        }
        if (others != null) {
            Iterator it = others.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                URI other = (URI)entry.getKey();
                String location = (String)entry.getValue();
                scan(task, basepath, location, other, okurls, badurls, cleanurls, checkexternal, checkspaces, checkforbidden, recurse == 1 ? 0 : 2, mappers, filters);
            }
        }
    }
    
    private static String normalize(String path, List<Mapper> mappers) throws IOException {
        try {
            for (Mapper m : mappers) {
                String[] nue = m.getImplementation().mapFileName(path);
                if (nue != null) {
                    for (int i = 0; i < nue.length; i++) {
                        File f = new File(nue[i]);
                        if (f.isFile()) {
                            return new File(f.toURI().normalize()).getAbsolutePath();
                        }
                    }
                }
            }
            return path;
        } catch (BuildException e) {
            throw new IOException(e.toString());
        }
    }
    
    private static String unescape(String text) {
        if (text == null) {
            return null;
        }
        int pos = 0;
        int search;
        while ((search = text.indexOf('&', pos)) != -1) {
            int semi = text.indexOf(';', search + 1);
            if (semi == -1) {
                // Unterminated &... leave rest as is??
                return text;
            }
            String entity = text.substring(search + 1, semi);
            String repl;
            if (entity.equals("amp")) {
                repl = "&";
            } else if (entity.equals("quot")) {
                repl = "\"";
            } else if (entity.equals("lt")) {
                repl = "<";
            } else if (entity.equals("gt")) {
                repl = ">";
            } else if (entity.equals("apos")) {
                repl = "'";
            } else {
                // ???
                pos = semi + 1;
                continue;
            }
            text = text.substring(0, search) + repl + text.substring(semi + 1);
            pos = search + repl.length();
        }
        return text;
    }
    
    private static String findLocation(CharSequence content, int pos) {
        Matcher lbm = lineBreak.matcher(content);
        int line = 0;
        int col = 1;
        while (lbm.find()) {
            if (lbm.start() <= pos) {
                line++;
                col = pos - lbm.start() + 1;
            } else {
                break;
            }
        }
        return ":" + line + ":" + col;
    }

    public final class Filter extends Object {
        private Boolean accept;
        private Pattern pattern;
        
        public void setAccept (boolean a) {
            accept = Boolean.valueOf (a);
        }
        
        public void setPattern (String s) {
            pattern = Pattern.compile (s, Pattern.CASE_INSENSITIVE);
        }
        
        /** Checks whether a URI is ok. 
         * @return null if not applicable, Boolean.TRUE if the URL is accepted, Boolean.FALSE if not
         */
        final Boolean isOk (URI u) throws BuildException {
            if (accept == null) {
                throw new BuildException ("Each filter must have accept attribute");
            }
            if (pattern == null) {
                throw new BuildException ("Each filter must have pattern attribute");
            }
            
            if (pattern.matcher (u.toString ()).matches ()) {
                log ("Matched " + u + " accepted: " + accept, org.apache.tools.ant.Project.MSG_VERBOSE);
                return accept;
            }
            return null;
        }
    }
}
