/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 *
 */

package org.netbeans.modules.etl.ui.palette;

import java.util.ArrayList;
import java.util.List;

import org.openide.nodes.Index;
import org.openide.nodes.Node;

/**
 *
 * @author nithya
 */
public class OperatorChildren extends Index.ArrayChildren {

    private Category category;

    private String[][] items = new String[][]{
        {"0", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/numberToHex.png", "Number To Hex"},
        {"1", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/leftTrim.png", "Left Trim"},
        {"2", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/length.png", "Length"},        
        {"3", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/replace.gif", "Replace"},
        {"4", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/rightTrim.png", "Right Trim"},
        {"5", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/stringToHex.png", "String to Hex"},
        {"6", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/substring.gif", "Substring"},
        {"7", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/lowercase.gif", "Lowercase"},
        {"8", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/uppercase.gif", "Uppercase"},
        {"9", "String Operators", "org/netbeans/modules/sql/framework/ui/resources/images/concat.gif", "Concatenation"},
        
        {"10", "Relational Operators", "org/netbeans/modules/sql/framework/ui/resources/images/equal.png", "Equal"},
        {"11", "Relational Operators", "org/netbeans/modules/sql/framework/ui/resources/images/greater_than.png", "Greater Than"},
        {"12", "Relational Operators", "org/netbeans/modules/sql/framework/ui/resources/images/greater_equal.png", "Greater Equal"},        
        {"13", "Relational Operators", "org/netbeans/modules/sql/framework/ui/resources/images/lesser_equal.png", "Lesser Equal"},
        {"14", "Relational Operators", "org/netbeans/modules/sql/framework/ui/resources/images/lesser_than.png", "Lesser Than"},
        {"15", "Relational Operators", "org/netbeans/modules/sql/framework/ui/resources/images/not_equal.png", "Not Equal"}, 
        
        {"16",  "SQL Operators", "org/netbeans/modules/sql/framework/ui/resources/images/Case.png", "Case"},
        {"17",  "SQL Operators", "org/netbeans/modules/sql/framework/ui/resources/images/castAs.png", "Cast As"},
        {"18",  "SQL Operators", "org/netbeans/modules/sql/framework/ui/resources/images/coalesce.png", "Coalesce"},        
        {"19",  "SQL Operators", "org/netbeans/modules/sql/framework/ui/resources/images/Count.png", "Count"},
        {"20",  "SQL Operators", "org/netbeans/modules/sql/framework/ui/resources/images/literal.png", "Literal"},
        {"21",  "SQL Operators", "org/netbeans/modules/sql/framework/ui/resources/images/null.png", "Null"},
        {"22",  "SQL Operators", "org/netbeans/modules/sql/framework/ui/resources/images/nullif.png", "Null If"},
        
        {"23",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/SUM.png", "Sum"},
        {"24",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/average.png", "Average"},
        {"25",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/max.png", "Max"},        
        {"26",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/min.png", "Min"},
        {"27",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/modulo.png", "Modulo"},
        {"28",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/multiplication.png", "Multiplication"},
        {"29",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/sign.png", "Sign"},
        {"30",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/subtraction.gif", "Subtraction"},
        {"31",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/division.png", "Division"},
        {"32",  "Function Operators", "org/netbeans/modules/sql/framework/ui/resources/images/addition.gif", "Addition"},
        
        {"33",  "Date Operators", "org/netbeans/modules/sql/framework/ui/resources/images/datePart.png", "DatePart"},
        {"34",  "Date Operators", "org/netbeans/modules/sql/framework/ui/resources/images/DateToChar.png", "DateToChar"},
        {"35",  "Date Operators", "org/netbeans/modules/sql/framework/ui/resources/images/DateAddition.png", "Date Addition"},        
        {"36",  "Date Operators", "org/netbeans/modules/sql/framework/ui/resources/images/DateSubtraction.png", "Date Subtraction"},
        {"37",  "Date Operators", "org/netbeans/modules/sql/framework/ui/resources/images/CharToDate.png", "CharToDate"},
        {"38",  "Date Operators", "org/netbeans/modules/sql/framework/ui/resources/images/NOW2.png", "Now"},
      
        {"39",   "Cleansing Operators", "org/netbeans/modules/sql/framework/ui/resources/images/normalizePerson.png", "Normalize Name"},
        {"40",   "Cleansing Operators", "org/netbeans/modules/sql/framework/ui/resources/images/parseAddress.png", "Parse Address"},
        {"41",   "Cleansing Operators", "org/netbeans/modules/sql/framework/ui/resources/images/parseBusinessName.png", "Parse Business Name"} ,
        {"42",   "Cleansing Operators", "org/netbeans/modules/sql/framework/ui/resources/images/Join.png", "Join"} ,
          
    };

    /**
     * 
     * @param Category 
     */
    public OperatorChildren(Category Category) {
        this.category = Category;
    }

    /**
     * 
     * @return childrenNodes List<Node>
     */
    @Override
    protected java.util.List<Node> initCollection() {
        List<Node> childrenNodes = new ArrayList<Node>( items.length );
        for( int i=0; i<items.length; i++ ) {
            if( category.getName().equals( items[i][1] ) ) {
                Operator item = new Operator();
                item.setNumber(new Integer(items[i][0]));
                item.setCategory(items[i][1]);
                item.setImage(items[i][2]);
                item.setName(items[i][3]);
                childrenNodes.add(new OperatorNode(item));
            }
        }
        return childrenNodes;
    }

}
