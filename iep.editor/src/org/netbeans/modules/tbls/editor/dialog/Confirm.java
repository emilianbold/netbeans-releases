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

package org.netbeans.modules.tbls.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

/**
 *  DOCUMENT ME!
 *
 * @author    Bing Lu
 */
public class Confirm implements OKCancelDialogInterface, ActionListener {
   
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(Confirm.class.getName());

    private String mMsg;

    private String mTitle;

    private boolean mOk = false;


    /**
     *  DOCUMENT ME!
     *
     * @param  title  The title
     * @param  msg    The message
     */
    public Confirm(String title, String msg) {
        mMsg = msg;
        mTitle = title;
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The actionListener value
     */
    public ActionListener getActionListener() {
        return this;
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The innerPane value
     */
    public Component getInnerPane() {
        return new JLabel(mMsg);
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The ok value
     */
    public boolean isOk() {
        return mOk;
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The title value
     */
    public String getTitle() {
        return mTitle;
    }


    /**
     *  DOCUMENT ME!
     *
     * @param  e  the action event
     */
    public void actionPerformed(ActionEvent e) {

        mOk =  (e.getActionCommand().equals("OK"));
    }
}

