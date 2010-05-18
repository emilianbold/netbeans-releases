/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.visualweb.insync.faces;


import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.beans.BeanStructureScanner;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.java.MethodInfo;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.visualweb.insync.java.Statement;
import org.openide.util.NbBundle;

public class ThresherFacesBeanStructureScanner extends BeanStructureScanner {
    public static String PROP_INITMETHOD = "_init";
    public static String INITMETHOD = "init";
    public static String DESTROYMETHOD = "destroy";

    protected MethodInfo initInfo = new MethodInfo(INITMETHOD, Modifier.PUBLIC, Void.TYPE, 
            getInitMethodComment(), ENSURE_INITBLOCK);

    public ThresherFacesBeanStructureScanner(BeansUnit unit) {
        super(unit);
        ctorInfo.setComment(getConstructorComment());
        ctorInfo.setEnsureMethodName(null);
        destroyInfo = new MethodInfo(DESTROYMETHOD);
        destroyInfo.setComment(getDestroyMethodComment());
        propertiesInitInfo = new MethodInfo(PROP_INITMETHOD, Modifier.PRIVATE, Void.TYPE,
                getComment("COMMENT_PropInitMethodComment"), null, "Exception");
    }

   public List<Statement> getPropertiesInitStatements() {
       List<Statement> stmts = ctorInfo.getMethod().getPropertySetStatements();
       stmts.addAll(propertiesInitInfo.getMethod().getPropertySetStatements());
       return stmts;
   }

   protected void ensureInitBlock(MethodInfo mi) {
       UndoEvent event = null;
       try {
           String eventName = NbBundle.getMessage(BeanStructureScanner.class, "EnsureInitBlock"); //NOI18N
           event = beansUnit.getModel().writeLock(eventName);
           if(mi.getMethod().hasInitBlock()) {
               return;
           }
           String bodyText = "// " + getComment("COMMENT_InitSuperCall") + "\n" +
                   "super.init();\n" +
                   "// " + getComment("COMMENT_UserPreInit") + "\n\n" +
                   "// <editor-fold defaultstate=\"collapsed\" desc=\"" +
                   getComment("COMMENT_InitDescription") + "\">\n" +
                   "// " + getComment("COMMENT_PropInit") + "\n" +
                   "try {\n" +
                   "_init();\n" +
                   "} catch (Exception e) {\n" +
                   "log(\"Page1 Initialization Failure\", e);\n" +
                   "throw e instanceof FacesException ? (FacesException) e: new FacesException(e);\n" +
                   "}\n\n" +
                   "// </editor-fold>\n" +
                   "// " + getComment("COMMENT_UserPostInit");
           mi.getMethod().replaceBody(bodyText);
           javaUnit.ensureImport("javax.faces.FacesException");
       }finally {
           if(event != null) {
               beansUnit.getModel().writeUnlock(event);
           }
       }
   }

   public String getConstructorComment() {
       return null;
   }
   
   public String getDestroyMethodComment() {
       return null;
   }
   
   public String getInitMethodComment() {
       return null;
   }
    
   public String getComment(String id) {
       return NbBundle.getMessage(ThresherFacesBeanStructureScanner.class, id);
   }
}
