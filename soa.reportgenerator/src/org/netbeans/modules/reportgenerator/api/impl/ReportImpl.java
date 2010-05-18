package org.netbeans.modules.reportgenerator.api.impl;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.reportgenerator.api.Report;
import org.netbeans.modules.reportgenerator.api.ReportAttribute;
import org.netbeans.modules.reportgenerator.api.ReportBody;
import org.netbeans.modules.reportgenerator.api.ReportFooter;
import org.netbeans.modules.reportgenerator.api.ReportHeader;

public class ReportImpl implements Report {

	private String mName;
	
	private String mDescription;
	
	private Image mImage;
	
	private List<ReportAttribute> mAttributes = new ArrayList<ReportAttribute>();

	private ReportHeader mHeader;
	
	private ReportBody mBody;
	
	private ReportFooter mFooter;
	
	public String getName() {
		return this.mName;
	}

	public void setName(String name) {
		this.mName = name;
	}


	public ReportBody getBody() {
		return this.mBody;
	}

	public void setBody(ReportBody body) {
		this.mBody = body;
	}
	
	public ReportFooter getFooter() {
		return this.mFooter;
	}

	public void setFooter(ReportFooter footer) {
		this.mFooter = footer;
	}
	
	public ReportHeader getHeader() {
		return this.mHeader;
	}
	
	public void setHeader(ReportHeader header) {
		this.mHeader = header;
	}

	public String getDescription() {
		return this.mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public Image getOverviewImage() {
		return mImage;
	}

	public void setOverViewImage(Image image) {
		this.mImage = image;
	}
	
	public void addAttribute(ReportAttribute attr) {
		this.mAttributes.add(attr);
	}

	public List<ReportAttribute> getAttributes() {
		return this.mAttributes;
	}

	public void removeAttribute(ReportAttribute attr) {
		this.mAttributes.remove(attr);
	}

		
}
