package org.netbeans.modules.cnd.callgraph.cndimpl;

import java.awt.Image;
import org.netbeans.modules.cnd.api.model.CsmClass;
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

    private CsmFunction function;
    private String htmlDisplayName = ""; // NOI18N

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
        if (CsmKindUtilities.isFunctionDeclaration(function)) {
            CsmFunction f = function.getDefinition();
            if (f != null) {
                return f;
            }
        }
        return function;
    }

    public String getName() {
        return function.getName().toString();
    }

    public String getHtmlDisplayName() {
        if (htmlDisplayName.length() == 0) {
            htmlDisplayName = createHtmlDisplayName();
        }
        return htmlDisplayName;
    }

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
                    //NOI18N
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

    public String getDescription() {
        return function.getSignature().toString();
    }

    public Image getIcon() {
        try {
            return CsmImageLoader.getImage(getDefinition());
        } catch (AssertionError ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void open() {
        CsmUtilities.openSource(getDefinition());
    }

    @Override
    public boolean equals(Object obj) {
        CsmFunction f = getDeclaration();
        if (f != null) {
            if (obj instanceof FunctionImpl) {
                return f.equals(((FunctionImpl) obj).getDeclaration());
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        CsmFunction f = getDeclaration();
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
