/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.model.spi;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.Variable;

/**
 * 
 * @author Vitaly Bychkov
 */
public abstract class NameGenerator {
    public static String DEFAULT_VARIABLE_PREFIX = "var"; // NOI18N
    public static final String INPUT_OPERATION_VARIABLE_PREFIX = "inOpVar"; // NOI18N
    public static final String OUTPUT_OPERATION_VARIABLE_PREFIX = "outOpVar"; // NOI18N
    public static final String INPUT_INVOKE_VARIABLE_PREFIX = "inInvokeVar"; // NOI18N
    public static final String OUTPUT_INVOKE_VARIABLE_PREFIX = "outInvokeVar"; // NOI18N
        

    abstract protected boolean isApplicable(TMapComponent parent, Class childType);

    abstract protected boolean isUniqueName(TMapComponent parent, String name);

    public abstract String getName(TMapComponent parent, String namePrefix);

    public abstract String getName(TMapComponent parent);

    private static NameGenerator[] NAME_GENERATORS = new NameGenerator[] 
            {new ServiceNameGenerator(), 
             new InvokeNameGenerator(), 
             new TransformNameGenerator(),
             new ParamNameGenerator(),
             new VariableNameGenerator()};
//    public static NameGenerator getDefault(TMapComponent component) {
//        for (NameGenerator nameGenerator : NAME_GENERATORS) {
//            if (nameGenerator.isApplicable(component)) {
//                return nameGenerator;
//            }
//        }
//        return null;
//    }

    public static NameGenerator getDefault(TMapComponent parent, Class childType) {
        for (NameGenerator nameGenerator : NAME_GENERATORS) {
            if (nameGenerator.isApplicable(parent, childType)) {
                return nameGenerator;
            }
        }
        return null;
    }

    public static boolean isUniqueName(TMapComponent parent, Class childType, String name) {
        NameGenerator generator = getDefault(parent, childType);
        return generator != null ? generator.isUniqueName(parent, name) : null;
    }
    
    public static String getUniqueName(TMapComponent parent, Class childType) {
        NameGenerator generator = getDefault(parent, childType);
        return generator != null ? generator.getName(parent) : null;
    }
    
    private static String getCamelCase(String namePrefix) {
        assert namePrefix != null;
        if (namePrefix.length() <= 0) {
            return namePrefix;
        }
        String lowerPart = namePrefix.substring(1);
        lowerPart = lowerPart.toLowerCase();

        char firstChar = namePrefix.charAt(0);
        if (!Character.isUpperCase(firstChar)) {
            firstChar = Character.toUpperCase(firstChar);
        }

        return Character.toString(firstChar) + lowerPart;
    }


    private static class ServiceNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent parent, Class childType) {
            return childType == Service.class;
        }

        @Override
        public String getName(TMapComponent component, String namePrefix) {
            if (component == null || namePrefix == null || !isApplicable(component, Service.class)) {
                return null;
            }

            namePrefix = getCamelCase(namePrefix);

            TMapModel model = component.getModel();
            if (model == null) {
                return null;
            }

            TransformMap tMap = model.getTransformMap();
            if (tMap == null) {
                return null;
            }

            List<Service> services = tMap.getServices();
            if (services == null) {
                return null;
            }

            String uniqueName = null;
            boolean isUnique = false;
            int i = 1;
            while (uniqueName == null) {
                uniqueName = namePrefix + i;
                for (Service tmpService : services) {
                    if (uniqueName.equalsIgnoreCase(tmpService.getName())) {
                        i++;
                        uniqueName = null;
                        break;
                    }
                }
            }
            return uniqueName;
        }

        @Override
        public String getName( TMapComponent parent) {
            if (!isApplicable(parent, Service.class)) {
                return null;
            }
            return getName(parent, Service.TYPE.getTagName());
        }

        @Override
        protected boolean isUniqueName(TMapComponent component, String name) {
            if (component == null || name == null || !isApplicable(component, Service.class)) {
                return false;
            }

            TMapModel model = component.getModel();
            if (model == null) {
                return false;
            }

            boolean isUnique = true;
            TransformMap tMap = model.getTransformMap();
            if (tMap == null) {
                return isUnique;
            }

            List<Service> services = tMap.getServices();
            if (services == null || services.size() == 0) {
                return isUnique;
            }

            for (Service tmpService : services) {
                if (tmpService == null) {
                    continue;
                }
                if (name.equals(tmpService.getName())) {
                    isUnique = false;
                    break;
                }
            }
            return isUnique;
        }
    }

    private static class InvokeNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent parent, Class childType) {
            return childType == Invoke.class;
        }

        @Override
        public String getName( TMapComponent parent, String namePrefix) {
            if (parent == null || namePrefix == null || !isApplicable(parent, Invoke.class)) {
                return null;
            }

//            namePrefix = namePrefix.toLowerCase();
            namePrefix = getCamelCase(namePrefix);

            TMapModel model = parent.getModel();
            if (model == null) {
                return null;
            }

            TransformMap tMap = model.getTransformMap();
            if (tMap == null) {
                return null;
            }

            List<Service> services = tMap.getServices();
            if (services == null) {
                return null;
            }

            List<Invoke> invokes = new ArrayList<Invoke>();
            for (Service tmpService : services) {
                if (tmpService == null) {
                    continue;
                }
                List<Operation> ops = tmpService.getOperations();
                if (ops == null) {
                    continue;
                }
                for (Operation op : ops) {
                    if (op == null) {
                        continue;
                    }
                    invokes.addAll(op.getInvokes());
                    invokes.addAll(getOpInternalInvokes(op));
                }
            }

            String uniqueName = null;
            boolean isUnique = false;
            int i = 1;
            while (uniqueName == null) {
                uniqueName = namePrefix + i;
                for (Invoke tmpInvoke : invokes) {
                    if (uniqueName.equalsIgnoreCase(tmpInvoke.getName())) {
                        i++;
                        uniqueName = null;
                        break;
                    }
                }
            }
            return uniqueName;
        }

        private List<Invoke> getOpInternalInvokes(Operation op) {
            List<Invoke> invokes = new ArrayList<Invoke>();
            if (op == null) {
                return invokes;
            }
            
            List<Transform> trs = op.getTransforms();
            if (trs == null) {
                return invokes;
            }
            for (Transform tr : trs) {
                if (tr == null) {
                    continue;
                }
                invokes.addAll(tr.getInvokes());
            }
            return invokes;
        }
        
        @Override
        public String getName( TMapComponent parent) {
            if (!isApplicable(parent, Invoke.class)) {
                return null;
            }
            return getName(parent, Invoke.TYPE.getTagName());
        }

        @Override
        protected boolean isUniqueName(TMapComponent component, String name) {
            if (component == null || name == null || !isApplicable(component, Invoke.class)) {
                return false;
            }

            TMapModel model = component.getModel();
            if (model == null) {
                return false;
            }

            boolean isUnique = true;
            TransformMap tMap = model.getTransformMap();
            if (tMap == null) {
                return isUnique;
            }

            List<Service> services = tMap.getServices();
            if (services == null || services.size() == 0) {
                return isUnique;
            }

            List<Invoke> invokes = new ArrayList<Invoke>();
            for (Service tmpService : services) {
                if (tmpService == null) {
                    continue;
                }
                List<Operation> ops = tmpService.getOperations();
                if (ops == null) {
                    continue;
                }
                for (Operation op : ops) {
                    if (op == null) {
                        continue;
                    }
                    invokes.addAll(op.getInvokes());
                }
            }

            for (Invoke tmpInvoke : invokes) {
                if (tmpInvoke == null) {
                    continue;
                }
                    
                if (name.equals(tmpInvoke.getName())) {
                    isUnique = false;
                    break;
                }
            }

            return isUnique;
         }
    }
    
    private static class TransformNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent parent, Class childType) {
            return childType == Transform.class;
        }

        @Override
        public String getName( TMapComponent parent, String namePrefix) {
            if (parent == null || namePrefix == null || !isApplicable(parent, Transform.class)) {
                return null;
            }

//            namePrefix = namePrefix.toLowerCase();
            namePrefix = getCamelCase(namePrefix);

            TMapModel model = parent.getModel();
            if (model == null) {
                return null;
            }

            TransformMap tMap = model.getTransformMap();
            if (tMap == null) {
                return null;
            }

            List<Service> services = tMap.getServices();
            if (services == null) {
                return null;
            }

            List<Transform> transforms = new ArrayList<Transform>();
            for (Service tmpService : services) {
                if (tmpService == null) {
                    continue;
                }
                List<Operation> ops = tmpService.getOperations();
                if (ops == null) {
                    continue;
                }
                for (Operation op : ops) {
                    if (op == null) {
                        continue;
                    }
                    transforms.addAll(op.getTransforms());
                }
            }

            String uniqueName = null;
            boolean isUnique = false;
            int i = 1;
            while (uniqueName == null) {
                uniqueName = namePrefix + i;
                for (Transform tmpTransform : transforms) {
                    if (uniqueName.equalsIgnoreCase(tmpTransform.getName())) {
                        i++;
                        uniqueName = null;
                        break;
                    }
                }
            }
            return uniqueName;
        }

        @Override
        public String getName( TMapComponent parent) {
            if (!isApplicable(parent, Transform.class)) {
                return null;
            }
            return getName(parent, Transform.TYPE.getTagName());
        }

        @Override
        protected boolean isUniqueName(TMapComponent component, String name) {
            if (component == null || name == null || !isApplicable(component, Transform.class)) {
                return false;
            }

            TMapModel model = component.getModel();
            if (model == null) {
                return false;
            }

            boolean isUnique = true;
            TransformMap tMap = model.getTransformMap();
            if (tMap == null) {
                return isUnique;
            }

            List<Service> services = tMap.getServices();
            if (services == null) {
                return isUnique;
            }

            List<Transform> transforms = new ArrayList<Transform>();
            for (Service tmpService : services) {
                if (tmpService == null) {
                    continue;
                }
                List<Operation> ops = tmpService.getOperations();
                if (ops == null) {
                    continue;
                }
                for (Operation op : ops) {
                    if (op == null) {
                        continue;
                    }
                    transforms.addAll(op.getTransforms());
                }
            }

            for (Transform tmpTransform : transforms) {
                if (tmpTransform == null) {
                    continue;
                }
                
                if (name.equals(tmpTransform.getName())) {
                    isUnique = false;
                    break;
                }
            }
            return isUnique;
        }
    }    

    private static class ParamNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent parent, Class childType) {
            return childType == Param.class && parent instanceof Transform;
        }

        @Override
        public String getName( TMapComponent parent, String namePrefix) {
            if (parent == null || namePrefix == null || !isApplicable(parent, Param.class)) {
                return null;
            }

//            namePrefix = namePrefix.toLowerCase();
            namePrefix = getCamelCase(namePrefix);

            Transform transform = (Transform)parent;

            List<Param> params = transform.getParams();
            if (params == null) {
                return null;
            }

            String uniqueName = null;
            boolean isUnique = false;
            int i = 1;
            while (uniqueName == null) {
                uniqueName = namePrefix + i;
                for (Param tmpParam : params) {
                    if (uniqueName.equalsIgnoreCase(tmpParam.getName())) {
                        i++;
                        uniqueName = null;
                        break;
                    }
                }
            }
            return uniqueName;
        }

        @Override
        public String getName( TMapComponent parent) {
            if (!isApplicable(parent, Param.class)) {
                return null;
            }
            return getName(parent, Param.EL_TYPE.getTagName());
        }

        @Override
        protected boolean isUniqueName(TMapComponent component, String name) {
            if (component == null || name == null || !isApplicable(component, Param.class)) {
                return false;
            }

            Transform transform = (Transform)component;

            boolean isUnique = true;
            List<Param> params = transform.getParams();
            if (params == null) {
                return isUnique;
            }

            for (Param tmpParam : params) {
                if (tmpParam == null) {
                    continue;
                }
                
                if (name.equals(tmpParam.getName())) {
                    isUnique = false;
                    break;
                }
            }
            return isUnique;
        }
    }    
    
    private static class VariableNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent parent, Class childType) {
            return childType == Variable.class && (parent == null || parent instanceof Operation);
        }

        @Override
        public String getName( TMapComponent parent, String namePrefix) {
            if (namePrefix == null || !isApplicable(parent, Variable.class)) {
                return null;
            }

            Operation operation = (Operation)parent;
            return getVariableName(namePrefix, getVariableNumber(operation, namePrefix, 1));
        }

        private int getVariableNumber(
                org.netbeans.modules.xslt.tmap.model.api.Operation operation, 
                String varNamePrefix, int startNumber) 
        {
            if (operation == null || varNamePrefix == null || !operation.isInDocumentModel()) {
                return startNumber;
            }

            List<Variable> vars = operation.getVariables();
            if (vars == null || vars.size() < 1) {
            }

            int count = startNumber;
            List<String> varNames = new ArrayList<String>();

            for (Variable var : vars) {
                String tmpVarName = var == null ? null : var.getName();
                if (tmpVarName != null) {
                    varNames.add(tmpVarName);
                }
            }

            while (true) {
                if (!varNames.contains(varNamePrefix + count)) {
                    break;
                }
                count++;
            }
            return count;
        }

        private String getVariableName(String varPrefix, int varNumber) {
            varPrefix = varPrefix == null ? DEFAULT_VARIABLE_PREFIX : varPrefix;
            return varPrefix + varNumber;
        }

        @Override
        public String getName( TMapComponent parent) {
            if (!isApplicable(parent, Variable.class)) {
                return null;
            }
            return getName(parent, DEFAULT_VARIABLE_PREFIX);
        }

        @Override
        protected boolean isUniqueName(TMapComponent parent, String name) {
            if (parent == null || name == null || !isApplicable(parent, Variable.class)) {
                return false;
            }

            Operation operation = (Operation)parent;

            boolean isUnique = true;
            List<Variable> vars = operation.getVariables();
            if (vars == null || vars.size() < 1) {
                return isUnique;
            }

            List<String> varNames = new ArrayList<String>();

            for (Variable var : vars) {
                if (var == null) {
                    continue;
                }
                if (name.equals(var.getName())) {
                    isUnique = false;
                    break;
                }
            }

            return isUnique;
        }
    }    

//        private void calculateNewIndex( BpelEntity component,
//            String lowerCaseName , IntWrapper wrapper  )
//    {
//        int index = wrapper.get();
//        if (component instanceof NamedElement) {
//            String name = ((NamedElement) component).getName();
//            if (name != null) {
//                name = name.toLowerCase();
//                if (name.startsWith(lowerCaseName)) {
//                    String postfix = name.substring(lowerCaseName.length());
//                    try {
//                        Integer number = Integer.parseInt(postfix);
//                        if (number > index) {
//                            index = number;
//                        }
//                    }
//                    catch (NumberFormatException e) {
//                        // postfix is not a number - we don't need it.
//                    }
//                }
//            }
//        }
//        wrapper.set( index );
//        for (BpelEntity child : component.getChildren()) {
//            calculateNewIndex(child, lowerCaseName, wrapper );
//        }
//    }
//    
}
