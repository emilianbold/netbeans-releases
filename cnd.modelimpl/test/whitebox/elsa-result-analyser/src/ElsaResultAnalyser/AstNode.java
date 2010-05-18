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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package ElsaResultAnalyser;

import ElsaResultAnalyser.Declaration.TYPE;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nk220367
 */
public class AstNode {

    String name;
    String value;
    List<AstNode> children = new ArrayList<AstNode>();
    int line;
    
    boolean logErrors = false;

    public void print(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(name + " " + value);

        for (AstNode astNode : children) {
            astNode.print(level + 1);
        }
    }

    public AstNode findChild(String name) {
        for (AstNode astNode : children) {
            if (astNode.name.equals(name)) {
                return astNode;
            }
        }
        //System.out.println(name + " was not found in " + this.name);
        return null;
    }

    public AstNode findChild(String name, String value) {
        return findChild(name, value, true);
    }

    public AstNode findChild(String name, String value, boolean reportError) {
        if (value == null) {
            for (AstNode astNode : children) {
                if (astNode.name.equals(name) && astNode.value == null) {
                    return astNode;
                }
            }
        } else {
            for (AstNode astNode : children) {
                if (astNode.value == null) {
                    continue;
                }
                if (astNode.name.equals(name) && astNode.value.equals(value)) {
                    return astNode;
                }
            }
        }
        if (reportError && logErrors) {
            System.out.println(name + " " + value + " was not found in " + this.name + " " + this.line);
            System.out.println("variants:");
            for (AstNode astNode : children) {
                System.out.println("    " + astNode.name + " " + astNode.value);
            }
        }

        return null;
    }

    public void findVariableDefinitions(TokenTable table) {
        for (AstNode astNode : children) {
            astNode.findVariableDefinitions(table);
        }

//          decl = Declaration:
//            dflags = 
//            spec = TS_name:
//              loc = /export/home/nk220367/projects/AstProvider/test.cc:38:5
//              cv = 
//              name = PQ_name:
//                loc = /export/home/nk220367/projects/AstProvider/test.cc:38:5
//                name = "CCC"
//              typenameUsed = false
//            decllist:
//              decllist[0] = Declarator:
//                var: <initialized> <definition> class CCC *cp
//                context = DC_S_DECL
//==============================================================================
//                decl = D_pointer:
//                  loc = /export/home/nk220367/projects/AstProvider/test.cc:38:9
//                  cv = 
//                  base = D_name:
//                    loc = /export/home/nk220367/projects/AstProvider/test.cc:38:10
//                    name = PQ_name:
//                      loc = /export/home/nk220367/projects/AstProvider/test.cc:38:10
//                      name = "cp"
//------------------------------------------------------------------------------        
//                decl = D_name:
//                  loc = /export/home/nk220367/projects/AstProvider/test.cc:44:9
//==============================================================================
//                  name = PQ_name:
//                    loc = /export/home/nk220367/projects/AstProvider/test.cc:44:9
//                    name = "d"
//------------------------------------------------------------------------------        
//                  name = PQ_qualifier:
//                    loc = /export/home/nk220367/projects/Application_1/newmain.cc:16:5
//                    qualifierVar: class C C, at /export/home/nk220367/projects/Application_1/newmain.cc:10:1 (0x08360790)
//                    qualifier = "C"
//                    templArgs is null
//                    rest = PQ_name:
//                      loc = /export/home/nk220367/projects/Application_1/newmain.cc:16:8
//                      name = "s"

        if (name != null && value != null && value.equals("Declaration")) {
            /*            if (findChild("dflags", "extern", false) != null) {
            return;
            }
            if (findChild("dflags", "extern<extern \"C\">", false) != null) {
            return;
            }
            if (findChild("dflags", "<extern \"C\">", false) != null) {
            return;
            }
            if (findChild("dflags", "<extern\"C\">", false) != null) {
            return;
            }
            if (findChild("dflags", "typedef<extern \"C\">", false) != null) {
            return;
            }
            if (findChild("dflags", "typedef", false) != null) {
            return;
            }
            if (findChild("dflags", "virtual", false) != null) {
            return;
            }
            if (findChild("dflags", "friend", false) != null) {
            return;
            }
            if (findChild("dflags", "<forward>", false) != null) {
            return;
            }
            if (findChild("dflags", "friend<forward>", false) != null) {
            return;
            }
             */
            AstNode decllist = findChild("decllist");
            if (decllist != null) {
                AstNode dflags = findChild("dflags");
                if (dflags != null && dflags.value != null && dflags.value.contains("static")) {
                    for (AstNode decllistItem : decllist.children) {
                        AstNode var1 = decllistItem.findChild("var");

                        AstNode firstDecl = decllistItem.findChild("decl");
                        
                        if (decllistItem.findChild("context", "DC_MR_DECL", false) != null) {
                            continue;
                        }

                        decllistItem = skipDDeclsToDNameorDFunc(decllistItem);

                        if (decllistItem.findChild("decl", "D_func", false) != null ||
                                decllistItem.findChild("base", "D_func", false) != null) {
                            continue;
                        }

                        if(firstDecl != null) {
                            addVariableByDName(table, decllistItem, var1, firstDecl.findChild("loc"), "static-member-definition");
                        }
                    }
                    return;
                }

                if (dflags != null) {
                    for (AstNode decllistItem : decllist.children) {
                        AstNode var1 = decllistItem.findChild("var");

                        AstNode firstDecl = decllistItem.findChild("decl");
                        
                        decllistItem = skipDDeclsToDNameorDFunc(decllistItem);

                        if (decllistItem.findChild("decl", "D_func", false) != null ||
                                decllistItem.findChild("base", "D_func", false) != null) {
                            continue;
                        }

                        if (decllistItem.findChild("decl", "D_attribute", false) != null) {
                            continue;
                        }

                        AstNode decl = decllistItem.findChild("decl", "D_bitfield", false);
                        if (decl != null && firstDecl != null) {
                            AstNode name1 = decl.findChild("name", "PQ_name");
                            if (name1 != null) {
                                AstNode name2 = name1.findChild("name");
                                AstNode nameLoc = name1.findChild("loc");
                                AstNode loc = firstDecl.findChild("loc");
                                if (loc != null && nameLoc != null && name2 != null) {
                                    Declaration var = new Declaration(name2.value, nameLoc.value, loc.value, TYPE.VARIABLE, name2.line, "bitfield-definition");

                                    if (var1 != null) {
                                        var.fullName = var1.value;
                                    }

                                    table.addVariable(var);
                                }
                            }
                            continue;
                        }

                        if(firstDecl != null) {
                            addVariableByDName(table, decllistItem, var1, firstDecl.findChild("loc"), "member-definition");
                        }
                    }
                }
            }
        }

        if (name != null && name.equals("params")) {
            for (AstNode param : children) {
                AstNode decl = param.findChild("decl", "Declarator", false);
                if (decl != null) {
                    AstNode var1 = decl.findChild("var");
                    AstNode firstDecl = decl.findChild("decl");

                    decl = skipDDeclsToDNameorDFunc(decl);

                    if(firstDecl != null) {
                        addVariableByDName(table, decl, var1, firstDecl.findChild("loc"), "variable-definition");
                    }
                }
            }
        }
//            params:
//              params[0] = ASTTypeId:
//                spec = TS_simple:
//                  loc = /export/home/nk220367/projects/Quote_1/cpu.cc:46:10
//                  cv = 
//                  id = int
//                decl = Declarator:
//                  var: <parameter> <definition> int type
//                  context = DC_D_FUNC
//                  decl = D_name:
//                    loc = /export/home/nk220367/projects/Quote_1/cpu.cc:46:14
//                    name = PQ_name:
//                      loc = /export/home/nk220367/projects/Quote_1/cpu.cc:46:14
//                      name = "type"
//                  init is null
//                  ctorStatement is null
//                  dtorStatement is null
//              params[1] = ASTTypeId:
//                spec = TS_simple:
//                  loc = /export/home/nk220367/projects/Quote_1/cpu.cc:46:21
//                  cv = 
//                  id = int
//                decl = Declarator:
//                  var: <parameter> <definition> int architecture
//                  context = DC_D_FUNC
//                  decl = D_name:
//                    loc = /export/home/nk220367/projects/Quote_1/cpu.cc:46:25
//                    name = PQ_name:
//                      loc = /export/home/nk220367/projects/Quote_1/cpu.cc:46:25
//                      name = "architecture"        


    }

    public void findVariableDeclarations(TokenTable table, Declaration decl) {
        for (AstNode astNode : children) {
            astNode.findVariableDeclarations(table, decl);
        }

//      decl = Declaration:
//        dflags = 
//        spec = TS_classSpec:
//          loc = /export/home/nk220367/projects/AstProvider/test.cc:16:1
//          cv = 
//          keyword = class
//          name = PQ_name:
//            loc = /export/home/nk220367/projects/AstProvider/test.cc:16:7
//            name = "CCC"
//          bases:
//          members = MemberList:
//            list:
//              list[0] = MR_decl:
//                loc = /export/home/nk220367/projects/AstProvider/test.cc:17:5
//                d = Declaration:
//                  dflags = virtual
//                  spec = TS_simple:
//                    loc = /export/home/nk220367/projects/AstProvider/test.cc:17:5
//                    cv = 
//                    id = int
//                  decllist:
//                    decllist[0] = Declarator:
//                      var: virtual <member> <definition> int f(/*m: class CCC & */ )
//                      context = DC_MR_DECL
//                      decl = = D_name:
//                          loc = /export/home/nk220367/projects/AstProvider/test.cc:17:17
//                          name = PQ_name:
//                            loc = /export/home/nk220367/projects/AstProvider/test.cc:17:17
//                            name = "f"
        if (name != null && value != null && value.equals("Declaration")) {
            AstNode spec = findChild("spec", "TS_classSpec", false);
            if (spec != null) {
//                    AstNode dflags = findChild("dflags", null);
                AstNode loc = spec.findChild("loc");
//                    if (/*dflags != null &&*/loc != null && decl.qualifierPos.equals(new Offset(loc.value, loc.line))) {
                AstNode members = spec.findChild("members", "MemberList");
                if (members != null) {
                    AstNode list = members.findChild("list");
                    if (list != null) {
                        for (AstNode listItem : list.children) {
                            if (listItem.value.equals("MR_decl")) {
                                AstNode d = listItem.findChild("d", "Declaration");
                                if (d != null) {
                                    AstNode dflags2 = d.findChild("dflags", "static", false);
                                    AstNode decllist = d.findChild("decllist");
                                    if (decllist != null && dflags2 != null) {
                                        for (AstNode decllistItem : decllist.children) {
                                            AstNode firstDecl = decllistItem.findChild("decl");

                                            AstNode decllistItem2 = skipDDeclsToDNameorDFunc(decllistItem);

                                            if (decllistItem2.findChild("decl", "D_func", false) != null ||
                                                    decllistItem2.findChild("base", "D_func", false) != null) {
                                                continue;
                                            }
                                            if (decllistItem2.findChild("decl", "D_attribute", false) != null ||
                                                    decllistItem2.findChild("base", "D_attribute", false) != null) {
                                                continue;
                                            }


                                            AstNode decl2 = decllistItem2.findChild("decl", "D_name", false);
                                            if (decl2 == null) {
                                                decl2 = decllistItem2.findChild("base", "D_name");
                                            }

                                            AstNode decl3 = skipDDeclsToDNameorDFunc(decl2);

                                            if (decl3 != null) {

                                                decl3 = skipQualifiers(decl3);

                                                AstNode name1 = decl3.findChild("name", "PQ_name");
                                                if (name1 == null) {
                                                    name1 = decl3.findChild("rest", "PQ_name");
                                                }
                                                if (name1 != null && firstDecl != null) {
                                                    AstNode name2 = name1.findChild("name");
                                                    AstNode nameLoc = name1.findChild("loc");
                                                    AstNode loc2 = firstDecl.findChild("loc");
                                                    AstNode var = decllistItem.findChild("var");
                                                    if (loc2 != null && nameLoc != null && name2 != null && var != null && loc != null && loc.value != null) {

                                                        Declaration nvar = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.VARIABLE, name2.line, "static-field-declaration");

                                                        Declaration def = table.findVariableDefinition(new Offset(loc.value, loc.line), var.value);
                                                        if (def != null) {
                                                            table.addVariableDeclaration(def, nvar);
                                                        //def.declarations.add(nvar);
                                                        } else {
                                                            table.addAloneVariableDeclaration(nvar);
                                                        //table.aloneVariableDeclarations.add(nvar);
                                                        }

                                                        continue;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

//            }

//      decl = Declaration:
//        dflags = extern
//        spec = TS_simple:
//          loc = /export/home/nk220367/projects/Application_1/file.h:23:1
//          cv = 
//          id = int
//        decllist:
//          decllist[0] = Declarator:
//            var: <global> <definition> int x
//            context = DC_TF_DECL
//            decl = D_name:
//              loc = /export/home/nk220367/projects/Application_1/file.h:23:12
//              name = PQ_name:
//                loc = /export/home/nk220367/projects/Application_1/file.h:23:12
//                name = "x"     
        if (name != null && value != null && value.equals("Declaration")) {
            AstNode dflags2 = findChild("dflags");
            AstNode decllist = findChild("decllist");
            if (dflags2 != null && decllist != null && dflags2.value != null && dflags2.value.contains("extern")) {
                for (AstNode decllistItem : decllist.children) {
                    AstNode firstDecl = decllistItem.findChild("decl");
                    
                    AstNode decllistItem2 = skipDDeclsToDNameorDFunc(decllistItem);

                    if (decllistItem2.findChild("decl", "D_func", false) != null ||
                            decllistItem2.findChild("base", "D_func", false) != null) {
                        continue;
                    }
                    if (decllistItem2.findChild("decl", "D_attribute", false) != null ||
                            decllistItem2.findChild("base", "D_attribute", false) != null) {
                        continue;
                    }

                    AstNode decl2 = decllistItem2.findChild("decl", "D_name", false);
                    if (decl2 == null) {
                        decl2 = decllistItem2.findChild("base", "D_name");
                    }

                    AstNode decl3 = skipDDeclsToDNameorDFunc(decl2);

                    if (decl3 != null) {

                        decl3 = skipQualifiers(decl3);

                        AstNode name1 = decl3.findChild("name", "PQ_name");
                        if (name1 == null) {
                            name1 = decl3.findChild("rest", "PQ_name");
                        }
                        if (name1 != null && firstDecl != null) {
                            AstNode name2 = name1.findChild("name");
                            AstNode nameLoc = name1.findChild("loc");
                            AstNode loc2 = firstDecl.findChild("loc");
                            AstNode var = decllistItem.findChild("var");
                            if (loc2 != null && nameLoc != null && name2 != null && var != null) {
                                /*if (decl.fullName.equals(var.value)) {
                                Declaration nvar = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.VARIABLE, name2.line);
                                
                                decl.declarations.add(nvar);
                                
                                continue;
                                }*/
                                Declaration nvar = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.VARIABLE, name2.line, "extern-variable-declaration");

                                Declaration def = table.findVariableDefinition(var.value);
                                if (def != null) {
                                    table.addVariableDeclaration(def, nvar);
                                //def.declarations.add(nvar);
                                } else {
                                    table.addAloneVariableDeclaration(nvar);
                                //table.aloneVariableDeclarations.add(nvar);
                                }
                            }
                        }
                    }
                }
            }
        }

    /*
    if (decl.qualifierPos != null) {
    
    //      decl = Declaration:
    //        dflags = 
    //        spec = TS_classSpec:
    //          loc = /export/home/nk220367/projects/AstProvider/test.cc:16:1
    //          cv = 
    //          keyword = class
    //          name = PQ_name:
    //            loc = /export/home/nk220367/projects/AstProvider/test.cc:16:7
    //            name = "CCC"
    //          bases:
    //          members = MemberList:
    //            list:
    //              list[0] = MR_decl:
    //                loc = /export/home/nk220367/projects/AstProvider/test.cc:17:5
    //                d = Declaration:
    //                  dflags = virtual
    //                  spec = TS_simple:
    //                    loc = /export/home/nk220367/projects/AstProvider/test.cc:17:5
    //                    cv = 
    //                    id = int
    //                  decllist:
    //                    decllist[0] = Declarator:
    //                      var: virtual <member> <definition> int f(m: class CCC &  )
    //                      context = DC_MR_DECL
    //                      decl = = D_name:
    //                          loc = /export/home/nk220367/projects/AstProvider/test.cc:17:17
    //                          name = PQ_name:
    //                            loc = /export/home/nk220367/projects/AstProvider/test.cc:17:17
    //                            name = "f"
    if (name != null && value != null && value.equals("Declaration")) {
    AstNode spec = findChild("spec", "TS_classSpec", false);
    if (spec != null) {
    //                    AstNode dflags = findChild("dflags", null);
    AstNode loc = spec.findChild("loc");
    if (loc != null && decl.qualifierPos.equals(new Offset(loc.value, loc.line))) {
    AstNode members = spec.findChild("members", "MemberList");
    if (members != null) {
    AstNode list = members.findChild("list");
    if (list != null) {
    for (AstNode listItem : list.children) {
    if (listItem.value.equals("MR_decl")) {
    AstNode d = listItem.findChild("d", "Declaration");
    if (d != null) {
    AstNode dflags2 = d.findChild("dflags", "static", false);
    AstNode decllist = d.findChild("decllist");
    if (decllist != null && dflags2 != null) {
    for (AstNode decllistItem : decllist.children) {
    
    AstNode decllistItem2 = skipDDeclsToDNameorDFunc(decllistItem);
    
    if (decllistItem2.findChild("decl", "D_func", false) != null ||
    decllistItem2.findChild("base", "D_func", false) != null) {
    continue;
    }
    if (decllistItem2.findChild("decl", "D_attribute", false) != null ||
    decllistItem2.findChild("base", "D_attribute", false) != null) {
    continue;
    }
    
    
    AstNode decl2 = decllistItem2.findChild("decl", "D_name", false);
    if (decl2 == null) {
    decl2 = decllistItem2.findChild("base", "D_name");
    }
    
    AstNode decl3 = skipDDeclsToDNameorDFunc(decl2);
    
    if (decl3 != null) {
    
    decl3 = skipQualifiers(decl3);
    
    AstNode name1 = decl3.findChild("name", "PQ_name");
    if (name1 == null) {
    name1 = decl3.findChild("rest", "PQ_name");
    }
    if (name1 != null) {
    AstNode name2 = name1.findChild("name");
    AstNode nameLoc = name1.findChild("loc");
    AstNode loc2 = decl2.findChild("loc");
    AstNode var = decllistItem.findChild("var");
    if (loc2 != null && nameLoc != null && name2 != null && var != null) {
    
    
    if (decl.fullName.equals(var.value)) {
    Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.VARIABLE, name2.line);
    decl.declarations.add(fun);
    
    continue;
    }
    
    
    }
    }
    }
    }
    }
    }
    }
    }
    }
    }
    }
    }
    }
    } else {
    
    //      decl = Declaration:
    //        dflags = extern
    //        spec = TS_simple:
    //          loc = /export/home/nk220367/projects/Application_1/file.h:23:1
    //          cv = 
    //          id = int
    //        decllist:
    //          decllist[0] = Declarator:
    //            var: <global> <definition> int x
    //            context = DC_TF_DECL
    //            decl = D_name:
    //              loc = /export/home/nk220367/projects/Application_1/file.h:23:12
    //              name = PQ_name:
    //                loc = /export/home/nk220367/projects/Application_1/file.h:23:12
    //                name = "x"     
    if (name != null && value != null && value.equals("Declaration")) {
    AstNode dflags2 = findChild("dflags");
    AstNode decllist = findChild("decllist");
    if (dflags2 != null && decllist != null && dflags2.value != null && dflags2.value.contains("extern")) {
    for (AstNode decllistItem : decllist.children) {
    AstNode decllistItem2 = skipDDeclsToDNameorDFunc(decllistItem);
    
    if (decllistItem2.findChild("decl", "D_func", false) != null ||
    decllistItem2.findChild("base", "D_func", false) != null) {
    continue;
    }
    if (decllistItem2.findChild("decl", "D_attribute", false) != null ||
    decllistItem2.findChild("base", "D_attribute", false) != null) {
    continue;
    }
    
    AstNode decl2 = decllistItem2.findChild("decl", "D_name", false);
    if (decl2 == null) {
    decl2 = decllistItem2.findChild("base", "D_name");
    }
    
    AstNode decl3 = skipDDeclsToDNameorDFunc(decl2);
    
    if (decl3 != null) {
    
    decl3 = skipQualifiers(decl3);
    
    AstNode name1 = decl3.findChild("name", "PQ_name");
    if (name1 == null) {
    name1 = decl3.findChild("rest", "PQ_name");
    }
    if (name1 != null) {
    AstNode name2 = name1.findChild("name");
    AstNode nameLoc = name1.findChild("loc");
    AstNode loc2 = decl2.findChild("loc");
    AstNode var = decllistItem.findChild("var");
    if (loc2 != null && nameLoc != null && name2 != null && var != null) {
    if (decl.fullName.equals(var.value)) {
    Declaration nvar = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.VARIABLE, name2.line);
    decl.declarations.add(nvar);
    
    continue;
    }
    }
    }
    }
    }
    }
    }
    }
     */
    }

    public void findVariableUssages(TokenTable table, Declaration decl) {
        for (AstNode astNode : children) {
            astNode.findVariableUssages(table, decl);
        }

//        * = *:
//          type: class CCC *&
//          var: class CCC *cp, at /export/home/nk220367/projects/AstProvider/test.cc:38:9 (0x0837C0E0)
//          name = PQ_name:
//            loc = /export/home/nk220367/projects/AstProvider/test.cc:40:12
//            name = "cp"        
        if (name != null && value != null) {
            AstNode name1 = findChild("name", "PQ_name", false);
            if (name1 != null) {
                AstNode name2 = name1.findChild("name");
                AstNode nameLoc = name1.findChild("loc");
                AstNode varLoc = findChild("var");
                if (varLoc != null && nameLoc != null && name1 != null) {
//                    if (decl.name.equals(name2.value) && decl.pos.equals(new Offset(varLoc.value, varLoc.line))) {
//                        decl.usages.add(new Offset(nameLoc.value, name2.line));
//                    }
                    Declaration d = table.findVariable(new Offset(varLoc.value, varLoc.line), name2.value);
                    if (d != null) {
                        table.addVariableUsage(d, new Offset(nameLoc.value, name2.line));
                    //d.usages.add(new Offset(nameLoc.value, name2.line));
                    }
                }
            }
        }

//          * = *:
//            type: int &
//            field: int s, at /export/home/nk220367/projects/Application_1/newmain.cc:16:5 (0x08360428)
//            obj = E_variable:
//              type: class C &
//              var: class C c, at /export/home/nk220367/projects/Application_1/newmain.cc:34:7 (0x0837B730)
//              name = PQ_name:
//                loc = /export/home/nk220367/projects/Application_1/newmain.cc:37:5
//                name = "c"
//            fieldName = PQ_name:
//              loc = /export/home/nk220367/projects/Application_1/newmain.cc:37:7
//              name = "s"       
        if (name != null && value != null) {
            AstNode fieldLoc = findChild("field");
            AstNode fieldName = findChild("fieldName", "PQ_name", false);
            if (fieldName != null && fieldLoc != null) {
                AstNode nameLoc = fieldName.findChild("loc");
                AstNode name1 = fieldName.findChild("name");
                if (fieldLoc != null && nameLoc != null && name1 != null) {
//                    if (decl.name.equals(name1.value) && decl.pos.equals(new Offset(fieldLoc.value, fieldLoc.line))) {
//                        decl.usages.add(new Offset(nameLoc.value, name1.line));
//                    }
                    Declaration d = table.findVariable(new Offset(fieldLoc.value, fieldLoc.line), name1.value);
                    if (d != null) {
                        table.addVariableUsage(d, new Offset(nameLoc.value, name1.line));
                    //d.usages.add(new Offset(nameLoc.value, name1.line));
                    }

                }
            }
        }

        if (name != null && value != null) {
            AstNode fieldLoc = findChild("member");
            AstNode fieldName = findChild("name", "PQ_name", false);
            if (fieldName != null && fieldLoc != null) {
                AstNode nameLoc = fieldName.findChild("loc");
                AstNode name1 = fieldName.findChild("name");
                if (fieldLoc != null && nameLoc != null && name1 != null) {
//                    if (decl.name.equals(name1.value) && decl.pos.equals(new Offset(fieldLoc.value, fieldLoc.line))) {
//                        decl.usages.add(new Offset(nameLoc.value, name1.line));
//                    }
                    Declaration d = table.findVariable(new Offset(fieldLoc.value, fieldLoc.line), name1.value);
                    if (d != null) {
                        table.addVariableUsage(d, new Offset(nameLoc.value, name1.line));
                    //d.usages.add(new Offset(nameLoc.value, name1.line));
                    }

                }
            }
        }


    }

    public void findFunctionDefinitions(TokenTable table) {
        for (AstNode astNode : children) {
            astNode.findFunctionDefinitions(table);
        }

//      f = Function:
//        funcType = int ()()
//        receiver = NULL
//        retVar: NULL
//        dflags = 
//        retspec = TS_simple:
//          loc = /export/home/nk220367/projects/AstProvider/test.cc:38:1
//          cv = 
//          id = int
//        nameAndParams = Declarator:
//          var: virtual <member> <definition> int f(/*m: class DDD & */ )
//          context = DC_FUNCTION
//          decl = D_func:
//            loc = /export/home/nk220367/projects/AstProvider/test.cc:29:5
//            base = D_name:
//              loc = /export/home/nk220367/projects/AstProvider/test.cc:29:5
//==============================================================================
//              name = PQ_qualifier:
//                loc = /export/home/nk220367/projects/AstProvider/test.cc:29:5
//                qualifierVar: class DDD DDD, at /export/home/nk220367/projects/AstProvider/test.cc:20:1 (0x08360420)
//                qualifier = "DDD"
//                templArgs is null
//==============================================================================
//                rest = PQ_name:
//                  loc = /export/home/nk220367/projects/AstProvider/test.cc:29:10
//                  name = "f"     
//------------------------------------------------------------------------------                
//                rest = PQ_operator:
//                  loc = /export/home/nk220367/projects/Application_1/newfile.cc:8:9
//                  o = ON_operator:
//                    op = -=
//                  fakeName = "operator-="        
//==============================================================================
//------------------------------------------------------------------------------        
//              name = PQ_name:
//                loc = /export/home/nk220367/projects/AstProvider/test.cc:35:5
//                name = "main"
//------------------------------------------------------------------------------        
//              name = PQ_operator:
//                loc = /export/home/nk220367/projects/Application_1/file.h:17:7
//                o = ON_operator:
//                  op = +
//                fakeName = "operator+"
//------------------------------------------------------------------------------        
//              name = PQ_template:
//                loc = /export/home/nk220367/projects/Application_1/newmain.cc:30:3
//                name = "PreHeat"        

//      f = Function:
//        funcType = char const *()(/*m: class Cpu const & */ ) const
//        receiver = class Cpu const &__receiver, at /export/home/nk220367/projects/Quote_1/cpu.cc:86:11 (0x08F14C00)
//        retVar: NULL
//        dflags = 
//        retspec = TS_simple:
//          loc = /export/home/nk220367/projects/Quote_1/cpu.cc:86:1
//          cv = const
//          id = char
//        nameAndParams = Declarator:
//          var: virtual <member> <definition> char const *GetCategory(/*m: class Cpu const & */ ) const
//          context = DC_FUNCTION
//          decl = D_pointer:
//            loc = /export/home/nk220367/projects/Quote_1/cpu.cc:86:11
//            cv = 
//            base = D_func:
//              loc = /export/home/nk220367/projects/Quote_1/cpu.cc:86:13
//              base = D_name:
//                loc = /export/home/nk220367/projects/Quote_1/cpu.cc:86:13
//                name = PQ_qualifier:
//                  loc = /export/home/nk220367/projects/Quote_1/cpu.cc:86:13
//                  qualifierVar: class Cpu Cpu, at /export/home/nk220367/projects/Quote_1/cpu.h:47:1 (0x08F12248)
//                  qualifier = "Cpu"
//                  templArgs is null
//                  rest = PQ_name:
//                    loc = /export/home/nk220367/projects/Quote_1/cpu.cc:86:18
//                    name = "GetCategory"
        if (name != null && value != null && name.equals("f") && value.equals("Function")) {
            AstNode nameAndParams = findChild("nameAndParams");
            if (nameAndParams != null) {
                AstNode var = nameAndParams.findChild("var");
                AstNode firstDecl = nameAndParams.findChild("decl");

                nameAndParams =
                        skipDDeclsToDNameorDFunc(nameAndParams);

                if(firstDecl != null) {
                    addFunctionByDName(table, nameAndParams, var, firstDecl.findChild("loc"));
                }
            }

        }
    }

    public void findFunctionDeclarations(TokenTable table, Declaration decl) {
        for (AstNode astNode : children) {
            astNode.findFunctionDeclarations(table, decl);
        }

//        if (decl.qualifierPos != null) {

//      decl = Declaration:
//        dflags = 
//        spec = TS_classSpec:
//          loc = /export/home/nk220367/projects/AstProvider/test.cc:16:1
//          cv = 
//          keyword = class
//          name = PQ_name:
//            loc = /export/home/nk220367/projects/AstProvider/test.cc:16:7
//            name = "CCC"
//          bases:
//          members = MemberList:
//            list:
//==============================================================================            
//              list[1] = MR_template:
//                loc = /export/home/nk220367/projects/Application_1/newmain.cc:12:5
//                d = TD_decl:
//                  params = TP_type:
//                    loc = /export/home/nk220367/projects/Application_1/newmain.cc:12:14
//                    name = "T"
//                    defaultType is null
//                    next is null
//------------------------------------------------------------------------------                            
//              list[0] = MR_decl:
//                loc = /export/home/nk220367/projects/AstProvider/test.cc:17:5
//==============================================================================            
//                d = Declaration:
//                  dflags = virtual
//                  spec = TS_simple:
//                    loc = /export/home/nk220367/projects/AstProvider/test.cc:17:5
//                    cv = 
//                    id = int
//                  decllist:
//                    decllist[0] = Declarator:
//                      var: virtual <member> <definition> int f(/*m: class CCC & */ )
//                      context = DC_MR_DECL
//                      decl = D_func:
//                        loc = /export/home/nk220367/projects/AstProvider/test.cc:17:17
//                        base = D_name:
//                          loc = /export/home/nk220367/projects/AstProvider/test.cc:17:17
//==============================================================================                                      
//                          name = PQ_name:
//                            loc = /export/home/nk220367/projects/AstProvider/test.cc:17:17
//                            name = "f"
//------------------------------------------------------------------------------
//                          name = PQ_operator:
//                            loc = /export/home/nk220367/projects/Application_1/newmain.cc:21:10
//                            o = ON_operator:
//                              op = +=
//                            fakeName = "operator+="
//------------------------------------------------------------------------------            
//                          name = PQ_template:
//                              loc = /export/home/nk220367/projects/Application_1/newmain.cc:30:3
//                              name = "PreHeat"            
        if (name != null && value != null && name != null && value.equals("Declaration")) {
            AstNode spec = findChild("spec", "TS_classSpec", false);
            if (spec != null) {
//                    AstNode dflags = findChild("dflags", null);
                AstNode loc = spec.findChild("loc");
                if (/*dflags != null &&*/loc != null /*&& decl.qualifierPos.equals(new Offset(loc.value, loc.line))*/) {
                    AstNode members = spec.findChild("members", "MemberList");
                    if (members != null) {
                        AstNode list = members.findChild("list");
                        if (list != null) {
                            for (AstNode listItem : list.children) {
                                if (listItem.value.equals("MR_decl")) {
                                    AstNode d = listItem.findChild("d", "Declaration");
                                    if (d != null) {
                                        AstNode decllist = d.findChild("decllist");
                                        if (decllist != null) {
                                            for (AstNode decllistItem : decllist.children) {
                                                AstNode firstDecl = decllistItem.findChild("decl");

                                                AstNode decllistItem2 = skipDDeclsToDNameorDFunc(decllistItem);

                                                AstNode decl2 = decllistItem2.findChild("decl", "D_func", false);
                                                if (decl2 == null) {
                                                    decl2 = decllistItem2.findChild("base", "D_func", false);
                                                }

                                                AstNode decl3 = skipDDeclsToDNameorDFunc(decl2);

                                                if (decl3 != null) {
                                                    AstNode base = decl3.findChild("base", "D_name", false);
                                                    if (base != null) {

                                                        base = skipQualifiers(base);

                                                        AstNode name1 = base.findChild("name", "PQ_name", false);
                                                        if (name1 == null) {
                                                            name1 = base.findChild("rest", "PQ_name", false);
                                                        }

                                                        if (name1 != null && firstDecl != null) {
                                                            AstNode name2 = name1.findChild("name");
                                                            AstNode nameLoc = name1.findChild("loc");
                                                            AstNode loc2 = firstDecl.findChild("loc");
                                                            AstNode var = decllistItem.findChild("var");
                                                            if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                                                    if (decl.fullName.equals(var.value)) {
//                                                                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                                                        decl.declarations.add(fun);
//                                                                    }
                                                                Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "class-method-declaration");
                                                                Declaration def = table.findFunctionDefinition(new Offset(loc.value, loc.line), var.value);
                                                                if (def != null) {
                                                                    table.addFunctionDeclaration(def, fun);
                                                                //def.declarations.add(fun);
                                                                } else {
                                                                    table.addAloneFunctionDeclaration(fun);
                                                                //table.aloneFunctionDeclarations.add(fun);
                                                                }


                                                            }
                                                            continue;
                                                        }

                                                        name1 = base.findChild("name", "PQ_template", false);
                                                        if (name1 == null) {
                                                            name1 = base.findChild("rest", "PQ_template", false);
                                                        }

                                                        if (name1 != null && firstDecl != null) {
                                                            AstNode name2 = name1.findChild("name");
                                                            AstNode nameLoc = name1.findChild("loc");
                                                            AstNode loc2 = firstDecl.findChild("loc");
                                                            AstNode var = decllistItem.findChild("var");
                                                            if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                                            if (decl.fullName.equals(var.value)) {
//                                                                Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                                                decl.declarations.add(fun);
//                                                            }
                                                                Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "class-template-method-declaration");
                                                                Declaration def = table.findFunctionDefinition(new Offset(loc.value, loc.line), var.value);
                                                                if (def != null) {
                                                                    table.addFunctionDeclaration(def, fun);
                                                                //def.declarations.add(fun);
                                                                } else {
                                                                    table.addAloneFunctionDeclaration(fun);
                                                                //table.aloneFunctionDeclarations.add(fun);
                                                                }


                                                            }
                                                            continue;
                                                        }

                                                        name1 = base.findChild("name", "PQ_operator", false);
                                                        if (name1 == null) {
                                                            name1 = base.findChild("rest", "PQ_operator");
                                                        }

                                                        if (name1 != null && firstDecl != null) {
                                                            AstNode name2 = name1.findChild("fakeName");
                                                            AstNode nameLoc = name1.findChild("loc");
                                                            AstNode loc2 = firstDecl.findChild("loc");
                                                            AstNode var = decllistItem.findChild("var");
                                                            if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                                            if (decl.fullName.equals(var.value)) {
//                                                                Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                                                decl.declarations.add(fun);
//                                                            }
                                                                Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "class-operator-declaration");
                                                                Declaration def = table.findFunctionDefinition(new Offset(loc.value, loc.line), var.value);
                                                                if (def != null) {
                                                                    table.addFunctionDeclaration(def, fun);
                                                                //def.declarations.add(fun);
                                                                } else {
                                                                    table.addAloneFunctionDeclaration(fun);
                                                                //table.aloneFunctionDeclarations.add(fun);
                                                                }


                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    continue;
                                }

                                if (listItem.value.equals("MR_template")) {
                                    AstNode tdd = listItem.findChild("d", "TD_decl");
                                    if (tdd != null) {
                                        AstNode d = tdd.findChild("d", "Declaration");
                                        if (d != null) {
                                            AstNode decllist = d.findChild("decllist");
                                            if (decllist != null) {
                                                for (AstNode decllistItem : decllist.children) {
                                                    AstNode firstDecl = decllistItem.findChild("decl");

                                                    AstNode decllistItem2 = skipDDeclsToDNameorDFunc(decllistItem);

                                                    AstNode decl2 = decllistItem2.findChild("decl", "D_func", false);
                                                    if (decl2 == null) {
                                                        decl2 = decllistItem2.findChild("base", "D_func", false);
                                                    }

                                                    AstNode decl3 = skipDDeclsToDNameorDFunc(decl2);

                                                    if (decl3 != null) {
                                                        AstNode base = decl3.findChild("base", "D_name", false);
                                                        if (base != null) {

                                                            base = skipQualifiers(base);

                                                            AstNode name1 = base.findChild("name", "PQ_name", false);
                                                            if (name1 == null) {
                                                                name1 = base.findChild("rest", "PQ_name", false);
                                                            }

                                                            if (name1 != null && firstDecl != null) {
                                                                AstNode name2 = name1.findChild("name");
                                                                AstNode nameLoc = name1.findChild("loc");
                                                                AstNode loc2 = firstDecl.findChild("loc");
                                                                AstNode var = decllistItem.findChild("var");
                                                                if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                                                if (decl.fullName.equals(var.value)) {
//                                                                    Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                                                    decl.declarations.add(fun);
//                                                                }
                                                                    Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "template-method-declaration");
                                                                    Declaration def = table.findFunctionDefinition(new Offset(loc.value, loc.line), var.value);
                                                                    if (def != null) {
                                                                        table.addFunctionDeclaration(def, fun);
                                                                    //def.declarations.add(fun);
                                                                    } else {
                                                                        table.addAloneFunctionDeclaration(fun);
                                                                    //table.aloneFunctionDeclarations.add(fun);
                                                                    }


                                                                }
                                                                continue;
                                                            }

                                                            name1 = base.findChild("name", "PQ_template", false);
                                                            if (name1 == null) {
                                                                name1 = base.findChild("rest", "PQ_template", false);
                                                            }

                                                            if (name1 != null && firstDecl != null) {
                                                                AstNode name2 = name1.findChild("name");
                                                                AstNode nameLoc = name1.findChild("loc");
                                                                AstNode loc2 = firstDecl.findChild("loc");
                                                                AstNode var = decllistItem.findChild("var");
                                                                if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                                                if (decl.fullName.equals(var.value)) {
//                                                                    Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                                                    decl.declarations.add(fun);
//                                                                }

                                                                    Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "template-template-method-declaration");
                                                                    Declaration def = table.findFunctionDefinition(new Offset(loc.value, loc.line), var.value);
                                                                    if (def != null) {
                                                                        table.addFunctionDeclaration(def, fun);
                                                                    //def.declarations.add(fun);
                                                                    } else {
                                                                        table.addAloneFunctionDeclaration(fun);
                                                                    //table.aloneFunctionDeclarations.add(fun);
                                                                    }

                                                                }
                                                                continue;
                                                            }

                                                            name1 = base.findChild("name", "PQ_operator");
                                                            if (name1 == null) {
                                                                name1 = base.findChild("rest", "PQ_operator", false);
                                                            }

                                                            if (name1 != null && firstDecl != null) {
                                                                AstNode name2 = name1.findChild("fakeName");
                                                                AstNode nameLoc = name1.findChild("loc");
                                                                AstNode loc2 = firstDecl.findChild("loc");
                                                                AstNode var = decllistItem.findChild("var");
                                                                if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                                                if (decl.fullName.equals(var.value)) {
//                                                                    Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                                                    decl.declarations.add(fun);
//                                                                }
                                                                    Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "template-operator-declaration");
                                                                    Declaration def = table.findFunctionDefinition(new Offset(loc.value, loc.line), var.value);
                                                                    if (def != null) {
                                                                        table.addFunctionDeclaration(def, fun);
                                                                    //def.declarations.add(fun);
                                                                    } else {
                                                                        table.addAloneFunctionDeclaration(fun);
                                                                    //table.aloneFunctionDeclarations.add(fun);
                                                                    }


                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
//                    }
                }
            }
//        } else {
//      decl = Declaration:
//        dflags = 
//        spec = TS_simple:
//          loc = /export/home/nk220367/projects/Application_1/file.h:18:1
//          cv = 
//          id = void
//        decllist:
//          decllist[0] = Declarator:
//            var: <global> <definition> void foo1()
//            context = DC_TF_DECL
//            decl = D_func:
//              loc = /export/home/nk220367/projects/Application_1/file.h:18:6
//              base = D_name:
//                loc = /export/home/nk220367/projects/Application_1/file.h:18:6
//==============================================================================                                                  
//                name = PQ_name:
//                  loc = /export/home/nk220367/projects/Application_1/file.h:18:6
//                  name = "foo1"
//------------------------------------------------------------------------------            
//                name = PQ_template:
//                  loc = /export/home/nk220367/projects/Application_1/newmain.cc:30:3
//                  name = "PreHeat"            
            if (name != null && value != null && name != null && value.equals("Declaration")) {
                AstNode decllist = findChild("decllist");
                if (decllist != null) {
                    for (AstNode decllistItem : decllist.children) {
                        AstNode firstDecl = decllistItem.findChild("decl");
                        
                        AstNode decllistItem2 = skipDDeclsToDNameorDFunc(decllistItem);

                        AstNode decl2 = decllistItem2.findChild("decl", "D_func", false);
                        if (decl2 == null) {
                            decl2 = decllistItem2.findChild("base", "D_func", false);
                        }

                        AstNode decl3 = skipDDeclsToDNameorDFunc(decl2);

                        if (decl3 != null) {
//                            AstNode dflags2 = findChild("dflags", null);
                            AstNode base = decl3.findChild("base", "D_name");
                            if (/*dflags2 != null &&*/base != null) {

                                base = skipQualifiers(base);

                                AstNode name1 = base.findChild("name", "PQ_name", false);
                                if (name1 == null) {
                                    name1 = base.findChild("rest", "PQ_name", false);
                                }

                                if (name1 != null && firstDecl != null) {
                                    AstNode name2 = name1.findChild("name");
                                    AstNode nameLoc = name1.findChild("loc");
                                    AstNode loc2 = firstDecl.findChild("loc");
                                    AstNode var = decllistItem.findChild("var");
                                    if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                    if (decl.fullName.equals(var.value)) {
//                                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                        decl.declarations.add(fun);
//                                    }
                                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "function-declaration");
                                        Declaration def = table.findFunctionDefinition(var.value);
                                        if (def != null) {
                                            table.addFunctionDeclaration(def, fun);
                                        //def.declarations.add(fun);
                                        } else {
                                            table.addAloneFunctionDeclaration(fun);
                                        //table.aloneFunctionDeclarations.add(fun);
                                        }


                                    }
                                    continue;
                                }

                                name1 = base.findChild("name", "PQ_template", false);
                                if (name1 == null) {
                                    name1 = base.findChild("rest", "PQ_template", false);
                                }

                                if (name1 != null && firstDecl != null) {
                                    AstNode name2 = name1.findChild("name");
                                    AstNode nameLoc = name1.findChild("loc");
                                    AstNode loc2 = firstDecl.findChild("loc");
                                    AstNode var = decllistItem.findChild("var");
                                    if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                        if (decl.fullName.equals(var.value)) {
//                                            Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                            decl.declarations.add(fun);
//                                        }
                                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "template-function-declaration");
                                        Declaration def = table.findFunctionDefinition(var.value);
                                        if (def != null) {
                                            table.addFunctionDeclaration(def, fun);
                                        //def.declarations.add(fun);
                                        } else {
                                            table.addAloneFunctionDeclaration(fun);
                                        //table.aloneFunctionDeclarations.add(fun);
                                        }

                                    }
                                    continue;
                                }

                                name1 = base.findChild("name", "PQ_operator");
                                if (name1 == null) {
                                    name1 = base.findChild("rest", "PQ_operator", false);
                                }

                                if (name1 != null && firstDecl != null) {
                                    AstNode name2 = name1.findChild("fakeName");
                                    AstNode nameLoc = name1.findChild("loc");
                                    AstNode loc2 = firstDecl.findChild("loc");
                                    AstNode var = decllistItem.findChild("var");
                                    if (loc2 != null && nameLoc != null && name2 != null && var != null) {
//                                        if (decl.fullName.equals(var.value)) {
//                                            Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line);
//                                            decl.declarations.add(fun);
//                                        }
                                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc2.value, TYPE.FUNCTION, name2.line, "operator-declaration");
                                        Declaration def = table.findFunctionDefinition(var.value);
                                        if (def != null) {
                                            table.addFunctionDeclaration(def, fun);
                                        //def.declarations.add(fun);
                                        } else {
                                            table.addAloneFunctionDeclaration(fun);
                                        //table.aloneFunctionDeclarations.add(fun);
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
//        }
        }
    }

    public void findFunctionUssages(TokenTable table, Declaration decl) {
        for (AstNode astNode : children) {
            astNode.findFunctionUssages(table, decl);
        }

//        * = E_funCall:        
//          type: int
//==============================================================================
//          func = E_fieldAcc:
//            type: int ()(/*m: class DDD & */ )
//            field: int f(/*m: class DDD & */ ), at /export/home/nk220367/projects/AstProvider/test.cc:29:5 (0x083604E0)
//            obj = E_variable:
//              type: class CCC &
//              var: class CCC c, at /export/home/nk220367/projects/Application_1/newmain.cc:53:9 (0x08380040)
//              name = PQ_name:
//                loc = /export/home/nk220367/projects/Application_1/newmain.cc:55:5
//                name = "c"
//==============================================================================
//            fieldName = PQ_name:
//              loc = /export/home/nk220367/projects/AstProvider/test.cc:40:17
//              name = "f"
//------------------------------------------------------------------------------
//            fieldName = PQ_operator:
//              loc = <noloc>:1:1
//              o = ON_operator:
//                op = +
//              fakeName = "operator+"        
//==============================================================================        
//          args:
//          retObj is null
//------------------------------------------------------------------------------        
//          func = E_variable:
//            type: void ()()
//            var: void foo1(), at /export/home/nk220367/projects/Application_1/file.h:18:6 (0x08369520)
//==============================================================================                
//            name = PQ_name:
//              loc = /export/home/nk220367/projects/Application_1/newmain.cc:46:5
//              name = "foo1"        
//------------------------------------------------------------------------------                
//            name = PQ_qualifier:
//              loc = /usr/sfw/lib/gcc/i386-pc-solaris2.11/3.4.3/../../../../include/c++/3.4.3/bits/stl_iterator_base_funcs.h:118:9
//              qualifierVar: namespace std
//              qualifier = "std"
//              templArgs is null
//              rest = PQ_name:
//                loc = /usr/sfw/lib/gcc/i386-pc-solaris2.11/3.4.3/../../../../include/c++/3.4.3/bits/stl_iterator_base_funcs.h:118:14
//                name = "__iterator_category"
        if (name != null && value != null && value.equals("E_funCall")) {
            AstNode func = findChild("func", "E_fieldAcc", false);
            if (func != null) {
                AstNode fieldLoc = func.findChild("field");
                AstNode fieldName = func.findChild("fieldName", "PQ_name", false);
                if (fieldName != null && fieldLoc != null) {
                    AstNode nameLoc = fieldName.findChild("loc");
                    AstNode name1 = fieldName.findChild("name");
                    if (fieldLoc != null && nameLoc != null && name1 != null) {
//                        if (decl.name.equals(name1.value) && decl.pos.equals(new Offset(fieldLoc.value, fieldLoc.line))) {
//                            decl.usages.add(new Offset(nameLoc.value, name1.line));
//                        }
                        Declaration d = table.findFunction(new Offset(fieldLoc.value, fieldLoc.line), name1.value);
                        if (d != null) {
                            table.addFunctionUsage(d, new Offset(nameLoc.value, name1.line));
                        //d.usages.add(new Offset(nameLoc.value, name1.line));
                        }

                    }
                    return;
                }

                fieldName = func.findChild("fieldName", "PQ_operator", false);
                if (fieldName != null && fieldLoc != null) {
                    AstNode nameLoc = fieldName.findChild("loc");
                    AstNode name1 = fieldName.findChild("fakeName");
                    if (fieldLoc != null && nameLoc != null && name1 != null) {
                        //if (decl.name.equals(name1.value) && decl.pos.equals(new Offset(fieldLoc.value, fieldLoc.line))) {
                        Declaration d = table.findFunction(new Offset(fieldLoc.value, fieldLoc.line), name1.value);
                        if (d != null) {
                            Offset offset = new Offset(nameLoc.value, nameLoc.line);
                            if (offset.line == 0) {
                                AstNode obj = func.findChild("obj", "E_variable", false);
                                if (obj != null) {
                                    AstNode name2 = obj.findChild("name", "PQ_name");
                                    if (name2 != null) {
                                        AstNode loc = name2.findChild("loc");
                                        if (loc != null) {
//                                            decl.usages.add(findOperatorOffset(decl.name, new Offset(loc.value, loc.line)));

                                            table.addFunctionUsage(d, findOperatorOffset(d.name, new Offset(loc.value, loc.line)));
                                        //d.usages.add(findOperatorOffset(d.name, new Offset(loc.value, loc.line)));
                                        }

                                    }
                                }
                            } else {
                                table.addFunctionUsage(d, offset);
                            //d.usages.add(offset);
                            }
                        }
                    }
                }
                return;
            }

            func = findChild("func", "E_variable", false);
            if (func != null) {
                AstNode varLoc = func.findChild("var");
                AstNode name1 = func.findChild("name", "PQ_qualifier", false);
                if (name1 != null && varLoc != null) {
                    AstNode rest = name1.findChild("rest", "PQ_name", false);
                    if (rest != null) {
                        AstNode nameLoc = rest.findChild("loc");
                        AstNode name2 = rest.findChild("name");
                        if (varLoc != null && nameLoc != null && name2 != null) {
//                            if (decl.name.equals(name2.value) && decl.pos.equals(new Offset(varLoc.value, varLoc.line))) {
//                                decl.usages.add(new Offset(nameLoc.value, name2.line));
//                            }
                            Declaration d = table.findFunction(new Offset(varLoc.value, varLoc.line), name2.value);
                            if (d != null) {
                                table.addFunctionUsage(d, new Offset(nameLoc.value, name2.line));
                            //d.usages.add(new Offset(nameLoc.value, name2.line));
                            }

                        }
                    }
                    return;
                }

                name1 = func.findChild("name", "PQ_name", false);
                if (name1 != null && varLoc != null) {
                    AstNode nameLoc = name1.findChild("loc");
                    AstNode name2 = name1.findChild("name");
                    if (varLoc != null && nameLoc != null && name2 != null) {
//                        if (decl.name.equals(name2.value) && decl.pos.equals(new Offset(varLoc.value, varLoc.line))) {
//                            decl.usages.add(new Offset(nameLoc.value, name2.line));
//                        }
                        Declaration d = table.findFunction(new Offset(varLoc.value, varLoc.line), name2.value);
                        if (d != null) {
                            table.addFunctionUsage(d, new Offset(nameLoc.value, name2.line));
                        //d.usages.add(new Offset(nameLoc.value, name2.line));
                        }

                    }
                }
            }
        }
    }

    public Offset findOperatorOffset(
            String operator, Offset parentPos) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(parentPos.file));
            Lexer l = new Lexer(in);
            l.seekTo(parentPos.line, parentPos.row);

            operator =
                    operator.replaceAll("\"", "");
            operator =
                    operator.replaceAll("operator", "");

            for (int i = 0; i <
                    4; i++) {
                Token t = l.getNextToken();
                if (t.name.equals(operator)) {
                    return new Offset(parentPos.file + ":" + t.line + ":" + t.row, 0);
                }


            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AstNode.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("operator " + operator + " was not found after" + parentPos.file + ":" + parentPos.line + ":" + parentPos.row);

        return parentPos;
    }

    public void addVariableByDName(TokenTable table, AstNode node, AstNode var1, AstNode loc, String description) {
        AstNode decl = node.findChild("decl", "D_name", false);
        if (decl == null) {
            decl = node.findChild("base", "D_name");
        }

        if (decl != null) {
            decl = skipQualifiers(decl);

            AstNode name1 = decl.findChild("name", "PQ_name", false);
            if (name1 == null) {
                name1 = decl.findChild("rest", "PQ_name");
            }

            AstNode qualifierVar = decl.findChild("qualifierVar");
            if (name1 != null) {
                //AstNode loc = decl.findChild("loc");
                AstNode nameLoc = name1.findChild("loc");
                AstNode name2 = name1.findChild("name");
                if (loc != null && nameLoc != null && name2 != null) {
                    if (qualifierVar != null) {
                        description = "classifier-" + description;
                    }
                    Declaration var = new Declaration(name2.value, nameLoc.value, loc.value, TYPE.VARIABLE, name2.line, description);
                    if (qualifierVar != null) {
                        var.qualifierPos = new Offset(qualifierVar.value, qualifierVar.line);
                    }

                    if (var1 != null) {
                        var.fullName = var1.value;
                    }

                    table.addVariable(var);
                }

            }
        }
    }

    public void addFunctionByDName(TokenTable table, AstNode node, AstNode var, AstNode loc) {
        AstNode decl = node.findChild("decl", "D_func", false);
        if (decl == null) {
            decl = node.findChild("base", "D_func");
        }

        if (decl != null) {

            AstNode base = decl.findChild("base", "D_name");
            if (base != null) {

                base = skipQualifiers(base);

                AstNode qualifierVar = base.findChild("qualifierVar");

                AstNode rest = base.findChild("rest", "PQ_name", false);
                if (rest == null) {
                    rest = base.findChild("name", "PQ_name", false);
                }

                String description;
                
                if (rest != null) {
                    //AstNode loc = decl.findChild("loc");
                    AstNode nameLoc = rest.findChild("loc");
                    AstNode name2 = rest.findChild("name");
                    if (loc != null && nameLoc != null && name2 != null) {
                        if (qualifierVar != null) {
                            description = "method-definition";
                        } else {
                            description = "function-definition";
                        }
                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc.value, TYPE.FUNCTION, name2.line, description);
                        if (qualifierVar != null) {
                            fun.qualifierPos = new Offset(qualifierVar.value, qualifierVar.line);
                        }

                        if (var != null) {
                            fun.fullName = var.value;
                        }

                        table.addFunction(fun);
                    }

                    return;
                }

                rest = base.findChild("rest", "PQ_template", false);
                if (rest == null) {
                    rest = base.findChild("name", "PQ_template", false);
                }

                if (rest != null) {
                    //AstNode loc = decl.findChild("loc");
                    AstNode nameLoc = rest.findChild("loc");
                    AstNode name2 = rest.findChild("name");
                    if (loc != null && nameLoc != null && name2 != null) {
                        if (qualifierVar != null) {
                            description = "template-method-definition";
                        } else {
                            description = "template-function-definition";
                        }
                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc.value, TYPE.FUNCTION, name2.line, description);
                        if (qualifierVar != null) {
                            fun.qualifierPos = new Offset(qualifierVar.value, qualifierVar.line);
                        }

                        if (var != null) {
                            fun.fullName = var.value;
                        }

                        table.addFunction(fun);
                    }

                    return;
                }

                rest = base.findChild("rest", "PQ_operator", false);
                if (rest == null) {
                    rest = base.findChild("name", "PQ_operator");
                }

                if (rest != null) {
                    //AstNode loc = decl.findChild("loc");
                    AstNode nameLoc = rest.findChild("loc");
                    AstNode name2 = rest.findChild("fakeName");
                    if (loc != null && nameLoc != null && name2 != null) {
                        if (qualifierVar != null) {
                            description = "operator-definition";
                        } else {
                            description = "operator-definition";
                        }
                        Declaration fun = new Declaration(name2.value, nameLoc.value, loc.value, TYPE.FUNCTION, name2.line, description);
                        if (qualifierVar != null) {
                            fun.qualifierPos = new Offset(qualifierVar.value, qualifierVar.line);
                        }

                        if (var != null) {
                            fun.fullName = var.value;
                        }

                        table.addFunction(fun);
                    }

                }
            }
        }
    }

    public AstNode skipQualifiers(
            AstNode node) {
        if (node == null) {
            return node;
        }

        AstNode decl = node;
        if (decl.findChild("name", "PQ_qualifier", false) != null) {
            AstNode newdecl = decl.findChild("name", "PQ_qualifier", false);
            if (newdecl != null) {
                decl = newdecl;
            }

        }

        while (decl.findChild("rest", "PQ_qualifier", false) != null) {
            AstNode newdecl = decl.findChild("rest", "PQ_qualifier", false);
            if (newdecl != null) {
                decl = newdecl;
                continue;

            }




        }

        return decl;
    }

    public AstNode skipDDeclsToDNameorDFunc(
            AstNode node) {

        if (node == null) {
            return node;
        }

        AstNode decl = node;
        if (decl.findChild("decl", "D_reference", false) != null ||
                decl.findChild("decl", "D_array", false) != null ||
                decl.findChild("decl", "D_pointer", false) != null ||
                decl.findChild("decl", "D_ptrToMember", false) != null ||
                decl.findChild("decl", "D_grouping", false) != null) {
            AstNode newdecl = decl.findChild("decl", "D_reference", false);
            if (newdecl != null) {
                decl = newdecl;
            }
            newdecl = decl.findChild("decl", "D_array", false);
            if (newdecl != null) {
                decl = newdecl;
            }
            newdecl = decl.findChild("decl", "D_pointer", false);
            if (newdecl != null) {
                decl = newdecl;
            }
            newdecl = decl.findChild("decl", "D_grouping", false);
            if (newdecl != null) {
                decl = newdecl;
            }
            newdecl = decl.findChild("decl", "D_ptrToMember", false);
            if (newdecl != null) {
                decl = newdecl;
            }
        }

        while (decl.findChild("base", "D_reference", false) != null ||
                decl.findChild("base", "D_array", false) != null ||
                decl.findChild("base", "D_pointer", false) != null ||
                decl.findChild("base", "D_ptrToMember", false) != null ||
                decl.findChild("base", "D_grouping", false) != null) {

            AstNode newdecl = decl.findChild("base", "D_reference", false);
            if (newdecl != null) {
                decl = newdecl;
                continue;

            }
            newdecl = decl.findChild("base", "D_array", false);
            if (newdecl != null) {
                decl = newdecl;
                continue;

            }
            newdecl = decl.findChild("base", "D_pointer", false);
            if (newdecl != null) {
                decl = newdecl;
                continue;

            }
            newdecl = decl.findChild("base", "D_grouping", false);
            if (newdecl != null) {
                decl = newdecl;
                continue;

            }
            newdecl = decl.findChild("base", "D_ptrToMember", false);
            if (newdecl != null) {
                decl = newdecl;
                continue;

            }
        }
        return decl;
    }

    public void verifyUsages(TokenTable table) {

        for (AstNode astNode : children) {
            astNode.verifyUsages(table);
        }

        AstNode loc = findChild("loc");
        AstNode name1 = findChild("name");

        if (name1 != null && loc != null && !name1.value.startsWith("PQ_")) {
            Offset offset = new Offset(loc.value, name1.line);
            if (offset.line != 0) {
                table.verifyDeclaration(name1.value, offset, name1.line);
            }
        }
    }
}



