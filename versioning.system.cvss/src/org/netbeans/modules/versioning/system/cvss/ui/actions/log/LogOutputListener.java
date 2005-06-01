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

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import java.util.List;
import java.util.Map;
import java.io.File;

/**
 * Contract for log and annotate commands output,
 *
 * @author Petr Kuzel
 */
public interface LogOutputListener {

    /**
     *
     * @param localFile file that is annotated
     * @param lines list of AnnotateLine
     */
    void annotationLines(File localFile, List lines);


    /**
     *
     * @param messages revision -> message
     */
    void commitMessages(Map messages);
}
