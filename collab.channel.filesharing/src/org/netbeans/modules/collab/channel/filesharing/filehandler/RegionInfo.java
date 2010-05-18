/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
