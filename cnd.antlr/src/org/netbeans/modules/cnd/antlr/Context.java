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

package org.netbeans.modules.cnd.antlr;

/**
 *
 * @author gorrus
 */
public class Context {
    public static final int NO_GUESSING = 0;
    public static final int DIRECT_GUESSING = 1; // rule reference found in synpred
    public static final int CLONE_GUESSING = 2; // guessed rule is being created
    
    public final String breakLabel;
    public final int guessing;
    public int checkedLA = 0;
    public String returnVar = "";
    
    public static final Context EMPTY = new Context("", NO_GUESSING);
    
    /** Creates a new instance of Context */
    public Context(String breakLabel, int guessing) {
        this(breakLabel, guessing, 0, "");
    }
    
    public Context(String breakLabel, int guessing, int checkedLA, String retVar) {
        this.breakLabel = breakLabel;
        this.guessing = guessing;
        this.checkedLA = checkedLA;
        this.returnVar = retVar;
    }
    
    public Context(Context another) {
        this(another.breakLabel, another.guessing, another.checkedLA, another.returnVar);
    }

    public int getCheckedLA() {
        return checkedLA;
    }
    
    public void decreaseLAChecked() {
        checkedLA--;
    }

    public void setCheckedLA(int checkedLA) {
        this.checkedLA = checkedLA;
    }
}
