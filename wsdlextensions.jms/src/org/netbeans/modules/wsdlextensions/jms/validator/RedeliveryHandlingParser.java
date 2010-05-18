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
package org.netbeans.modules.wsdlextensions.jms.validator;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.modules.wsdlextensions.jms.JMSConstants;

/**
 * Is used to deal with "poisonous messages". A poison message is a message that fails
 * to be processed time and time again, thereby stopping other messages from being
 * processed.
 *
 * Is invoked for each message. Maintains a cache of msgids of messages that have the
 * JMSRedelivered flag set, and keeps a count of these messages. Based on the number
 * of times a message was redelivered, a particular Action can be invoked. Actions are
 * delay, moving or deleting the message.
 *
 * The msgid cache is not persistent, nor is it shared between multiple activations. This
 * means that if a message was seen 10 times with the redelivered flag set, and the
 * project is undeployed, the redelivery count will be reset to zero. Also, if there are
 * multiple application servers reading from the same queue, a message may be redelivered
 * 10 times to one application server, and 10 times to the other application server, and
 * both activations will see a count of 10 instead of 20.
 *
 * The msgid cache is limited to 5000 (check source); when this limit is reached, the
 * oldest msgids are flushed from the cache. "Oldest" means least recently seen.
 *
 * Specification of the actions is done through a specially formatted string. The string
 * has this format:
 *    format := entry[; entry]*
 *    entry := idx ":" action
 *    idx := number (denotes the n-th time a msg was seen)
 *    action := number (denotes delay in ms) | "delete" | "move"(args)
 *    move := "queue"|"topic" | "same" ":" destname
 *    destname :=  any string, may include "$" which will be replaced with the original
 *        destination name.
 *
 * Examples:
 *     5:1000; 10:5000; 50:move(queue:mydlq)
 * This causes no delay up to the 5th delivery; a 1000 ms delay is invoked when the
 * message is seen the 5th, 6th, 7th, 8th, and 9th time. A 5 second delay is invoked
 * when the msg is invoked the 10th, 11th, ..., 49th time. When the msg is seen the 50th
 * time the msg is moved to a queue with the name "mydlq".
 *
 * If the messages were received from "Queue1" and if the string was specified as
 *     5:1000; 10:5000; 50:move(queue:dlq$oops)
 * the messages would be moved to the destination "dlqQueue1oops".
 *
 * Another example:
 *     5:1000; 10:5000
 * This causes no delay up to the 5th delivery; a 1000 ms delay is invoked when the
 * message is seen the 5th, 6th, 7th, 8th, and 9th time. A 5 second delay is invoked
 * for each time the message is seen thereafter.
 *
 * Moving messages is done in the same transaction if the transaction is XA. Moving
 * messages is done using auto-commit if the delivery is non-XA.
 *
 * Moving messages is done by creating a new message of the same type unless the
 * property JMSJCA.redeliveryRedirect is set to true in which case the messages are
 * simply redirected. In the first case, the payload of the new message is set as follows:
 * - for a ObjectMessage this will be done through getObject(), setObject();
 * - for a StreamMessage through readObject/writeObject,
 * - for a BytesMessage through readBytes() and writeBytes()
 *   (avoiding the getBodyLength() method new in JMS 1.1)
 * Copying the payload of an ObjectMessage may cause classloader problems since the
 * context classloader is not properly set. In this case the redelivery handler should
 * be configured to redirect the message instead.
 * The new message will have properties as follows:
 * * JMS properties
 * - JMSCorrelationID: copied
 * - JMSDestination: see above; set by JMS provider
 * - JMSExpiration: copied through the send method
 * - JMSMessageID: set by the JMS provider
 * - JMSPriority: set by the JMS provider; propagated through the send() method
 * - JMSRedelivered: NOT copied
 * - JMSReplyTo: copied
 * - JMSTimestamp: copied into the user property field JMSJCATimestamp
 * - JMSType: copied
 * - JMSDeliveryMode: set by the JMS provider; propagated through the send() method
 * * All user defined properties: copied
 * * Additional properties:
 * - JMS_Sun-JMSJCA.RedeliveryCount: number of times the message was seen with the redelivered
 *   flag set by JMSJCA. Will accurately reflect the total number of redelivery attempts
 *   only if there's one instance of the inbound adapter, and the inbound adapter was
 *   not redeployed.
 * - JMS_Sun-JMSJCA.OriginalDestinationName: name of the destination as specified in the
 *   activation spec
 * - JMS_Sun-JMSJCA.OriginalDestinationType: either "javax.jms.Queue" or "javax.jms.Topic"
 * - JMS_Sun-JMSJCA.SubscriberName: as specified in the activation spec
 * - JMS_Sun-JMSJCA.ContextName: as specified in the activation spec
 *
 * Invoking a delay takes place by holding the processing thread occupied, that means
 * that while the thread is sleeping, this thread will not be used to process any other
 * messages. Undeployment interrupts threads that are delaying message delivery. If a
 * msg delay is divisible by 1000, an INFO message is written to the log indicating that
 * the thead is delaying message delivery.
 *
 * There is a default behavior for message redelivery handling: see source.
 *
 * Implementation notes: this class is made abstract to enhance testability.
 *
 */
public class RedeliveryHandlingParser {

    /**
     * A baseclass of all actions that could happen in response to a a repeated
     * redelivered message
     */
    public abstract static class Action {
        private int mAt;

        /**
         * Constructor
         *
         * @param at at which encounter to invoke
         */
        public Action(int at) throws ValidationException {
            if (at <= 0) {
                throw new ValidationException("Index " + at + " should be > 0");
            }
            if (at > 5000) {
                throw new ValidationException("Index " + at + " should be <= 5000");
            }
            mAt = at;
        }

        /**
         * @return at which encounter to invoke
         */
        public int getAt() {
            return mAt;
        }

        /**
         * Asserts that the next value is greater than the previous value
         *
         * @param lastAt last value
         * @return current value of lastAt
         * @throws Exception on assertion failure
         */
        public int checkLast(int lastAt) throws Exception {
            if (lastAt == mAt) {
                throw new Exception("Duplicate entry at: " + lastAt);
            }
            if (lastAt >= mAt) {
                throw new Exception("Should be properly ordered: " + lastAt + " >= " + mAt);
            }
            return mAt;
        }
    }

    /**
     * No action; always at the beginning
     */
    public static class VoidAction extends Action {
        /**
         * Constructor
         */
        public VoidAction() throws ValidationException {
            super(1);
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "Void";
        }
    }

    /**
     * A delay action
     */
    public static class Delay extends Action {
        /**
         * How to recognize a delay
         */
        public static final String PATTERN = "(\\d+):\\s?(\\d+)";
        /**
         * Compiled regex pattern
         */
        public static Pattern sPattern = Pattern.compile(PATTERN);
        private long mDelay;

        /**
         * Constructor
         *
         * @param at when
         * @param delay how long (ms)
         * @throws Exception on invalid arguments
         */
        public Delay(int at, long delay) throws Exception {
            super(at);
            /* Not valid anymore
            if (delay > 5000 && delay != Integer.MAX_VALUE) {
                // Note: max_value is used for testing
                throw new Exception("Delay of [" + delay + "] exceeds maximum of 5000 ms");
            }
            */
            mDelay = delay;
        }

        /**
         * @return delay time in ms
         */
        public long getHowLong() {
            return mDelay;
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "At " + getAt() + ": delay for " + mDelay + " ms";
        }
    }

    /**
     * Moves a msg to a different queue or topic
     */
    public static class Move extends Action {
        /**
         * How to recognize a delay
         */
        public static final String PATTERN = "(\\d+):\\s?move\\((.*)\\)";
        /**
         * Compiled version
         */
        public static Pattern sPattern = Pattern.compile(PATTERN);

        /**
         * The argument pattern
         */
        public static String ARGPATTERN =  "(queue|same|topic)\\s?:\\s?(.+)";
        /**
         * Compiled version
         */
        public static Pattern sArgPattern = Pattern.compile(ARGPATTERN);

        private String mType; // either Queue or Topic
        private String mName;

        /**
         * @param at when to invoke
         * @param type destination type
         * @param name destination name
         * @param destinationType type from wsdl
         * @throws Exception on failure
         */
        public Move(int at, String type, String name, String destinationType) throws ValidationException {
            super(at);
            mName = name;
            if (!JMSConstants.QUEUE.equals(destinationType) && !JMSConstants.TOPIC.equals(destinationType)) {
                throw new ValidationException("Invalid destination type [" + destinationType + "]");
            }
            if ("same".equals(type)) {
                mType = destinationType;
            } else if ("queue".equals(type)) {
                mType = JMSConstants.QUEUE;
            } else if ("topic".equals(type)) {
                mType = JMSConstants.TOPIC;
            } else {
                throw new ValidationException("Invalid type [" + type + "]");
            }
        }

        /**
         * @return javax.jms.Queue or javax.jms.Topic
         */
        public String getDestinationType() {
            return mType;
        }

        /**
         * @return true if Queue
         */
        public boolean isQueue() {
            return mType.equals(JMSConstants.QUEUE);
        }

        /**
         * @return true if topic
         */
        public boolean isTopic() {
            return mType.equals(JMSConstants.TOPIC);
        }

        /**
         * @return destination name to use for DLQ
         */
        public String getDestinationName() {
            return mName;
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "At " + getAt() + ": move to " + mType + " with name [" + mName + "]";
        }
    }

    /**
     * Deletes a msg
     */
    public static class Delete extends Action {
        /**
         * How to recognize a delete
         */
        public static final String PATTERN = "(\\d+):\\s?delete";
        /**
         * Compiled
         */
        public static Pattern sPattern = Pattern.compile(PATTERN);

        /**
         * Constructor
         *
         * @param at when
         * @throws Exception on illegal argument
         */
        public Delete(int at) throws Exception {
            super(at);
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "At " + getAt() + ": delete";
        }
    }

    /**
     * @param actions action string
     * @return true if can be parsed properly
     */
    public static boolean checkValid(String actions) {
        try {
            parse(actions, "nothing", JMSConstants.QUEUE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parses an action string into separate actions and performs validations.
     * The returned action array is guaranteed to be ordered and without duplicates.
     *
     * @param s string to be parsed
     * @param destName destination name being used (for dlq name construction)
     * @param destType type from activation spec (javax.jms.Queue or javax.jms.Topic)
     * @return array of actions
     * @throws Exception upon parsing failure
     */
    public static Action[] parse(String s, String destName, String destType) throws Exception {
        if (s.trim().length() == 0) {
            return new Action[] {new VoidAction() };
        }

        // Split the string in different actions
        String[] actions = s.split("\\s*;\\s*");
        Action[] ret = new Action[actions.length];

        // Go over all actions and try to parse each action
        int lastAt = 0;
        for (int i = 0; i < actions.length; i++) {

            try {
                Matcher m;
                boolean last = i == (actions.length - 1);

                // Delay
                m = Delay.sPattern.matcher(actions[i]);
                if (m.matches()) {
                    String at = m.group(1);
                    String delay = m.group(2);
                    ret[i] = new Delay(Integer.parseInt(at), Long.parseLong(delay));
                    lastAt = ret[i].checkLast(lastAt);
                    continue;
                }

                // Delete
                m = Delete.sPattern.matcher(actions[i]);
                if (m.matches()) {
                    String at = m.group(1);

                    if (!last) {
                        throw new Exception("Move command should be last command");
                    }

                    ret[i] = new Delete(Integer.parseInt(at));
                    lastAt = ret[i].checkLast(lastAt);
                    continue;
                }

                // Move
                m = Move.sPattern.matcher(actions[i]);
                if (m.matches()) {
                    String at = m.group(1);
                    String guts = m.group(2);
                    Matcher g = Move.sArgPattern.matcher(guts);
                    if (!g.matches()) {
                        throw new Exception("Wrong arguments: should match " + Move.ARGPATTERN);
                    }
                    String type = g.group(1);
                    String name = g.group(2);

                    if (!last) {
                        throw new Exception("Move command should be last command");
                    }

                    name = name.replaceAll("\\$", destName);

                    ret[i] = new Move(Integer.parseInt(at), type, name, destType);
                    lastAt = ret[i].checkLast(lastAt);
                    continue;
                }

                throw new ValidationException("Action '" + actions[i] + "' is not a valid action");
            } catch (Exception e) {
                throw new ValidationException("Could not parse [" + s + "]: error [" + e
                    + "] in element number " + i + ": [" + actions[i] + "]", e);
            }
        }

        return ret;
    }
}
