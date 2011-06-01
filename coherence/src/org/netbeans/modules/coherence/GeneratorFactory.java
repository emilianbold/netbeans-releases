/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import org.netbeans.modules.coherence.generators.Constants;
import org.netbeans.modules.coherence.generators.Implements;
import org.netbeans.modules.coherence.generators.ReadExternal;
import org.netbeans.modules.coherence.generators.WriteExternal;
import org.netbeans.modules.coherence.generators.impl.ConstantsImpl;
import org.netbeans.modules.coherence.generators.impl.ImplementsImpl;
import org.netbeans.modules.coherence.generators.impl.ReadExternalImpl;
import org.netbeans.modules.coherence.generators.impl.WriteExternalImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class GeneratorFactory {

    private static final Logger logger = Logger.getLogger(GeneratorFactory.class.getCanonicalName());
    private static GeneratorFactory instance = null;

    private GeneratorFactory() {
    }

    public static synchronized GeneratorFactory getInstance() {
        if (instance == null) {
            instance = new GeneratorFactory();
        }
        return instance;
    }

    public static synchronized GeneratorFactory newGeneratorFactory() {
        return new GeneratorFactory();
    }

    // Getters for new Functionality.
    public Constants newConstants() {
        return new ConstantsImpl(this);
    }

    public Implements newImplements() {
        return new ImplementsImpl(this);
    }

    public ReadExternal newReadExternal() {
        return new ReadExternalImpl(this);
    }

    public WriteExternal newWriteExternal() {
        return new WriteExternalImpl(this);
    }
    // Commmon Methods

    /*
     * The following methods are used to test the class / interface associated with
     * the currently processing object.
     */
    // Check if the Class is a String type
    public final static String STRING_CLASS_NAME = String.class.getCanonicalName();

    public boolean isString(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(STRING_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a BigDecimal
    public final static String BIGDECIMAL_CLASS_NAME = BigDecimal.class.getCanonicalName();

    public boolean isBigDecimal(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(BIGDECIMAL_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a BigInteger
    public final static String BIGINTEGER_CLASS_NAME = BigInteger.class.getCanonicalName();

    public boolean isBigInteger(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(BIGINTEGER_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a Date
    public final static String DATE_CLASS_NAME = Date.class.getCanonicalName();

    public boolean isDate(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(DATE_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a SQL Date
    public final static String SQLDATE_CLASS_NAME = java.sql.Date.class.getCanonicalName();

    public boolean isSQLDate(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(SQLDATE_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a Binary
    public final static String BINARY_CLASS_NAME = "com.tangosol.util.Binary";

    public boolean isBinary(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(BINARY_CLASS_NAME);
        }
        return is;
    }
    // Check if the class implements the Map interface
    public final static String MAP_CLASS_NAME = Map.class.getCanonicalName();

    public boolean isMap(VariableElement ve) {
        boolean is = false;

        if (ve != null) {
            is = ve.asType().toString().contains(MAP_CLASS_NAME);
            if (!is) {
                String className = ve.asType().toString();
                int genericsPos = className.indexOf("<");
                if (genericsPos > 0) {
                    className = ve.asType().toString().substring(0, genericsPos);
                }
                try {
                    Class theClass = Class.forName(className);
                    is = isMap(theClass);
                } catch (ClassNotFoundException e) {
                }
            }
        }

        return is;
    }

    public boolean isMap(Class theClass) {
        boolean isMap = false;
        if (theClass != null) {
            Class[] classes = theClass.getInterfaces();
            for (Class oneClass : classes) {
                isMap = MAP_CLASS_NAME.equals(oneClass.getCanonicalName());
                if (isMap) {
                    break;
                }
            }
            if (!isMap) {
                isMap = isMap(theClass.getSuperclass());
            }
        }
        return isMap;
    }
    // check if the class implements Collection
    public final static String COLLECTION_CLASS_NAME = Collection.class.getCanonicalName();

    public boolean isCollection(VariableElement ve) {
        boolean is = false;

        if (ve != null) {
            is = ve.asType().toString().contains(COLLECTION_CLASS_NAME);
            if (!is) {
                String className = ve.asType().toString();
                int genericsPos = className.indexOf("<");
                if (genericsPos > 0) {
                    className = ve.asType().toString().substring(0, genericsPos);
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "*** APH-I4 : isCollection() className {0} Full {1}", new Object[]{className, ve.asType().toString()});
                }
                try {
                    Class theClass = Class.forName(className);
                    is = isCollection(theClass);
                } catch (ClassNotFoundException e) {
                }
            }
        }

        return is;
    }

    public boolean isCollection(Class theClass) {
        boolean isCollection = false;
        if (theClass != null) {
            Class[] classes = theClass.getInterfaces();
            for (Class oneClass : classes) {
                isCollection = COLLECTION_CLASS_NAME.equals(oneClass.getCanonicalName());
                if (isCollection) {
                    break;
                }
            }
            if (!isCollection) {
                isCollection = isCollection(theClass.getSuperclass());
            }
        }
        return isCollection;
    }

    /**
     * Check if the enclose VariableElement is Serializeable.
     * That is to say not final, transient, etc.
     * @param enclosedElement
     * @return
     */
    public boolean isSerializable(VariableElement enclosedElement) {
        boolean isSerializable = true;

        Set<Modifier> modifiers = (enclosedElement).getModifiers();
        Iterator<Modifier> itr = modifiers.iterator();
        String name = "";

        while (itr.hasNext()) {
            name = itr.next().name();
            if ("TRANSIENT".equalsIgnoreCase(name)
                    || "FINAL".equalsIgnoreCase(name)) {
                isSerializable = false;
                break;
            }
        }

        return isSerializable;
    }

    public String getInitialisedType(WorkingCopy workingCopy, String fieldName) {
        String initType = null;

        try {
            Document document = workingCopy.getDocument();
            String lines[] = document.getText(0, document.getLength()).split(";");
            int namePos = 0;
            int equalsPos = 0;
            int newPos = 0;
            int parenPos = 0;
            String line = null;

            for (int i = 0; i < lines.length; i++) {
                line = lines[i];
                if ((namePos = line.indexOf(fieldName)) >= 0) {
                    if ((equalsPos = line.indexOf("=", namePos)) >= 0) {
                        if ((newPos = line.indexOf("new", equalsPos)) > 0) {
                            if ((parenPos = line.indexOf("(", newPos)) >= 0) {
                                initType = line.substring(newPos + 4, parenPos);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return initType;
    }

    /**
     * Simply Take the String and Capitalise the first character.
     * @param s
     * @return
     */
    public String initCaps(String s) {
        if (s == null) {
            return s;
        } else {
            return s.toUpperCase().substring(0, 1) + s.toLowerCase().substring(1);
        }
    }

    public String generateConstantName(String s) {
        if (s == null) {
            return s;
        } else {
            return s.trim().toUpperCase() + "_IDX";
        }
    }
}
