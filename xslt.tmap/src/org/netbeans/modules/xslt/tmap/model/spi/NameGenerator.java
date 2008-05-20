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
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;

/**
 * 
 * @author Vitaly Bychkov
 */
public abstract class NameGenerator {

    abstract protected boolean isApplicable(TMapComponent component);

    abstract String getName(TMapComponent component, String namePrefix);

    abstract String getName(TMapComponent component);

    private static NameGenerator[] NAME_GENERATORS = new NameGenerator[] 
            {new ServiceNameGenerator(), 
             new InvokeNameGenerator(), 
             new TransformNameGenerator()};
    public static NameGenerator getDefault(TMapComponent component) {
        for (NameGenerator nameGenerator : NAME_GENERATORS) {
            if (nameGenerator.isApplicable(component)) {
                return nameGenerator;
            }
        }
        return null;
    }

    public static String getUniqueName(TMapComponent component) {
        NameGenerator generator = getDefault(component);
        if (generator != null) {
        }
        return generator != null ? generator.getName(component) : null;
    }
    
    private static class ServiceNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent component) {
            return component instanceof Service;
        }

        private String getCamelCase(String namePrefix) {
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
        
        @Override
        String getName( TMapComponent component, String namePrefix) {
            if (component == null || namePrefix == null || !isApplicable(component)) {
                return null;
            }

//            namePrefix = namePrefix.toLowerCase();
            namePrefix = getCamelCase(namePrefix);

            Service service = (Service) component;
            TMapModel model = service.getModel();
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
        String getName( TMapComponent component) {
            if (!isApplicable(component)) {
                return null;
            }
            return getName(component, ((Service) component).TYPE.getTagName());
        }
    }

    private static class InvokeNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent component) {
            return component instanceof Invoke;
        }

        @Override
        String getName( TMapComponent component, String namePrefix) {
            if (component == null || namePrefix == null || !isApplicable(component)) {
                return null;
            }

            namePrefix = namePrefix.toLowerCase();

            Invoke invoke = (Invoke) component;
            TMapModel model = invoke.getModel();
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

        @Override
        String getName( TMapComponent component) {
            if (!isApplicable(component)) {
                return null;
            }
            return getName(component, ((Invoke) component).TYPE.getTagName());
        }
    }
    
    private static class TransformNameGenerator extends NameGenerator {

        @Override
        protected boolean isApplicable(TMapComponent component) {
            return component instanceof Transform;
        }

        @Override
        String getName( TMapComponent component, String namePrefix) {
            if (component == null || namePrefix == null || !isApplicable(component)) {
                return null;
            }

            namePrefix = namePrefix.toLowerCase();

            Transform transform = (Transform) component;
            TMapModel model = transform.getModel();
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
            int i = 0;
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
        String getName( TMapComponent component) {
            if (!isApplicable(component)) {
                return null;
            }
            return getName(component, ((Transform) component).TYPE.getTagName());
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
