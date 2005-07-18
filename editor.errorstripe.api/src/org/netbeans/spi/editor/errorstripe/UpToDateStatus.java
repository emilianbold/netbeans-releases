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

package org.netbeans.spi.editor.errorstripe;

/**Up-to-date status enumeration. See {@link UpToDateStatusProvider#getUpToDate}
 * for more information.
 *
 * @author Jan Lahoda
 */
public final class UpToDateStatus implements Comparable {
    
    /**Up-to-date status saying everything is up-to-date.
     */
    private static final int UP_TO_DATE_OK_VALUE = 0;
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, but a up-to-date list of marks is currently
     * being found.
     */
    private static final int UP_TO_DATE_PROCESSING_VALUE = 1;
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, and nothing is currently done in order to
     * get the up-to-date list.
     */
    private static final int UP_TO_DATE_DIRTY_VALUE = 2;

    /**Up-to-date status saying everything is up-to-date.
     */
    public static final UpToDateStatus UP_TO_DATE_OK = new UpToDateStatus (UP_TO_DATE_OK_VALUE);
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, but a up-to-date list of marks is currently
     * being found.
     */
    public static final UpToDateStatus UP_TO_DATE_PROCESSING = new UpToDateStatus (UP_TO_DATE_PROCESSING_VALUE);
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, and nothing is currently done in order to
     * get the up-to-date list.
     */
    public static final UpToDateStatus UP_TO_DATE_DIRTY = new UpToDateStatus (UP_TO_DATE_DIRTY_VALUE);

    private int status;
    
    /** Creates a new instance of UpToDateStatus */
    private UpToDateStatus(int status) {
        this.status = status;
    }
    
    private int getStatus() {
        return status;
    }
    
    public int compareTo(Object o) {
        UpToDateStatus remote = (UpToDateStatus) o;
        
        return status - remote.status;
    }
    
    public int hashCode() {
        return 73 ^ status;
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof UpToDateStatus))
            return false;
        
        return compareTo(obj) == 0;
    }
    
    private static final String[] statusNames = new String[] {
        "OK",
        "PROCESSING",
        "DIRTY",
    };
    
    public String toString() {
        return statusNames[getStatus()];
    }
}
