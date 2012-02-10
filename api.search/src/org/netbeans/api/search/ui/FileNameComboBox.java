/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.search.ui;

import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;

/**
 * Component for specifying file name pattern.
 *
 * @author jhavlin
 */
public abstract class FileNameComboBox extends JComboBox {

    /**
     * Get pattern for matching file names.
     */
    public abstract String getFileNamePattern();

    /**
     * Sets whether the contained expression should be interpreted as a simple
     * pattern or a regular expression pattern.
     *
     * @param regularExpression True if the contained string is a regular
     * expression, false if it is a simple pattern.
     */
    public abstract void setRegularExpression(boolean regularExpression);

    /**
     * Tells whether the contained expression should be interpreted as a simple
     * pattern or a regular expression pattern.
     *
     * @return True if the contained string stands for a regula expressiong,
     * false if it stands for a simple pattern.
     */
    public abstract boolean isRegularExpression();

    /**
     * Tell whether gray (all files) text is currently displayed.
     */
    public abstract boolean isAllFilesInfoDisplayed();

    /**
     * Display (all files) text.
     */
    public abstract void displayAllFilesInfo();

    /**
     * Hide (all files) text.
     */
    public abstract void hideAllFilesInfo();

    /**
     * Add listener that is notified about chagnes in the file name pattern.
     */
    public abstract void addPatternChangeListener(ChangeListener l);

    /**
     * Remove pattern change listener.
     */
    public abstract void removePatternChangeListener(ChangeListener l);
}
