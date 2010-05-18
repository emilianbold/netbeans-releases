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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package com.sun.rave.designtime.impl;

import java.awt.Image;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of DisplayAction to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DisplayAction
 */
public class BasicDisplayAction implements DisplayAction {

    protected String displayName;
    protected String description;
    protected String helpKey;
    protected Image smallIcon;
    protected Image largeIcon;
    protected boolean enabled = true;

    public BasicDisplayAction() {}

    public BasicDisplayAction(String displayName) {
        this.displayName = displayName;
    }

    public BasicDisplayAction(String displayName, String description) {
        this(displayName);
        this.description = description;
    }

    public BasicDisplayAction(String displayName, String description, String helpKey) {
        this(displayName, description);
        this.helpKey = helpKey;
    }

    public BasicDisplayAction(String displayName, String description, String helpKey,
        Image smallIcon) {
        this(displayName, description, helpKey);
        this.smallIcon = smallIcon;
    }

    public BasicDisplayAction(String displayName, String description, String helpKey,
        Image smallIcon, Image largeIcon) {
        this(displayName, description, helpKey, smallIcon);
        this.largeIcon = largeIcon;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setSmallIcon(Image smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Image getSmallIcon() {
        return smallIcon;
    }

    public void setLargeIcon(Image largeIcon) {
        this.largeIcon = largeIcon;
    }

    public Image getLargeIcon() {
        return largeIcon;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setHelpKey(String helpKey) {
        this.helpKey = helpKey;
    }

    public String getHelpKey() {
        return helpKey;
    }

    public Result invoke() {
        return Result.SUCCESS;
    }
}
