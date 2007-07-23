/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning.spi.testvcs;

import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.Image;
import java.awt.event.ActionEvent;

/**
 * Annotator for TestVCS.
 * 
 * @author Maros Sandor
 */
public class TestVCSAnnotator extends VCSAnnotator {
    
    public TestVCSAnnotator() {
    }

    public String annotateName(String name, VCSContext context) {
        if (name.equals("annotate-me")) {
            return "annotated";
        }
        return name;
    }

    public Image annotateIcon(Image icon, VCSContext context) {
        return icon;
    }

    public Action[] getActions(VCSContext context, ActionDestination destination) {
        return new Action[] {
            new DummyAction()
        };
    }
    
    private static class DummyAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            // do nothing
        }
    }
}
