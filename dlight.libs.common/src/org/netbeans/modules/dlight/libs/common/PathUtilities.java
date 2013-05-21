/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.libs.common;

/**
 *
 * @author Vladimir Kvashin
 */
public class PathUtilities {

    private PathUtilities() {
    }
    
    /** Same as the C library dirname function: given a path, return
     * its directory name. Unlike dirname, however, return null if
     * the file is in the current directory rather than ".".
     */
    public static String getDirName(String path) {
        if (path == null) {
            return null;
        }        
        path = trimRightSlashes(path);
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return trimRightSlashes(path.substring(0, sep));
        }
        return null;
    }

    private static String trimRightSlashes(String path) {
        int length = path.length();
        while (length > 0 && (path.charAt(length-1) == '\\' || path.charAt(length-1) == '/')) {
            path = path.substring(0,length-1);
            length = path.length();
            break;
        }
        return path;
    }
        
    /** Same as the C library basename function: given a path, return
     * its filename.
     */
    public static String getBaseName(String path) {
        if (path == null) {
            return null; // making it consistent with getDirName
        }
        if (path.length()>0 && (path.charAt(path.length()-1) == '\\' || path.charAt(path.length()-1) == '/')) {
            path = path.substring(0,path.length()-1);
        }
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return path.substring(sep + 1);
        }
        return path;
    }        

    /**
     * Normalizes a Unix path, not necessarily absolute
     */
    public static String normalizeUnixPath(String absPath) {
        String norm = normalize(absPath);
        if (norm.startsWith("/../")) { //NOI18N
            int pos = norm.lastIndexOf("/../"); //NOI18N
            // the path normalize returns can only start with several "/../" , 
            // (it is when there are more "/../" segments than path nesting level)
            // but can not contain it in the middle
            if (norm.endsWith("/")) { // NOI18N
                norm = norm.substring(pos + 3, norm.length() - 1);
            } else {
                norm = norm.substring(pos + 3);
            }
        } else if (norm.endsWith("/")) { // NOI18N
            norm = norm.substring(0, norm.length() - 1);
        }
        return norm;
    }
   
    
    // Normalize the given path string.  A normal path string has no empty
    // segments (i.e., occurrences of "//"), no segments equal to ".", and no
    // segments equal to ".." that are preceded by a segment not equal to "..".
    // In contrast to Unix-style pathname normalization, for URI paths we
    // always retain trailing slashes.
    //
    private static String normalize(String ps) {

	// Does this path need normalization?
	int ns = needsNormalization(ps);	// Number of segments
	if (ns < 0)
	    // Nope -- just return it
	    return ps;

	char[] path = ps.toCharArray();		// Path in char-array form

	// Split path into segments
	int[] segs = new int[ns];		// Segment-index array
	split(path, segs);

	// Remove dots
	removeDots(path, segs);

	// Prevent scheme-name confusion
	maybeAddLeadingDot(path, segs);

	// Join the remaining segments and return the result
	String s = new String(path, 0, join(path, segs));
	if (s.equals(ps)) {
	    // string was already normalized
	    return ps;
	}
	return s;
    }
    
    
    // Join the segments in the given path according to the given segment-index
    // array, ignoring those segments whose index entries have been set to -1,
    // and inserting slashes as needed.  Return the length of the resulting
    // path.
    //
    // Preconditions:
    //   segs[i] == -1 implies segment i is to be ignored
    //   path computed by split, as above, with '\0' having replaced '/'
    //
    // Postconditions:
    //   path[0] .. path[return value] == Resulting path
    //
    static private int join(char[] path, int[] segs) {
	int ns = segs.length;		// Number of segments
	int end = path.length - 1;	// Index of last char in path
	int p = 0;			// Index of next path char to write

	if (path[p] == '\0') {
	    // Restore initial slash for absolute paths
	    path[p++] = '/';
	}

	for (int i = 0; i < ns; i++) {
	    int q = segs[i];		// Current segment
	    if (q == -1)
		// Ignore this segment
		continue;

	    if (p == q) {
		// We're already at this segment, so just skip to its end
		while ((p <= end) && (path[p] != '\0'))
		    p++;
		if (p <= end) {
		    // Preserve trailing slash
		    path[p++] = '/';
		}
	    } else if (p < q) {
		// Copy q down to p
		while ((q <= end) && (path[q] != '\0'))
		    path[p++] = path[q++];
		if (q <= end) {
		    // Preserve trailing slash
		    path[p++] = '/';
		}
	    } else
		throw new InternalError(); // ASSERT false
	}

	return p;
    }

    // DEVIATION: If the normalized path is relative, and if the first
    // segment could be parsed as a scheme name, then prepend a "." segment
    //
    private static void maybeAddLeadingDot(char[] path, int[] segs) {

	if (path[0] == '\0')
	    // The path is absolute
	    return;

	int ns = segs.length;
	int f = 0;			// Index of first segment
	while (f < ns) {
	    if (segs[f] >= 0)
		break;
	    f++;
	}
	if ((f >= ns) || (f == 0))
	    // The path is empty, or else the original first segment survived,
	    // in which case we already know that no leading "." is needed
	    return;

	int p = segs[f];
	while ((p < path.length) && (path[p] != ':') && (path[p] != '\0')) p++;
	if (p >= path.length || path[p] == '\0')
	    // No colon in first segment, so no "." needed
	    return;

	// At this point we know that the first segment is unused,
	// hence we can insert a "." segment at that position
	path[0] = '.';
	path[1] = '\0';
	segs[0] = 0;
    }

    // Remove "." segments from the given path, and remove segment pairs
    // consisting of a non-".." segment followed by a ".." segment.
    //
    private static void removeDots(char[] path, int[] segs) {
	int ns = segs.length;
	int end = path.length - 1;

	for (int i = 0; i < ns; i++) {
	    int dots = 0;		// Number of dots found (0, 1, or 2)

	    // Find next occurrence of "." or ".."
	    do {
		int p = segs[i];
		if (path[p] == '.') {
		    if (p == end) {
			dots = 1;
			break;
		    } else if (path[p + 1] == '\0') {
			dots = 1;
			break;
		    } else if ((path[p + 1] == '.')
			       && ((p + 1 == end)
				   || (path[p + 2] == '\0'))) {
			dots = 2;
			break;
		    }
		}
		i++;
	    } while (i < ns);
	    if ((i > ns) || (dots == 0))
		break;

	    if (dots == 1) {
		// Remove this occurrence of "."
		segs[i] = -1;
	    } else {
		// If there is a preceding non-".." segment, remove both that
		// segment and this occurrence of ".."; otherwise, leave this
		// ".." segment as-is.
		int j;
		for (j = i - 1; j >= 0; j--) {
		    if (segs[j] != -1) break;
		}
		if (j >= 0) {
		    int q = segs[j];
		    if (!((path[q] == '.')
			  && (path[q + 1] == '.')
			  && (path[q + 2] == '\0'))) {
			segs[i] = -1;
			segs[j] = -1;
		    }
		}
	    }
	}
    }

    
    // Split the given path into segments, replacing slashes with nulls and
    // filling in the given segment-index array.
    //
    // Preconditions:
    //   segs.length == Number of segments in path
    //
    // Postconditions:
    //   All slashes in path replaced by '\0'
    //   segs[i] == Index of first char in segment i (0 <= i < segs.length)
    //
    static private void split(char[] path, int[] segs) {
	int end = path.length - 1;	// Index of last char in path
	int p = 0;			// Index of next char in path
	int i = 0;			// Index of current segment

	// Skip initial slashes
	while (p <= end) {
	    if (path[p] != '/') break;
	    path[p] = '\0';
	    p++;
	}

	while (p <= end) {

	    // Note start of segment
	    segs[i++] = p++;

	    // Find beginning of next segment
	    while (p <= end) {
		if (path[p++] != '/')
		    continue;
		path[p - 1] = '\0';

		// Skip redundant slashes
		while (p <= end) {
		    if (path[p] != '/') break;
		    path[p++] = '\0';
		}
		break;
	    }
	}

	if (i != segs.length)
	    throw new InternalError();	// ASSERT
    }
    
    
    // -- Path normalization --

    // The following algorithm for path normalization avoids the creation of a
    // string object for each segment, as well as the use of a string buffer to
    // compute the final result, by using a single char array and editing it in
    // place.  The array is first split into segments, replacing each slash
    // with '\0' and creating a segment-index array, each element of which is
    // the index of the first char in the corresponding segment.  We then walk
    // through both arrays, removing ".", "..", and other segments as necessary
    // by setting their entries in the index array to -1.  Finally, the two
    // arrays are used to rejoin the segments and compute the final result.
    //
    // This code is based upon src/solaris/native/java/io/canonicalize_md.c


    // Check the given path to see if it might need normalization.  A path
    // might need normalization if it contains duplicate slashes, a "."
    // segment, or a ".." segment.  Return -1 if no further normalization is
    // possible, otherwise return the number of segments found.
    //
    // This method takes a string argument rather than a char array so that
    // this test can be performed without invoking path.toCharArray().
    //
    static private int needsNormalization(String path) {
	boolean normal = true;
	int ns = 0;			// Number of segments
	int end = path.length() - 1;	// Index of last char in path
	int p = 0;			// Index of next char in path

	// Skip initial slashes
	while (p <= end) {
	    if (path.charAt(p) != '/') break;
	    p++;
	}
	if (p > 1) normal = false;

	// Scan segments
	while (p <= end) {

	    // Looking at "." or ".." ?
	    if ((path.charAt(p) == '.')
		&& ((p == end)
		    || ((path.charAt(p + 1) == '/')
			|| ((path.charAt(p + 1) == '.')
			    && ((p + 1 == end)
				|| (path.charAt(p + 2) == '/')))))) {
		normal = false;
	    }
	    ns++;

	    // Find beginning of next segment
	    while (p <= end) {
		if (path.charAt(p++) != '/')
		    continue;

		// Skip redundant slashes
		while (p <= end) {
		    if (path.charAt(p) != '/') break;
		    normal = false;
		    p++;
		}

		break;
	    }
	}

	return normal ? -1 : ns;
    }    
}
