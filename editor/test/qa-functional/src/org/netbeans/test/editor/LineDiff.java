/*
 * LineDiff.java
 *
 * Created on March 28, 2002, 9:49 AM
 */

package org.netbeans.test.editor;

import org.netbeans.junit.diff.Diff;
import java.io.File;
import java.io.LineNumberReader;
import java.io.FileReader;

/**
 *
 * @author  lahvac
 */
public class LineDiff implements Diff {
    
    private boolean ignoreCase;
    
    /** Creates a new instance of LineDiff */
    public LineDiff(boolean ignoreCase) {
	this.ignoreCase = ignoreCase;
    }
    
    public boolean getIgnoreCase() {
	return ignoreCase;
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(String first, String second, String diff) throws java.io.IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(java.io.File firstFile, java.io.File secondFile, java.io.File diffFile) throws java.io.IOException {
	LineNumberReader first = new LineNumberReader(new FileReader(firstFile));
	LineNumberReader second = new LineNumberReader(new FileReader(secondFile));
	
	while (true) {
	    String firstLine = first.readLine();
	    String secondLine = second.readLine();
	    
	    if ((firstLine == null) ^ (secondLine == null))
		return true;
	    
	    if (secondLine == null)
		return false;
	    
	    if (getIgnoreCase()) {
		if (!secondLine.equalsIgnoreCase(firstLine))
		    return true;
	    } else {
		if (!secondLine.equals(firstLine))
		    return true;
	    }
	}
    }
    
}
