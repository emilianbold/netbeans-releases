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

public class OptionSource {

    private String name;

    private static int nextNum = 0;

    private final int num = nextNum++;

    public static final OptionSource SIMPLE = new OptionSource("simple");	// NOI18N
    public static final OptionSource DEVELOPMENT = new OptionSource("devel");	// NOI18N
    public static final OptionSource FINAL = new OptionSource("final");	// NOI18N

    private OptionSource(String name) {
	this.name = name;
    }

    public String toString() {
	return name;
    }
}
