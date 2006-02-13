/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * @author Ayub Khan, ayub.khan@sun.com
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
