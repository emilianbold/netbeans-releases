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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.progress.module;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.progress.spi.ExtractedProgressUIWorker;
import org.netbeans.progress.spi.ProgressEvent;
import org.netbeans.progress.spi.ProgressUIWorkerProvider;
import org.netbeans.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.progress.spi.TaskModel;

/**
 * Fallback provider in case no GUI is registered.
 * Just enough to make unit tests run without errors, etc.
 * @author Jesse Glick
 * @see "issue #87812"
 */
public class TrivialProgressUIWorkerProvider implements ProgressUIWorkerProvider, ProgressUIWorkerWithModel, ExtractedProgressUIWorker {

    public TrivialProgressUIWorkerProvider() {}

    public ProgressUIWorkerWithModel getDefaultWorker() {
        return this;
    }

    public ExtractedProgressUIWorker getExtractedComponentWorker() {
        return this;
    }

    public void setModel(TaskModel model) {}

    public void showPopup() {}

    public void processProgressEvent(ProgressEvent event) {}

    public void processSelectedProgressEvent(ProgressEvent event) {}

    public JComponent getProgressComponent() {
        return new JPanel();
    }

    public JLabel getMainLabelComponent() {
        return new JLabel();
    }

    public JLabel getDetailLabelComponent() {
        return new JLabel();
    }

}
