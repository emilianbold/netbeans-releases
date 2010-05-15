package org.netbeans.modules.reportgenerator.api;

import java.util.List;

/**
 * Base interface for Report objects
 * @author radval
 *
 */
public interface ReportNode {

	List<ReportNode> getChildren();
	
	void addChild(ReportNode child);
	
	void removeChild(ReportNode child);
}
