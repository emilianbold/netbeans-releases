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

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;


/**
 * Bean that holds region information
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class RegionInfo extends Object implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public final static String LINE_RANGE = "LineRange";
    public final static String CHAROFFSET_RANGE = "CharacterRange";

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String regionName = null;
    private String fileName = null;
    private String fileGroupName = null;
    private String annotation = null;
    private String mode = null;
    private int begin = 0;
    private int end = 0;
    private int endCorrection = 0;
    private Vector lineRegions = null;

    /**
         *
         * @param regionName                region Name
         * @param fileName                        file where this region belong
         * @param annotation                annotation
         * @param mode                                LINE_RANGE or CHAROFFSET_RANGE
         * @param begin                                (beginOffset or beginLine)
         * @param end                                (endOffset or endLine)
         */
    public RegionInfo(
        String regionName, String fileName, String fileGroupName, String annotation, String mode, int begin, int end,
        int endCorrection, Vector lineRegions
    ) {
        super();
        this.regionName = regionName;
        this.fileName = fileName;
        this.fileGroupName = fileGroupName;
        this.annotation = annotation;
        this.mode = mode;
        this.begin = begin;
        this.end = end;
        this.endCorrection = endCorrection;
        this.lineRegions = lineRegions;
    }

    /**
         *
         * @param regionName                region Name
         * @param fileName                        file where this region belong
         * @param mode                                LINE_RANGE or CHAROFFSET_RANGE
         * @param begin                                (beginOffset or beginLine)
         * @param end                                (endOffset or endLine)
         */
    public RegionInfo(
        String regionName, String fileName, String fileGroupName, String mode, int begin, int end, int endCorrection
    ) {
        this(
            regionName, fileName, fileGroupName, fileName + FILE_SEPERATOR + regionName, mode, begin, end, endCorrection,
            null
        );
    }

    /**
         *
         * @param regionName                region Name
         * @param fileName                        file where this region belong
         * @param mode                                LINE_RANGE or CHAROFFSET_RANGE
         * @param begin                                (beginOffset or beginLine)
         * @param end                                (endOffset or endLine)
         */
    public RegionInfo(
        String regionName, String fileName, String fileGroupName, String mode, int begin, int end, int endCorrection,
        Vector lineRegions
    ) {
        this(
            regionName, fileName, fileGroupName, fileName + FILE_SEPERATOR + regionName, mode, begin, end, endCorrection,
            lineRegions
        );
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         *
         * @return regionName                region Name
         */
    public String getID() {
        return this.regionName;
    }

    /**
         *
         * @return fileName                        file where this region belong
         */
    public String getFileName() {
        return this.fileName;
    }

    /**
         *
         * @return fileNameGroup                        fileGroup where this region belong
         */
    public String getFileGroupName() {
        return this.fileGroupName;
    }

    /**
         *
         * @return mode                                LINE_RANGE or CHAROFFSET_RANGE
         */
    public String getMode() {
        return this.mode;
    }

    /**
         *
         * @param begin                                (beginOffset or beginLine)
         */
    public void setbegin(int begin) {
        this.begin = begin;
    }

    /**
         *
         * @return begin                        (beginOffset or beginLine)
         */
    public int getbegin() {
        return this.begin;
    }

    /**
         *
         * @param end                                (endOffset or endLine)
         */
    public void setend(int end) {
        this.end = end;
    }

    /**
         *
         * @return end                                (endOffset or endLine)
         */
    public int getend() {
        return this.end;
    }

    /**
         *
         * @return endCorrection
         */
    public int getCorrection() {
        return this.endCorrection;
    }

    /**
         *
         * @return lineRegions
         */
    public Vector getLineRegion() {
        return this.lineRegions;
    }
}
