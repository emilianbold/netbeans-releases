/*
 * QuestionDialogOperator.java
 *
 * Created on 9/23/02 12:32 PM
 */

package org.netbeans.jellytools;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.NbDialogOperator;

/** Class implementing all necessary methods for handling question dialog.
 *
 * @author Jiri Kovalsky
 * @version 1.0
 */
public class QuestionDialogOperator extends NbDialogOperator {

    /** Creates new QuestionDialogOperator that can handle it.
     */
    public QuestionDialogOperator(String question) {
        super("Question");
        new JLabelOperator(this, question);
    }

    /** Performs simple test of QuestionDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new QuestionDialogOperator("Are you sure you want to remove the last revision of the file \"A_File.java\"?");
        System.out.println("QuestionDialogOperator verification finished.");
    }
}