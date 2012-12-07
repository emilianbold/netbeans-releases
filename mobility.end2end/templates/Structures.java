/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
# import org.netbeans.mobility.end2end.core.model.*;
# import org.netbeans.mobility.end2end.core.model.protocol.binary.ComplexTypeSerializer;
# import org.netbeans.mobility.end2end.core.model.classdata.*;
# import org.netbeans.mobility.end2end.core.model.protocol.Serializer;
# import java.util.*;
# import java.io.File;
#
#  ProtocolSupport support = new ProtocolSupport(data, this, true);
#  String outputDir = data.getClientOutputDirectory();
#  ClassData[] types = data.getSupportedTypes();
#      for( int i = 0; i < types.length; i++ ) {
#          Serializer serializer = types[i].getSerializer();
#          if(!( serializer instanceof ComplexTypeSerializer )) continue;
#          ClassData beanType = types[i];
#
#          String fileName = data.getClientRootDirectory() + File.separator + beanType.getClassName().replace( '.', File.separatorChar ) + ".java";
#          setOut( fileName );
#          getOutput().addCreatedFile( fileName );
#          if( !beanType.getPackageName().equals( "" )) {
package ${beanType.getPackageName()};

#          }
#          FieldData[] fields = beanType.getFields();
#          boolean vector = false, hashtable = false, date = false;
#          for( int j = 0; j < fields.length; j++ ) {
#              if( fields[j].isInherited()) continue;
#              String className = fields[j].getType().getClassName();
#              if( "java.util.Vector".equals( className )) {
#                  vector = true;
#              } else if( "java.util.Hashtable".equals( className )) {
#                  hashtable = true;
#              } else if( "java.util.Date".equals( className )) {
#                  date = true;
#              }
#          } 
#          if( vector ) {
import java.util.Vector;
#          }
#          if( hashtable ) {
import java.util.Hashtable;
#          }
#          if( date ) {
import java.util.Date;
#          }

#          if( "java.lang.Object".equals( beanType.getSuperClassName())) {
public class ${beanType.getLeafClassName()} {
#          } else {
public class ${beanType.getLeafClassName()} extends ${beanType.getSuperClassName()} {    
#          }

#          for( int j = 0; j < fields.length; j++ ) {
#              if( fields[j].isInherited()) continue;
#              String fieldTypeName = fields[j].getType().getClassName();
#              if( fieldTypeName.startsWith( "java.lang" )) { 
#                  fieldTypeName = fieldTypeName.substring( "java.lang.".length());
#              }
    public ${fieldTypeName} ${fields[j].getName()};
#          }
                
}
#            
#      }
