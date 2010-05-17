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

import com.sun.collablet.CollabException;

import org.openide.loaders.*;


/**
 * CollabRegion interface
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public interface CollabRegion {
    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getter for regionName
     *
     * @return regionName
     */
    public String getID();

    /**
     * getter for guarded
     *
     * @return guarded
     */
    public boolean isGuarded();

    /**
     * setter for guarded section
     *
     * @param sect
     */
    public void setGuard(Object sect);

    /**
     * getter for guarded section
     *
     * @return sect
     */
    public Object getGuard();

    /**
     * getter for regionBegin
     *
     * @return regionBegin
     */
    public int getBeginOffset();

    /**
     * getter for regionEnd
     *
     * @return regionEnd
     */
    public int getEndOffset();

    /**
     * getter for region content
     *
     * @throws CollabException
     * @return region content
     */
    public String getContent() throws CollabException;

    /**
     * test if region changed
     *
     * @return true if region changed
     */
    public boolean isChanged();

    /**
     * API to change status
     *
     * @param change, if true then isChanged() returns true
     */
    public void updateStatusChanged(boolean change);

    /**
     * test if region is ready to unlock (ready for removal)
     *
     * @return true if region is ready to unlock (ready for removal)
     */
    public boolean isReadyToUnlock();

    /**
     * set ready to unlock (ready for removal)
     *
     */
    public void setReadyToUnlock();

    /**
     * getter for region unlock interval
     *
     * @return interval
     */
    public int getInterval();

    /**
     * addAnnotation
     *
     * @param dataObject
     * @param annotation
     */
    public void addAnnotation(
        DataObject dataObject, CollabFileHandler fileHandler, int style,
        String annotationMessage
    ) throws CollabException;

    /**
     * removeAnnotation
     *
     */
    public void removeAnnotation() throws CollabException;

    /**
     * setValid
     *
     * @param        status                                        if false handler is invalid
     * @throws CollabException
     */
    public void setValid(boolean valid);

    /**
     * test if the filehandler is valid
     *
     * @return        true/false                                true if valid
     */
    public boolean isValid();
}
