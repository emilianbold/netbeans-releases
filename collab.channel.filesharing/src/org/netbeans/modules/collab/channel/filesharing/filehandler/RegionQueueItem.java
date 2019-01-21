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
import java.io.*;

import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.openide.text.NbDocument;


/**
 * QueueItem
 *
 * 
 * @version 1.0
 */
public class RegionQueueItem extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Position beginLine;
    private Position endLine;
    private int endOffsetCorrection;
    private StyledDocument doc;

    /**
         *
         *
         */
    public RegionQueueItem(Document doc2, int beginLine, int endLine, int endOffsetCorrection) throws CollabException {
        super();
        this.doc = (StyledDocument)doc2;
        try {
            this.beginLine = NbDocument.createPosition(doc, NbDocument.findLineOffset(doc, beginLine), Position.Bias.Backward);
            this.endLine = NbDocument.createPosition(doc, NbDocument.findLineOffset(doc, endLine), Position.Bias.Forward);
        } catch (BadLocationException be) {
            throw (CollabException)new CollabException(be.getMessage()).initCause(be);
        }
        this.endOffsetCorrection = endOffsetCorrection;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         * getBeginLine
         *
         * @return beginLine
         */
    public int getBeginLine() {
        return NbDocument.findLineNumber(doc, beginLine.getOffset());
    }

    /**
         * getEndLine
         *
         * @return endLine
         */
    public int getEndLine() {
        return NbDocument.findLineNumber(doc, endLine.getOffset());
    }

    /**
         * getEndOffsetCorrection
         *
         * @return endOffsetCorrection
         */
    public int getEndOffsetCorrection() {
        return this.endOffsetCorrection;
    }
}
