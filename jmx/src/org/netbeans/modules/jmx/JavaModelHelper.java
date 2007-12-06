/*
 * JavaModelHelper.java
 *
 * Created on November 14, 2006, 11:49 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.jmx;

import com.sun.javadoc.Doc;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
//import com.sun.tools.javac.code.Symbol.TypeSymbol;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import java.text.MessageFormat;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.management.NotCompliantMBeanException;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;

/**
 *
 * @author jfdenise
 */
public class JavaModelHelper {
    
    private static final String METHOD_SIGNATURE_DEF =
            "String[] methodSignature;\n\n"; // NOI18N
    
    private static final String METHOD_SIGNATURE =
            "methodSignature = new String[] {\n"; // NOI18N
    
    // {0} = operation name
    // {1} = operation code
    // {2} = operation name code to check
    private static final String OPERATION_CHECK_PATTERN =
            "if ({2}.equals(\"{0}\") && Arrays.equals(signature, methodSignature)) '{'\n" + // NOI18N
            "    {1}\n" + // NOI18N
            "'}'\n\n"; // NOI18N
    
    public static String INIT_METHOD_NAME = "init";// NOI18N
    public static String GET_MBEANSERVER_METHOD_NAME = "getMBeanServer";// NOI18N
    private static class ValueHolder {
        Object value;
        void setValue(Object value) {
            this.value = value;
        }
        Object getValue() {
            return value;
        }
    }
    
    private abstract static class MemberVisitor extends TreePathScanner<Void, Void> {
        protected CompilationInfo info;
        protected ValueHolder holder;
        public MemberVisitor(CompilationInfo info, ValueHolder holder) {
            this.info = info;
            this.holder = holder;
        }
    }
    
    private static class IsDynamicMBeanVisitor extends MemberVisitor {
        public IsDynamicMBeanVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            if(te.getKind().equals(ElementKind.INTERFACE)) {
                holder.setValue(null);
                return null;
            }
            // Is it a DynamicMBean
            TypeElement dmb = info.getElements().getTypeElement("javax.management.DynamicMBean");// NOI18N
            
            if(dmb == null) {
                holder.setValue(null);
                return null;
            }
            
            boolean isDynamic = info.getTypes().isSubtype(te.asType(), dmb.asType());
            holder.setValue(isDynamic);
            return null;
        }
    }
    
    private static class SearchInterfaceVisitor extends MemberVisitor {
        private String itf;
        public SearchInterfaceVisitor(CompilationInfo info, ValueHolder holder, String itf) {
            super(info, holder);
            this.itf = itf;
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            
            if(te.getKind().equals(ElementKind.INTERFACE)) {
                holder.setValue(null);
                return null;
            }
            
            List<? extends TypeMirror> interfaces = te.getInterfaces();
            TypeElement type = info.getElements().getTypeElement(itf);
            if(type != null &&
                    info.getTypes().isSubtype(te.asType(), type.asType()))
                holder.setValue(type);
            
            return null;
        }
    }
    
    private static class ConstructorsMemberVisitor extends MemberVisitor {
        public ConstructorsMemberVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            String className = te.getSimpleName().toString();
            List<ExecutableElement> constructors =
                    ElementFilter.constructorsIn(te.getEnclosedElements());
            Map<String, ExecutableElement>  mapConstructors = new HashMap<String, ExecutableElement>();
            int i = 0;
            for(ExecutableElement constructor : constructors){
                List<? extends VariableElement> params =
                        constructor.getParameters();
                String construct = className + "("; // NOI18N
                for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
                    // javax.lang.model ELement
                    VariableElement param = it.next();
                    // To type
                    TypeMirror type = param.asType();
                    String tName = null;
                    //if(typeElement == null)
                    //    tName = getPrimitive(type.getKind());
                    //else
                        tName = type.toString();
                    // We should access the quli
                    construct += tName;
                    if (it.hasNext())
                        construct += ", "; // NOI18N
                }
                construct += ")"; // NOI18N
                mapConstructors.put(construct, constructor);
                i++;
            }
            holder.setValue(mapConstructors);
            return null;
        }
    }
    
    private static class SimpleClassNameMemberVisitor extends MemberVisitor {
        public SimpleClassNameMemberVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            holder.setValue(te.getSimpleName().toString());
            return null;
        }
    }
    
    private static class IsInterfaceVisitor extends MemberVisitor {
        public IsInterfaceVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            holder.setValue(el.getKind().equals(ElementKind.INTERFACE));
            return null;
        }
    }
    
    private static class HasClassModifierVisitor extends MemberVisitor {
        Modifier m;
        public HasClassModifierVisitor(CompilationInfo info, ValueHolder holder, Modifier m) {
            super(info, holder);
            this.m = m;
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            holder.setValue(el.getModifiers().contains(m));
            return null;
        }
    }
    
    private static class StandardMBeanItfVisitor extends MemberVisitor {
        private JavaSource mbeanClass;
        public StandardMBeanItfVisitor(CompilationInfo info, ValueHolder holder,
                JavaSource mbeanClass) {
            super(info, holder);
            this.mbeanClass = mbeanClass;
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            
            TypeElement te = (TypeElement) el;
            
            String itfName = te.getQualifiedName().toString() + WizardConstants.MBEAN_ITF_SUFFIX;
            
            TypeElement itfType = info.getElements().getTypeElement(itfName);
            FileObject sourceFile = SourceUtils.getFile(itfType, info.getClasspathInfo());
            JavaSource js = null;
            if(sourceFile != null) {
                //Has sources
                js = mbeanClass.forFileObject(sourceFile);
            }
            
            holder.setValue(js);
            return null;
        }
    }
    
    private static class FullClassNameMemberVisitor extends MemberVisitor {
        private boolean leafFound;
        public FullClassNameMemberVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
           if(leafFound) return null;
           leafFound=true;
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            holder.setValue(te.getQualifiedName().toString());
            return null;
        }
    }
    
    private static class PackageMemberVisitor extends MemberVisitor {
        public PackageMemberVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            // Should be the package because the current class is not an
            // inner class.
            String name =  ((PackageElement)SourceUtils.getOutermostEnclosingTypeElement(te).getEnclosingElement()).getQualifiedName().toString();
            holder.setValue(name);
            return null;
        }
    }
    private static class HasOnlyDefaultConstructorMemberVisitor extends MemberVisitor {
        public HasOnlyDefaultConstructorMemberVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            boolean superClassCheck = false;
            if (te.getSuperclass() != null) {
                superClassCheck = checkDefaultConstruct(te.getSuperclass());
            }
            List<ExecutableElement> constructors =
                    ElementFilter.constructorsIn(te.getEnclosedElements());
            holder.setValue((constructors.size() == 0) && superClassCheck);
            return null;
        }
        private boolean checkDefaultConstruct(TypeMirror clazz) {
            boolean superClassCheck = true;
            TypeElement te =
                    (TypeElement)info.getTypes().asElement(clazz);
            if (te.getSuperclass() != null) {
                superClassCheck = checkDefaultConstruct(te.getSuperclass());
            }
            List<ExecutableElement> constructors =
                    ElementFilter.constructorsIn(te.getEnclosedElements());
            
            boolean defaultExists = false;
            for(ExecutableElement constructor :constructors) {
                boolean isPrivate = false;
                for(Modifier m : constructor.getModifiers()) {
                    if(m.compareTo(Modifier.PRIVATE) == 0)
                        isPrivate = true;
                }
                if ((constructor.getParameters().size() == 0) && !isPrivate) {
                    defaultExists = true;
                    break;
                }
            }
            return superClassCheck && (defaultExists || (constructors.size() == 0));
        }
    }
    
    private static class InterfacesMemberVisitor extends MemberVisitor {
        public InterfacesMemberVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            Set results = new HashSet();
            TypeElement te = (TypeElement) el;
            getInterfaceNames(te, results);
            
            holder.setValue(results.toArray(new String[results.size()]));
            return null;
        }
        
        private void getInterfaceNames(TypeElement te, Set results) {
            if (te.getSuperclass() != null) {
                TypeElement superClass =
                        (TypeElement)info.getTypes().asElement(te.getSuperclass());
                List<? extends TypeMirror> superInterfaces = superClass.getInterfaces();
                for(TypeMirror superInterface : superInterfaces) {
                    TypeElement interfaceElement = (TypeElement)info.getTypes().asElement(superInterface);
                    String intfName = interfaceElement.getQualifiedName().toString();
                    //if (!containsString(results, intfName))
                    results.add(intfName);
                    getInterfaceNames(interfaceElement, results);
                }
            }
            List<? extends TypeMirror> interfaces = te.getInterfaces();
            for (TypeMirror interf : interfaces) {
                TypeElement interfaceElement = (TypeElement)info.getTypes().asElement(interf);
                String intfName = interfaceElement.getQualifiedName().toString();
                //if (!containsString(results, intfName))
                results.add(intfName);
            }
        }
    }
    
    
    private static class MBeanModelVisitor extends MemberVisitor {
        
        private static final String attributeDescription =
                "Attribute exposed for management"; // NOI18N
        private static final String operationDescription =
                "Operation exposed for management"; // NOI18N
        private static final String constructorDescription =
                "Public constructor of the MBean"; // NOI18N
        private static final String mbeanInfoDescription =
                "Information on the management interface of the MBean"; // NOI18N
        private boolean real;
        private TypeElement itfType;
        private CompilationInfo info;
        private boolean introspectedLeaf;
        public MBeanModelVisitor(CompilationInfo info, ValueHolder holder, boolean real, TypeElement itfType) {
            super(info, holder);
            this.real = real;
            this.itfType = itfType;
            this.info = info;
            
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            if(introspectedLeaf) return null;
            introspectedLeaf = true;
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            Set results = new HashSet();
            TypeElement te = (TypeElement) el;
            
            // XXX REVISIT, WHAT ABOUT MXBEAN
           /* String suffix = isMXBean ? 
                WizardConstants.MXBEAN_SUFFIX : WizardConstants.MBEAN_ITF_SUFFIX;
            String itfName = te.getQualifiedName().toString() + suffix;
            
            TypeElement itfType = null;
            */
            // Real means that we are intorpsecting a real Standard MBean
            // !Real means that we are trying to map any Java Class to MBean
            if(!real)
              //  itfType = info.getElements().getTypeElement(itfName);
            //else
                itfType = te;
            
            List<MBeanAttribute> attributes =
                    new ArrayList<MBeanAttribute>();
            List<MBeanOperation> operations =
                    new ArrayList<MBeanOperation>();
            try {
                //First, current interface
                deepIntrospection(te, itfType, attributes, operations);
            }catch(Exception  ex) {
                // XXX REVISIT With LOGGING
                ex.printStackTrace();
            }
            // XXX REVISIT
            // DO WE REALLY NEED TO CHECK FOR DUPLICATION?
            // WAITING FOR TOMAS REPLY ON THAT
            //System.out.println("CREATE result 1");
            holder.setValue(constructResult(attributes,operations, info));
            //System.out.println("CREATE result 2");
            return null;
        }
        
        private boolean isObjectMethod(ExecutableElement method) {
            Element el = method.getEnclosingElement();
            TypeMirror obj = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            if(info.getTypes().isSameType(obj, el.asType())) return true;
            return false;
        }
        
        private void deepIntrospection(TypeElement clazz,
                TypeElement itftype,
                List<MBeanAttribute> attributes,
                List<MBeanOperation> operations) throws NotCompliantMBeanException {
            if(itftype == null) return;
            
            //Top level loop to iterate on all interfaces
            List<? extends TypeMirror> itfs = itftype.getInterfaces();
            for(TypeMirror itf : itfs) {
                TypeElement itfElement =
                       (TypeElement)info.getTypes().asElement(itf);
                deepIntrospection(clazz, itfElement, attributes, operations);
            }
            List<ExecutableElement> methods =
                    ElementFilter.methodsIn(itftype.getEnclosedElements());
            // Now analyze each method.
            for (ExecutableElement method : methods) {
                if(isObjectMethod(method)) continue;
                if(!method.getModifiers().contains(Modifier.PUBLIC)) continue;
                String name = method.getSimpleName().toString();
              //System.out.println("METHOD NAME : " + name);
                List<? extends VariableElement> args = method.getParameters();
                TypeMirror ret = method.getReturnType();
                boolean isVoid = ret.getKind().equals(TypeKind.VOID);
                int argCount = args.size();
                final MBeanAttribute attr;
                
                if (name.startsWith("get") && !name.equals("get") // NOI18N
                        && argCount == 0 && !isVoid) {
                    // if the method is "T getX()" it is a getter
                    attr = new MBeanAttribute(name.substring(3),
                            attributeDescription,
                            method,
                            null, clazz.getTypeParameters(), info);
                } else if (name.startsWith("set") && !name.equals("set") // NOI18N
                        && argCount == 1 && isVoid) {
                    // if the method is "void setX(T x)" it is a setter
                    attr = new MBeanAttribute(name.substring(3),
                            attributeDescription,
                            null,
                            method, clazz.getTypeParameters(), info);
                } else if (name.startsWith("is") && !name.equals("is") // NOI18N
                        && argCount == 0
                        && ret.getKind().equals(TypeKind.BOOLEAN)) {
                    // if the method is "boolean isX()" it is a getter
                    attr = new MBeanAttribute(name.substring(2),
                            attributeDescription,
                            method,
                            null,clazz.getTypeParameters(), info);
                } else {
                    // in all other cases it is an operation
                    attr = null;
                }
                
                if (attr != null) {
                  //System.out.println("We have a anattribute to add " 
                  //          + attr.getName());
                    if (testConsistency(attributes, attr)) {
                   //     System.out.println("Adding " +
                    //        attr.getName());
                        attributes.add(attr);
                    }
                } else {
                    final MBeanOperation oper =
                            new MBeanOperation(method, operationDescription,
                            clazz.getTypeParameters(), info);
                    operations.add(oper);
                }
            }
            
            
        }
        
        /**
         * Checks if the types and the signatures of
         * getters/setters/operations are conform to the MBean design patterns.
         *
         * Error cases:
         * 	-  It exposes a method void Y getXX() AND a method void setXX(Z)
         *     (parameter type mismatch)
         * 	-  It exposes a method void setXX(Y) AND a method void setXX(Z)
         *     (parameter type mismatch)
         *  -  It exposes a  boolean isXX() method AND a YY getXX() or a void setXX(Y).
         * Returns false if the attribute is already in attributes List
         */
            private static boolean testConsistency(List/*<MBeanAttribute>*/attributes,
					   MBeanAttribute attr)
	throws NotCompliantMBeanException {
	for (Iterator it = attributes.iterator(); it.hasNext(); ) {
	    MBeanAttribute mb = (MBeanAttribute) it.next();
	    if (mb.getName().equals(attr.getName())) {
		if ((attr.isReadable() && mb.isReadable())) {
		    final String msg =
			"Conflicting getters for attribute " + mb.getName(); // NOI18N
		    throw new NotCompliantMBeanException(msg);
		}
		if (!mb.getTypeName().equals(attr.getTypeName())) {
		    if (mb.isWritable() && attr.isWritable()) {
			final String msg =
			    "Type mismatch between parameters of set" + // NOI18N
			    mb.getName() + " methods"; // NOI18N
			throw new NotCompliantMBeanException(msg);
		    } else {
			final String msg =
			    "Type mismatch between parameters of get or is" + // NOI18N
			    mb.getName() + ", set" + mb.getName() + " methods"; // NOI18N
			throw new NotCompliantMBeanException(msg);
		    }
		}
		if (attr.isReadable() && mb.isReadable()) {
		    return false;
		}
		if (attr.isWritable() && mb.isWritable()) {
		    return false;
		}
	    }
	}
	return true;
    }
        
        /*
         * We are merging getters and setters in a single Attribute.
         *
         */
        private MBeanDO constructResult(List<MBeanAttribute> attributes,
                List<MBeanOperation> operations, CompilationInfo info) {
            final int len = attributes.size();
            final MBeanAttribute[] attrlist = new MBeanAttribute[len];
            attributes.toArray(attrlist);
            final ArrayList mergedAttributes = new ArrayList();
            //System.out.println("LEN : " + len);
            for (int i=0;i<len;i++) {
                final MBeanAttribute bi = attrlist[i];
                        
                // bi can be null if it has already been eliminated
                // by the loop below at an earlier iteration
                // (cf. attrlist[j]=null;) In this case, just skip it.
                //
                if (bi == null) continue;
                
                // Placeholder for the final attribute info we're going to
                // keep.
                //
                MBeanAttribute att = bi;
                
                // The loop below will try to find whether bi is also present
                // elsewhere further down the list.
                // If it is not, att will be left unchanged.
                // Otherwise, the found attribute info will be merged with
                // att and `removed' from the array by setting them to `null'
                //
                for (int j=i+1;j<len;j++) {
                    MBeanAttribute mi = attrlist[j];
                    
                    // mi can be null if it has already been eliminated
                    // by this loop at an earlier iteration.
                    // (cf. attrlist[j]=null;) In this case, just skip it.
                    //
                    if (mi == null) continue;
                    if ((mi.getName().compareTo(bi.getName()) == 0)) {
                        if(mi.getTypeName().equals(bi.getTypeName())) {
                        // mi and bi have the same name, which means that
                        // that the attribute has been inserted twice in
                        // the list, which means that it is a read-write
                        // attribute.
                        // So we're going to replace att with a new
                        // attribute info with read-write mode.
                        // We also set attrlist[j] to null in order to avoid
                        // duplicates (attrlist[j] and attrlist[i] are now
                        // merged into att).
                        //
                        attrlist[j]=null;
                        att = new MBeanAttribute(bi.getName(),
                                bi.getDescription(),
                                bi.getGetter() != null ? bi.getGetter() : mi.getGetter(),
                                bi.getSetter() != null ? bi.getSetter() : mi.getSetter(),
                                bi.getClassParameterTypes(), info);
                        // I think we could break, but it is probably
                        // safer not to...
                        //
                        // break;
                        }
                    }
                }
                
                // Now all attributes info which had the same name than bi
                // have been merged together in att.
                // Simply add att to the merged list.
                //
                mergedAttributes.add(att);
            }
            /*final int opNb = operations.size();
            final MBeanOperation[] oplist = new MBeanOperation[opNb];
            operations.toArray(oplist);
            final ArrayList mergedOperations = new ArrayList();

            for (int i = 0; i < opNb; i++) {
                final MBeanOperation bi = oplist[i];

                // bi can be null if it has already been eliminated
                // by the loop below at an earlier iteration
                // (cf. attrlist[j]=null;) In this case, just skip it.
                //
                if (bi == null) {
                    continue;
                }

                // Placeholder for the final attribute info we're going to
                // keep.
                //
                MBeanOperation op = bi;

                // The loop below will try to find whether bi is also present
                // elsewhere further down the list.
                // If it is not, op will be left unchanged.
                // Otherwise, the found attribute info will be merged with
                // att and `removed' from the array by setting them to `null'
                //
                for (int j = i + 1; j < opNb; j++) {
                    MBeanOperation mi = oplist[j];

                    // mi can be null if it has already been eliminated
                    // by this loop at an earlier iteration.
                    // (cf. attrlist[j]=null;) In this case, just skip it.
                    //
                    if (mi == null) {
                        continue;
                    }
                    if (bi.getName().equals(mi.getName())) {
                        if (isEqual(bi, mi)) {
                            // bi override mi so mi will not be be added to merged operations.

                            oplist[j] = null;

                            op = new MBeanOperation(bi.getMethod(), bi.getDescription(), bi.getClassParameterTypes(), info);
                        }
                    }
                }

                // Now all operations info which had the same name and the same signature
                // or herited signature than have been merged together in op.
                // Simply add op to the merged list.
                //
                mergedOperations.add(op);
            }
             */
            return new MBeanDO(mergedAttributes, operations);
        }
         private boolean isEqual(MBeanOperation bi, MBeanOperation mi) {
            if (bi.getParametersSize() != mi.getParametersSize()) {
                return false;
            }

            for (int i = 0; i < bi.getParametersSize(); i++) {
                TypeMirror biParam = bi.getParameter(i).getTypeMirror();
                TypeMirror miParam = mi.getParameter(i).getTypeMirror();
                if (bi.compareTo(mi) != 0)
                    return false;
            }

            return true;
        }

    }

  private static class MBeanModelFromClassVisitor extends MemberVisitor {
        
        private static final String attributeDescription =
                "Attribute exposed for management"; // NOI18N
        private static final String operationDescription =
                "Operation exposed for management"; // NOI18N
        private static final String constructorDescription =
                "Public constructor of the MBean"; // NOI18N
        private static final String mbeanInfoDescription =
                "Information on the management interface of the MBean"; // NOI18N
        private boolean real;
        private TypeElement itfType;
        private CompilationInfo info;
        private boolean introspectedLeaf;
        public MBeanModelFromClassVisitor(CompilationInfo info, ValueHolder holder, boolean real, TypeElement itfType) {
            super(info, holder);
            this.real = real;
            this.itfType = itfType;
            this.info = info;  
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            if(introspectedLeaf) return null;
            introspectedLeaf = true;
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            Set results = new HashSet();
            TypeElement te = (TypeElement) el;
            
            itfType = te;
            
            List<MBeanAttribute> attributes =
                    new ArrayList<MBeanAttribute>();
            List<MBeanOperation> operations =
                    new ArrayList<MBeanOperation>();
            try {
                //Introspect class
                //System.out.println("ROOT INTROSPECT " + itfType.getQualifiedName());
                deepIntrospection(te, itfType, attributes, operations);
            }catch(Exception  ex) {
                // XXX REVISIT With LOGGING
                ex.printStackTrace();
            }
            // XXX REVISIT
            // DO WE REALLY NEED TO CHECK FOR DUPLICATION?
            // WAITING FOR TOMAS REPLY ON THAT
            //System.out.println("CREATE result 1");
            holder.setValue(constructResult(attributes,operations, info));
            //System.out.println("CREATE result 2");
            return null;
        }
        
        private boolean isObjectMethod(ExecutableElement method) {
            Element el = method.getEnclosingElement();
            TypeMirror obj = info.getElements().getTypeElement("java.lang.Object").asType();// NOI18N
            if(info.getTypes().isSameType(obj, el.asType())) return true;
            return false;
        }
        
        private void deepIntrospection(TypeElement clazz,
                TypeElement itftype,
                List<MBeanAttribute> attributes,
                List<MBeanOperation> operations) throws NotCompliantMBeanException {
            if(itftype == null) return;
            
            //Top level loop to iterate on all Mother classes
            TypeMirror mother = itftype.getSuperclass();
            TypeElement itfElement =
                  (TypeElement)info.getTypes().asElement(mother);
            deepIntrospection(clazz, itfElement, attributes, operations);
           
            
            List<ExecutableElement> methods =
                    ElementFilter.methodsIn(itftype.getEnclosedElements());
            // Now analyze each method.
            //System.out.println("ITERATING METHODS of " + itftype.getQualifiedName().toString());
            for (ExecutableElement method : methods) {
                //System.out.println("ITERATING METHOD " + method.getSimpleName());
                if(isObjectMethod(method)) continue;
                if(!method.getModifiers().contains(Modifier.PUBLIC)) continue;
                String name = method.getSimpleName().toString();
                //System.out.println("METHOD NAME : " + name);
                List<? extends VariableElement> args = method.getParameters();
                TypeMirror ret = method.getReturnType();
                boolean isVoid = ret.getKind().equals(TypeKind.VOID);
                int argCount = args.size();
                final MBeanAttribute attr;
                
                if (name.startsWith("get") && !name.equals("get") // NOI18N
                        && argCount == 0 && !isVoid) {
                    // if the method is "T getX()" it is a getter
                    attr = new MBeanAttribute(name.substring(3),
                            attributeDescription,
                            method,
                            null, clazz.getTypeParameters(), info);
                } else if (name.startsWith("set") && !name.equals("set") // NOI18N
                        && argCount == 1 && isVoid) {
                    // if the method is "void setX(T x)" it is a setter
                    attr = new MBeanAttribute(name.substring(3),
                            attributeDescription,
                            null,
                            method, clazz.getTypeParameters(), info);
                } else if (name.startsWith("is") && !name.equals("is") // NOI18N
                        && argCount == 0
                        && ret.getKind().equals(TypeKind.BOOLEAN)) {
                    // if the method is "boolean isX()" it is a getter
                    attr = new MBeanAttribute(name.substring(2),
                            attributeDescription,
                            method,
                          null, clazz.getTypeParameters(), info);
              } else {
                  // in all other cases it is an operation
                  attr = null;
              }

              if (attr != null) {
                  /* System.out.println("We have a anattribute to add " 
                  + attr.getName());
                  if (testConsistency(attributes, attr)) {
                  System.out.println("Adding " +
                  attr.getName());
                  attributes.add(attr);
                  }*/
                  attributes.add(attr);
              } else {
                  final MBeanOperation oper =
                          new MBeanOperation(method, operationDescription,
                          clazz.getTypeParameters(), info);
                  operations.add(oper);
              }
          }
      }

      /**
       * Checks if the types and the signatures of
       * getters/setters/operations are conform to the MBean design patterns.
       *
       * Error cases:
       * 	-  It exposes a method void Y getXX() AND a method void setXX(Z)
       *     (parameter type mismatch)
       * 	-  It exposes a method void setXX(Y) AND a method void setXX(Z)
       *     (parameter type mismatch)
       *  -  It exposes a  boolean isXX() method AND a YY getXX() or a void setXX(Y).
       * Returns false if the attribute is already in attributes List
       */
      private static boolean testConsistency(List<MBeanAttribute> attributes,
              MBeanAttribute attr)
              throws NotCompliantMBeanException {
          for (Iterator it = attributes.iterator(); it.hasNext();) {
              MBeanAttribute mb = (MBeanAttribute) it.next();
              if (mb.getName().equals(attr.getName())) {
                  //if ((attr.isReadable() && mb.isReadable())) {
                  // final String msg =
                  //         "Conflicting getters for attribute " + mb.getName(); // NOI18N
                  //  throw new NotCompliantMBeanException(msg);
                  return false;
              }
              if (mb.getTypeName().equals(attr.getTypeName())) {
                  if (mb.isWritable() && attr.isWritable()) {
                      // final String msg =
                      //         "Type mismatch between parameters of set" + // NOI18N
                      //         mb.getName() + " methods"; // NOI18N
                      //throw new NotCompliantMBeanException(msg);
                      return false;
                  } else {
                      //final String msg =
                      //        "Type mismatch between parameters of get or is" + // NOI18N
                      //        mb.getName() + ", set" + mb.getName() + " methods"; // NOI18N
                      //throw new NotCompliantMBeanException(msg);
                      return false;
                  }
              }
              if (attr.isReadable() && mb.isReadable()) {
                  return false;
              }
              if (attr.isWritable() && mb.isWritable()) {
                  return false;
              }
          }

          return true;
      }

      /*
       * We are merging getters and setters in a single Attribute.
       *
       */
      private MBeanDO constructResult(List<MBeanAttribute> attributes,
              List<MBeanOperation> operations, CompilationInfo info) {
          final int len = attributes.size();
          final MBeanAttribute[] attrlist = new MBeanAttribute[len];
          attributes.toArray(attrlist);
          final ArrayList<MBeanAttribute> mergedAttributes = new ArrayList<MBeanAttribute>();
          //System.out.println("LEN : " + len);
          for (int i = 0; i < len; i++) {
              final MBeanAttribute bi = attrlist[i];

              // bi can be null if it has already been eliminated
              // by the loop below at an earlier iteration
              // (cf. attrlist[j]=null;) In this case, just skip it.
              //
              if (bi == null) {
                  continue;
              }

              // Placeholder for the final attribute info we're going to
              // keep.
              //
              MBeanAttribute att = bi;

              // The loop below will try to find whether bi is also present
              // elsewhere further down the list.
              // If it is not, att will be left unchanged.
              // Otherwise, the found attribute info will be merged with
              // att and `removed' from the array by setting them to `null'
              //
              for (int j = i + 1; j < len; j++) {
                  MBeanAttribute mi = attrlist[j];

                  // mi can be null if it has already been eliminated
                  // by this loop at an earlier iteration.
                  // (cf. attrlist[j]=null;) In this case, just skip it.
                  //
                  if (mi == null) {
                      continue;
                  }
                  if ((mi.getName().compareTo(bi.getName()) == 0)) {
                      if ((mi.getTypeName().compareTo(bi.getTypeName()) == 0)) {

                          // mi and bi have the same name, which means that
                          // that the attribute has been inserted twice in
                          // the list, which means that it is a read-write
                          // attribute.
                          // So we're going to replace att with a new
                          // attribute info with read-write mode.
                          // We also set attrlist[j] to null in order to avoid
                          // duplicates (attrlist[j] and attrlist[i] are now
                          // merged into att).
                          //
                          attrlist[j] = null;
                          att = new MBeanAttribute(bi.getName(),
                                  bi.getDescription(),
                                  bi.getGetter() != null ? bi.getGetter() : mi.getGetter(),
                                  bi.getSetter() != null ? bi.getSetter() : mi.getSetter(),
                                  bi.getClassParameterTypes(), info);
                      // I think we could break, but it is probably
                      // safer not to...
                      //
                      // break;
                      } else 
                          // mi and bi have the same name, which means that
                          // that the attribute has been inserted twice in
                          // the list, which means that it is two different 
                          // attributes with different type.
                          att = new MBeanAttribute(bi.getName(),
                                  bi.getDescription(),
                                  bi.getGetter(),
                                  bi.getSetter(),
                                  bi.getClassParameterTypes(), info);
                      }
                  }

              // Now all attributes info which had the same name than bi
              // have been merged together in att.
              // Simply add att to the merged list.
              //
              //System.out.println("ADDING MERGED ATTRIBUTE " + att);
              mergedAttributes.add(att);
          }
          
          // Remove Overriden attributes
          int len2 = mergedAttributes.size();
          final MBeanAttribute[] attrlist2 = new MBeanAttribute[len2];
          mergedAttributes.toArray(attrlist2);
          final ArrayList<MBeanAttribute> finalMergedAttributes = new ArrayList<MBeanAttribute>();
          for(int i = 0; i < len2; i++){
              final MBeanAttribute bi = attrlist2[i];
              // bi can be null if it has already been eliminated
              // by the loop below at an earlier iteration
              // (cf. attrlist[j]=null;) In this case, just skip it.
              //
              if (bi == null) {
                  continue;
              }

              // Placeholder for the final attribute info we're going to
              // keep.
              //
              MBeanAttribute att = bi;

              // The loop below will try to find whether bi is also present
              // elsewhere further down the list.
              // If it is not, att will be left unchanged.
              // Otherwise, the found attribute info will be merged with
              // att and `removed' from the array by setting them to `null'
              //
              for (int j = i + 1; j < len2; j++) {
                  MBeanAttribute mi = attrlist2[j];

                  // mi can be null if it has already been eliminated
                  // by this loop at an earlier iteration.
                  // (cf. attrlist[j]=null;) In this case, just skip it.
                  //
                  if (mi == null) {
                      continue;
                  }
                  if ((bi.getName().compareTo(mi.getName()) == 0)) {
                      if(overridedType(bi.getTypeMirror(),mi.getTypeMirror(), info)){
                         att = mi;
                         attrlist2[j] = null;
                      }else
                         if(overridedType(mi.getTypeMirror(),bi.getTypeMirror(), info)){
                            att = bi;
                            attrlist2[j] = null;
                        }
                  }
             }
              //System.out.println("Finally adding : " + att.getName() + " " + 
              //        att.getAccess());
             finalMergedAttributes.add(att);
          }
          
          final int opNb = operations.size();
          final MBeanOperation[] oplist = new MBeanOperation[opNb];
          operations.toArray(oplist);
          final ArrayList mergedOperations = new ArrayList();

          for (int i = 0; i < opNb; i++) {
              final MBeanOperation bi = oplist[i];

              // bi can be null if it has already been eliminated
              // by the loop below at an earlier iteration
              // (cf. attrlist[j]=null;) In this case, just skip it.
              //
              if (bi == null) {
                  continue;
              }

              // Placeholder for the final attribute info we're going to
              // keep.
              //
              MBeanOperation op = bi;

              // The loop below will try to find whether bi is also present
              // elsewhere further down the list.
              // If it is not, op will be left unchanged.
              // Otherwise, the found attribute info will be merged with
              // att and `removed' from the array by setting them to `null'
              //
              for (int j = i + 1; j < opNb; j++) {
                  MBeanOperation mi = oplist[j];

                  // mi can be null if it has already been eliminated
                  // by this loop at an earlier iteration.
                  // (cf. attrlist[j]=null;) In this case, just skip it.
                  //
                  if (mi == null) {
                      continue;
                  }
                  if (bi.getName().equals(mi.getName())) {

                      if (override(bi, mi, info)) {
                          // bi override mi so mi will not be be added to merged operations.

                          oplist[j] = null;

                          op = new MBeanOperation(mi.getMethod(), mi.getDescription(), mi.getClassParameterTypes(), info);
                      } else if (override(mi, bi, info)) {
                          // bi override mi so mi will not be be added to merged operations.

                          oplist[j] = null;

                          op = new MBeanOperation(bi.getMethod(), bi.getDescription(), bi.getClassParameterTypes(), info);
                      } else {
                          // mi and bi have the same name, which means that
                          // that the attribute has been inserted twice in
                          // the list, which means that it is two different 
                          // attributes with different type.
                          op = new MBeanOperation(bi.getMethod(), bi.getDescription(), bi.getClassParameterTypes(), info);
                      }
                  }
              }

              // Now all operations info which had the same name and the same signature
              // or herited signature than have been merged together in op.
              // Simply add op to the merged list.
              //
              mergedOperations.add(op);
          }
          return new MBeanDO(finalMergedAttributes, mergedOperations);
      }
        private boolean override(MBeanOperation bi, MBeanOperation mi, CompilationInfo info) {
            if (bi.getParametersSize() != mi.getParametersSize()) {
                return false;
            }

            for (int i = 0; i < bi.getParametersSize(); i++) {
                TypeMirror biParam = bi.getParameter(i).getTypeMirror();
                TypeMirror miParam = mi.getParameter(i).getTypeMirror();
                if (!overridedType(biParam, miParam, info)) {
                    return false;
                }
            }

            return true;
        }

        private boolean overridedType(TypeMirror bi, TypeMirror mi, 
                CompilationInfo info) {
            //System.out.println("TYPE KIND : " + bi.getKind());
            TypeKind biKind = bi.getKind();
            TypeKind miKind = mi.getKind();
            if(biKind.equals(miKind)) {
                if(biKind.equals(TypeKind.BOOLEAN) || 
                   biKind.equals(TypeKind.BYTE) ||
                   biKind.equals(TypeKind.CHAR) ||
                   biKind.equals(TypeKind.DOUBLE) ||
                   biKind.equals(TypeKind.FLOAT) ||
                   biKind.equals(TypeKind.INT) ||
                   biKind.equals(TypeKind.LONG) ||
                   biKind.equals(TypeKind.SHORT))
                    return true;
                if(biKind.equals(TypeKind.DECLARED)) {
                    //System.out.println("BI " + bi + " MI " + mi);
                   if(info.getTypeUtilities().isCastable(bi, mi)) {
                       //System.out.println("Overriding !!!!!");
                       return true;
                   }
                }
                if(biKind.equals(TypeKind.ARRAY)) {
                    int dim1 = getArrayDimension(bi);
                    int dim2 = getArrayDimension(mi);
                    //System.out.println("ARRAY DIM " + dim1 + " ARRAY DIM "  + dim2);
                    if(dim1 != dim2) return false;
                    bi = getComponentType(bi);
                    mi = getComponentType(mi);
                    TypeElement ex1 = (TypeElement) info.getTypes().asElement(bi);
                    TypeElement ex2 = (TypeElement) info.getTypes().asElement(mi);
                    boolean over = ex1.getQualifiedName().toString().equals(ex2.
                            getQualifiedName().toString());
                    if(over)
                        //System.out.println("Overriding !!!!!");
                    return over;
                }
            }
            return false;
            
        }
    }
    private static class AgentGeneratorSampleCodeTransformer extends TreePathScanner<Void,Object> {
        private static final String NO_SAMPLE_BODY = "{//TODO Add your MBean registration code here}";// NOI18N
        private static final String PROP_AGENT_INIT_METHOD_NAME = "init";// NOI18N
        private final WorkingCopy w;
        private final boolean removeSampleCode;
        
        public AgentGeneratorSampleCodeTransformer(WorkingCopy w, boolean removeSampleCode) {
            this.w  = w;
            this.removeSampleCode = removeSampleCode;
        }
        
        //Called for every method in the java source file
        @Override
        public Void visitMethod(MethodTree tree, Object p) {
            ClassTree owner = (ClassTree) getCurrentPath().getParentPath().getLeaf();
            
            //is this method a main method
            //Get the Element for this method tree  - Trees.getElement(TreePath)
            ExecutableElement currentMethod = (ExecutableElement)w.getTrees().getElement(getCurrentPath());
            if(PROP_AGENT_INIT_METHOD_NAME.contentEquals(currentMethod.getSimpleName().toString())) { // NOI18N
                
                if(!removeSampleCode) {
                    // XXX REVISIT WAITING FOR METHOD REWRITING TO WORK!!!
                }
            }
            //  super.visitMethod(tree, p);
            return null;
        }
    }
    
    private static class UpdateClassJavaDocTransformer extends TreePathScanner<Void,Void> {
        
        private String text;
        private WorkingCopy w;
        public UpdateClassJavaDocTransformer(WorkingCopy w,
                String text) {
            this.w = w;
            this.text = text;
        }
        
        @Override
        public Void visitClass(ClassTree clazz, Void v) {
            Element el = w.getTrees().getElement(getCurrentPath());
            updateDescription(w, el, w.getTreeMaker(), text, clazz);
            return null;
        }
    }
    
    private static class AddAttributesTransformer extends TreePathScanner<Void,Void> {
        
        private MBeanAttribute[] attributes;
        private WorkingCopy w;
        private boolean isClass;
        public AddAttributesTransformer(WorkingCopy w,
                MBeanAttribute[] attributes) {
            this.w = w;
            this.attributes = attributes;
        }
        
        @Override
        public Void visitClass(ClassTree clazz, Void v) {
            TreeMaker make = w.getTreeMaker();
            Element el = w.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            // Needed not to update the method body
            isClass = ! el.getKind().equals(ElementKind.INTERFACE);
            ClassTree updated = clazz;
            for (int i = 0; i < attributes.length; i++) {
                boolean hasGetter = (attributes[i].getGetMethodExits() ||
                        attributes[i].getIsMethodExits()) && !attributes[i].isWrapped();
                boolean hasSetter = (attributes[i].getSetMethodExits() &&
                        !attributes[i].isWrapped());

                if (attributes[i].isReadable()) {
                    if (!isClass || !hasGetter) {
                        MethodTree method = addGetAttrMethod(attributes[i]);
                        updated = make.addClassMember(updated, method);
                    }
                }

                if (attributes[i].isWritable()) {
                    if (!isClass || !hasSetter) {
                        MethodTree method = addSetAttrMethod(attributes[i]);
                        updated = make.addClassMember(updated, method);
                    //w.rewrite(clazz, copy);
                    }
                }
            }   
                    
            if(isClass){
                for (int i = attributes.length - 1; i >= 0; i--) {
                    if (!attributes[i].getIsMethodExits() && !attributes[i].getGetMethodExits() &&
                            !attributes[i].getSetMethodExits() && !attributes[i].isWrapped()) {
                        VariableTree var = addAttrField(attributes[i]);
                        updated = make.insertClassMember(updated, 0, var);
                        //w.rewrite(clazz, clazzCopy);
                    }
                }
            }
            w.rewrite(clazz, updated);
            return null;
        }
        
        private MethodTree addGetAttrMethod(MBeanAttribute attribute) {
            TreeMaker make = w.getTreeMaker();
            String methodBody = "{return " +  // NOI18N
                    WizardHelpers.forceFirstLetterLowerCase(attribute.getName()) +
                    ";}"; // NOI18N
            
            String prefix = "get";// NOI18N
            
            if (attribute.isWrapped()) {
                if (attribute.getIsMethodExits())
                    prefix = "is";// NOI18N
                
                methodBody = "{return theRef." + prefix + attribute.getName() + "();}"; // NOI18N
            } else if (attribute.getSetMethodExits())
                methodBody = "{return " +  // NOI18N
                        WizardHelpers.getDefaultValue(attribute.getTypeName()) +
                        ";}"; // NOI18N
            
            //Exceptions
            StringBuffer doc = new StringBuffer();
            List<String> exceps = attribute.getGetterExceptions();
            List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>();
            for(String excep : exceps) {
                exceptions.add(make.Identifier(excep));
                doc.append("@throws " + excep + " " + // NOI18N
                        excep + "\n"); // NOI18N
            }
           // String t = attribute.
           // if(attribute.isArray()) {
           // }
            ExpressionTree retType = getType(w, attribute.getTypeName(), attribute.getTypeMirror());
            TypeElement retTypeElement = w.getElements().getTypeElement(attribute.getTypeName());
            
            MethodTree newMethod = null;
            String operationName = prefix + attribute.getName();
            if(isClass) {
                newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    operationName, // name
                    retType, // ret type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    exceptions, // throws
                    methodBody,//body
                    null // default value - not applicable here, used by annotations
                    );
            } else {
                 newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    operationName, // name
                    retType, // ret type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    exceptions, // throws
                    (BlockTree)null, //body
                    null // default value - not applicable here, used by annotations
                    );
            }
            JavaModelHelper.addComment(make, newMethod, Style.JAVADOC, "Get " + attribute.getDescription() + "\n" + doc.toString()); // NOI18N
            return newMethod;
        }
        
        private MethodTree addSetAttrMethod(MBeanAttribute attribute) {
            TreeMaker make = w.getTreeMaker();
            String methodBody = "{" + // NOI18N
                    WizardHelpers.forceFirstLetterLowerCase(attribute.getName()) +
                    " = value;}"; // NOI18N
            
            if (attribute.isWrapped()) {
                methodBody = "{theRef.set" + // NOI18N
                        WizardHelpers.capitalizeFirstLetter(attribute.getName()) +
                        "(value);}"; // NOI18N
            } else if (attribute.getGetMethodExits() || attribute.getIsMethodExits())
                methodBody = "{//TODO add your own implementation.}"; // NOI18N
            
            //Exceptions
            StringBuffer doc = new StringBuffer();
            List<String> exceps = attribute.getSetterExceptions();
            List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>();
            for(String excep : exceps) {
                exceptions.add(make.Identifier(excep));
                doc.append("@throws " + excep + " " + // NOI18N
                        excep + "\n"); // NOI18N
            }
            
            //Create a parameter
            ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            ExpressionTree t = getType(w, attribute.getTypeName(), attribute.getTypeMirror());
            VariableTree par1 = make.Variable(parMods, "value", t, null);// NOI18N
            List<VariableTree> parList = new ArrayList<VariableTree>(1);
            parList.add(par1);
         
            // now, start the method creation
            MethodTree newMethod = null;
            String opName = "set" + attribute.getName();// NOI18N
            if(isClass) {
                 newMethod = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                        opName, // name
                        make.PrimitiveType(TypeKind.VOID), // return type
                        Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                        parList, // parameters
                        exceptions, // throws
                        methodBody, //body
                        null // default value - not applicable here, used by annotations
                        );
            } else {
                 newMethod = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                        opName, // name
                        make.PrimitiveType(TypeKind.VOID), // return type
                        Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                        parList, // parameters
                        exceptions, // throws
                        (BlockTree) null, //body
                        null // default value - not applicable here, used by annotations
                        );
            }
            JavaModelHelper.addComment(make, newMethod, Comment.Style.JAVADOC, "Set " + attribute.getDescription() + "\n" + doc.toString()); // NOI18N
            return newMethod;
        }
        
        private VariableTree addAttrField(MBeanAttribute attribute) {
            ExpressionTree t = getType(w, attribute.getTypeName(), attribute.getTypeMirror());
            TreeMaker make = w.getTreeMaker();
            Set<Modifier> modifiers  = new HashSet<Modifier>();
            modifiers.add(Modifier.PRIVATE);
            VariableTree var = make.Variable(make.Modifiers(modifiers),
                    WizardHelpers.forceFirstLetterLowerCase(attribute.getName()), t, null);
            JavaModelHelper.addComment(make, var, Comment.Style.JAVADOC, "Attribute : " + attribute.getName()); // NOI18N
            return var;
        }
    }
    
    private static class AddOperationsTransformer extends TreePathScanner<Void,Void> {
        
        private MBeanOperation[] operations;
        private WorkingCopy w;
        private boolean isClass;
        public AddOperationsTransformer(WorkingCopy w,
                MBeanOperation[] operations) {
            this.w = w;
            this.operations = operations;
        }
        
        @Override
        public Void visitClass(ClassTree clazz, Void v) {
            TreeMaker make = w.getTreeMaker();
            Element el = w.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            // Needed not to update the method body
            isClass = ! el.getKind().equals(ElementKind.INTERFACE);
            ClassTree updated = clazz;
            for(int i = 0; i < operations.length; i++) {
                if(isClass && operations[i].isMethodExists()) continue;
                MethodTree method = addOperation(operations[i]);
                updated = make.addClassMember(updated, method);
            }
            w.rewrite(clazz, updated);
            return null;
        }
        private MethodTree addOperation(MBeanOperation operation) {
            TreeMaker make = w.getTreeMaker();
            
            StringBuffer body = new StringBuffer();
            if (operation.isWrapped()) {
                body.append("{");// NOI18N
                if (!operation.getReturnTypeName().equals(WizardConstants.VOID_NAME))
                    body.append("return "); // NOI18N
                body.append("theRef." + operation.getName() + "("); // NOI18N
                for (int i = 0; i < operation.getParametersSize(); i ++) {
                    MBeanOperationParameter param = operation.getParameter(i);
                    body.append(param.getParamName());
                    if (i < operation.getParametersSize() - 1)
                        body.append(","); // NOI18N
                }
                body.append(");}"); // NOI18N
            } else {
                body.append("{//TODO add your own implementation\n"); // NOI18N
                if  (!operation.getReturnTypeName().equals(WizardConstants.VOID_NAME)) {
                    body.append("return " +  // NOI18N
                            WizardHelpers.getDefaultValue(operation.getReturnTypeName()) +
                            ";"); // NOI18N
                }
                body.append("}");// NOI18N
            }
            
            StringBuffer doc = new StringBuffer();
            doc.append(operation.getDescription() + "\n"); // NOI18N
            
            ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            List<VariableTree> params = new ArrayList<VariableTree>(operation.getParametersSize());
            for (int i = 0; i < operation.getParametersSize(); i ++) {
                MBeanOperationParameter param = operation.getParameter(i);
                ExpressionTree t = getType(w, param.getParamType(), param.getTypeMirror());
                VariableTree par = make.Variable(parMods, param.getParamName(), t, null);
                params.add(par);
                doc.append("@param " + param.getParamName() + " " +  // NOI18N
                        param.getParamDescription() +"\n"); // NOI18N
            }
            
            List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>();
            for (int i = 0; i < operation.getExceptionsSize(); i ++) {
                MBeanOperationException exception = operation.getException(i);
                ExpressionTree t = getType(w, exception.getExceptionClass(), null);
                exceptions.add(t);
                doc.append("@throws " + exception.getExceptionClass() + " " + // NOI18N
                        exception.getExceptionDescription() + "\n"); // NOI18N
            }
            
            if (!operation.getReturnTypeName().equals(WizardConstants.VOID_NAME)) {
                doc.append("@return " + operation.getReturnTypeName() +  "\n"); // NOI18N
            }
            ExpressionTree t = getType(w, operation.getReturnTypeName(), operation.getTypeMirror());
            String opName = operation.getName();
            MethodTree newMethod = null;
            if(isClass) {
                newMethod = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                        opName, // name
                        t, // return type
                        Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                        params, // parameters
                        exceptions, // throws
                        body.toString(), //body
                        null // default value - not applicable here, used by annotations
                        );
            } else {
                newMethod = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                        opName, // name
                        t, // return type
                        Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                        params, // parameters
                        exceptions, // throws
                        (BlockTree)null, //body
                        null // default value - not applicable here, used by annotations
                        );
            }
            JavaModelHelper.addComment(make, newMethod, Comment.Style.JAVADOC,doc.toString());
            return newMethod;
        }
    }
    
    private static class AttributeImplementationVisitor extends MemberVisitor {
        
        private MBeanAttribute attribute;
        private boolean leafDone;
        public AttributeImplementationVisitor(CompilationInfo info, ValueHolder holder,
                MBeanAttribute attribute) {
            super(info, holder);
            this.attribute = attribute;
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            if(leafDone) return null;
            leafDone = true;
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            //System.out.println("SERACHING 1");
                    
            MBeanAttribute found = searchAttributeImplementation(te, info);
            //System.out.println("SERACHING 2");
            holder.setValue(found);
            return null;
        }
        
        private MBeanAttribute searchAttributeImplementation(TypeElement clazz, CompilationInfo info) {
            if(clazz == null) return null;
            TypeMirror sup = clazz.getSuperclass();
            MBeanAttribute found = null;
            if(sup != null) {
                TypeElement supElement = (TypeElement) info.getTypes().asElement(sup);
                searchAttributeImplementation(supElement, info);
            }
            //System.out.println("SERACHING FOR " + clazz.getSimpleName());
            List<ExecutableElement> methods =
                    ElementFilter.methodsIn(clazz.getEnclosedElements());
            for(ExecutableElement method : methods) {
                //System.out.println("METHOD " + method.getSimpleName());
                if(method.getSimpleName().toString().equals("get" + attribute.getName())) {// NOI18N
                    attribute.setGetMethodExits(method.getParameters().isEmpty());// &&
                           // method.getModifiers().contains(Modifier.PUBLIC));
                    // Update with potential Exceptions
                    if(attribute.getGetMethodExits()) {
                        updateAttributeWithExceptions(method, attribute, info);
                    }
                }
                if(method.getSimpleName().toString().equals("is" + attribute.getName())) {// NOI18N
                    attribute.setIsMethodExits(method.getParameters().isEmpty());// &&
                           //method.getModifiers().contains(Modifier.PUBLIC));
                    // Update with potential Exceptions
                    if(attribute.getIsMethodExits()) {
                        updateAttributeWithExceptions(method, attribute, info);
                    }
                }
                if(method.getSimpleName().toString().equals("set" + attribute.getName())) {// NOI18N
                    boolean sameType = false;
                    
                    if(method.getParameters().size() == 1) {
                        List<? extends VariableElement> params = method.getParameters();
                        VariableElement p = params.get(0);
                       
                        //System.out.println("set" + attribute.getName());
                        //System.out.println("What is the impl type " + p.asType());
                        //System.out.println("What is the itf type " + attribute.getTypeMirror());
                        // XXX equals on MirrorType seems not to work in some cases...
                        //TypeMirror tm = attribute.getTypeMirror();
                        //if(tm != null)
                         //   sameType = p.asType().equals(tm);
                        //else {
                            String tn = getTypeName(p.asType(), null, null, info);
                            sameType = tn.contains(attribute.getTypeName());
                        //} 
                    }
                    attribute.setSetMethodExits(sameType);// &&
                           // method.getModifiers().contains(Modifier.PUBLIC));
                    // Update with potential Exceptions
                    if(attribute.getSetMethodExits()) {
                        updateAttributeWithExceptions(method, attribute, info);
                    }
                }
            }
            
            if(attribute.getIsMethodExits() ||
                    attribute.getGetMethodExits() ||
                    attribute.getSetMethodExits())
                return attribute;
            else
                return null;
        }
        
        private static void updateAttributeWithExceptions(ExecutableElement method,
                MBeanAttribute attribute,
                CompilationInfo info) {
            List<? extends TypeMirror> exceptions = method.getThrownTypes();
            List<String> attrExceptions = attribute.getGetterExceptions();
            for(TypeMirror exception : exceptions) {
                TypeElement elem = (TypeElement) info.getTypes().asElement(exception);
                attrExceptions.add(elem.getQualifiedName().toString());
            }
        }
    }
    
    private static class OperationImplementationVisitor extends MemberVisitor {
        
        private MBeanOperation operation;
        private boolean leafVisited;
        public OperationImplementationVisitor(CompilationInfo info, ValueHolder holder,
                MBeanOperation operation) {
            super(info, holder);
            this.operation = operation;
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            if(leafVisited) return null;
            leafVisited = true;
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
           // System.out.println("Search Operatione 1");
            MBeanOperation found = searchOperationImplementation(te, info);
                    //    System.out.println("Search Operatione 2");
                        
            holder.setValue(found);
            return null;
        }
        
        private MBeanOperation searchOperationImplementation(TypeElement clazz, CompilationInfo info) {
            if(clazz == null)
                return null;
            TypeMirror sup = clazz.getSuperclass();
            MBeanOperation found = null;
            if(sup != null) {
                TypeElement supElement = (TypeElement) info.getTypes().asElement(sup);
                found = searchOperationImplementation(supElement, info);
            }
            if(found != null) return found;
            List<ExecutableElement> methods =
                    ElementFilter.methodsIn(clazz.getEnclosedElements());
            //System.out.println("CLAZZ " + clazz.getSimpleName());
            for(ExecutableElement method : methods) {
               // System.out.println("OPERATION " + method.getSimpleName());
                if(method.getSimpleName().toString().equals(operation.getName())) {
                 //   System.out.println("SAME OPERATION " + method.getSimpleName());
                    List<? extends VariableElement> params = method.getParameters();
                    if(params.size() == operation.getParametersList().size()) {
                     //   System.out.println("SAME SIZE " + params.size());
                        List<MBeanOperationParameter> ps = operation.getParametersList();
                        int i = 0;
                        boolean sameParams = true;
                        for(VariableElement param : params) {
                            TypeMirror type = param.asType();
                            MBeanOperationParameter p = ps.get(i);
                            //TypeElement te = info.getElements().getTypeElement(WizardHelpers.getFullTypeName(p.getParamType()));
                            //System.out.println("IS SAME" + te.asType() + " " + type );
                            //System.out.println("IS SAME" + te.asType().equals(type) );
                            //String tn = getTypeName(te.asType(), null, null, info);
                            //System.out.println("IS SAME" + p.getParamType() + " " + type );
                            boolean sameType = type.toString().contains(p.getParamType());
                            sameParams = sameParams && sameType;
                            i++;
                        }
                        if(sameParams){// && method.getModifiers().contains(Modifier.PUBLIC)) {
                            operation.setMethodExists(true);
                            //updateOperationWithExceptions(method, operation, info);
                            return operation;
                        }
                    }
                }
            }
            return null;
        }
        
       
    }
    
    private static class IsGeneratedAgentAgentVisitor extends MemberVisitor {
        public IsGeneratedAgentAgentVisitor(CompilationInfo info, ValueHolder holder) {
            super(info, holder);
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                throw new RuntimeException("Invalid Class");// NOI18N
            }
            TypeElement te = (TypeElement) el;
            // How do I test the TypeElement to be an Interface????
            if(te.getClass().isInterface()) {
                holder.setValue(Boolean.FALSE);
            } else {
                boolean hasInit = false;
                boolean hasGetMBS = false;
                List<ExecutableElement> methods =
                        ElementFilter.methodsIn(te.getEnclosedElements());
                for(ExecutableElement method : methods){
                    if(method.getSimpleName().toString().equals(INIT_METHOD_NAME))
                        hasInit = true;
                    if(method.getSimpleName().toString().equals(GET_MBEANSERVER_METHOD_NAME)){
                        hasGetMBS = method.getParameters().isEmpty();
                    }
                }
                holder.setValue((hasInit && hasGetMBS));
            }
            return null;
        }
    }
    
    private static class MBeanNotificationTransformer extends TreePathScanner<Void,Void> {
        // MBeanNotificationInfo instantiation pattern
        // {0} = notification type
        // {1} = notification class
        // {2} = notification description
        private static final String MBEAN_NOTIF_INFO_PATTERN =
                "      new MBeanNotificationInfo(new String[] '{'\n" + // NOI18N
                "             {0}'}',\n" + // NOI18N
                "             {1}.class.getName(),\n" + // NOI18N
                "             \"{2}\")"; // NOI18N
        
        private static final String BODY_COMMENT =
                "{//TODO add your code here}"; // NOI18N
        
        private final WorkingCopy w;
        private final MBeanNotification[] notifs;
        private final boolean genBroadcastDeleg;
        private final boolean genSeqNumber;
        public MBeanNotificationTransformer(WorkingCopy w, MBeanNotification[] notifs,
                boolean genBroadcastDeleg, boolean genSeqNumber) {
            this.w = w;
            this.notifs = notifs;
            this.genSeqNumber = genSeqNumber;
            this.genBroadcastDeleg = genBroadcastDeleg;
        }
        
        @Override public Void visitClass(ClassTree clazz, Void v) {
            //Obtain the owner of this method, getCurrentPath() returns a path from root (CompilationUnitTree) to current node (tree)
            // XXX REVISIT WITH API CHANGES
            TreeMaker make = w.getTreeMaker();
            TypeElement emitter = w.getElements().getTypeElement("javax.management.NotificationEmitter");// NOI18N
            ClassTree copy = make.addClassImplementsClause(clazz, make.QualIdent(emitter));
            
            MethodTree m = addAddNotifListMethod(genBroadcastDeleg);
            copy = make.addClassMember(copy, m);
            
            // Remove existing getNotificationInfo
            List<? extends Tree> members = copy.getMembers();
            for(Tree t : members) {
                if(t instanceof MethodTree) {
                    MethodTree mt = (MethodTree) t;
                    String methodName = mt.getName().toString();
                    if("getNotificationInfo".equals(methodName)){//NOI18N
                        TreeMaker treeMaker = w.getTreeMaker();
                        //Create a new class tree without main method
                        copy = treeMaker.removeClassMember(copy, mt);
                    }
                }
            }
            m = addGetNotifInfoMethod(notifs);
            copy = make.addClassMember(copy, m);
            
            m = addRemoveNotifListMethod1Param(genBroadcastDeleg);
            copy = make.addClassMember(copy, m);
            
            m = addRemoveNotifListMethod(genBroadcastDeleg);
            copy = make.addClassMember(copy, m);
            
            if (genSeqNumber) {
                m = addGetSeqNumberMethod();
                copy = make.addClassMember(copy, m);
            }
            
            if(genSeqNumber) {
                VariableTree var = addSeqNumField();
                copy = make.insertClassMember(copy, 0, var);
            }
            if(genBroadcastDeleg) {
                VariableTree var = addBroadcasterField();
                copy = make.insertClassMember(copy, 0, var);
            }
            
            copy = addNotifTypes(notifs, copy);
            w.rewrite(clazz, copy);
            super.visitClass(clazz, v);
            return null;
        }
        
        private MethodTree addAddNotifListMethod(boolean genBroadcastDeleg) {
            TreeMaker make = w.getTreeMaker();
            ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            // make a variable trees - representing parameters
            TypeElement listenerType = w.getElements().getTypeElement("javax.management.NotificationListener");// NOI18N
            TypeElement filterType = w.getElements().getTypeElement("javax.management.NotificationFilter");// NOI18N
            TypeElement handbackType = w.getElements().getTypeElement("java.lang.Object");// NOI18N
            VariableTree par1 = make.Variable(parMods, "listener", make.QualIdent(listenerType), null);// NOI18N
            VariableTree par2 = make.Variable(parMods, "filter", make.QualIdent(filterType), null);// NOI18N
            VariableTree par3 = make.Variable(parMods, "handback", make.QualIdent(handbackType), null);// NOI18N
            List<VariableTree> parList = new ArrayList<VariableTree>(3);
            parList.add(par1);
            parList.add(par2);
            parList.add(par3);
            
            // now, start the method creation
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)),// NOI18N // modifiers and annotations
                    "addNotificationListener",// NOI18N // name
                    make.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    Collections.singletonList(make.Identifier("IllegalArgumentException")), // NOI18N// throws
                    (genBroadcastDeleg ? "{broadcaster.addNotificationListener(listener, filter, handback);}" : BODY_COMMENT), // NOI18N //body
                    null // default value - not applicable here, used by annotations
                    );
            return newMethod;
        }
        
        private MethodTree addGetNotifInfoMethod(MBeanNotification[] notifs) {
            TreeMaker make = w.getTreeMaker();
            // remove getNotificationInfo() if it already exists
            List params = new ArrayList();
            
            // XXX Revisit, Don't understand this the current method shouldn't have any getNotificationInfo method
            //Method getNotifInfo = tgtClass.getMethod("getNotificationInfo", params,false); // NOI18N
            //if (getNotifInfo != null)
            //  getNotifInfo.refDelete();
            
            StringBuffer methodBody = new StringBuffer();
            methodBody.append("{return new MBeanNotificationInfo[] {\n"); // NOI18N
            MessageFormat notifInfo = new MessageFormat(MBEAN_NOTIF_INFO_PATTERN);
            TypeElement te =
                    w.getElements().getTypeElement("javax.management.MBeanNotificationInfo");// NOI18N
            
            Tree retValue = make.ArrayType(make.QualIdent(te));
            int notifTypeIndex = 0;
            for (int i = 0; i < notifs.length; i ++) {
                StringBuffer notifType = new StringBuffer();
                for (int j = 0 ; j < notifs[i].getNotificationTypeCount() ; j++) {
                    if (!notifs[i].getNotificationClass().equals(
                            WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                        notifType.append("NOTIF_TYPE_" + notifTypeIndex); // NOI18N
                        notifTypeIndex++;
                    } else {
                        notifType.append(WizardConstants.ATTRIBUTECHANGE_TYPE);
                    }
                    if (j < notifs[i].getNotificationTypeCount() - 1) {
                        notifType.append(",\n             "); // NOI18N
                    }
                }
                Object[] notifArguments = { notifType.toString(),
                notifs[i].getNotificationClass(),
                notifs[i].getNotificationDescription() };
                methodBody.append(notifInfo.format(notifArguments));
                if ((notifs.length > 1) && (i < (notifs.length - 1))) {
                    methodBody.append(","); // NOI18N
                }
                methodBody.append("\n"); // NOI18N
            }
            methodBody.append("};}"); // NOI18N
            
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "getNotificationInfo", // NOI18N// name
                    retValue, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    methodBody.toString(), //body
                    null // default value - not applicable here, used by annotations
                    );
            return newMethod;
        }
        
        private MethodTree addRemoveNotifListMethod1Param(boolean genBroadcastDeleg) {
            TreeMaker make = w.getTreeMaker();
            
            ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            // make a variable trees - representing parameters
            TypeElement listenerType = w.getElements().getTypeElement("javax.management.NotificationListener");// NOI18N
            VariableTree par1 = make.Variable(parMods, "listener", make.QualIdent(listenerType), null);// NOI18N
            List<VariableTree> parList = new ArrayList<VariableTree>(1);
            parList.add(par1);
            
            // now, start the method creation
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "removeNotificationListener", // NOI18N // name
                    make.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    Collections.singletonList(make.Identifier("ListenerNotFoundException")), // NOI18N // throws
                    (genBroadcastDeleg ? "{broadcaster.removeNotificationListener(listener);}" : BODY_COMMENT), // NOI18N //body
                    null // default value - not applicable here, used by annotations
                    );
            return newMethod;
        }
        
        private MethodTree addRemoveNotifListMethod(boolean genBroadcastDeleg) {
            TreeMaker make = w.getTreeMaker();
            ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            // make a variable trees - representing parameters
            TypeElement listenerType = w.getElements().getTypeElement("javax.management.NotificationListener");// NOI18N
            TypeElement filterType = w.getElements().getTypeElement("javax.management.NotificationFilter");// NOI18N
            TypeElement handbackType = w.getElements().getTypeElement("java.lang.Object");// NOI18N
            VariableTree par1 = make.Variable(parMods, "listener", make.QualIdent(listenerType), null);// NOI18N
            VariableTree par2 = make.Variable(parMods, "filter", make.QualIdent(filterType), null);// NOI18N
            VariableTree par3 = make.Variable(parMods, "handback", make.QualIdent(handbackType), null);// NOI18N
            List<VariableTree> parList = new ArrayList<VariableTree>(3);
            parList.add(par1);
            parList.add(par2);
            parList.add(par3);
            
            // now, start the method creation
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "removeNotificationListener", // NOI18N // name
                    make.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    Collections.singletonList(make.Identifier("ListenerNotFoundException")), // NOI18N // throws
                    (genBroadcastDeleg ? "{broadcaster.removeNotificationListener(listener, filter, handback);}" : BODY_COMMENT), // NOI18N //body
                    null // default value - not applicable here, used by annotations
                    );
            return newMethod;
        }
        
        private MethodTree addGetSeqNumberMethod() {
            TreeMaker make = w.getTreeMaker();
            String methodBody = "{return seqNumber++;}"; // NOI18N
            Set<Modifier> modifiers = new HashSet<Modifier>(2);
            modifiers.add(Modifier.PUBLIC);
            modifiers.add(Modifier.SYNCHRONIZED);
            MethodTree newMethod = make.Method(
                    make.Modifiers(modifiers), // modifiers and annotations
                    "getNextSeqNumber", // NOI18N // name
                    make.PrimitiveType(TypeKind.LONG), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    methodBody, //body
                    null // default value - not applicable here, used by annotations
                    );
            return newMethod;
        }
        private VariableTree addSeqNumField() {
            TreeMaker make = w.getTreeMaker();
            Set<Modifier> modifiers  = new HashSet<Modifier>();
            modifiers.add(Modifier.PRIVATE);
            VariableTree var = make.Variable(make.Modifiers(modifiers), "seqNumber", make.PrimitiveType(TypeKind.LONG), null);// NOI18N
            return var;
        }
        private VariableTree addBroadcasterField() {
            TreeMaker make = w.getTreeMaker();
            TypeElement supportType = w.getElements().getTypeElement("javax.management.NotificationBroadcasterSupport");// NOI18N
            // Write a new XX
            ExpressionTree ex = make.QualIdent(supportType);
            NewClassTree initializer =
                    make.NewClass(null, Collections.<ExpressionTree>emptyList(), ex,  Collections.<ExpressionTree>emptyList(), null);
            Set<Modifier> modifiers  = new HashSet<Modifier>();
            modifiers.add(Modifier.PRIVATE);
            VariableTree var = make.Variable(make.Modifiers(modifiers), "broadcaster", ex,initializer);// NOI18N
            return var;
        }
        
        private ClassTree addNotifTypes(MBeanNotification[] notifs, ClassTree copy) {
            TreeMaker make = w.getTreeMaker();
            String comments =
                    "Notification types definitions. To use when creating JMX Notifications."; // NOI18N
            int notifTypeIndex = 0;
            Set<Modifier> modifiers = new  HashSet<Modifier>();
            modifiers.add(Modifier.PRIVATE);
            modifiers.add(Modifier.STATIC);
            modifiers.add(Modifier.FINAL);
            TypeElement stringElem = w.getElements().getTypeElement("java.lang.String");// NOI18N
            ExpressionTree stringType = make.QualIdent(stringElem);
            for (int i = 0; i < notifs.length; i ++) {
                if (!notifs[i].getNotificationClass().equals(
                        WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                    for (int j = 0; j < notifs[i].getNotificationTypeCount(); j++) {
                        VariableTree var =
                                make.Variable(make.Modifiers(modifiers),
                                "NOTIF_TYPE_" + notifTypeIndex,// NOI18N
                                stringType,
                                make.Literal(notifs[i].getNotificationType(j).getNotificationType())
                                );
                        if(notifTypeIndex == 0) {
                            JavaModelHelper.addComment(make, var, Comment.Style.JAVADOC, comments);
                        }
                        copy = make.addClassMember(copy, var);
                        notifTypeIndex++;
                    }
                }
            }
            return copy;
        }
    }
    
    private static class MBeanRegistrationTransformer extends TreePathScanner<Void,Void> {
        
        private static final String PRE_REGISTER_JAVADOC =
                "Allows the MBean to perform any operations it needs before being\n" + // NOI18N
                "registered in the MBean server. If the name of the MBean is not\n" + // NOI18N
                "specified, the MBean can provide a name for its registration. If\n" + // NOI18N
                "any exception is raised, the MBean will not be registered in the\n" + // NOI18N
                "MBean server.\n" + // NOI18N
                "@param server The MBean server in which the MBean will be registered.\n" + // NOI18N
                "@param name The object name of the MBean. This name is null if the\n" + // NOI18N
                "name parameter to one of the createMBean or registerMBean methods in\n" + // NOI18N
                "the MBeanServer interface is null. In that case, this method must\n" + // NOI18N
                "return a non-null ObjectName for the new MBean.\n" + // NOI18N
                "@return The name under which the MBean is to be registered. This value\n" + // NOI18N
                "must not be null. If the name parameter is not null, it will usually\n" + // NOI18N
                "but not necessarily be the returned value.\n" + // NOI18N
                "@throws Exception This exception will be caught by the MBean server and\n" + // NOI18N
                "re-thrown as an MBeanRegistrationException."; // NOI18N
        
        private static final String POST_REGISTER_JAVADOC =
                "Allows the MBean to perform any operations needed after having\n" + // NOI18N
                "been registered in the MBean server or after the registration has\n" + // NOI18N
                "failed.\n" + // NOI18N
                "@param registrationDone Indicates wether or not the MBean has been\n" + // NOI18N
                "successfully registered in the MBean server. The value false means\n" + // NOI18N
                "that the registration has failed.\n"; // NOI18N
        
        private static final String PRE_DEREGISTER_JAVADOC =
                "Allows the MBean to perform any operations it needs before being\n" + // NOI18N
                "unregistered by the MBean server.\n" + // NOI18N
                "@throws Exception This exception will be caught by the MBean server and\n" + // NOI18N
                "re-thrown as an MBeanRegistrationException."; // NOI18N
        
        private static final String POST_DEREGISTER_JAVADOC =
                "Allows the MBean to perform any operations needed after having been\n" + // NOI18N
                "unregistered in the MBean server.\n"; // NOI18N
        
        private final WorkingCopy w;
        private final boolean keepRefSelected;
        public MBeanRegistrationTransformer(WorkingCopy w, boolean keepRefSelected) {
            this.w = w;
            this.keepRefSelected = keepRefSelected;
        }
        
        @Override public Void visitClass(ClassTree clazz, Void v) {
            TreeMaker make = w.getTreeMaker();
            TypeElement emitter = w.getElements().getTypeElement("javax.management.MBeanRegistration");// NOI18N
            ClassTree copy = make.addClassImplementsClause(clazz, make.QualIdent(emitter));
            
            
            MethodTree m = addPreRegisterMethod();
            copy = make.addClassMember(copy, m);
            
            m = addPostRegisterMethod();
            copy = make.addClassMember(copy, m);
            
            m = addPreDeregisterMethod();
            copy = make.addClassMember(copy, m);
            
            m = addPostDeregisterMethod();
            copy = make.addClassMember(copy, m);
            
            if (keepRefSelected) {
                VariableTree var = addMBeanServerField();
                copy = make.insertClassMember(copy, 0, var);
                var = addObjectNameField();
                copy = make.insertClassMember(copy, 0, var);
            }
            w.rewrite(clazz, copy);
            super.visitClass(clazz, v);
            return null;
        }
        
        private MethodTree addPreRegisterMethod() {
            TreeMaker make = w.getTreeMaker();
            
            String methodBody =
                    "return name;"; // NOI18N
            if (keepRefSelected)
                methodBody = "objectName = name;" + // NOI18N
                        "mbeanServer = server;" +  // NOI18N
                        methodBody;
            
            methodBody = "{" + methodBody +"}";// NOI18N
            
            ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            // make a variable trees - representing parameters
            TypeElement serverType = w.getElements().getTypeElement("javax.management.MBeanServer");// NOI18N
            TypeElement nameType = w.getElements().getTypeElement("javax.management.ObjectName");// NOI18N
            Tree nameTree = make.QualIdent(nameType);
            VariableTree par1 = make.Variable(parMods, "server", make.QualIdent(serverType), null);// NOI18N
            VariableTree par2 = make.Variable(parMods, "name", make.QualIdent(nameType), null);// NOI18N
            
            List<VariableTree> parList = new ArrayList<VariableTree>(2);
            parList.add(par1);
            parList.add(par2);
            
            // now, start the method creation
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "preRegister", // NOI18N
                    nameTree, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    Collections.singletonList(make.Identifier("Exception")), // NOI18N
                    methodBody,//body
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(make, newMethod, Comment.Style.JAVADOC,PRE_REGISTER_JAVADOC);
           
            return newMethod;
        }
        
        private MethodTree addPostRegisterMethod() {
            TreeMaker make = w.getTreeMaker();
            String methodBody =
                    "{//TODO postRegister implementation;}"; // NOI18N
            ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            // make a variable trees - representing parameters
            VariableTree par1 = make.Variable(parMods, "registrationDone", make.Identifier("Boolean"), null); // NOI18N
            
            List<VariableTree> parList = new ArrayList<VariableTree>(1);
            parList.add(par1);
            
            // now, start the method creation
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "postRegister", // NOI18N // name
                    make.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    methodBody,//body
                    null // default value - not applicable here, used by annotations
                    );
            
            JavaModelHelper.addComment(make, newMethod, Comment.Style.JAVADOC, POST_REGISTER_JAVADOC);
            return newMethod;
        }
        
        private MethodTree addPreDeregisterMethod() {
            TreeMaker make = w.getTreeMaker();
            
            String methodBody =
                    "{//TODO preDeregister implementation}"; // NOI18N
            
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "preDeregister", // NOI18N // name
                    make.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    Collections.singletonList(make.Identifier("Exception")), // NOI18N // throws
                    methodBody, //body
                    null // default value - not applicable here, used by annotations
                    );
            
            JavaModelHelper.addComment(make, newMethod, Comment.Style.JAVADOC,PRE_DEREGISTER_JAVADOC);
            return newMethod;
        }
        
        private MethodTree addPostDeregisterMethod() {
            TreeMaker make = w.getTreeMaker();
            
            String methodBody =
                    "{//TODO postDeregister implementation}"; // NOI18N
            
            MethodTree newMethod = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "postDeregister", // NOI18N // name
                    make.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    methodBody, //body
                    null // default value - not applicable here, used by annotations
                    );
            
            JavaModelHelper.addComment(make, newMethod, Comment.Style.JAVADOC, POST_DEREGISTER_JAVADOC);
            return newMethod;
        }
        
        private VariableTree addMBeanServerField() {
            TreeMaker make = w.getTreeMaker();
            Set<Modifier> modifiers  = new HashSet<Modifier>();
            modifiers.add(Modifier.PRIVATE);
            TypeElement serverType = w.getElements().getTypeElement("javax.management.MBeanServer");// NOI18N
            VariableTree var = make.Variable(make.Modifiers(modifiers), "mbeanServer", make.QualIdent(serverType), null);// NOI18N
            return var;
        }
        
        private VariableTree addObjectNameField() {
            TreeMaker make = w.getTreeMaker();
            Set<Modifier> modifiers  = new HashSet<Modifier>();
            modifiers.add(Modifier.PRIVATE);
            TypeElement nameType = w.getElements().getTypeElement("javax.management.ObjectName");// NOI18N
            VariableTree var = make.Variable(make.Modifiers(modifiers), "objectName", make.QualIdent(nameType), null);// NOI18N
            return var;
        }
    }
    
    private static class UpdateDynamicMBeanTemplateTransformer extends TreePathScanner<Void,Void> {
        
        // {0} = attribute name to check
        // {1} = attribute name
        // {2} = code to execute for attribute
        // {3} = return statement
        private static final String CHECK_ATTR_NAME_PATTERN =
                "if ({0}.equals(\"{1}\")) '{'\n\n" + // NOI18N
                "   {2}\n\n{3}" + // NOI18N
                "'}'"; // NOI18N
        
        // {0} = attribute name
        // {1} = return or set
        private static final String COMMENT_ATTR_VALUE_PATTERN =
                "//TODO {1} value of {0} attribute"; // NOI18N
        
        // {0} = attribute name
        private static final String THROW_ATTR_VALUE_PATTERN =
                "throw new MBeanException(\n" + // NOI18N
                "    new IllegalArgumentException(\"{0} is read-only.\"));"; // NOI18N
        
        private static final String COMMENT_INVOKE =
                "\n" + // NOI18N
                "     //TODO add your code here\n\n" + // NOI18N
                "     return "; // NOI18N
        
        // {0} = attribute name
        // {1} = attribute type
        // {2} = attribute description
        // {3} = isReadable
        // {4} = isWritable
        // {5} = isIs
        private static final String MBEAN_ATT_INFO_PATTERN = new String(
                "    new MBeanAttributeInfo(\"{0}\",\n" +// NOI18N
                "                           {1},\n" +// NOI18N
                "                           \"{2}\",\n" +// NOI18N
                "                           {3},\n" +// NOI18N
                "                           {4},\n" +// NOI18N
                "                           {5})");// NOI18N
        
        // {0} = operation name
        // {1} = operation desc
        // {2} = operation type full name
        // {3} = operation index
        private static final String MBEAN_OPERATION_INFO_PATTERN = new String(
                "    new MBeanOperationInfo(\"{0}\",\n" +// NOI18N
                "                           \"{1}\",\n" +// NOI18N
                "                           op{3}Params,\n" +// NOI18N
                "                           {2},\n" +// NOI18N
                "                           MBeanOperationInfo.ACTION)");// NOI18N
        
        // {0} = operation index
        private static final String MBEAN_PARAMS_INFO_PATTERN = new String(
                "       MBeanParameterInfo[] op{0}Params = new MBeanParameterInfo[] '{'\n");// NOI18N
        
        // {0} = param name
        // {1} = param type full name
        // {2} = param desc
        private static final String MBEAN_PARAM_INFO_PATTERN = new String(
                "           new MBeanParameterInfo(\"{0}\",{1},\"{2}\")");// NOI18N
        
        private final WorkingCopy w;
        private final MBeanDO mbean;
        public UpdateDynamicMBeanTemplateTransformer(WorkingCopy w, MBeanDO mbean) {
            this.w = w;
            this.mbean = mbean;
        }
        
        @Override
        public Void visitClass(ClassTree clazz, Void v) {
            TreeMaker maker = w.getTreeMaker();
            Element el = w.getTrees().getElement(getCurrentPath());
            updateDescription(w, el, maker, mbean.getDescription(), clazz);
            
            List attrList = mbean.getAttributes();
            MBeanAttribute[] attributes = (MBeanAttribute[])
                    attrList.toArray(new MBeanAttribute[attrList.size()]);
            
            ClassTree copy = addGetAttribute(maker, mbean, attributes, clazz);
            copy = addSetAttribute(maker, mbean, attributes, copy);
            copy = addInvoke(maker, mbean, copy);
            copy = addBuildMBeanInfo(maker, mbean, attributes, copy);
                 
            w.rewrite(clazz, copy);
            super.visitClass(clazz, v);
            return null;
        }
        
        private ClassTree addGetAttribute(TreeMaker maker, 
                MBeanDO mbean, MBeanAttribute[] attributes, ClassTree clazz) {
            TypeElement teret = w.getElements().getTypeElement("java.lang.Object");// NOI18N
            Tree retVal = maker.QualIdent(teret);
            
            ModifiersTree parMods = maker.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            // make a variable trees - representing parameters
            TypeElement attributeType = w.getElements().getTypeElement("java.lang.String");// NOI18N
            VariableTree par1 = maker.Variable(parMods, "attributeName", maker.QualIdent(attributeType), null);// NOI18N
            List<VariableTree> parList = new ArrayList<VariableTree>(1);
            parList.add(par1);
            List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>(3);
            exceptions.add(maker.Identifier("AttributeNotFoundException"));// NOI18N
            exceptions.add(maker.Identifier("MBeanException"));// NOI18N
            exceptions.add(maker.Identifier("ReflectionException"));// NOI18N

            // getAttribute Content
           
            StringBuffer content = new StringBuffer();
            
            content.append("{");// NOI18N
            MessageFormat formCheckAttr = new MessageFormat(CHECK_ATTR_NAME_PATTERN);
            MessageFormat formAttrComment = new MessageFormat(COMMENT_ATTR_VALUE_PATTERN);
            Object[] args;
            for (int i = 0; i < attributes.length ; i++) {
                args = new Object[] { "attributeName" , attributes[i].getName(), // NOI18N
                formAttrComment.format(new Object[] {attributes[i].getName(),"return"}), // NOI18N
                "return " + WizardHelpers.getDefaultValue( // NOI18N
                        attributes[i].getTypeName()) + ";\n" // NOI18N
                };
                content.append(formCheckAttr.format(args)+"\n\n"); // NOI18N
            }
            content.append("throw new AttributeNotFoundException(\"Unknown Attribute \" + attributeName);"); // NOI18N
            content.append("}");// NOI18N
            // MethodTree mt = w.getTrees().getTree(method);
            // BlockTree copyBody = maker.createMethodBody(mt, content.toString());
            // w.rewrite(mt.getBody(), copyBody);
            
            MethodTree newMethod = maker.Method(
                    maker.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "getAttribute", // NOI18N // name
                    retVal, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    exceptions, // throws
                    content.toString(), //body
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, newMethod, Comment.Style.JAVADOC,
                    "Gets the value of the specified attribute of the DynamicMBean.\n"+// NOI18N
                                       " @param attributeName The attribute name");// NOI18N
            ClassTree copy = maker.insertClassMember(clazz, 1, newMethod);
            return copy;
        }
        
        private ClassTree addSetAttribute(TreeMaker maker, 
                MBeanDO mbean, MBeanAttribute[] attributes, ClassTree clazz) {
            ModifiersTree parMods = maker.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
            TypeElement attributeType = w.getElements().getTypeElement("javax.management.Attribute");// NOI18N
            VariableTree par1 = maker.Variable(parMods, "attribute", maker.QualIdent(attributeType), null);// NOI18N
            List<VariableTree> parList = new ArrayList<VariableTree>(1);
            parList.add(par1);
            List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>(4);
            exceptions.add(maker.Identifier("AttributeNotFoundException"));// NOI18N
            exceptions.add(maker.Identifier("MBeanException"));// NOI18N
            exceptions.add(maker.Identifier("ReflectionException"));// NOI18N
            exceptions.add(maker.Identifier("InvalidAttributeValueException"));// NOI18N
            
            // setAttribute content
            StringBuffer content = new StringBuffer();
            content.append("{");// NOI18N
            MessageFormat formCheckAttr = new MessageFormat(CHECK_ATTR_NAME_PATTERN);
            MessageFormat formAttrComment = new MessageFormat(COMMENT_ATTR_VALUE_PATTERN);
            MessageFormat formAttrThrow = new MessageFormat(THROW_ATTR_VALUE_PATTERN);
            
            for (int i = 0; i < attributes.length ; i++) {
                String checkedAttrBody;
                if (attributes[i].isWritable())
                    checkedAttrBody = formAttrComment.format(
                            new Object[] {attributes[i].getName(),"set"}); // NOI18N
                else
                    checkedAttrBody = formAttrThrow.format(
                            new Object[] {attributes[i].getName() });
                Object[] args = new Object[] { "attribute.getName()" , attributes[i].getName(), // NOI18N
                checkedAttrBody, "" // NOI18N
                };
                content.append(formCheckAttr.format(args));
                content.append(" else ");// NOI18N
                if (i == attributes.length - 1)
                    content.append("\n   ");// NOI18N
            }
            content.append("throw new AttributeNotFoundException(\"Unknown Attribute \" + attribute.getName());"); // NOI18N
            content.append("}");// NOI18N
            
            MethodTree newMethod = maker.Method(
                    maker.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "setAttribute", // NOI18N // name
                    maker.PrimitiveType(TypeKind.VOID), // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    exceptions, // throws
                    content.toString(), //body
                    null // default value - not applicable here, used by annotations
                    );
             JavaModelHelper.addComment(maker, newMethod, Comment.Style.JAVADOC, "Sets the value of the specified attribute of the DynamicMBean.\n"+// NOI18N
                                "@param attribute The attribute to set");// NOI18N
             ClassTree copy = maker.insertClassMember(clazz, 2, newMethod);
             
             return copy;
        }
        
        private ClassTree addInvoke(TreeMaker maker,
                MBeanDO mbean, ClassTree clazz) {
            TypeElement operName = w.getElements().getTypeElement("java.lang.String");// NOI18N
            TypeElement paramsType = w.getElements().getTypeElement("java.lang.Object");// NOI18N
            Tree params = maker.ArrayType(maker.QualIdent(paramsType));
            TypeElement signType = w.getElements().getTypeElement("java.lang.String");// NOI18N
            Tree sign = maker.ArrayType(maker.QualIdent(signType));
            
            ModifiersTree parMods = maker.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());

            VariableTree par1 = maker.Variable(parMods, "operationName", maker.QualIdent(operName), null);// NOI18N
            VariableTree par2 = maker.Variable(parMods, "params", params, null);// NOI18N
            VariableTree par3 = maker.Variable(parMods, "signature", sign, null);// NOI18N
            List<VariableTree> parList = new ArrayList<VariableTree>(1);
            parList.add(par1);
            parList.add(par2);
            parList.add(par3);
            
            List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>(2);
            exceptions.add(maker.Identifier("MBeanException"));// NOI18N
            exceptions.add(maker.Identifier("ReflectionException"));// NOI18N
            
            // Invoke content
            List opList = mbean.getOperations();
            MBeanOperation[] operations = (MBeanOperation[])
                    opList.toArray(new MBeanOperation[opList.size()]);
            StringBuffer content = new StringBuffer();
            content.append("{");// NOI18N
            if (operations.length > 0) {
                content.append(METHOD_SIGNATURE_DEF);
            }
            
            MessageFormat formOperation =
                    new MessageFormat(OPERATION_CHECK_PATTERN);
            for (int i = 0; i < operations.length ; i++) {
                content.append(METHOD_SIGNATURE);
                for (int j = 0; j < operations[i].getParametersSize(); j ++) {
                    MBeanOperationParameter param = operations[i].getParameter(j);
                    content.append(WizardHelpers.getFullTypeNameCode( // NOI18N
                            param.getParamType()));
                    if (j < operations[i].getParametersSize() - 1) {
                        content.append(","); // NOI18N
                    }
                    content.append("\n"); // NOI18N
                }
                content.append("};\n"); // NOI18N
                String code = COMMENT_INVOKE + WizardHelpers.getDefaultValue(
                        operations[i].getReturnTypeName()) + ";"; // NOI18N
                Object[] arg = { operations[i].getName(), code , "operationName" }; // NOI18N
                content.append(formOperation.format(arg));
                content.append("\n"); // NOI18N
            }
            content.append("throw new IllegalArgumentException(\"Unknown Operation \" +\n" +// NOI18N
                    "operationName);");// NOI18N
            content.append("}");// NOI18N
            
            MethodTree newMethod = maker.Method(
                    maker.Modifiers(Collections.singleton(Modifier.PUBLIC)), // modifiers and annotations
                    "invoke", // NOI18N // name
                    maker.QualIdent(w.getElements().getTypeElement("java.lang.Object")), // NOI18N // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    parList, // parameters
                    exceptions, // throws
                    content.toString(), //body
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, newMethod, Comment.Style.JAVADOC, "Allows an operation to be invoked on the DynamicMBean.");// NOI18N
            ClassTree copy = maker.insertClassMember(clazz, 3, newMethod);

            return copy;
        }
        
        private ClassTree addBuildMBeanInfo(TreeMaker maker,
                MBeanDO mbean, MBeanAttribute[] attributes, ClassTree clazz) {
            
            StringBuffer attrInfo = new StringBuffer();
            attrInfo.append("{\n"); // NOI18N
            MessageFormat attrInfoForm = new MessageFormat(MBEAN_ATT_INFO_PATTERN);
            for (int i = 0; i < attributes.length ; i++) {
                Object[] args = new Object[] {
                    attributes[i].getName(),
                    WizardHelpers.getFullTypeNameCode(attributes[i].getTypeName()),
                    attributes[i].getDescription(),
                    attributes[i].isReadable(),
                    attributes[i].isWritable(),
                    "false" // NOI18N
                };
                attrInfo.append(attrInfoForm.format(args));
                if (i != attributes.length - 1)
                    attrInfo.append(","); // NOI18N
                attrInfo.append("\n"); // NOI18N
            }
            attrInfo.append("}"); // NOI18N
            
            // add MBeanOperationInfo
            List opList = mbean.getOperations();
            MBeanOperation[] operations = (MBeanOperation[])
                    opList.toArray(new MBeanOperation[opList.size()]);
            StringBuffer opInfo = new StringBuffer();
            StringBuffer paramsInfo = new StringBuffer();
            MessageFormat opInfoForm = new MessageFormat(MBEAN_OPERATION_INFO_PATTERN);
            MessageFormat opParamsForm = new MessageFormat(MBEAN_PARAMS_INFO_PATTERN);
            MessageFormat opParamForm = new MessageFormat(MBEAN_PARAM_INFO_PATTERN);
            opInfo.append("{\n"); // NOI18N
            for (int i = 0; i < operations.length ; i++) {
                //add MBeanParameterInfo
                Object[] args = new Object[] { i };
                paramsInfo.append(opParamsForm.format(args));
                int nbParam = operations[i].getParametersSize();
                for (int j = 0; j < nbParam; j++) {
                    MBeanOperationParameter param = operations[i].getParameter(j);
                    Object[] pargs = new Object[] {
                        param.getParamName(),
                        WizardHelpers.getFullTypeNameCode(param.getParamType()),
                        param.getParamDescription()
                    };
                    paramsInfo.append(opParamForm.format(pargs));
                    if (j != nbParam - 1)
                        paramsInfo.append(","); // NOI18N
                    paramsInfo.append("\n"); // NOI18N
                }
                paramsInfo.append("};\n"); // NOI18N
                
                //add MBeanOperationInfo
                args = new Object[] {
                    operations[i].getName(),
                    operations[i].getDescription(),
                    WizardHelpers.getFullTypeNameCode(operations[i].getReturnTypeName()),
                    i
                };
                opInfo.append(opInfoForm.format(args));
                if (i != operations.length - 1)
                    opInfo.append(","); // NOI18N
                opInfo.append("\n"); // NOI18N
            }
            opInfo.append("}"); // NOI18N
            StringBuffer content = new StringBuffer();
            content.append("{");// NOI18N
            content.append("MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[]");// NOI18N
            content.append(attrInfo.toString());
            content.append(";");// NOI18N
            content.append("MBeanConstructorInfo[] dConstructors = createConstructors();");// NOI18N
            content.append(paramsInfo.toString());
            content.append("MBeanOperationInfo[] dOperations = new MBeanOperationInfo[]");// NOI18N
            content.append(opInfo.toString());
            content.append(";");// NOI18N
            content.append("dMBeanInfo = new MBeanInfo(\""+ mbean.getName()+"\",\n"+// NOI18N
                    "\"");// NOI18N
            content.append(mbean.getDescription());
            content.append("\",\n" +// NOI18N
                    "dAttributes,\n"+// NOI18N
                    "dConstructors,\n"+// NOI18N
                    "dOperations,\n"+// NOI18N
                    "getNotificationInfo());" +// NOI18N
                    "}");// NOI18N
            MethodTree newMethod = maker.Method(
                    maker.Modifiers(Collections.singleton(Modifier.PRIVATE)), // modifiers and annotations
                    "buildDynamicMBeanInfo", // NOI18N // name
                    maker.PrimitiveType(TypeKind.VOID), // return type
                    Collections.EMPTY_LIST,
                    Collections.EMPTY_LIST,
                    Collections.EMPTY_LIST,
                    content.toString(),//body
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, newMethod, Comment.Style.JAVADOC,"Build the private dMBeanInfo field,\n"+// NOI18N
                    "which represents the management interface exposed by the MBean,\n"+// NOI18N
                    "that is, the set of attributes, constructors, operations and\n"+// NOI18N
                    "notifications which are available for management.\n"+// NOI18N
                    "A reference to the dMBeanInfo object is returned by the getMBeanInfo()\n"+// NOI18N
                    "method of the DynamicMBean interface. Note that, once constructed,\n"+// NOI18N
                    "an MBeanInfo object is immutable.");// NOI18N
            ClassTree copy = maker.insertClassMember(clazz, 5, newMethod);
            
            return copy;
        }
    }
    
    private static class UpdateExtendedStandardMBeanTemplateTransformer extends TreePathScanner<Void,Void> {
        
        private static final String OPERATION_SIGNATURE =
                "MBeanParameterInfo[] params = info.getSignature();\n" + // NOI18N
                "String[] signature = new String[params.length];\n" + // NOI18N
                "for (int i = 0; i < params.length; i++)\n" + // NOI18N
                "    signature[i] = params[i].getType();\n"; // NOI18N
        
        private static String METADATA_MBEANINFO_UPDATE_COMMENT =
                " Override customization hook:\n" + // NOI18N
                " You can supply a customized description for MBeanInfo.getDescription()";// NOI18N
        
        private static String METADATA_MBEANPARAMATER_NAME_UPDATE_COMMENT =
                " Override customization hook:\n" + // NOI18N
                " You can supply a customized description for MBeanParameterInfo.getName()";// NOI18N
        
        private static String METADATA_MBEANPARAMATER_DESCRIPTION_UPDATE_COMMENT =
                " Override customization hook:\n" + // NOI18N
                " You can supply a customized description for MBeanParameterInfo.getDescription()";// NOI18N
            
        private static String METADATA_MBEANATTRIBUTE_DESCRIPTION_UPDATE_COMMENT =
                " Override customization hook:\n" + // NOI18N
                " You can supply a customized description for MBeanAttributeInfo.getDescription()";// NOI18N
        private static String METADATA_MBEANOPERATION_DESCRIPTION_UPDATE_COMMENT =
                " Override customization hook:\n" + // NOI18N
                " You can supply a customized description for MBeanOperationInfo.getDescription()";// NOI18N
        
        private final WorkingCopy w;
        private final MBeanDO mbean;
        public UpdateExtendedStandardMBeanTemplateTransformer(WorkingCopy w, MBeanDO mbean) {
            this.w = w;
            this.mbean = mbean;
        }
        
        @Override
        public Void visitClass(ClassTree clazz, Void v) {
            TreeMaker make = w.getTreeMaker();
            Element el = w.getTrees().getElement(getCurrentPath());
            updateDescription(w, el, make, mbean.getDescription() +
                    "\nDynamic MBean based on StandardMBean\n", clazz); // NOI18N
            addMethods(mbean, clazz);
            super.visitClass(clazz, v);
            return null;
        }
        
        private void addMethods(MBeanDO mbean, ClassTree clazz) {
            TreeMaker maker = w.getTreeMaker();
            ClassTree copy = clazz;
            if (mbean.isWrapppedClass()) {
                ExpressionTree t = getType(w, mbean.getWrappedClassName(), null);
                
                 // Add a field
                Set<Modifier> modifiers  = new HashSet<Modifier>();
                modifiers.add(Modifier.PRIVATE);
                VariableTree theRef = maker.Variable(maker.Modifiers(modifiers), "theRef", t, null);// NOI18N
                copy = maker.insertClassMember(copy, 0, theRef);
                
                // Add a constructor
                
                VariableTree p = maker.Variable(maker.Modifiers(Collections.<Modifier>emptySet()),
                        "theRef", t, null); // NOI18N
                List<VariableTree> params = new ArrayList<VariableTree>(1);
                params.add(p);
                    
                Set<Modifier> modifiers2 = new HashSet<Modifier>(1);
                modifiers2.add(Modifier.PUBLIC);
                List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>(1);
                exceptions.add(maker.Identifier("NotCompliantMBeanException"));// NOI18N
                StringBuffer body = new StringBuffer();
                body.append("{//WARNING Uncomment the following call to super() to make this class compile (see BUG ID 122377)\n");
                body.append("// super(" + mbean.getName() + WizardConstants.MBEAN_ITF_SUFFIX + ".class");// NOI18N
                if(mbean.isWrapppedClassMXBean())
                    body.append(", true");// NOI18N
                body.append(");\n");// NOI18N
                 body.append("this.theRef = theRef;}");   // NOI18N
                MethodTree newConstructor =
                        maker.Constructor(maker.Modifiers(modifiers2),
                        Collections.EMPTY_LIST,
                        params,
                        exceptions,
                        body.toString());
                copy = maker.insertClassMember(copy, 1, newConstructor);
            }
            TypeElement annotationType = w.getElements().getTypeElement("java.lang.Override"); // NOI18N
            Tree annotationTree = maker.QualIdent(annotationType);
            AnnotationTree overrideAnnotation = maker.Annotation(annotationTree, Collections.<ExpressionTree>emptyList());
            List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();
            annotations.add(overrideAnnotation);
            ModifiersTree parMods = maker.Modifiers(Collections.<Modifier>emptySet());
            ModifiersTree mt = maker.Modifiers(Collections.singleton(Modifier.PROTECTED), annotations);
            IdentifierTree strTree = maker.Identifier("String"); // NOI18N
            
            // Add protected String getDescription(MBeanInfo info)
            TypeElement infoType = w.getElements().getTypeElement("javax.management.MBeanInfo"); // NOI18N
            ExpressionTree mbeanInfo = maker.QualIdent(infoType);
            VariableTree descMBeanInfo = maker.Variable(parMods, "info", mbeanInfo, null); // NOI18N
            List<VariableTree> paramsMBeanInfo = new ArrayList<VariableTree>(1);
            paramsMBeanInfo.add(descMBeanInfo);
            MethodTree methodDescrMBeanInfo = maker.Method(
                    mt, // modifiers and annotations
                    "getDescription", // NOI18N
                    strTree, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    paramsMBeanInfo, // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    "{return \"" + mbean.getDescription() +"\";}", // NOI18N
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, methodDescrMBeanInfo, Comment.Style.JAVADOC,METADATA_MBEANINFO_UPDATE_COMMENT);

            copy = maker.addClassMember(copy, methodDescrMBeanInfo);
            
            // Add protected String getDescription(MBeanAttributeInfo info)
            TypeElement attrInfoType = w.getElements().getTypeElement("javax.management.MBeanAttributeInfo"); // NOI18N
            ExpressionTree attrInfo = maker.QualIdent(attrInfoType);
            VariableTree descAttrInfo = maker.Variable(parMods, "info", attrInfo, null); // NOI18N
            List<VariableTree> paramsAttrInfo = new ArrayList<VariableTree>(1);
            paramsAttrInfo.add(descAttrInfo);
            
            MethodTree methodDescrAttributeInfo = maker.Method(
                    mt, // modifiers and annotations
                    "getDescription", // NOI18N
                    strTree, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    paramsAttrInfo, // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    "{String description = null;\n" + getAttDescCode(mbean) + "\nreturn description;}", // NOI18N
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, methodDescrAttributeInfo, Comment.Style.JAVADOC,METADATA_MBEANATTRIBUTE_DESCRIPTION_UPDATE_COMMENT);
            copy = maker.addClassMember(copy, methodDescrAttributeInfo);
            
            // Add protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param,int sequence)
            TypeElement operInfoType = w.getElements().getTypeElement("javax.management.MBeanOperationInfo"); // NOI18N
            TypeElement paramInfoType = w.getElements().getTypeElement("javax.management.MBeanParameterInfo"); // NOI18N
            ExpressionTree operInfo = maker.QualIdent(operInfoType);
            ExpressionTree paramInfo = maker.QualIdent(paramInfoType);
            ExpressionTree seq = maker.Identifier("int");// NOI18N
            VariableTree param1 = maker.Variable(parMods, "op", operInfo, null); // NOI18N
            VariableTree param2 = maker.Variable(parMods, "param", paramInfo, null); // NOI18N
            VariableTree param3 = maker.Variable(parMods, "sequence", seq, null); // NOI18N
            List<VariableTree> paramsOperParamInfo = new ArrayList<VariableTree>(3);
            paramsOperParamInfo.add(param1);
            paramsOperParamInfo.add(param2);
            paramsOperParamInfo.add(param3);
            
            MethodTree methodDescrOperationParamInfo = maker.Method(
                    mt, // modifiers and annotations
                    "getDescription", // NOI18N
                    strTree, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    paramsOperParamInfo, // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    "{"+ getParamDescCode(mbean, true) + "\nreturn null;}", // NOI18N
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, methodDescrOperationParamInfo, Comment.Style.JAVADOC, METADATA_MBEANPARAMATER_DESCRIPTION_UPDATE_COMMENT);
            copy = maker.addClassMember(copy, methodDescrOperationParamInfo);
            
            // Add protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence)
            // Idem signature than previous one.
            MethodTree methodParamName = maker.Method(
                    mt, // modifiers and annotations
                    "getParameterName", // NOI18N
                    strTree, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    paramsOperParamInfo, // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    "{"+getParamDescCode(mbean,false) + "\nreturn null;}", // NOI18N
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, methodParamName, Comment.Style.JAVADOC,METADATA_MBEANPARAMATER_NAME_UPDATE_COMMENT);
            copy = maker.addClassMember(copy, methodParamName);
            
            // Add getDescription(MBeanOperationInfo)
            List<VariableTree> paramsOperInfo = new ArrayList<VariableTree>(1);
            VariableTree param = maker.Variable(parMods, "info", operInfo, null); // NOI18N
            paramsOperInfo.add(param);
            
            MethodTree methodDescrOperationInfo = maker.Method(
                    mt, // modifiers and annotations
                    "getDescription", // NOI18N
                    strTree, // return type
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    paramsOperInfo, // parameters
                    Collections.<ExpressionTree>emptyList(), // throws
                    "{String description = null;\n" + getOpDescCode(mbean) + "\nreturn description;}", // NOI18N
                    null // default value - not applicable here, used by annotations
                    );
            JavaModelHelper.addComment(maker, methodDescrOperationInfo, Comment.Style.JAVADOC,METADATA_MBEANOPERATION_DESCRIPTION_UPDATE_COMMENT);
            copy = maker.addClassMember(copy, methodDescrOperationInfo);
            
            w.rewrite(clazz, copy); 
        }
        
        private String getOpDescCode(MBeanDO mbean) {
            List opList = mbean.getOperations();
            MBeanOperation[] operations = (MBeanOperation[])
                    opList.toArray(new MBeanOperation[opList.size()]);
            StringBuffer content = new StringBuffer();
            if (operations.length > 0) {
                content.append(OPERATION_SIGNATURE);
                content.append(METHOD_SIGNATURE_DEF);
            }
            MessageFormat formOperation =
                    new MessageFormat(OPERATION_CHECK_PATTERN);
            for (int i = 0; i < operations.length; i++) {
                content.append(METHOD_SIGNATURE);
                for (int j = 0; j < operations[i].getParametersSize(); j ++) {
                    MBeanOperationParameter param = operations[i].getParameter(j);
                    content.append(WizardHelpers.getFullTypeNameCode(
                            removeGeneric(param.getParamType())));
                    if (j < operations[i].getParametersSize() - 1) {
                        content.append(","); // NOI18N
                    }
                    content.append("\n"); // NOI18N
                }
                content.append("};\n"); // NOI18N
                String descCode = "     description = \"" + // NOI18N
                        operations[i].getDescription() + "\";"; // NOI18N
                Object[] arg = { operations[i].getName(), descCode, "info.getName()" }; // NOI18N
                content.append(formOperation.format(arg));
            }
            
            return content.toString();
        }
        
        private String getParamDescCode(MBeanDO mbean, boolean isGetDescription) {
            List opList = mbean.getOperations();
            MBeanOperation[] operations = (MBeanOperation[])
                    opList.toArray(new MBeanOperation[opList.size()]);
            StringBuffer content = new StringBuffer();
            for (int i = 0; i < operations.length ; i++) {
                if (i!=0) {
                    content.append("} else "); // NOI18N
                }
                content.append("if (op.getName().equals(\""); // NOI18N
                content.append(operations[i].getName());
                content.append("\")) {\n"); // NOI18N
                content.append("           switch (sequence) {\n"); // NOI18N
                for (int j = 0; j < operations[i].getParametersSize(); j++) {
                    content.append("     case " + j + ": return \""); // NOI18N
                    String stringToReturn = null;
                    MBeanOperationParameter param = operations[i].getParameter(j);
                    if (isGetDescription) {
                        stringToReturn = param.getParamDescription();
                    } else {
                        stringToReturn = param.getParamName();
                    }
                    content.append(stringToReturn);
                    content.append("\";\n"); // NOI18N
                }
                content.append("     default : return null;\n"); // NOI18N
                content.append("   }\n"); // NOI18N
            }
            if (operations.length != 0) {
                content.append("}\n"); // NOI18N
            }
            return content.toString();
        }
        
        private String getAttDescCode(MBeanDO mbean) {
            List attrList = mbean.getAttributes();
            MBeanAttribute[] attributes = (MBeanAttribute[])
                    attrList.toArray(new MBeanAttribute[attrList.size()]);
            StringBuffer content = new StringBuffer();
            for (int i = 0; i < attributes.length ; i++) {
                if (i!=0)
                    content.append("} else "); // NOI18N
                content.append("if (info.getName().equals(\""); // NOI18N
                content.append(attributes[i].getName());
                content.append("\")) {\n"); // NOI18N
                content.append("     description = \""); // NOI18N
                content.append(attributes[i].getDescription() + "\";\n"); // NOI18N
                if (i == attributes.length - 1) {
                    content.append("}\n"); // NOI18N
                }
            }
            return content.toString();
        }
        
    }
    
    private static class AddMBeanRegistrationCodeTransformer extends TreePathScanner<Void,Object> {
        private final String objectName;
        private final WorkingCopy w;
        private final ExecutableElement constructor;
        private final static String REGISTRATION_COMMENTS = "TODO update MBean constructor parameters with valid values."; // NOI18N
        public AddMBeanRegistrationCodeTransformer(WorkingCopy w, String objectName,
                ExecutableElement constructor) {
            this.w = w;
            this.constructor = constructor;
            this.objectName = objectName;
        }
        
        //Called for every method in the java source file
        @Override
        public Void visitMethod(MethodTree tree, Object p) {
            //Obtain the owner of this method, getCurrentPath() returns a path from root (CompilationUnitTree) to current node (tree)
            //Tree owner =  getCurrentPath().getParentPath();
            if("init".contentEquals(tree.getName())) { // NOI18N
                List<? extends StatementTree> statements = tree.getBody().getStatements();
                List<StatementTree> newStatements = new ArrayList<StatementTree>(statements);
                
                // Write the code generation
                TreeMaker treeMaker = w.getTreeMaker();
                
                // Access to the getMbeanServer
                //Element el = w.getTrees().getElement(getCurrentPath().getParentPath());
                //TypeElement te = (TypeElement) el;
                
                // Access to the ObjectName constructor
                TypeElement objectNameClass =
                        w.getElements().getTypeElement("javax.management.ObjectName");// NOI18N
                // ObjectName creation
                LiteralTree objNameValTree = treeMaker.Literal(objectName);
                ArrayList<ExpressionTree> initializer = new ArrayList<ExpressionTree>(1);
                initializer.add(objNameValTree);
                ExpressionTree ex = treeMaker.QualIdent(objectNameClass);
                NewClassTree objectNameConstructor =
                        treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), ex, initializer, null);
                
                // MBean creation
                ArrayList<ExpressionTree> initializerMBeanClass = new ArrayList<ExpressionTree>(1);
                // Workaround. We go to string to come back to type
                // Better handling of imports
                String mbeanClassStr = ((TypeElement)constructor.getEnclosingElement()).getQualifiedName().toString();
                TypeElement mbeanClass =
                        w.getElements().getTypeElement(mbeanClassStr);
                ExpressionTree mbeanClassEx = treeMaker.QualIdent(mbeanClass);
                List<? extends VariableElement> params = constructor.getParameters();
                for(VariableElement param : params) {
                    // Just put null for all
                    // XXX REVISIT IS IT LIKE THAT?
                    initializerMBeanClass.add(treeMaker.Literal(null));
                }
                NewClassTree mbeanConstructor =
                        treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), mbeanClassEx, initializerMBeanClass, null);
                
                // getMBeanServer()
                IdentifierTree getMBSTree = w.getTreeMaker().Identifier("getMBeanServer");// NOI18N
                MethodInvocationTree getMBSInvocation =
                        treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                        getMBSTree,
                        Collections.<ExpressionTree>emptyList());
                
                // getMBeanServer().registerMBean
                MemberSelectTree registerMBeanSelIdent = w.getTreeMaker().MemberSelect(getMBSInvocation, "registerMBean");// NOI18N
                
                // Now register MBean MethodInvocation
                List<ExpressionTree> registerParams = new ArrayList<ExpressionTree>(2);
                registerParams.add(mbeanConstructor);
                registerParams.add(objectNameConstructor);
                //IdentifierTree registTree = w.getTreeMaker().Identifier("registerMBean");
                MethodInvocationTree registerInvocation =
                        treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                        // registTree,
                        registerMBeanSelIdent,
                        registerParams);
                
                // Add at the end of the statements
                ExpressionStatementTree t = treeMaker.ExpressionStatement(registerInvocation);
                newStatements.add((StatementTree)t);
                
                if(constructor != null &&
                        constructor.getParameters().size() != 0) {
                    JavaModelHelper.addComment(treeMaker, t, Comment.Style.LINE, REGISTRATION_COMMENTS);
                }
                
                BlockTree newBody = treeMaker.Block(newStatements, false);
                w.rewrite(tree.getBody(), newBody);
            }
            super.visitMethod(tree, p);
            return null;
        }
        /**
         * Returns an array of this method param types.
         * @param completeSignature <CODE>String</CODE> a method signature
         * @return <CODE>String[]</CODE> an array of param types.
         */
        private static String[] getSignature(String completeSignature) {
            int signBegin = completeSignature.lastIndexOf("(");// NOI18N
            int signEnd = completeSignature.lastIndexOf(")");// NOI18N
            String[] params = completeSignature.substring(
                    signBegin + 1, signEnd).split(",");// NOI18N
            if ((params.length == 1) && (params[0].equals("")))// NOI18N
                params = new String[] {};
            for (int i = 0; i < params.length; i++)
                params[i] = params[i].trim();
            return params;
        }
    }
    
    private static class AddStandardMBeanRegistrationCodeTransformer extends TreePathScanner<Void,Object> {
        private final String objectName;
        private final String itf;
        private final WorkingCopy w;
        private final ExecutableElement constructor;
        private final static String REGISTRATION_COMMENTS = "TODO update MBean constructor parameters with valid values."; // NOI18N
        public AddStandardMBeanRegistrationCodeTransformer(WorkingCopy w, String objectName, String itf,
                ExecutableElement constructor) {
            this.w = w;
            this.constructor = constructor;
            this.objectName = objectName;
            this.itf = itf;
        }
        
        //Called for every method in the java source file
        @Override
        public Void visitMethod(MethodTree tree, Object p) {
            //Obtain the owner of this method, getCurrentPath() returns a path from root (CompilationUnitTree) to current node (tree)
            //Tree owner =  getCurrentPath().getParentPath();
            if("init".contentEquals(tree.getName())) { // NOI18N
                List<? extends StatementTree> statements = tree.getBody().getStatements();
                List<StatementTree> newStatements = new ArrayList<StatementTree>(statements);
                
                // Write the code generation
                TreeMaker treeMaker = w.getTreeMaker();
                
                // Access to the getMbeanServer
                //Element el = w.getTrees().getElement(getCurrentPath().getParentPath());
                //TypeElement te = (TypeElement) el;
                
                // Access to the ObjectName constructor
                TypeElement objectNameClass =
                        w.getElements().getTypeElement("javax.management.ObjectName");// NOI18N
                // ObjectName creation
                LiteralTree objNameValTree = treeMaker.Literal(objectName);
                ArrayList<ExpressionTree> initializer = new ArrayList<ExpressionTree>(1);
                initializer.add(objNameValTree);
                ExpressionTree ex = treeMaker.QualIdent(objectNameClass);
                NewClassTree objectNameConstructor =
                        treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), ex, initializer, null);
                
                // Standard MBean creation
                ArrayList<ExpressionTree> initializerStdMBeanClass = new ArrayList<ExpressionTree>(1);
                TypeElement stdMbeanClass =
                        w.getElements().getTypeElement("javax.management.StandardMBean");// NOI18N
                ExpressionTree stdMbeanClassEx = treeMaker.QualIdent(stdMbeanClass);
                
                // Means that we need to generate the Wrapped Object creation
                String wrappedClassStr = null;
                if(constructor != null) {
                    ArrayList<ExpressionTree> initializerWrappedClass = new ArrayList<ExpressionTree>(1);
                    wrappedClassStr = ((TypeElement)constructor.getEnclosingElement()).getQualifiedName().toString();
                    TypeElement wrappedClass =
                            w.getElements().getTypeElement(wrappedClassStr);
                    ExpressionTree mbeanClassEx = treeMaker.QualIdent(wrappedClass);
                    List<? extends VariableElement> params = constructor.getParameters();
                    for(VariableElement param : params) {
                        // Just put null for all
                        // XXX REVISIT IS IT LIKE THAT?
                        initializerWrappedClass.add(treeMaker.Literal(null));
                    }
                    NewClassTree wrappedConstructor =
                            treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), mbeanClassEx, initializerWrappedClass, null);
                    
                    // Add wrapped creation as the first parameter of StandardMBean constructor
                    initializerStdMBeanClass.add(wrappedConstructor);
                } else
                    initializerStdMBeanClass.add(treeMaker.Literal(null));
                
                // Handle Management Interface
                if(itf != null) {
                    TypeElement mgtItf =
                            w.getElements().getTypeElement(itf);
                    ExpressionTree mgtItfEx = treeMaker.QualIdent(mgtItf);
                    MemberSelectTree mst = treeMaker.MemberSelect(mgtItfEx, "class");// NOI18N
                    initializerStdMBeanClass.add(mst);
                } else
                    initializerStdMBeanClass.add(treeMaker.Literal(null));
                
                NewClassTree stdMbeanConstructor =
                        treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), stdMbeanClassEx, initializerStdMBeanClass, null);
           
                // getMBeanServer()
                IdentifierTree getMBSTree = w.getTreeMaker().Identifier("getMBeanServer");// NOI18N
                MethodInvocationTree getMBSInvocation =
                        treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                        getMBSTree,
                        Collections.<ExpressionTree>emptyList());
                
                // getMBeanServer().registerMBean
                MemberSelectTree registerMBeanSelIdent = w.getTreeMaker().MemberSelect(getMBSInvocation, "registerMBean");// NOI18N
                
                // Now register MBean MethodInvocation
                List<ExpressionTree> registerParams = new ArrayList<ExpressionTree>(2);
                
                registerParams.add(stdMbeanConstructor);
                registerParams.add(objectNameConstructor);
                //IdentifierTree registTree = w.getTreeMaker().Identifier("registerMBean");
                MethodInvocationTree registerInvocation =
                        treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                        // registTree,
                        registerMBeanSelIdent,
                        registerParams);
                
                // Add at the end of the statements
                ExpressionStatementTree t = treeMaker.ExpressionStatement(registerInvocation);
                newStatements.add((StatementTree)t);
                if(constructor == null) {
                    JavaModelHelper.addComment(treeMaker, t, Comment.Style.LINE,"TODO replace first StandardMBean constructor parameter by your own " + wrappedClassStr +" object."); // NOI18N
                }
                
                if(constructor != null &&
                        constructor.getParameters().size() != 0) {
                    JavaModelHelper.addComment(treeMaker, t, Comment.Style.LINE,REGISTRATION_COMMENTS);
                }
                
                BlockTree newBody = treeMaker.Block(newStatements, false);
                w.rewrite(tree.getBody(), newBody);
            }
            super.visitMethod(tree, p);
            return null;
        }
        /**
         * Returns an array of this method param types.
         * @param completeSignature <CODE>String</CODE> a method signature
         * @return <CODE>String[]</CODE> an array of param types.
         */
        private static String[] getSignature(String completeSignature) {
            int signBegin = completeSignature.lastIndexOf("(");// NOI18N
            int signEnd = completeSignature.lastIndexOf(")");// NOI18N
            String[] params = completeSignature.substring(
                    signBegin + 1, signEnd).split(",");// NOI18N
            if ((params.length == 1) && (params[0].equals("")))// NOI18N
                params = new String[] {};
            for (int i = 0; i < params.length; i++)
                params[i] = params[i].trim();
            return params;
        }
    }
    
    /** Creates a new instance of JavaModelHelper */
    private JavaModelHelper() {
    }
    
    public static JavaSource getSource(FileObject fo) {
        JavaSource js = JavaSource.forFileObject(fo);
        return js;
    }
    public static String[] getInterfaceNames(JavaSource clazz) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        clazz.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new InterfacesMemberVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (String[]) value.getValue();
    }
    
    public static void addImports(final List<String> imports, JavaSource clazz) throws IOException {
        if(imports == null || imports.size() == 0) return;
        clazz.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                // XXX REVISIT
                TreeMaker make = w.getTreeMaker();
                CompilationUnitTree cut = w.getCompilationUnit();
                List<? extends ImportTree> currentImports = cut.getImports();

                for(ImportTree current : currentImports) {
                    String toAdd = current.getQualifiedIdentifier().toString();
                    if(imports.contains(toAdd))
                        imports.remove(toAdd);
                }
                CompilationUnitTree copy =cut;
                for(String imp : imports) {
                    copy = make.addCompUnitImport(
                            copy,
                            make.Import(make.Identifier(imp), false));
                }
                w.rewrite(cut, copy);
            }
        }).commit();
    }
    
    /**
     * Returns an array containing all public constructors of the specified class.
     *
     * @param clazz <code>JavaClass</code> examine
     * @return  <code>Method[]</code> class methods or <code>null</code> if
     * there is no method
     */
    public static Map<String, ExecutableElement> getConstructors(JavaSource clazz) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        clazz.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new ConstructorsMemberVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (Map<String, ExecutableElement>) value.getValue();
    }
    
    public static final boolean isGeneratedAgent(final JavaSource src) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        src.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new IsGeneratedAgentAgentVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (Boolean) value.getValue();
    }
    
    public static JavaSource findClassInProject(Project project, final String fullClassName) throws IOException {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] grps = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        final FileObject[] fo = new FileObject[1];

        for(int i = 0; i < grps.length; i++) {
            FileObject root = grps[i].getRootFolder();
            final ClasspathInfo cpi = ClasspathInfo.create(root);
            JavaSource javaSrc = JavaSource.create(cpi);
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {
                public void run(CompilationController c) throws IOException {
                    Elements e = c.getElements();
                    TypeElement te = e.getTypeElement(fullClassName);
                    if(te != null) {
                        //The element is under this root
                        fo[0] = SourceUtils.getFile(te, cpi);
                    }
                }
                public void cancel() {}
            }, true);
            if (fo[0] != null) {
                return getSource(fo[0]);
            }
        }
        
        return null;
    }
    
    public static boolean isDynamicMBean(final JavaSource baseClass) throws IOException {
         final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new IsDynamicMBeanVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        Object val = value.getValue();
        if(val == null) return false;
        return (Boolean) value.getValue();
    }
    
    public static boolean testMBeanCompliance(JavaSource baseClass) throws IOException {
       Object itf = getManagementInterface(baseClass);
       if(itf != null)
           return true;
       return isDynamicMBean(baseClass);
    }
    
    public static boolean checkKnownType(JavaSource baseClass, final String type) {
        try {
            final ValueHolder value = new ValueHolder();
            // Check if the Agent has getMBeanServer method and a method named init
            baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {}
                public void run(CompilationController ctrl) throws IOException {
                    ctrl.toPhase(Phase.ELEMENTS_RESOLVED);
                    value.setValue(isKnownType(ctrl, type));
                }
            }, true);
            Object val = value.getValue();
            if(val == null) return false;
            return (Boolean)val;
        }catch(IOException ex){
           // System.out.println("Eoor occured" + ex);
        }
        return false;
    }
    
    public static String getFullClassName(JavaSource baseClass) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new FullClassNameMemberVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (String) value.getValue();
    }
    
    public static boolean implementsMBeanRegistrationItf(JavaSource baseClass) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new SearchInterfaceVisitor(parameter, value, "javax.management.MBeanRegistration").scan(parameter.getCompilationUnit(), null);// NOI18N
            }
        }, true);
        
        return value.getValue() != null;
    }
    
    public static MBeanAttribute[] getAttributes(JavaSource baseClass) throws IOException {
        MBeanDO mbeando = getMBeanModel(baseClass);
        if(mbeando == null) return null;
        List<MBeanAttribute> attributes = mbeando.getAttributes();
        return attributes.toArray(new MBeanAttribute[attributes.size()]);
    }
    
    public static MBeanOperation[] getOperations(JavaSource baseClass) throws IOException {
        MBeanDO mbeando = getMBeanModel(baseClass);
        if(mbeando == null) return null;
        List<MBeanOperation> operations = mbeando.getOperations();
        return operations.toArray(new MBeanOperation[operations.size()]);
    }
    
    public static MBeanDO getMBeanModel(JavaSource baseClass) throws IOException {
        return getMBeanModel(baseClass, true);
    }
    
    public static MBeanDO getMBeanLikeModel(JavaSource baseClass) throws IOException {
        return getMBeanModel(baseClass, false);
    }
    
    public static String getManagementInterfaceSimpleName(JavaSource baseClass) throws IOException {
        TypeElement itf = getManagementInterface(baseClass);
        return itf.getSimpleName().toString();
    }
    
    private static TypeElement getManagementInterface(JavaSource baseClass) throws IOException {
          TypeElement itf = isStandardMBean(baseClass);
          if(itf != null)
            return itf;
          itf = isMXBean(baseClass);
          return itf;
    }
    
    private static MBeanDO getMBeanModel(final JavaSource baseClass, final boolean real) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement mgtItf = null;
                if (real) {
                    mgtItf = getManagementInterface(baseClass);
                    if (mgtItf == null) {
                        throw new IOException("Invalid MBean class " + baseClass);// NOI18N
                    }// NOI18N
                
                    new MBeanModelVisitor(parameter, value, real, mgtItf).scan(parameter.getCompilationUnit(), null);
                } else
                    new MBeanModelFromClassVisitor(parameter, value, real, mgtItf).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (MBeanDO) value.getValue();
    }
    
    public static final TypeElement isStandardMBean(final JavaSource baseClass) throws IOException {
        final ValueHolder value = new ValueHolder();
        final String fullClassName = getFullClassName(baseClass);
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new SearchInterfaceVisitor(parameter, value, fullClassName+WizardConstants.MBEAN_ITF_SUFFIX).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        TypeElement itf = (TypeElement)value.getValue();
        if(itf != null && itf.getModifiers().contains(Modifier.PUBLIC))
            return itf;
        return null;
    }
    
    public static final TypeElement isMXBean(final JavaSource baseClass) throws IOException {
        final ValueHolder value = new ValueHolder();
        final String fullClassName = getFullClassName(baseClass);
        
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new SearchInterfaceVisitor(parameter, value, fullClassName+WizardConstants.MXBEAN_ITF_SUFFIX).scan(parameter.getCompilationUnit(), null);
                TypeElement itf = (TypeElement)value.getValue();
                
                if(itf == null)
                    return;
                
                if(!itf.getModifiers().contains(Modifier.PUBLIC))
                    value.setValue(null);
                
                boolean isMXBean = true;
                List<? extends AnnotationMirror> annotations = itf.getAnnotationMirrors();
                for(AnnotationMirror annotation : annotations) {
                    if ("javax.management.MXBean".equals(((TypeElement) // NOI18N
                            annotation.getAnnotationType().asElement()).
                            getQualifiedName().toString())) {
                        Map<? extends ExecutableElement, ? extends AnnotationValue> values = 
                                parameter.getElements().getElementValuesWithDefaults(annotation);
                        for(ExecutableElement name : values.keySet()) {
                            if("value".equals(name.getSimpleName().toString())) {// NOI18N
                                AnnotationValue value = values.get(name);
                                if(Boolean.FALSE.equals(value.getValue())) {
                                    isMXBean = false;
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                if(!isMXBean)
                    value.setValue(null);
            }
        }, true);
        return (TypeElement)value.getValue();
    }
    
    /*public static boolean implementsDynamicMBeanItf(JavaSource baseClass) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new SearchInterfaceVisitor(parameter, value, "javax.management.DynamicMBean").scan(parameter.getCompilationUnit(), null);// NOI18N
            }
        }, true);
        return value.getValue() != null;
    }
    */
    public static boolean implementsNotificationBroadcaster(JavaSource baseClass) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new SearchInterfaceVisitor(parameter, value, "javax.management.NotificationBroadcaster").scan(parameter.getCompilationUnit(), null);// NOI18N
            }
        }, true);
        return value.getValue() != null;
    }
    
    public static boolean hasOnlyDefaultConstruct(JavaSource clazz) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        clazz.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new HasOnlyDefaultConstructorMemberVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (Boolean) value.getValue();
    }
    public static String getSimpleName(JavaSource clazz) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        clazz.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new SimpleClassNameMemberVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (String) value.getValue();
    }
    public static String getPackage(JavaSource clazz) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        clazz.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new PackageMemberVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (String) value.getValue();
    }
    
    public static String getBodyText(MethodTree method, WorkingCopy workingCopy) {
        BlockTree body = method.getBody();
        CompilationUnitTree cut = workingCopy.getCompilationUnit();
        // get SourcePositions instance for your working copy and
        // fetch out start and end position.
        SourcePositions sp = workingCopy.getTrees().getSourcePositions();
        int start = (int) sp.getStartPosition(cut, body);
        int end = (int) sp.getEndPosition(cut, body);
        // get body text from source text
        String bodyText = workingCopy.getText().substring(start, end);
        return bodyText;
    }
    
    public static void updateMBeanWithNotificationEmitter(JavaSource js, final MBeanNotification[] notifs,
            final boolean genBroadcastDeleg, final boolean genSeqNumber)
            throws IOException {
        js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                // XXX REVISIT
                new MBeanNotificationTransformer(w, notifs, genBroadcastDeleg, genSeqNumber).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    
    public static void updateMBeanWithRegistration(JavaSource js, final boolean keepRefSelected)
            throws IOException {
        js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                // XXX REVISIT
                new MBeanRegistrationTransformer(w, keepRefSelected).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    
    public static MBeanAttribute searchAttributeImplementation(final JavaSource baseClass,
            final MBeanAttribute attribute) throws IOException {
        final ValueHolder value = new ValueHolder();
        
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new AttributeImplementationVisitor(parameter, value, attribute).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (MBeanAttribute)value.getValue();
    }
    
    public static MBeanOperation searchOperationImplementation(final JavaSource baseClass,
            final MBeanOperation operation) throws IOException {
        final ValueHolder value = new ValueHolder();
        
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new OperationImplementationVisitor(parameter, value, operation).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (MBeanOperation)value.getValue();
    }
    
    private static void addAttributes(JavaSource baseClass, final MBeanAttribute[] attributes) throws IOException {
        baseClass.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                new AddAttributesTransformer(w, attributes).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    
    private static void addOperations(JavaSource baseClass, final MBeanOperation[] operations) throws IOException {
        baseClass.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                new AddOperationsTransformer(w, operations).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    
    private static JavaSource getStandardMBeanItf(final JavaSource baseClass) throws IOException {
        final ValueHolder value = new ValueHolder();
        
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new StandardMBeanItfVisitor(parameter, value, baseClass).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (JavaSource)value.getValue();
    }
    
    public static void generateMBeanRegistration(JavaSource baseClass, final String objectName, final ExecutableElement constructor) throws IOException {
        baseClass.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                new AddMBeanRegistrationCodeTransformer(w, objectName, constructor).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    
    public static void generateStdMBeanRegistration(JavaSource baseClass,
            final String objectName, final String itfName,
            final ExecutableElement constructor) throws IOException {
        baseClass.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                new AddStandardMBeanRegistrationCodeTransformer(w, objectName, itfName, constructor).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    public static void addCommentSurroundedByWhiteLine(TreeMaker treeMaker,
            Tree t, Comment.Style style, String content) {
        Comment commentEmptyLine = Comment.create(Comment.Style.WHITESPACE, -1, -1, -1, "\n\n");// NOI18N
        treeMaker.insertComment(t, commentEmptyLine, -1, true);
        addComment(treeMaker, t, style, content);  
        Comment commentEmptyLine2 = Comment.create(Comment.Style.WHITESPACE, -1, -1, -1, "\n\n");// NOI18N
        treeMaker.insertComment(t, commentEmptyLine2, -1, true);
    }
    public static void addCommentPrefixedByWhiteLine(TreeMaker treeMaker,
            Tree t, Comment.Style style, String content) {
        Comment commentEmptyLine = Comment.create(Comment.Style.WHITESPACE, -1, -1, -1, "\n\n");// NOI18N
        treeMaker.insertComment(t, commentEmptyLine, -1, true);
        addComment(treeMaker, t, style, content);
    }
    public static void addCommentFollowedByWhiteLine(TreeMaker treeMaker,
            Tree t, Comment.Style style, String content) {
        addComment(treeMaker, t, style, content);
        Comment commentEmptyLine = Comment.create(Comment.Style.WHITESPACE, -1, -1, -1, "\n\n");// NOI18N
        treeMaker.insertComment(t, commentEmptyLine, -1, true);
    }
    
    public static void addComment(TreeMaker treeMaker,
            Tree t, Comment.Style style, String content) {
        Comment comment = Comment.create(style, -1, -1, -1, content);
        treeMaker.insertComment(t, comment, -1, true);
    }
    
    public static void addAttributesToMBean(final JavaSource baseClass, final MBeanAttribute[] attributes) throws IOException {
        baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement itfType = getManagementInterface(baseClass);
                
                FileObject sourceFile = SourceUtils.getFile(itfType, parameter.getClasspathInfo());
                JavaSource itfSource = null;
                if(sourceFile != null)
                    itfSource = baseClass.forFileObject(sourceFile);
                
                addAttributes(baseClass, attributes);
                addAttributes(itfSource, attributes);
            }
        }, true);
        
    }
    
    public static boolean canUpdateAttributesOrOperations(JavaSource baseClass) throws IOException {
        return getManagementInterface(baseClass) != null;
    }
    
    public static void addOperationsToMBean(final JavaSource baseClass, final MBeanOperation[] operations) throws IOException {
         baseClass.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement itfType = isStandardMBean(baseClass);
                if(itfType == null)
                    itfType = isMXBean(baseClass);
                if(itfType == null)
                    throw new IOException("Invalid MBean class " + baseClass);// NOI18N
                
                FileObject sourceFile = SourceUtils.getFile(itfType, parameter.getClasspathInfo());
                JavaSource itfSource = null;
                if(sourceFile != null)
                    itfSource = baseClass.forFileObject(sourceFile);
                
                addOperations(baseClass, operations);
                addOperations(itfSource, operations);
            }
        }, true);
    }
    
    public static void updateStandardMBeanTemplate(JavaSource baseClass, JavaSource itf, final MBeanDO mbean) throws IOException {
        MBeanAttribute[] attributes = new MBeanAttribute[mbean.getAttributes().size()];
        attributes = mbean.getAttributes().toArray(attributes);
        MBeanOperation[] operations = new MBeanOperation[mbean.getOperations().size()];
        operations = mbean.getOperations().toArray(operations);
        
        //Update description
        updateClassJavaDoc(baseClass, mbean.getDescription());
        updateClassJavaDoc(itf, mbean.getDescription());
        addAttributes(baseClass,attributes);
        addAttributes(itf, attributes);
        addOperations(baseClass, operations);
        addOperations(itf, operations);
    }
    
    public static void updateExtendedStandardMBeanTemplate(JavaSource baseClass, JavaSource itf, final MBeanDO mbean) throws IOException {
        MBeanAttribute[] attributes = new MBeanAttribute[mbean.getAttributes().size()];
        attributes = mbean.getAttributes().toArray(attributes);
        MBeanOperation[] operations = new MBeanOperation[mbean.getOperations().size()];
        operations = mbean.getOperations().toArray(operations);
        if(mbean.isWrapppedClass()) {
            // Remove constructor
           removeMethod(baseClass, "<init>");// NOI18N
        }
        
        // Update Template (Metadata + description methods)
        baseClass.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                new UpdateExtendedStandardMBeanTemplateTransformer(w, mbean).scan(w.getCompilationUnit(), null);
            }
        }).commit();
        
        //Update Interface description
        updateClassJavaDoc(itf, mbean.getDescription());
        
        // Update Attributes and Operations
        addAttributes(baseClass,attributes);
        addAttributes(itf, attributes);
        addOperations(baseClass, operations);
        addOperations(itf, operations);
    }
    private static void updateClassJavaDoc(JavaSource clazz, final String text) throws IOException {
        clazz.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                new UpdateClassJavaDocTransformer(w, text).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    
    public static void updateDynamicMBeanTemplate(JavaSource template, final MBeanDO mbean) throws IOException {
        template.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {}
            public void run(WorkingCopy w) throws IOException {
                w.toPhase(Phase.ELEMENTS_RESOLVED);
                new UpdateDynamicMBeanTemplateTransformer(w, mbean).scan(w.getCompilationUnit(), null);
            }
        }).commit();    //Commit the changes into document;
    }
    
    // XXX REVISIT to be private
    public static void rewriteMethod(String text, WorkingCopy workingCopy, MethodTree method) {
        TreeMaker make = workingCopy.getTreeMaker();
        MethodTree modified = make.Method(
                method.getModifiers(), // copy original values
                method.getName(),
                method.getReturnType(),
                method.getTypeParameters(),
                method.getParameters(),
                method.getThrows(),
                text, // replace body with the new text
                null // not applicable here
                );
        // rewrite the original modifiers with the new one:
        workingCopy.rewrite(method, modified);
    }
    
    public static boolean isInterface(JavaSource js) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new IsInterfaceVisitor(parameter, value).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (Boolean) value.getValue();
    }
    
    public static boolean isMain(ExecutableElement method) {
        //has "main" name
        if (!"main".contentEquals(method.getSimpleName())) {// NOI18N
            return false;
        }
        //is public static?
        Set<Modifier> modifiers = method.getModifiers();
        if (!modifiers.contains(Modifier.STATIC) || !modifiers.contains(Modifier.PUBLIC)) {
            return false;
        }
        //has String[] arg?
        List<? extends VariableElement> params = method.getParameters();
        //Has single arg
        if (params.size() != 1) {
            return false;
        }
        //Is arg an array?
        TypeMirror type = params.get(0).asType();
        if (type.getKind() != TypeKind.ARRAY) {
            return false;
        }
        //Is it an array of declared type?
        type = ((ArrayType) type).getComponentType();
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        //Is it an array of String?
        if (!"java.lang.String".contentEquals(((TypeElement)((DeclaredType)type).asElement()).getQualifiedName())) {// NOI18N
            return false;
        }
        return true;
    }
    
    public static boolean isAbstract(JavaSource js) throws IOException {
        final ValueHolder value = new ValueHolder();
        // Check if the Agent has getMBeanServer method and a method named init
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                new HasClassModifierVisitor(parameter, value, Modifier.ABSTRACT).scan(parameter.getCompilationUnit(), null);
            }
        }, true);
        return (Boolean) value.getValue();
    }
    
    public static void removeMethod(JavaSource js, final String name) throws IOException {
        js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy w) throws Exception {
                w.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = w.getCompilationUnit();
                TreeMaker make = w.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        List<? extends Tree> members = clazz.getMembers();
                        for(Tree t : members) {
                            if(t instanceof MethodTree) {
                                MethodTree mt = (MethodTree) t;
                                String methodName = mt.getName().toString();
                                if(name.equals(methodName)){
                                    TreeMaker treeMaker = w.getTreeMaker();
                                    //Create a new class tree without main method
                                    ClassTree newTree = treeMaker.removeClassMember(clazz, mt);
                                    //do the change
                                    w.rewrite(clazz, newTree);
                                }
                            }
                        }
                    }
                }
            }
            
            public void cancel() {
                //Not important for userActionTasks
            }
        }).commit();    //Commit the changes into document
    }
    
    public static void generateAgent(JavaSource js, final boolean removeMainMethod, final boolean removeSampleCode) throws IOException {
        //Perform an action which changes the content of the java file
        js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy w) throws Exception {
                w.toPhase(JavaSource.Phase.RESOLVED);
                //Visitor for scanning javac's trees
                AgentGeneratorSampleCodeTransformer gen =
                        new AgentGeneratorSampleCodeTransformer(w, removeSampleCode);
                //execute the visitor on the root (CompilationUnitTree) with no parameter (null)
                gen.scan(w.getCompilationUnit(), null);
            }
            public void cancel() {
                //Not important for userActionTasks
            }
        }).commit();    //Commit the changes into document
        
        if(removeMainMethod)
            removeMethod(js, "main");// NOI18N
    }
    
    public static TypeMirror getComponentType(TypeMirror array) {
        if(!(array instanceof ArrayType))
            return array;
        else
            return getComponentType(((ArrayType)array).getComponentType());
    }
    public static int getArrayDimension(TypeMirror array) {
        if(!(array instanceof ArrayType))
            return 0;
        else
            return getArrayDimension(((ArrayType)array).getComponentType()) + 1;
    }
    public static String getTypeName(TypeMirror type, 
            List<? extends TypeParameterElement> methodParameterTypes, 
            List<? extends TypeParameterElement> classParameterTypes,
            CompilationInfo info) {
        
        String tName = type.toString();
        
        TypeMirror comp = JavaModelHelper.getComponentType(type);
        if(comp == null) throw new RuntimeException("Unknown Type : " + // NOI18N
                tName);
        if(comp.getKind().equals(TypeKind.TYPEVAR))
            tName = "java.lang.Object";// NOI18N
        
        return tName;
    }
    
    public static String getPrimitive(TypeKind type) {
        if(type.equals(TypeKind.VOID))
            return WizardConstants.VOID_NAME;
        
        if(type.equals(TypeKind.BOOLEAN))
            return WizardConstants.BOOLEAN_NAME;
        
        if(type.equals(TypeKind.BYTE))
            return WizardConstants.BYTE_NAME;
        
        if(type.equals(TypeKind.CHAR))
            return WizardConstants.CHAR_NAME;
        
        if(type.equals(TypeKind.INT))
            return WizardConstants.INT_NAME;
        
        if(type.equals(TypeKind.LONG))
            return WizardConstants.LONG_NAME;
        
        if(type.equals(TypeKind.FLOAT))
            return WizardConstants.FLOAT_NAME;
        
        if(type.equals(TypeKind.DOUBLE))
            return WizardConstants.DOUBLE_NAME;
        
        return null;
    }
    
    
    private static String getMethodBodyText(CompilationInfo info, ExecutableElement m) {
        MethodTree method = info.getTrees().getTree(m);
        BlockTree body = method.getBody();
        
        // get SourcePositions instance for your working copy and
        // fetch out start and end position.
        SourcePositions sp = info.getTrees().getSourcePositions();
        int start = (int) sp.getStartPosition(info.getCompilationUnit(), body);
        int end = (int) sp.getEndPosition(info.getCompilationUnit(), body);
        // get body text from source text
        String bodyText = info.getText().substring(start, end);
        return bodyText;
    }
    
    static String removeGeneric(String type) {
         int gen = type.indexOf("<");// NOI18N
         if(gen != -1)
            type = type.substring(0,gen);
         return type;
    }
    
    private static boolean isKnownTypeNoVoid(CompilationController ctrl, String type) {
        if(getTypeElement(ctrl, type) != null)
            return true;
        if(WizardHelpers.isPrimitiveType(type) ||
           WizardHelpers.isStandardWrapperType(type))
            return true;
        if(getArrayType(ctrl, type) != null)
            return true;
        return false;
        
    }
    
    private static boolean isKnownType(CompilationController ctrl, String type) {
        if(type == null) return false;
        if(isKnownTypeNoVoid(ctrl, type)) return true;
        
        return "void".equals(type);// NOI18N
    }
    
    private static TypeElement getTypeElement(CompilationController ctrl, String type) {
        TypeElement retTypeElement = null;
        if(type == null) return null;
        type = removeGeneric(type);
        retTypeElement = ctrl.getElements().getTypeElement(type);
       // System.out.println("retTypeElement " + retTypeElement);
        return retTypeElement;
    }
    
    private static ExpressionTree getType(WorkingCopy w, String type, TypeMirror mirror) {
      //  System.out.println("TYPE " + type + " MIRROR " + mirror);
        if(type == null && mirror == null)
            return null;
        TypeElement retTypeElement = null;
        
        /*if (mirror != null) {
            Object obj = w.getTypes().asElement(mirror);
            if (obj instanceof TypeElement) {
                retTypeElement = (TypeElement) obj;
            } else {
                String primitive = getPrimitive(mirror.getKind());
                if(primitive == null)
                    return w.getTreeMaker().Identifier("Object"); // NOI18N
                else
                   return w.getTreeMaker().Identifier(primitive);
            }
            if (retTypeElement != null) {
                return w.getTreeMaker().QualIdent(retTypeElement);
            }
        }
        */
        
        if(type != null) {
            if(retTypeElement == null)
                retTypeElement = getTypeElement(w, type);          
            if(retTypeElement == null)
                retTypeElement = w.getElements().getTypeElement(type);
            if(retTypeElement != null)
                return w.getTreeMaker().QualIdent(retTypeElement);
            ExpressionTree arrayType = getArrayTree(w, type);
            if(arrayType != null)
                return arrayType;
            if(WizardHelpers.isPrimitiveType(type) ||
                    WizardHelpers.isStandardWrapperType(type) || "void".equals(type))// NOI18N
                return w.getTreeMaker().Identifier(type);
            
            // XXX REVISIT, THIS SHOULD NEVER HAPPEN IF THEN INPUT WERE CHECKED
            return w.getTreeMaker().Identifier(type);
        }
        
        throw new IllegalArgumentException("Unknown type " + type);// NOI18N
    }
    
    private static Object getArrayType(CompilationController ctrl, String type) {
        //System.out.println("getArrayType " + type);
        TypeMirror ret = null;
        int length = type.length();
        int current = type.indexOf("[]");// NOI18N
        if(current == -1) return null;
        String componentType = type.substring(0, current);
        //System.out.println("COmnent type " + componentType);
        if(WizardHelpers.isPrimitiveType(componentType) ||
           WizardHelpers.isStandardWrapperType(componentType))
            return componentType;
        ret = ctrl.getElements().getTypeElement(componentType).asType();
        if(ret == null) return null;
        String arrays = type.substring(current, type.length());
        int dim = arrays.length() / 2;
        for(int i = 0; i < dim; i++) {
            ret = ctrl.getTypes().getArrayType(ret);
        }
        return ret;
    }
    
    public static ExpressionTree getArrayTree(WorkingCopy w, 
            String type) {
        String componentType = null;
        Object o = getArrayType(w, type);
        if(o instanceof String)
            return w.getTreeMaker().Identifier(type);
        if(o instanceof TypeMirror)
            return (ExpressionTree) w.getTreeMaker().Type((TypeMirror)o);
        return null;
    }
    
    private static void updateDescription(WorkingCopy ctrl,
            Element clazzElement,
            TreeMaker make,
            String text, ClassTree clazz) {
        Doc doc = ctrl.getElementUtilities().javaDocFor(clazzElement);
        //doc.setRawCommentText("TEST IT TEST OY");
        String txt = doc.commentText();
        int i = txt.indexOf("{0}");// NOI18N
        //System.out.println( "JAVADOC TEXT : [" + txt + "]");
        MessageFormat formDoc = new MessageFormat(txt);
        Object[] args = new Object[] { text };
        //addComment(make, clazz, Comment.Style.JAVADOC, formDoc.format(args));
        //ctrl.rewriteInComment(i,3,text);
        //ctrl.getTreeMaker().removeComment(clazz, 1, true);
        //ctrl.rewrite(clazz,clazz);
    }
    
}
