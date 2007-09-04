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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import java.text.MessageFormat;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Comment;

/**
 * Generates the code needed for invoking a container-managed <code>EntityManager</code> 
 * in classes that don't support dependency injection.
 * 
 * @author Erno Mononen
 */
public final class ContainerManagedJTANonInjectableInWeb extends EntityManagerGenerationStrategySupport {
    
    public ClassTree generate() {
    
        FieldInfo em = getEntityManagerFieldInfo();
        
        ModifiersTree methodModifiers = getTreeMaker().Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>emptyList()
                );
        
        MethodTree newMethod = getTreeMaker().Method(
                methodModifiers, 
                computeMethodName(),
                getTreeMaker().PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                getParameterList(),
                Collections.<ExpressionTree>emptyList(),
                "{ " +
                getMethodBody(em)
                + "}",
                null
                );

        //TODO: should be added automatically, but accessing web / ejb apis from this module
        // would require API changes that might not be doable for 6.0
        String addToWebXmlComment =
                " Add this to the deployment descriptor of this module (e.g. web.xml, ejb-jar.xml):\n" +
                "<persistence-context-ref>\n" +
                "   <persistence-context-ref-name>persistence/LogicalName</persistence-context-ref-name>\n" +
                "   <persistence-unit-name>"+ getPersistenceUnitName() + "</persistence-unit-name>\n" +
                "</persistence-context-ref>\n" +
                "<resource-ref>\n" +
                "    <res-ref-name>UserTransaction</res-ref-name>\n" +
                "     <res-type>javax.transaction.UserTransaction</res-type>\n" +
                "     <res-auth>Container</res-auth>\n" +
                "</resource-ref>\n";

        
        getTreeMaker().addComment(newMethod.getBody().getStatements().get(0), 
                Comment.create(Comment.Style.BLOCK, 0, 0, 4, addToWebXmlComment), true);
        return getTreeMaker().addClassMember(getClassTree(), importFQNs(newMethod));
    }

    private String getMethodBody(FieldInfo em){
        String emInit = em.isExisting() ? "    {0}.joinTransaction();\n" :"    javax.persistence.EntityManager {0} =  (javax.persistence.EntityManager) ctx.lookup(\"java:comp/env/persistence/LogicalName\");\n";

        String text = 
                "try '{'\n" +
                "    javax.naming.Context ctx = new javax.naming.InitialContext();\n" +
                "    javax.transaction.UserTransaction utx = (javax.transaction.UserTransaction) ctx.lookup(\"java:comp/env/UserTransaction\");\n" +
                "    utx.begin();\n" +
                emInit +
                generateCallLines(em.getName()) +
                "    utx.commit();\n" +
                "} catch(Exception e) '{'\n" +
                "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
                "    throw new RuntimeException(e);\n" +
                "}";
        return MessageFormat.format(text, em.getName());
    }
}
