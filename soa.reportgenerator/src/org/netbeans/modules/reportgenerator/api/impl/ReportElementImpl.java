package org.netbeans.modules.reportgenerator.api.impl;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.reportgenerator.api.ReportAttribute;
import org.netbeans.modules.reportgenerator.api.ReportElement;

public class ReportElementImpl extends ReportNodeImpl implements ReportElement {

	
	private List<ReportAttribute> mAttributes = new ArrayList<ReportAttribute>();
	
	private String mName;
	
	private String mDescription;
	
	private Image mImage;
	
	public void addAttribute(ReportAttribute attr) {
		this.mAttributes.add(attr);
	}

	public List<ReportAttribute> getAttributes() {
		return this.mAttributes;
	}

	public void removeAttribute(ReportAttribute attr) {
		this.mAttributes.remove(attr);
	}


    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Image getImage() {
        return this.mImage;
    }

    public void setImage(Image image) {
        this.mImage = image;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

}
