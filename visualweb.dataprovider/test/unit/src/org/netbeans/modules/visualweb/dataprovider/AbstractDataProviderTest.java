/*
* {START_JAVA_COPYRIGHT_NOTICE
* Copyright 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
* Use is subject to license terms.
* END_COPYRIGHT_NOTICE}
*/

package org.netbeans.modules.visualweb.dataprovider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;


public class AbstractDataProviderTest extends NbTestCase {
    ArrayList dataTypes = new ArrayList();

    public AbstractDataProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dataTypes.add(Void.TYPE);
        dataTypes.add(Object[].class);
        dataTypes.add(String[].class);
        dataTypes.add(ArrayList.class);
        dataTypes.add(List.class);
        dataTypes.add(HashMap.class);
        dataTypes.add(Map.class);
        dataTypes.add(Boolean.TYPE);
        dataTypes.add(Character.TYPE);
        dataTypes.add(Byte.TYPE);
        dataTypes.add(Short.TYPE);
        dataTypes.add(Integer.TYPE);
        dataTypes.add(Long.TYPE);
        dataTypes.add(Float.TYPE);
        dataTypes.add(Double.TYPE);
        dataTypes.add(Boolean.class);
        dataTypes.add(Date.class);
        dataTypes.add(BigDecimal.class);
        dataTypes.add(BigInteger.class);
        dataTypes.add(Character.class);
        dataTypes.add(Byte.class);
        dataTypes.add(Short.class);
        dataTypes.add(Integer.class);
        dataTypes.add(Long.class);
        dataTypes.add(Float.class);
        dataTypes.add(Double.class);
        dataTypes.add(String.class);
        dataTypes.add(TestBean.class);
        dataTypes.add(AbstractDataProviderTest.class);
        dataTypes.add(IllegalArgumentException.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFakeDataTypes() {
//        for (int i = 0; i < dataTypes.size(); i++) {
//            Class c = (Class)dataTypes.get(i);
//            System.out.println(c.getName() + " --> " +
//                AbstractDataProvider.getFakeData(c, String.class));
//        }
    }

    public void testFakeTestBean() {
//        Object o = AbstractDataProvider.getFakeData(TestBean.class);
//        System.out.println(o);
//        try {
//            BeanInfo bi = Introspector.getBeanInfo(o.getClass());
//            PropertyDescriptor[] props = bi.getPropertyDescriptors();
//            for (int i = 0; i < props.length; i++) {
//                if (props[i].getReadMethod() != null) {
//                    System.out.print(props[i].getName() + " [" +
//                        props[i].getPropertyType() + "] --> ");
//                    Object data = props[i].getReadMethod().invoke(o, null);
//                    if (data != null && data.getClass().isArray()) {
//                        System.out.println(data);
//                        for (int a = 0; a < Array.getLength(data); a++) {
//                            System.out.println("  " + Array.get(data, a));
//                        }
//                    } else if (data instanceof Object[]) {
//                        System.out.println(data);
//                        Object[] adata = (Object[])data;
//                        for (int a = 0; a < adata.length; a++) {
//                            System.out.println("  " + adata[a]);
//                        }
//                    }
//                    else {
//                        System.out.println(data);
//                    }
//                }
//            }
//        } catch (Exception x) {
//            x.printStackTrace();
//        }
    }
}
