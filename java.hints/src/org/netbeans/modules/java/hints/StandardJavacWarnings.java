/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author phrebejk
 */
public class StandardJavacWarnings extends AbstractHint implements PreferenceChangeListener {
  
    private static StandardJavacWarnings deprecated;
    private static StandardJavacWarnings unchecked;
    private static StandardJavacWarnings fallthrough;
    private static StandardJavacWarnings serialization;
    private static StandardJavacWarnings fajnly;
    private static StandardJavacWarnings unnecessaryCast;
    private static StandardJavacWarnings emptyStatementAfterIf;
    private static StandardJavacWarnings overrides;
    private static StandardJavacWarnings divisionByZero;
    private static StandardJavacWarnings rawTypes;
        
    private String JAVAC_ID = "Javac_"; // NOI18N
    
    private Set<Tree.Kind> treeKinds = EnumSet.noneOf(Tree.Kind.class);
    
    private Kind kind;
    
    private StandardJavacWarnings( Kind kind ) {
        super( kind.defaultOn(), false, HintSeverity.WARNING );
        this.kind = kind;        
        this.getPreferences(null); // Adds listener automatically                                              ;
    }

    public static synchronized StandardJavacWarnings createDeprecated() {
        if ( deprecated == null ) {
            deprecated = new StandardJavacWarnings(Kind.DEPRECATED);
        }
        return deprecated;
    }
    
    public static synchronized StandardJavacWarnings createUnchecked() {
        if ( unchecked == null ) {
            unchecked = new StandardJavacWarnings(Kind.UNCHECKED);
        }
        return unchecked;
    }
    
    public static synchronized StandardJavacWarnings createFallthrough() {
        if ( fallthrough == null ) {
            fallthrough = new StandardJavacWarnings(Kind.FALLTHROUGH);
        }
        return fallthrough;
    }
    
    public static synchronized StandardJavacWarnings createSerialization() {
        if ( serialization == null ) {
            serialization = new StandardJavacWarnings(Kind.SERIALIZATION);
        }
        return serialization;
    }
    
    public static synchronized StandardJavacWarnings createFinally() {
        if ( fajnly == null ) {
            fajnly = new StandardJavacWarnings(Kind.FINALLY);
        }
        return fajnly;
    }
    
    public static synchronized StandardJavacWarnings createUnnecessaryCast() {
        if ( unnecessaryCast == null ) {
            unnecessaryCast = new StandardJavacWarnings(Kind.UNNECESSARY_CAST);
        }
        return unnecessaryCast;
    }
    
    public static synchronized StandardJavacWarnings createEmptyStatementAfterIf() {
        if ( emptyStatementAfterIf == null ) {
            emptyStatementAfterIf = new StandardJavacWarnings(Kind.EMPTY_STATEMENT_AFTER_IF);
        }
        return emptyStatementAfterIf;
    }
    
    public static synchronized StandardJavacWarnings createOverrides() {
        if ( overrides == null ) {
            overrides = new StandardJavacWarnings(Kind.OVERRIDES);
        }
        return overrides;
    }
    
    public static synchronized StandardJavacWarnings createDivisionByZero() {
        if ( divisionByZero == null ) {
            divisionByZero = new StandardJavacWarnings(Kind.DIVISION_BY_ZERO);
        }
        return divisionByZero;
    }
            
    public static synchronized StandardJavacWarnings createRawTypes() {
        if ( rawTypes == null ) {
            rawTypes = new StandardJavacWarnings(Kind.RAWTYPES);
        }
        return rawTypes;
    }

    public Set<Tree.Kind> getTreeKinds() {
        return treeKinds;        
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        // Will never run
        return null;
    }
        
    public void cancel() {
        // Will never run
    }

    public String getId() {
        return JAVAC_ID + kind.toString();
    }
    
    public String getDisplayName() {        
        return NbBundle.getMessage(Imports.class, "LBL_Javac_" + kind.toString()); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(Imports.class, "DSC_Javac_" + kind.toString()); // NOI18N
    }

    @Override
    public final Preferences getPreferences(String profile) {
        Preferences p = super.getPreferences(profile);
        try {
            p.removePreferenceChangeListener(this);
        }
        catch( IllegalArgumentException e ) {
            // Ignore
        }
        p.addPreferenceChangeListener(this);        
        return p;
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        Preferences p = NbPreferences.forModule(JavaSource.class);
        p = p.node("compiler_settings"); // NOI18N
        p.putBoolean(this.kind.key(), this.isEnabled());
    }

    // Private methods ---------------------------------------------------------
    
    private static enum Kind {

        DEPRECATED,
        UNCHECKED,
        FALLTHROUGH,
        SERIALIZATION,
        FINALLY,
        UNNECESSARY_CAST,
        EMPTY_STATEMENT_AFTER_IF,
        OVERRIDES,
        DIVISION_BY_ZERO,
        RAWTYPES;
        
        boolean defaultOn() {        
            return false;
        }
        
        String key() {
            switch( this ) {
                case DEPRECATED:
                    return "enable_lint_deprecation"; // NOI18N
                case UNCHECKED:    
                    return "enable_lint_unchecked"; // NOI18N
                case FALLTHROUGH:
                    return "enable_lint_fallthrough"; // NOI18N
                case SERIALIZATION:
                    return "enable_lint_serial"; // NOI18N
                case FINALLY:
                    return "enable_lint_finally"; // NOI18N
                case UNNECESSARY_CAST:
                    return "enable_lint_cast"; // NOI18N
                case DIVISION_BY_ZERO:
                    return "enable_lint_dvizero"; // NOI18N
                case EMPTY_STATEMENT_AFTER_IF:
                    return "enable_lint_empty"; // NOI18N
                case OVERRIDES:
                    return "enable_lint_overrides"; // NOI18N
                case RAWTYPES:
                    return "enable_lint_rawtypes"; // NOI18N
            }
            return "unknown_kind"; // NOI18N
        }
    }
   
}
