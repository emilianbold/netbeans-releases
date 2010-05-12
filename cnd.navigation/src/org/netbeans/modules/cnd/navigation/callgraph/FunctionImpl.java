/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.navigation.callgraph;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.NbBundle;

public class FunctionImpl implements Function {

    private static final Map<CsmDeclaration.Kind, CsmDeclaration.Kind> preferredIcons = new HashMap<CsmDeclaration.Kind, CsmDeclaration.Kind>();

    static {
        preferredIcons.put(CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION);
        preferredIcons.put(CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_FRIEND);
        preferredIcons.put(CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);
    }

    private final CsmFunction function;
    private String htmlDisplayName = ""; // NOI18N
    private String scopeName = null; // NOI18N
    private CsmFunction cachedFunctionDefinition;

    public FunctionImpl(CsmFunction function) {
        super();
        this.function = function;
    }

    public CsmFunction getDeclaration() {
        if (CsmKindUtilities.isFunctionDefinition(function)) {
            CsmFunction f = ((CsmFunctionDefinition) function).getDeclaration();
            if (f != null) {
                return f;
            }
        }
        return function;
    }

    public CsmFunction getDefinition() {
        if (cachedFunctionDefinition == null) {
            if (CsmKindUtilities.isFunctionDeclaration(function)) {
                CsmFunction f = function.getDefinition();
                if (f != null) {
                    cachedFunctionDefinition = f;
                    return f;
                }
            }
            cachedFunctionDefinition = function;
            return function;
        }
        return cachedFunctionDefinition;
    }

    @Override
    public String getName() {
        return function.getName().toString();
    }

    @Override
    public String getScopeName() {
        if (scopeName == null) {
            scopeName = "";
            try {
                CsmFunction f = getDeclaration();
                if (CsmKindUtilities.isClassMember(f)) {
                    CsmClass cls = ((CsmMember) f).getContainingClass();
                    if (cls != null && cls.getName().length() > 0) {
                        scopeName = cls.getName().toString()+"::"; // NOI18N
                    }
                }
            } catch (AssertionError ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return scopeName;
    }

    @Override
    public String getHtmlDisplayName() {
        if (htmlDisplayName.length() == 0) {
            htmlDisplayName = createHtmlDisplayName();
        }
        return htmlDisplayName;
    }

    @Override
    public boolean isVurtual() {
        try {
            CsmFunction f = getDeclaration();
            if (CsmKindUtilities.isClassMember(f)) {
                CsmClass cls = ((CsmMember) f).getContainingClass();
                if (cls != null && cls.getName().length() > 0) {
                    return CsmKindUtilities.isMethod(f) && CsmVirtualInfoQuery.getDefault().isVirtual((CsmMethod)f);
                }
            }
        } catch (AssertionError ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String createHtmlDisplayName() {
        String displayName = function.getName().toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
        if (scopeName == null) {
            scopeName = "";
        }
        try {
            CsmFunction f = getDeclaration();
            if (CsmKindUtilities.isClassMember(f)) {
                CsmClass cls = ((CsmMember) f).getContainingClass();
                if (cls != null && cls.getName().length() > 0) {
                    String name = cls.getName().toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
                    String in = NbBundle.getMessage(CallImpl.class, "LBL_inClass"); // NOI18N
                    if (isVurtual()){
                        displayName ="<i>"+displayName+"</i>"; // NOI18N
                    }
                    scopeName = cls.getName().toString()+"::"; // NOI18N
                    return displayName + "<font color=\'!controlShadow\'>  " + in + " " + name; // NOI18N
                }
            }
        } catch (AssertionError ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return displayName;
    }

    @Override
    public String getDescription() {
        return scopeName+function.getSignature().toString();
    }

    @Override
    public Image getIcon() {
        try {
            return CsmImageLoader.getImage(getDefinition(), preferredIcons);
        } catch (AssertionError ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void open() {
        CsmUtilities.openSource(getDefinition());
    }

    @Override
    public boolean equals(Object obj) {
        CsmFunction f = getDefinition();
        if (f != null) {
            if (obj instanceof FunctionImpl) {
                return f.equals(((FunctionImpl) obj).getDefinition());
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        CsmFunction f = getDefinition();
        if (f != null) {
            return f.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
