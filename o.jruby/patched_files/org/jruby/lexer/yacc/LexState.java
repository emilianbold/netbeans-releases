/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2001-2002 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.lexer.yacc;

/**
 *
 * @author  jpetersen
 */
public final class LexState {
    public static final LexState EXPR_BEG    = new LexState("EXPR_BEG");
    public static final LexState EXPR_END    = new LexState("EXPR_END");
    public static final LexState EXPR_ARG    = new LexState("EXPR_ARG");
    public static final LexState EXPR_CMDARG = new LexState("EXPR_CMDARG");
    public static final LexState EXPR_ENDARG = new LexState("EXPR_ENDARG");
    public static final LexState EXPR_MID    = new LexState("EXPR_MID");
    public static final LexState EXPR_FNAME  = new LexState("EXPR_FNAME");
    public static final LexState EXPR_DOT    = new LexState("EXPR_DOT");
    public static final LexState EXPR_CLASS  = new LexState("EXPR_CLASS");

    private final String debug;

    // BEGIN NETBEANS MODIFICATIONS
    private int ordinal;
    static {
        EXPR_BEG.ordinal = 0;
        EXPR_END.ordinal = 1;
        EXPR_ARG.ordinal = 2;
        EXPR_CMDARG.ordinal = 3;
        EXPR_ENDARG.ordinal = 4;
        EXPR_MID.ordinal = 5;
        EXPR_FNAME.ordinal = 6;
        EXPR_DOT.ordinal = 7;
        EXPR_CLASS.ordinal = 8;
    }
    
    public int getOrdinal() {
        return ordinal;
    }
    
    public static LexState fromOrdinal(int ordinal) {
        switch (ordinal) {
            case 0: return EXPR_BEG;
            case 1: return EXPR_END;
            case 2: return EXPR_ARG;
            case 3: return EXPR_CMDARG;
            case 4: return EXPR_ENDARG;
            case 5: return EXPR_MID;
            case 6: return EXPR_FNAME;
            case 7: return EXPR_DOT;
            case 8: return EXPR_CLASS;
        }
        return null;
    }
    // END NETBEANS MODIFICATIONS
    
    private LexState(String debug) {
        this.debug = debug;
    }

    public boolean isExprBeg() {
        return this == EXPR_BEG;
    }

    public boolean isExprEnd() {
        return this == EXPR_END;
    }

    public boolean isExprArg() {
        return this == EXPR_ARG;
    }

    public boolean isExprCmdArg() {
        return this == EXPR_CMDARG;
    }

    public boolean isExprEndArg() {
        return this == EXPR_ENDARG;
    }

    public boolean isExprMid() {
        return this == EXPR_MID;
    }

    public boolean isExprFName() {
        return this == EXPR_FNAME;
    }

    public boolean isExprDot() {
        return this == EXPR_DOT;
    }

    public boolean isExprClass() {
        return this == EXPR_CLASS;
    }
    
    public boolean isArgument () {
        return this == EXPR_ARG || this == EXPR_CMDARG;
    }

    public String toString() {
        return debug;
    }
}
