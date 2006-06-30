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

package org.netbeans.modules.diff;

import java.awt.Component;
import java.io.IOException;
import java.io.Reader;
import javax.swing.JPanel;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.DefaultDiff;

/**
 *
 * @author Martin Entlicher
 */
public class DefaultDiffViewTest extends DiffViewAbstract {

    /** Creates a new instance of DefaultDiffViewTest */
    public DefaultDiffViewTest(String name) {
        super(name);
    }

    protected DiffView createDiffView(StreamSource ss1, StreamSource ss2) throws IOException {
        DefaultDiff dd = new DefaultDiff();
        return dd.createDiff(ss1, ss2);
    }
    
}
