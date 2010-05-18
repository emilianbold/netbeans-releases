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
package org.netbeans.lib.cvsclient.command.annotate;

import java.text.*;
import java.util.*;

/**
 * @author  Thomas Singer
 */
public class AnnotateLine {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yy", //NOI18N
                                                                       Locale.US);

    private String author;
    private String revision;
    private Date date;
    private String dateString;
    private String content;
    private int lineNum;

    public AnnotateLine() {
    }

    /**
     * Returns the author of this line.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of this line.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the revision of this line.
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision of this line.
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the date of this line.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns the date in original String-representation of this line.
     */
    public String getDateString() {
        return dateString;
    }

    /**
     * Sets the date of this line.
     */
    public void setDateString(String dateString) {
        this.dateString = dateString;
        try {
            this.date = DATE_FORMAT.parse(dateString);
        }
        catch (ParseException ex) {
            // print stacktrace, because it's a bug
            ex.printStackTrace();
        }
    }

    /**
     * Return the line's content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the line's content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the line's number. It's 1 based.
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Returns the line's number.
     */
    public Integer getLineNumInteger() {
        return new Integer(lineNum);
    }

    /**
     * Sets the line's number.
     */
    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }
}
