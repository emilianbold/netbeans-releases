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

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author echou
 */
public class RedeliveryHandling {

    public enum ActionType { DELAY, DELETE, MOVE }
    public enum MoveType { QUEUE, TOPIC, SAME }

    private List<Action> actions  = new ArrayList<Action> ();

    public RedeliveryHandling() {

    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    private void addAction(Action action) {
        actions.add(action);
    }

    public static RedeliveryHandling parse(String s) throws Exception {
        RedeliveryHandling instance = new RedeliveryHandling();

        String[] firstLevelTokens = s.split("\\s*;\\s*"); // NOI18N
        for (String firstLevelToken : firstLevelTokens) {
            if (firstLevelToken.length() == 0) {
                continue;
            }
            Action action = new Action();
            String[] secondLevelTokens = firstLevelToken.split("\\s*:\\s*", 2); // NOI18N
            if (secondLevelTokens.length != 2) {
                throw new Exception("illegal token: " + firstLevelToken); // NOI18N
            }
            String timesSeenStr = secondLevelTokens[0];
            String actionStr = secondLevelTokens[1];
            action.timesSeen = Long.parseLong(timesSeenStr);
            if (actionStr.startsWith("delete")) { // NOI18N
                action.actionType = ActionType.DELETE;
            } else if (actionStr.startsWith("move")) { // NOI18N
                action.actionType = ActionType.MOVE;
                String moveArgStr = actionStr.replaceAll("move\\s*\\x28\\s*", "").replaceAll("\\s*\\x29", ""); // NOI18N
                String[] argTokens = moveArgStr.split("\\s*:\\s*"); // NOI18N
//                if (argTokens.length == 1) {
//                    if (argTokens[0].equals("same")) { // NOI18N
//                        action.moveType = MoveType.SAME;
//                    } else {
//                        throw new Exception("illegal token: " + argTokens[0]); // NOI18N
//                    }
//                } else 
                if (argTokens.length > 1) {
                    String moveType = argTokens[0];
                    if (moveType.equals("queue")) { // NOI18N
                        action.moveType = MoveType.QUEUE;
                        action.moveDestinationName = argTokens[1];
                    } else if (moveType.equals("topic")) { // NOI18N
                        action.moveType = MoveType.TOPIC;
                        action.moveDestinationName = argTokens[1];
                    } else if (moveType.equals("same")) { // NOI18N
                        action.moveType = MoveType.SAME;
                        action.moveDestinationName = argTokens[1];
                    } else {
                        throw new Exception("illegal token: " + moveType); // NOI18N
                    }
                }
            } else {
                action.actionType = ActionType.DELAY;
                action.delayTimeInMillis = Long.parseLong(actionStr);
            }

            instance.addAction(action);
        }

        return instance;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < actions.size(); i++) {
            sb.append(actions.get(i).toString());
            if (i < actions.size() - 1) {
                sb.append(";");
            }
        }

        return sb.toString();
    }

    public static class Action {

        public long timesSeen ;
        public ActionType actionType;
        public long delayTimeInMillis;
        public MoveType moveType;
        public String moveDestinationName;

        public Action() {

        }

        public String toString() {
            String str = timesSeen + ":"; // NOI18N
            if (actionType == ActionType.DELAY) {
                str = str + delayTimeInMillis;
            } else if (actionType == ActionType.DELETE) {
                str = str + "delete"; // NOI18N
            } else {
                str = str + "move"; // NOI18N
                if (moveType == MoveType.QUEUE) {
                    str = str + "(queue:" + moveDestinationName + ")"; // NOI18N
                } else if (moveType == MoveType.TOPIC){
                    str = str + "(topic:" + moveDestinationName + ")"; // NOI18N
                } else {
                    //str = str + "(same)"; // NOI18N
                    str = str + "(same:" + moveDestinationName + ")"; // NOI18N
                }
            }
            return str;
        }

    }

    public static void main(String[] args) {
        try {

            String expectedStr = "5:1000; 10:5000; 50:move(queue:dlq)";
            RedeliveryHandling redeliveryHandling = RedeliveryHandling.parse(expectedStr);
            System.out.println("expectedStr = " + expectedStr);
            System.out.println("resultStr   = " + redeliveryHandling.toString());
            System.out.println();

            expectedStr = "5:1000 ; 10:5000; 10:5000 ; 25:delete ;";
            redeliveryHandling = RedeliveryHandling.parse(expectedStr);
            System.out.println("expectedStr = " + expectedStr);
            System.out.println("resultStr   = " + redeliveryHandling.toString());
            System.out.println();

            expectedStr = "5:1000; 10:5000; 100:move(same)";
            redeliveryHandling = RedeliveryHandling.parse(expectedStr);
            System.out.println("expectedStr = " + expectedStr);
            System.out.println("resultStr   = " + redeliveryHandling.toString());
            System.out.println();

            expectedStr = "5:1000; 10:5000; 50:move(topic:queue2)";
            redeliveryHandling = RedeliveryHandling.parse(expectedStr);
            System.out.println("expectedStr = " + expectedStr);
            System.out.println("resultStr   = " + redeliveryHandling.toString());
            System.out.println();

            expectedStr = "50 : move (topic : abc) ;";
            redeliveryHandling = RedeliveryHandling.parse(expectedStr);
            System.out.println("expectedStr = " + expectedStr);
            System.out.println("resultStr   = " + redeliveryHandling.toString());
            System.out.println();

            expectedStr = "";
            redeliveryHandling = RedeliveryHandling.parse(expectedStr);
            System.out.println("expectedStr = " + expectedStr);
            System.out.println("resultStr   = " + redeliveryHandling.toString());
            System.out.println();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
