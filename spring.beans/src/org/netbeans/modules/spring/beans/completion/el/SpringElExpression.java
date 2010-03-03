package org.netbeans.modules.spring.beans.completion.el;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.netbeans.modules.web.core.syntax.completion.api.ElCompletionItem;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author alex
 */
public class SpringElExpression extends ELExpression {

    public static final int SPRING_BEAN = 201;

    public SpringElExpression(Document doc) {
        super(doc);
    }
    public List<CompletionItem> getMethodCompletionItems(String beanType, int anchor)
    {
        JSFCompletionItemsTask task = new JSFCompletionItemsTask(beanType, anchor);
        runTask(task);
        return task.getCompletionItems();
    }

    @Override
    protected int findContext(String expr) {
        int dotIndex = expr.indexOf('.');
        int bracketIndex = expr.indexOf('[');

        if (dotIndex > -1 || bracketIndex > -1) {
            final String first = expr.substring(0, getPositiveMin(dotIndex, bracketIndex));

            final boolean[] found = {false};
            if (findBean(first)!=null) {
                return SPRING_BEAN;
            }
        } else if (dotIndex == -1 && bracketIndex == -1) {
            return  EL_START;
        }

        //unknown context
        return EL_UNKNOWN;
    }

    public boolean checkMethod( ExecutableElement method ,
            CompilationController controller )
    {
        TypeMirror returnType = method.getReturnType();
        if ( returnType.getKind() == TypeKind.VOID &&
                method.getSimpleName().toString().startsWith("set")
                && method.getParameters().size() == 1)    // NOI18N
        {
            VariableElement param = method.getParameters().get(0);
            // probably method is setter for some property...
            String propertyName = method.getSimpleName().toString().
                substring(3);
            String getterName = "get"+propertyName;
            for ( ExecutableElement exec : ElementFilter.methodsIn(
                    method.getEnclosingElement().getEnclosedElements()))
            {
                if ( exec.getSimpleName().contentEquals(getterName) &&
                        exec.getParameters().size() == 0 )
                {
                    TypeMirror execReturnType = exec.getReturnType();
                    if ( controller.getTypes().
                            isSameType(param.asType(), execReturnType))
                    {
                        /*
                         *  Found getter which correspond
                         *  <code>method</code> as setter. So this method
                         *  should not be available in completion list .
                         *  Pair setter/getter is represented just property name.
                         */
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private SpringBean findBean(final String nameOrId) {
        final SpringBean[] bean = new SpringBean[1];
        SpringScope scope = SpringScope.getSpringScope(getFileObject());
        for (SpringConfigModel model : scope.getAllConfigModels()) {
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    @Override
                    public void run(SpringBeans beans) {
                        bean[0] = beans.findBean(nameOrId);
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (bean[0] != null) {
                break;
            }
        }
        return bean[0];
    }

    /** returns a type name of the resolved expression.
     * So for example Company.product.name will cause
     * name property class type being returned.
     */
    @Override
    public String getObjectClass() {
        return getTypeName(getResolvedExpression());
    }

    private String getTypeName(final String varType) {
        Part[] parts = getParts(varType);
        String name = null;
        if (parts != null && parts.length > 0) {
            name = parts[0].getPart();
        } else {
            return null;
        }

        if (name == null) {
            return null;
        }

        SpringBean bean = findBean(name);
        if (bean != null) {
            name = bean.getClassName();
        }

        final String[] result = new String[1];
        InspectPropertiesTask inspectPropertiesTask = new InspectPropertiesTask(name) {

            public void run(CompilationController controller) throws Exception {
                TypeMirror type = getTypePreceedingCaret(controller, varType);
                if (type == null) {
                    //unresolvable type
                    return;
                }
                if (type.getKind() == TypeKind.DECLARED) {
                    Element el = ((DeclaredType) type).asElement();
                    if (el instanceof TypeElement) {
                        result[0] = ((TypeElement) el).getQualifiedName().toString();
                    }
                }

                if (type.getKind() == TypeKind.ARRAY) {
                    TypeMirror typeMirror = ((ArrayType) type).getComponentType();
                    if (typeMirror.getKind() == TypeKind.DECLARED) {
                        result[0] = ((TypeElement) controller.getTypes().asElement(
                                typeMirror)).getQualifiedName().toString();
                    }
                }
            }
        };
        inspectPropertiesTask.execute();
        return result[0] != null ? result[0] : name;
    }


    public class JSFCompletionItemsTask extends ELExpression.BaseELTaskClass
        implements CancellableTask<CompilationController>
    {

        private List<CompletionItem> completionItems = new ArrayList<CompletionItem>();
        private int anchor;

        JSFCompletionItemsTask(String beanType, int anchor){
            super(beanType);
            this.anchor = anchor;
        }

        @Override
        public void cancel() {}

        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

            TypeElement bean = getTypePreceedingCaret(controller);

            if (bean != null){
                String prefix = getPropertyBeingTypedName();
                // Fix for IZ#173117 - multiplied EL completion items
                Set<String> addedItems = new HashSet<String>();

                for (ExecutableElement method : ElementFilter.methodsIn(
                        controller.getElements().getAllMembers(bean)))
                {
                    /* EL 2.1 for JSF allows to call any method , not just action listener
                     * if (isActionListenerMethod(method)) {
                      */
                    // skip bean property accessors
                    if ( method.getSimpleName().toString().equals(
                            getExpressionSuffix(method, controller)) )
                    {
                        String methodName = method.getSimpleName().toString();
                            if (methodName != null && methodName.startsWith(prefix)){
                                // Fix for IZ#173117 - multiplied EL completion items
                                if ( addedItems.contains( methodName)){
                                    continue;
                                }
                                addedItems.add(methodName);
                                TypeMirror methodType = controller.getTypes().asMemberOf(
                                        (DeclaredType)bean.asType(), method);
                                String retType = ((ExecutableType)methodType).
                                    getReturnType().toString();
                                CompletionItem item = new JsfMethod(
                                    methodName, anchor, retType);

                            completionItems.add(item);
                        }
                    }
                }
            }
        }


        public List<CompletionItem> getCompletionItems(){
            return completionItems;
        }

        protected boolean isActionListenerMethod(ExecutableElement method){
            boolean isALMethod = false;

            if (method.getModifiers().contains(Modifier.PUBLIC)
                    && method.getParameters().size() == 1) {
                TypeMirror type = method.getParameters().get(0).asType();
                if ("javax.faces.event.ActionEvent".equals(type.toString()) //NOI18N
                        && TypeKind.VOID == method.getReturnType().getKind()) {
                    isALMethod = true;
                }
            }

            return isALMethod;
        }

        @Override
        protected boolean checkMethodParameters( ExecutableElement method ,
                CompilationController controller)
        {
            return true;
        }

        @Override
        protected boolean checkMethod( ExecutableElement method ,
                CompilationController compilationController)
        {
            // Fix for IZ#173117 -  multiplied EL completion items
            if ( super.checkMethodParameters(method, compilationController)&&
                    super.checkMethod(method, compilationController))
            {
                return false;
            }
            return SpringElExpression.this.checkMethod(method, compilationController);
        }
    }

    public static class JsfMethod extends ElCompletionItem.ELBean {

        private static final String METHOD_PATH = "org/netbeans/modules/web/jsf/editor/resources/method_16.png";      //NOI18N

        public JsfMethod(String text, int substitutionOffset, String type) {
            super(text, substitutionOffset, type);
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(METHOD_PATH, false);
        }
    }

}
