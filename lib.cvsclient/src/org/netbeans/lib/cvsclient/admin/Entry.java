/*****************************************************************************
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
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.admin;

import java.text.*;
import java.util.*;

/**
 * The class abstracts the CVS concept of an <i>entry line</i>. The entry
 * line is textually of the form:<p>
 * / name / version / conflict / options / tag_or_date
 * <p>These are explained in section 5.1 of the CVS protocol 1.10 document.
 * @author  Robert Greig
 */
public final class Entry {
    /**
     * The dummy timestamp set the conflict information for added or removed
     * files.
     */
    public static final String DUMMY_TIMESTAMP = "dummy timestamp"; //NOI18N
    public static final String DUMMY_TIMESTAMP_NEW_ENTRY = "dummy timestamp from new-entry"; //NOI18N

    public static final String MERGE_TIMESTAMP = "Result of merge"; //NOI18N

    /**
     * Indicates a sticky tag.
     */
    private static final String TAG = "T"; //NOI18N

    /**
     * Indicates a sticky date.
     */
    private static final String DATE = "D"; //NOI18N

    /**
     * The instance of the date formatter for sticky dates.
     */
    private static SimpleDateFormat stickyDateFormatter;

    /**
     * Returns the instance of the date formatter for sticky dates.
     */
    private static SimpleDateFormat getStickyDateFormatter() {
        if (stickyDateFormatter == null) {
            stickyDateFormatter = new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss"); //NOI18N
        }
        return stickyDateFormatter;
    }

    /**
     * Indicates a binary file.
     */
    private static final String BINARY_FILE = "-kb"; //NOI18N

    /**
     * Indicates that no user file is meant by the version details
     */
    private static final String NO_USER_FILE = ""; //NOI18N

    /**
     * Indicates that a new user file is meant by the version details
     */
    private static final String NEW_USER_FILE = "0"; //NOI18N

    /**
     * Indicates that the file is to be removed, in the version details
     */
    private static final String REMOVE_USER_FILE = "-"; //NOI18N

    /**
     * Returns the instance of the Last-Modified-Date-Formatter.
     */
    public static SimpleDateFormat getLastModifiedDateFormatter() {
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US); //NOI18N
        df.setTimeZone(getTimeZone());
        return df;
    }

    /**
     * All entries times are by defaulf in Zulu/GMT0
     */
    public static TimeZone getTimeZone() {
        return TimeZone.getTimeZone("GMT"); //NOI18N
    }
    
    /**
     * Indicates that the file had conflicts.
     */
    public static final char HAD_CONFLICTS = '+';

    /**
     * Indicates that the timestamp matches the file.
     */
    public static final char TIMESTAMP_MATCHES_FILE = '=';

    /**
     * Indicates that the file had conflicts and timestamp matches.
     * It likely means unresolved conflict.
     */
    public static final String HAD_CONFLICTS_AND_TIMESTAMP_MATCHES_FILE = "+=";

    /**
     * Initial letter that indicates a directory entry.
     */
    private static final String DIRECTORY_PREFIX = "D/";

    /**
     * The name of the file.
     */
    private String name;

    /**
     * The revision. There are constants defined for no user file, new user
     * file and user file has to be removed.
     */
    private String revision;

    /**
     * The conflict information. There are constants defined for indicating
     * that conflicts occurred and that the timestamp matches the file
     */
    private String conflict;

    /**
     * The last modified date of the file.
     */
    private Date lastModified;

    /**
     * The options for signifying keyword expansion.
     */
    private String options;

    /**
     * The tag. May be present in place of the date information.
     */
    private String tag;

    /**
     * The date. May be present in place of the tag information.
     */
    private Date date;

    /**
     * Indicates whether the entry is for a directory.
     */
    private boolean directory;

    /**
     * Construct a new Entry from a given entry line.
     */
    public Entry(String entryLine) {
        init(entryLine);
    }

    /**
     * Construct a new blank Entry.
     */
    public Entry() {
    }

    /**
     * Initialise the Entry by parsing an entry line.
     * @param entryLine the entry line in standard CVS format
     */
    protected void init(String entryLine) {
        //System.err.println("Constructing an entry line from: " + entryLine);
        // try to parse the entry line, if we get stuck just
        // throw an illegal argument exception

        if (entryLine.startsWith(DIRECTORY_PREFIX)) {
            directory = true;
            entryLine = entryLine.substring(1);
        }

        // first character is a slash, so name is read from position 1
        // up to the next slash
        final int[] slashPositions = new int[5];

        try {
            slashPositions[0] = 0;
            for (int i = 1; i < 5; i++) {
                slashPositions[i] = entryLine.indexOf('/',
                                                      slashPositions[i - 1] + 1);
            }

            // Test if this is a D on its own, a special case indicating that
            // directories are understood and there are no subdirectories
            // in the current folder
            if (slashPositions[1] > 0) {
                // note that the parameters to substring are treated as follows:
                // (inclusive, exclusive)
                name = entryLine.substring(slashPositions[0] + 1,
                                           slashPositions[1]);
                revision = entryLine.substring(slashPositions[1] + 1,
                                               slashPositions[2]);
                if ((slashPositions[3] - slashPositions[2]) > 1) {
                    String conflict = entryLine.substring(slashPositions[2] + 1,
                                                          slashPositions[3]);
                    setConflict(conflict);
                }
                if ((slashPositions[4] - slashPositions[3]) > 1) {
                    options = entryLine.substring(slashPositions[3] + 1,
                                                  slashPositions[4]);
                }
                if (slashPositions[4] != (entryLine.length() - 1)) {
                    String tagOrDate = entryLine.substring(slashPositions[4]
                                                           + 1);
                    if (tagOrDate.startsWith(TAG)) {
                        setTag(tagOrDate.substring(1));
                    }
                    else if (tagOrDate.startsWith(DATE)) {
                        //TODO process date into something useful
                        // MK - I didn't notice any time conversions (according to timezone)
                        // So I just convert it from String to Date and back.
                        try {
                            String dateString = tagOrDate.substring(DATE.length());
                            Date stickyDate = getStickyDateFormatter().
                                    parse(dateString);
                            setDate(stickyDate);
                        }
                        catch (ParseException exc) {
                            System.err.println("We got another inconsistency in the library's date formatting."); //NOI18N
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error parsing entry line: " + e); //NOI18N
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid entry line: " + //NOI18N
                                               entryLine);
        }
    }

    /**
     * Get the name of the associated file.
     * @return the file name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param theName the filename to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the revision.
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Set the revision.
     * @param theVersion the revision to set
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Get the last modification time.
     *
     * @return date.getTime() compatible with File.lastModified() 
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Get the conflict information.
     * @return the conflict String
     */
    public String getConflict() {
        return conflict;
    }

    /**
     * Set the conflict information.
     * @param theConflict the conflict information
     */
    public void setConflict(String conflict) {
        this.conflict = conflict;
        this.lastModified = null;

        if (conflict == null
                || conflict.equals(DUMMY_TIMESTAMP)
                || conflict.equals(MERGE_TIMESTAMP)
                || conflict.equals(DUMMY_TIMESTAMP_NEW_ENTRY)) {
            return;
        }

        String dateString = conflict;

        // Look for the position of + which indicates a conflict
        int conflictIndex = dateString.indexOf(HAD_CONFLICTS);
        if (conflictIndex >= 0) {
            // if the timestamp matches the file, there will be an = following
            // the +
            int timeMatchIndex = dateString.indexOf(TIMESTAMP_MATCHES_FILE);
            conflictIndex = Math.max(conflictIndex, timeMatchIndex);
        }

        // At this point the conflict index tells us where the real conflict
        // string starts
        if (conflictIndex >= 0) {
            dateString = dateString.substring(conflictIndex + 1);
        }

        // if we have nothing after the = then don't try to parse it
        if (dateString.length() == 0) {
            return;
        }

        try {
            this.lastModified = getLastModifiedDateFormatter().parse(dateString);
        }
        catch (Exception ex) {
            lastModified = null;
//            System.err.println("[Entry] can't parse " + dateString); //NOI18N
        }
    }

    /**
     * Get the options information.
     * @return the options details
     */
    public String getOptions() {
        return options;
    }

    /**
     * Set the options information.
     * @param theOptions the options
     */
    public void setOptions(String options) {
        this.options = options;
    }

    /**
     * Get the sticky information.
     * It's either a tag, a date or null.
     */
    public String getStickyInformation() {
        if (tag != null) {
            return tag;
        }
        return getDateFormatted();
    }

    /**
     * Get the sticky tag information.
     * May return null if no tag information was present. If so, you should
     * check for date information. Note that tag and date information cannot
     * both be present.
     * @return the tag, or null if none is present
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set the sticky tag information.
     * Setting this will remove any date information that is set.
     * @param theTag the tag information
     */
    public void setTag(String tag) {
        this.tag = tag;
        date = null;
    }

    /**
     * Get sticky date information.
     * May return null if no date information is available. If so, you should
     * check for tag informaton. Note that tag and date information cannot both
     * be present.
     * @return the date, or null if none is present
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the sticky date information as a string in the appropriate format.
     * Returns null if there ain't a sticky date assigned.
     */
    public String getDateFormatted() {
        if (getDate() == null) {
            return null;
        }
        SimpleDateFormat format = getStickyDateFormatter();
        String dateFormatted = format.format(getDate());
        return dateFormatted;
    }

    /**
     * Set the sticky date information.
     * Note that setting this will remove any tag information that is currently set.
     * @param theDate the date to use.
     */
    public void setDate(Date date) {
        this.date = date;
        tag = null;
    }

    /**
     * Determines whether the entry has a date (as opposed to a tag).
     * @return true if the entry has a date, false otherwise
     */
    public boolean hasDate() {
        return (date != null);
    }

    /**
     * Determines whether the entry has a tag (as opposed to a date).
     * @return true if the entry has a tag, false otherwise
     */
    public boolean hasTag() {
        return (tag != null);
    }

    /**
     * Determines whether the file is a binary file.
     */
    public boolean isBinary() {
        return options != null
                && options.equals(BINARY_FILE);
    }

    /**
     * Determine whether there is no user file of that name.
     * @return true if there is no user file of that name
     */
    public boolean isNoUserFile() {
        return revision == null
                || revision.equals(NO_USER_FILE);
    }

    /**
     * Determine whether there is a new user file of that name.
     * @return true if there is a new user file with that name
     */
    public boolean isNewUserFile() {
        return revision != null
                && revision.startsWith(NEW_USER_FILE);
    }

    /**
     * Determine whether the user file of that name is to be removed.
     * @return true if the user file with this name is to be removed
     */
    public boolean isUserFileToBeRemoved() {
        return revision != null
                && revision.startsWith(REMOVE_USER_FILE);
    }

    /**
     * Determines whether the entry is valid.
     * A valid entry has at least a name.
     */
    public boolean isValid() {
        return getName() != null &&
                getName().length() > 0;
    }

    /**
     * Determine whether the entry refers to a directory.
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Set whether the entry refers to a directory.
     */
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    /**
     * Determine whether there were any conflicts.
     * @return true if there were conflicts, false otherwise
     */
    public boolean hadConflicts() {
        if (conflict != null) {
            return conflict.indexOf(HAD_CONFLICTS) >= 0;
        }
        else {
            return false;
        }
    }

    /**
     * Determine whether the timestamp matches the file.
     * @return true if the timpestamp does match the file, false otherwise
     */
    public boolean timestampMatchesFile() {
        return (conflict.charAt(1) == TIMESTAMP_MATCHES_FILE);
    }

    /**
     * Create a string representation of the entry line.
     * Create the standard CVS 1.10 entry line format.
     * <p>
     * Th eline format is suitable for writing into <tt>CVS/Entries</tt> file.
     * Conflict one must be transformed before sending to wire
     * {@link org.netbeans.lib.cvsclient.command.BasicCommand#sendEntryAndModifiedRequests}.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (directory) {
            buf.append(DIRECTORY_PREFIX);
        }
        else {
            buf.append('/');
        }
        // if name is null, then this is a totally empty entry, so append
        // nothing further
        if (name != null) {
            buf.append(name);
            buf.append('/');
            if (revision != null) {
                buf.append(revision);
            }
            buf.append('/');
            if (conflict != null) {
                buf.append(conflict);
            }
            buf.append('/');
            if (options != null) {
                buf.append(options);
            }
            buf.append('/');
            // TODO: put in tag_or_date section!!!
            // MK - Added. Based on assumption "There can be only one"
            if (tag != null && date == null) {
                if ("HEAD".equals(tag) == false) {
                    buf.append(TAG);
                    buf.append(getTag());
                }
            }
            else if (tag == null && date != null) {
                String dateString = getDateFormatted();
                buf.append(DATE);
                buf.append(dateString);
            }
        }
        return buf.toString();
    }
}
