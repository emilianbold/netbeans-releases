/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import java.io.File;
import java.util.StringTokenizer;

public class JDKVersion {

    /* If a version string is 1.4.2_03, the major number is 1. */
    private int majorNum  = 0;

    /* If a version string is 1.4.2_03, the minor number is 4. */
    private int minorNum  = 0;

    /* If a version string is 1.4.2_03, the micro number is 2. */
    private int microNum  = 0;

    /* If a version string is 1.4.2_03, the update number is 01. */
    private int updateNum = 0;

    /* Indicates that an update number exists in the version string. */
    private boolean hasUpdateNum    = false;

    /* Indicates that a micro number exists in the version string. */
    private boolean has3VersionNums = false;

    /* The string representation of this class. */
    private String jdkVersion = null;

    private static String minimumJDKString = "1.4.2";
    private static String recommendedJDKString = "1.5.0";

    private static JDKVersion minimumJDKVersion = null;
    private static JDKVersion recommendedJDKVersion = null;

    /** This class is useful for comparing the jdk version strings obtained from
     *  the command "java -version". This typically returns a string similar to
     *  "1.4.2_03".
     *  Assume we always have a major number and a minor number for this class.
     *  Assume the version number always has the form 1.2, 1.2.3_nn or 1.2-aaa
     *  and is embedded in quotes.
     */
    public JDKVersion(String version) {
	jdkVersion = version;

	// Strip quotes then strip ending alpha characters, if any.
	version = trimEndingAlphaChars(trimQuotes(version));

	// Check to see if there is an update number. If so, get it and strip
	// the string from the underscore to the end of the string;
	int index = version.indexOf("_");
	if (index > 0) {
	    String subStr = version.substring(index+1,version.length());
	    updateNum = Integer.valueOf(subStr).intValue();
	    hasUpdateNum = true;
	    version = version.substring(0,index);
	}

	// Check to see if there is a dash. If so, strip it and
	// the rest of the string;
	index = version.indexOf("-");
	if (index > 0) {
	    version = version.substring(0, index);
	}

	StringTokenizer versionPieces = new StringTokenizer(version, ".");
	if (versionPieces.hasMoreTokens()) {
	    majorNum = Integer.valueOf(versionPieces.nextToken()).intValue();
	}
	if (versionPieces.hasMoreTokens()) {
	    minorNum = Integer.valueOf(versionPieces.nextToken()).intValue();
	}
	if (versionPieces.hasMoreTokens()) {
	    microNum = Integer.valueOf(versionPieces.nextToken()).intValue();
	    has3VersionNums = true;
	}
    }

    public String toString() {
	return jdkVersion;
    }

    /** Compares the Object to the Object's argument.
     *  @return  true if Object is equal to the argument.
     */
    public boolean equals(JDKVersion jdkVersion) {
	if (this.getMajorNum() == jdkVersion.getMajorNum()) {
	    if (this.getMinorNum() == jdkVersion.getMinorNum()) {
		if (this.has3VersionNums() && jdkVersion.has3VersionNums()) {
		    if (this.getMicroNum() == jdkVersion.getMicroNum()) {
			if (this.hasUpdateNum() && jdkVersion.hasUpdateNum()) {
			    if (this.getUpdateNum() == jdkVersion.getUpdateNum()) {
				return true;
			    }
			}
			else if (!this.hasUpdateNum() && !jdkVersion.hasUpdateNum()) {
			    return true;
			}
		    }
		}
		else if (!this.has3VersionNums() && !jdkVersion.has3VersionNums()) {
		    return true;
		}
	    }
	}
	return false;
    }

    /** Compares the Object to the Object's argument.
     *  @return -1 if Object is less than the argument,
     *  @return  0 if Object is equal to the argument,
     *  @return  1 if Object is greater than the argument.
     */
    public int compareTo(JDKVersion jdkVersion) {
	if (this.getMajorNum() > jdkVersion.getMajorNum()) {
	    return 1;
	} else if (this.getMajorNum() == jdkVersion.getMajorNum()) {
	    if (this.getMinorNum() > jdkVersion.getMinorNum()) {
		return 1;
	    } else if (this.getMinorNum() == jdkVersion.getMinorNum()) {
		if (this.has3VersionNums() && jdkVersion.has3VersionNums()) {
		    if (this.getMicroNum() > jdkVersion.getMicroNum()) {
			return 1;
		    } else if (this.getMicroNum() == jdkVersion.getMicroNum()) {
			if (this.hasUpdateNum() && jdkVersion.hasUpdateNum()) {
			    if (this.getUpdateNum() > jdkVersion.getUpdateNum()) {
				return 1;
			    } else if (this.getUpdateNum() == jdkVersion.getUpdateNum()) {
				return 0;
			    }
			} else if (!this.hasUpdateNum() && !jdkVersion.hasUpdateNum()) {
			    return 0;
			} else if (this.hasUpdateNum()) {
			    return 1;
			}
		    }
		} else if (!this.has3VersionNums() && !jdkVersion.has3VersionNums()) {
		    return 0;
		} else if (this.has3VersionNums()) {
		    return 1;
		}
	    }
	}
	return -1;
    }

    /* Remove the enclosing quotes from the version string. */
    public static String trimQuotes(String version) {
	if (version.length() > 0) {
	    // strip spaces
	    version = version.trim();
	    // Strip quotes, if any
	    int i = version.lastIndexOf("\"");
	    if ( i > 0) {
		version = version.substring(0, i);
	    }
	    i = version.lastIndexOf("\"");
	    if (i >= 0 && version.charAt(i) == ('\"')) {
		version = version.substring(i+1);
	    }
	}
	return version;
    }

    public static String trimEndingAlphaChars(String version) {
	if (version.length() > 0) {
	    for (int i = version.length(); i>0; i--) {
		try {
		    String chr = version.substring(i-1,i);
		    Integer.valueOf(chr);
		    version = version.substring(0,i);
		    break;
		} catch (NumberFormatException err) {
		    // Just continue till you get all characters removed.
		}
	    }
	}
	return version;
    }
	    

    public static boolean isBelowMinimumJDK(String jdkPath) {
	String version = JDKVersion.getVersionString(jdkPath);
	JDKVersion jdkVersion = new JDKVersion(version);
	
	return jdkVersion.isBelowMinimumJDK();
    }

    public boolean isBelowMinimumJDK() {
	if (this.compareTo(getMinimumJDKVersion()) < 0) return true;
	return false;
    }

    public static boolean isBelowRecommendedJDK(String jdkPath) {
	String version = JDKVersion.getVersionString(jdkPath);
	JDKVersion jdkVersion = new JDKVersion(version);
	
	return jdkVersion.isBelowRecommendedJDK();
    }

    public boolean isBelowRecommendedJDK() {
	if (this.compareTo(getRecommendedJDKVersion()) < 0) return true;
	return false;
    }

    public boolean isEqualToRecommendedJDK() {
	return this.equals(getRecommendedJDKVersion());
    }

    public JDKVersion getMinimumJDKVersion() {
	if (minimumJDKVersion == null) {
	    minimumJDKVersion = new JDKVersion(minimumJDKString);
	}
	return minimumJDKVersion;
    }

    public JDKVersion getRecommendedJDKVersion() {
	if (recommendedJDKVersion == null) {
	    recommendedJDKVersion = new JDKVersion(recommendedJDKString);
	}
	return recommendedJDKVersion;
    }

    public static String getMinimumJDKString() {
	return minimumJDKString;
    }

    public static String getRecommendedJDKString() {
	return recommendedJDKString;
    }

    /** Get the version string from JVM pointed to by the path argument.
      * @return - empty string if it can't get the version string.
      */
    public static String getVersionString(String jdkPath) {
	File jvmFile = new File(jdkPath + File.separator + "bin" + File.separator + Util.getJVMName()); //NOI18N
	RunCommand runCommand = new RunCommand();
        String [] cmdArr = new String[2];
        cmdArr[0] = jvmFile.getAbsolutePath();
        cmdArr[1] = "-version";
	runCommand.execute(cmdArr);
        runCommand.waitFor();
	String line = runCommand.getErrorLine();
	if (line == null) {
	    line =  "";
	}

	return trimQuotes(line);
    }

    public int getMajorNum() {
	return majorNum;
    }
    
    public int getMinorNum() {
	return minorNum;
    }
    
    public int getMicroNum() {
	return microNum;
    }
    
    public int getUpdateNum() {
	return updateNum;
    }
    
    private boolean has3VersionNums() {
	return has3VersionNums;
    }
    
    private boolean hasUpdateNum() {
	return hasUpdateNum;
    }
}
