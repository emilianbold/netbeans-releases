/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
