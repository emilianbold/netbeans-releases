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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package  org.netbeans.modules.cnd.makewizard;

import java.util.EventObject;

public class MakefileWizardEvent extends EventObject {
    /** Identifies one or more changes in the lists contents. */
    static int MAKEFILE_NEW = 0;

    /** * The type of this event; */
    private int type;

    private String makefilePath = null;

    private String buildDirectory = null;

    private String makeCommand = null;

    private String[] targets = null;

    private String[] executables = null;

    /**
     * Constructs a PicklistDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying {@link #CONTENTS_CHANGED}
     */
    public MakefileWizardEvent(
	    Object source,
	    int type,
	    String makefilePath,
	    String buildDirectory,
	    String makeCommand,
	    String[] targets,
	    String[] executables) {
	super(source);
	this.type = type;
	this.makefilePath = makefilePath;
	this.buildDirectory = buildDirectory;
	this.makeCommand = makeCommand;
	this.targets = targets;
	this.executables = executables;
    }

    /**
     * Returns the event type. The possible values are:
     * <ul>
     * <li> {@link #CONTENTS_CHANGED}
     * </ul>
     *
     * @return an int representing the type value
     */
    public int getType() {
	return type;
    }

    public String getMakefilePath() {
	return makefilePath;
    }

    public String getBuildDirectory() {
	return buildDirectory;
    }

    public String getMakeCommand() {
	return makeCommand;
    }

    public String[] getTargets() {
	return targets;
    }

    public String[] getExecutables() {
	return executables;
    }
}
