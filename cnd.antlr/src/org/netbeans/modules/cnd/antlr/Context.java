/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
