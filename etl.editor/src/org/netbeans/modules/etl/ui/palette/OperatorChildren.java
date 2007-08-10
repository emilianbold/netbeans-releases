/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
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