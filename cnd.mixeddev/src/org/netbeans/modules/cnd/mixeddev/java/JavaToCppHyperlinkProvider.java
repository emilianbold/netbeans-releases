package org.netbeans.modules.cnd.mixeddev.java;

import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmSymbolResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public class JavaToCppHyperlinkProvider extends AbstractJavaToCppHyperlinkProvider {

    public JavaToCppHyperlinkProvider() {
        // Default no-arg constructor
    }

    @Override
    protected String getCppName(Document doc, int offset) {
        String cppName = null;
        for (NativeNameProvider provider : NativeNameProviders.values()) {
            String nativeName = provider.getNativeName(doc, offset);
            if (nativeName != null) {
                cppName = nativeName;
                break;
            }
        }      
        return cppName;
    }

    @Override
    protected boolean navigate(Document doc, int offset) {
        String cppName = getCppName(doc, offset);
        if (cppName != null) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            for (Project prj : projects) {
                NativeProject nativeProject = prj.getLookup().lookup(NativeProject.class);
                if (nativeProject != null) {
                    Collection<CsmOffsetable> candidates = CsmSymbolResolver.resolveSymbol(nativeProject, cppName);
                    if (!candidates.isEmpty()) {
                        CsmOffsetable candidate = candidates.iterator().next();
                        CsmUtilities.openSource(tryGetDefinition(candidate));
                        break;
                    }
                }
            }
            return true; // anyway it has to be java to c++ hyperlink
        }
        return false;
    }
    
    private static interface NativeNameProvider {
        
        String getNativeName(Document doc, int offset);
        
    }
    
    private enum NativeNameProviders implements NativeNameProvider {
        JNI {

            @Override
            public String getNativeName(Document doc, int offset) {
                String cppName = null;
                JavaEntityInfo entity = JNISupport.getJNIMethod(doc, offset);
                if (entity instanceof JavaMethodInfo) {
                    cppName = JNISupport.getCppMethodSignature((JavaMethodInfo) entity);
                }
                return cppName;
            }
            
        },
        
        JNA {
            
            @Override
            public String getNativeName(Document doc, int offset) {
                String cppName = null;
                JavaEntityInfo entity = JNASupport.getJNAEntity(doc, offset);
                if (entity instanceof JavaMethodInfo) {
                    cppName = JNASupport.getCppMethodSignature((JavaMethodInfo) entity);
                }
                return cppName;
            }            
            
        }
    }
    
    private static CsmOffsetable tryGetDefinition(CsmOffsetable candidate) {
        if (CsmKindUtilities.isFunctionDeclaration(candidate)) {
            CsmFunction function = (CsmFunction) candidate;
            CsmFunctionDefinition definition = function.getDefinition();
            return definition != null ? definition : candidate;
        }
        return candidate;
    }    
}
