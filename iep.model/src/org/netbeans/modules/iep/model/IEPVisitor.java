package org.netbeans.modules.iep.model;




/**
 *
 * 
 */
public interface IEPVisitor {
        
        void visit(Component component);
        
        void visit(Property property);
        
	/*void visitTasks (TTasks tasks);
	void visitTask (TTask task);
	void visitImport (TImport importEl);
	void visitAssignment (TAssignment assignment);
	void visitTimeout (TTimeout timeout);
	void visitEscalation (TEscalation escalation);
	void visitNotification (TNotification notification);
	void visitUser (User user);
	void visitRole (Role role);
	void visitGroup (Group group);
	void visitAction (TAction action);
	void visitRecipient (TRecipient recipient);
	void visitAddress (TAddress address);
	void visitDeadLine (TDeadlineExpr deadline);
	void visitDuration (TDurationExpr duration);
        */
}
