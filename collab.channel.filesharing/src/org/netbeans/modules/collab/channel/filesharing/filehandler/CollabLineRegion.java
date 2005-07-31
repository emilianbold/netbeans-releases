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

import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;

import javax.swing.text.*;

import org.netbeans.modules.collab.core.Debug;


/**
 * Support for line regions
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabLineRegion extends CollabRegionSupport implements CollabRegion {
    private Annotation lineAnnotation = null;

    /**
     *
     * @param regionName
     * @param regionBegin
     * @param regionEnd
     * @throws CollabException
     */
    public CollabLineRegion(StyledDocument document, String regionName, int regionBegin, int regionEnd)
    throws CollabException {
        super(document, regionName, regionBegin, regionEnd, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                        

    /**
     * getter for region content
     *
     * @throws CollabException
     * @return region content
     */
    public String getContent() throws CollabException {
        return super.getContent();
    }

    /**
     * getLineIndex
     *
     * @return lineIndex
     */
    public int getLineIndex() {
        return getDocument().getDefaultRootElement().getElementIndex(getBeginOffset());
    }

    /**
     * addAnnotation
     *
     * @param dataObject
     * @param annotation
     */
    public void addAnnotation(
        DataObject dataObject, CollabFileHandler fileHandler, int style, String annotationMessage
    ) throws CollabException {
        Debug.log(this, "CollabRegionSupport, adding Annotation for region: " + //NoI18n
            regionName
        );

        LineCookie cookie = (LineCookie) dataObject.getCookie(LineCookie.class);
        Line.Set lineSet = cookie.getLineSet();
        Line currLine = lineSet.getCurrent(getLineIndex());
        removeAnnotation();
        lineAnnotation = ((CollabFileHandlerSupport) fileHandler).createRegionAnnotation(style, annotationMessage);
        lineAnnotation.attach(currLine);
    }

    /**
     * removeAnnotation
     *
     */
    public void removeAnnotation() {
        if (lineAnnotation != null) {
            lineAnnotation.detach();
        }
    }
}
