/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "LogicalLineVisitor.java"
 * LogicalLineVisitor.java 1.1 01/07/24
 */

package org.netbeans.lib.terminalemulator;

/**
 * Passed to one of visitLogicalLines() or reverseVisitLogicalLines().
 */

public interface LogicalLineVisitor {
    /**
     * Called for each logical line.
     * <p>
     * 'text' contains the actual text of a complete line which may be 
     * wrapped multiple times. 'begin' and 'end' mark the region.
     * <p>
     * Note that for the first line 'begin' will match the 'begin' passed to
     * visitLogicalLines(), so watch out if you specify a visitation range
     * that starts in the middle of a line.
     * <p>
     * Normally end.row == begin.row, but if the logical line was wrapped,
     * end.row > begin.row.
     * <p>
     * 'line' is intended to represent a line number, however if the
     * visitation range isn't the whole document it should be interpreted only
     * as a serial number and in the case of reverseVisitLogicalLines() the
     * sequence of line numbers will be backwards.
     * <p>
     * If you locate something in 'text' and need to convert it back to 
     * a Coord use extentInLogicalLine().
     */

    public boolean visit(int line, Coord begin, Coord end, String text);
}
