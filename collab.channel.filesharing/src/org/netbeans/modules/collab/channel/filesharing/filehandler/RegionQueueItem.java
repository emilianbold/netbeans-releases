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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import java.io.*;

import java.util.*;


/**
 * QueueItem
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class RegionQueueItem extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private int beginLine;
    private int endLine;
    private int endOffsetCorrection;

    /**
         *
         *
         */
    public RegionQueueItem(int beginLine, int endLine, int endOffsetCorrection) {
        super();
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.endOffsetCorrection = endOffsetCorrection;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         * getBeginLine
         *
         * @return beginLine
         */
    public int getBeginLine() {
        return this.beginLine;
    }

    /**
         * getEndLine
         *
         * @return endLine
         */
    public int getEndLine() {
        return this.endLine;
    }

    /**
         * getEndOffsetCorrection
         *
         * @return endOffsetCorrection
         */
    public int getEndOffsetCorrection() {
        return this.endOffsetCorrection;
    }
}
