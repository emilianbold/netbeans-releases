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


package org.netbeans.modules.visualweb.extension.openide.awt;


import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;

import org.openide.awt.StatusDisplayer;


/**
 * Rave extension of StatusDisplayer.
 *
 * @author  Peter Zavadsky
 * @author  Winston Prakash (the extended methods)
 */
public abstract class StatusDisplayer_RAVE extends StatusDisplayer {


    private static StatusDisplayer_RAVE INSTANCE = null;


    /** Creates a new instance of StatusDisplayer_RAVE */
    protected StatusDisplayer_RAVE() {
    }

    /** Get the default Rave status displayer.
     * @return the default Rave instance  */
    public static synchronized StatusDisplayer_RAVE getRaveDefault() {
        if (INSTANCE == null) {
            StatusDisplayer sd = StatusDisplayer.getDefault();
            if(sd instanceof StatusDisplayer_RAVE) {
                INSTANCE = (StatusDisplayer_RAVE)sd;
            } else {
                INSTANCE = new Trivial();
            }
        }
        return INSTANCE;
    }

// <RAVE>
    public abstract void clearPositionLabel();

    public abstract void setPositionLabelIcon(ImageIcon icon);

    public abstract void setPositionLabelText(final String text);

    public abstract void displayError(String errMsg, int severity);
// </RAVE>

    /**
     * Trivial default impl for standalone usage.
     * @see "#32154"
     */
    private static final class Trivial extends StatusDisplayer_RAVE {

        public String getStatusText() {
            return StatusDisplayer.getDefault().getStatusText();
        }

        public void setStatusText(String text) {
            StatusDisplayer.getDefault().setStatusText(text);
        }

        public void addChangeListener(ChangeListener l) {
            StatusDisplayer.getDefault().addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            StatusDisplayer.getDefault().removeChangeListener(l);
        }

// <RAVE>
        public void clearPositionLabel() {}

        public void setPositionLabelIcon(ImageIcon icon) {}

        public void setPositionLabelText(final String text) {}

        public void displayError(String errMsg, int severity) {
            setStatusText(errMsg);
        }
// </RAVE>
    }
}
