package org.netbeans.modules.reportgenerator.api.impl.pdf;

import com.lowagie.text.BadElementException;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.reportgenerator.api.Report;
import org.netbeans.modules.reportgenerator.api.ReportAttribute;
import org.netbeans.modules.reportgenerator.api.ReportBody;
import org.netbeans.modules.reportgenerator.api.ReportElement;
import org.netbeans.modules.reportgenerator.api.ReportException;
import org.netbeans.modules.reportgenerator.api.ReportFooter;
import org.netbeans.modules.reportgenerator.api.ReportGenerator;
import org.netbeans.modules.reportgenerator.api.ReportHeader;
import org.netbeans.modules.reportgenerator.api.ReportSection;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.reportgenerator.api.ReportCustomizationOptions;
import org.netbeans.modules.reportgenerator.generator.ReportGeneratorFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PDFReportGenerator implements ReportGenerator {

	private OutputStream mReportStream;
	
	private File mReportFile;
	
	private Document mDocument;
	
        private ReportCustomizationOptions mOptions;
	public PDFReportGenerator(File reportFile, ReportCustomizationOptions options) {
		this.mReportFile = reportFile;
                this.mOptions = options;
	}
	
	public void generateReport(Report report) throws ReportException {
		
		    if(report == null) {
		    	return;
		    }
		    
			// step 1: creation of a document-object
			mDocument = new Document();
        
            try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            FileOutputStream fStream = new FileOutputStream(this.mReportFile);	
            PdfWriter.getInstance(mDocument, fStream);
            
            // step 3: we open the document
            mDocument.open();
            
            genReport(report);
            
        }catch(DocumentException de) {
            System.err.println(de.getMessage());
        } catch (FileNotFoundException ex) {
        	System.err.println(ex.getMessage());
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        // step 5: we close the document
        mDocument.close();
        
        if(this.mReportFile.exists()) {
            FileObject pdfFile =  FileUtil.toFileObject(this.mReportFile);
            //call getOwner so that first time when wsdl is
            //created project can refresh and show it immediately
            FileOwnerQuery.getOwner(pdfFile);
        }
        
	}

	private void genReport(Report report) throws DocumentException, IOException {
		//report name is the heading;
		String reportName = report.getName();
		if(report.getName() != null) {
			Font reportNameFont = FontFactory.getFont(FontFactory.HELVETICA, 20, Font.NORMAL, Color.BLUE);
			Paragraph sTitle = new Paragraph(reportName, reportNameFont);
			sTitle.setAlignment(Paragraph.ALIGN_CENTER);
			
			mDocument.add(sTitle);
//			
			//add a gap
			Paragraph gap = new Paragraph("     ");
			mDocument.add(gap);
			
			
			
		}
		//we add a paragraph to the document
		mDocument.add(new Paragraph(report.getDescription()));
		
		Image image = report.getOverviewImage();
		if(image != null) {
			com.lowagie.text.Image png = com.lowagie.text.Image.getInstance(image, null);
			mDocument.add(png);
		}
		
                boolean generateVerboseReport = this.mOptions.isGenerateVerboseReport();
		//generate report level attributes.
                if(generateVerboseReport) {
                    generateReportAttributes(report);
                }
		
		
		//generate header
		ReportHeader header = report.getHeader();
		if(header != null) {
			generateHeader(header);
		}
		
		//generate body
		ReportBody body = report.getBody();
		if(body != null) {
			generateBody(body);
		}
		
		//generate footer
		ReportFooter footer = report.getFooter();
		if(footer != null) {
			generateFooter(footer);
		}
	}
	
	private void generateReportAttributes(Report report) throws DocumentException {
		List<ReportAttribute> attrs = report.getAttributes();
		if(attrs.size() > 0) {
			//add a gap
			Paragraph gap = new Paragraph("     ");
			mDocument.add(gap);
			
			float[] widths = {2f, 4f};
			PdfPTable table = new PdfPTable(widths);
			
			Iterator<ReportAttribute> it = attrs.iterator();
			while(it.hasNext()) {
				ReportAttribute rAtttribute = it.next();
				generateAttribute(rAtttribute, table);
			}
			
			//now add table to document
			mDocument.add(table);
		}
		
		
	}
	
	private void generateHeader(ReportHeader header) throws DocumentException {
		//step 4: we add a paragraph to the document
		mDocument.add(new Paragraph(header.getDescription()));
	}
	
	private void generateBody(ReportBody body) throws BadElementException, IOException, DocumentException {
		List<ReportSection> sections = body.getReportSection();
		Iterator<ReportSection> it = sections.iterator();
		
		while(it.hasNext()) {
			ReportSection section = it.next();
			generateSection(section, sections.indexOf(section) +1);
		}
		
	}
	
	private void generateSection(ReportSection section, int sectionIndex) throws BadElementException, IOException, DocumentException {
		String description = section.getDescription();
		Image image = section.getImage();
		
		Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.BLACK);
        
		
		Chapter chapter = null;
		
		if(description != null) {
				PdfPTable table = new PdfPTable(1);
				Paragraph cTitle = new Paragraph(description, chapterFont);
				chapter = new Chapter(cTitle, sectionIndex);
				
				if(image != null) {
					com.lowagie.text.Image sectionImg = com.lowagie.text.Image.getInstance(image, null);
					PdfPCell cell = new PdfPCell(sectionImg);
					table.addCell(cell);
					chapter.add(table);
					
					
				}
				
		} else if (image != null) {
			chapter = new Chapter("", sectionIndex);
			com.lowagie.text.Image sectionImg = com.lowagie.text.Image.getInstance(image, null);
			chapter.add(sectionImg);
		}
		
		
		//go throw report elements
		List<ReportElement> elements = section.getReportElements();
		Iterator<ReportElement> rIt = elements.iterator();
		while(rIt.hasNext()) {
			ReportElement rElement = rIt.next();
			//TODO: use visitor then if else
			if(rElement instanceof ReportSection) {
				generateSection((ReportSection)rElement, elements.indexOf(rElement));
			} else {
                                boolean isIncludeOnlyElementsWithDocumentation = this.mOptions.isIncludeOnlyElementsWithDocumentation();
                                if(isIncludeOnlyElementsWithDocumentation) {
                                    if(rElement.getDescription() != null) {
                                        generateElement(rElement, chapter);
                                    }
                                } else {
                                    generateElement(rElement, chapter);
                                }
			}
		}
		
		mDocument.add(chapter);
	}
	
	
	private void generateElement(ReportElement element, Chapter chapter) throws BadElementException, DocumentException, IOException {
		String name = element.getName();
		String description = element.getDescription();
		Image image = element.getImage();
	
		Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, Color.BLUE);
        
		Section section = null;
		
		if(name == null) {
			name = "";
		}
		
		Paragraph sTitle = new Paragraph(name, sectionFont);
		section = chapter.addSection(sTitle, 2);
        Paragraph gap1 = new Paragraph("     ");
		section.add(gap1);
		section.setIndentationLeft(20);
		
		if(description != null) {
			Paragraph desPara = new Paragraph(description);
			section.add(desPara);
			
			//add a gap
			Paragraph gap2 = new Paragraph("     ");
			section.add(gap2);
			
		}
		
		if(image != null) {
			PdfPTable table = new PdfPTable(1);
			
			com.lowagie.text.Image sectionImg = com.lowagie.text.Image.getInstance(image, null);
			sectionImg.setAlignment(Element.ALIGN_CENTER);
			
			PdfPCell cell = new PdfPCell(sectionImg);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			
			table.addCell(cell);
			
			section.add(table);
		}
		
//		if(name != null) {
//			PdfPTable table = null;
//			if(image != null && description != null) {
//				table = new PdfPTable(2);
//			} else {
//				table = new PdfPTable(1);
//			}
//			
//			Paragraph sTitle = new Paragraph(name, sectionFont);
//            section = chapter.addSection(0.3f, sTitle, 1);
//            Paragraph gap = new Paragraph("     ");
//    		section.add(gap);
//    		//section.setIndentationLeft(50);
//    		
//            if(description != null) {
//				Paragraph desPara = new Paragraph(description);
//				PdfPCell cell = new PdfPCell(desPara);
//				table.addCell(cell);
//			}
//            
//			if(image != null) {
//				com.lowagie.text.Image sectionImg = com.lowagie.text.Image.getInstance(image, null);
//				sectionImg.setAlignment(Element.ALIGN_CENTER);
//				PdfPCell cell = new PdfPCell(sectionImg);
//				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//				cell.setVerticalAlignment(Element.ALIGN_CENTER);
//				
//				table.addCell(cell);
//			}
//			
//			
//			
//			section.add(table);
//		} else if (image != null) {
//			section = chapter.addSection("", 1);
//            com.lowagie.text.Image sectionImg = com.lowagie.text.Image.getInstance(image, null);
//            section.add(sectionImg);
//		}
		
		//go through attributes
                boolean generateVerboseReport = this.mOptions.isGenerateVerboseReport();
                if(generateVerboseReport) {
                    float[] widths = {2f, 4f};
                    PdfPTable table = new PdfPTable(widths);

                    List<ReportAttribute> attrs = element.getAttributes();
                    Iterator<ReportAttribute> attrsIt = attrs.iterator();
                    while(attrsIt.hasNext()) {
                            ReportAttribute rAtttribute = attrsIt.next();
                            generateAttribute(rAtttribute, table);
                    }

                    section.add(table);
                }
                
		Paragraph gap = new Paragraph("     ");
		section.add(gap);
                
		
	}
	
	
	private void generateAttribute(ReportAttribute attribute, PdfPTable table) {
		String name = attribute.getName();
		Object value = attribute.getValue();
		
		PdfPCell cell1 = new PdfPCell(new Paragraph(name));
		cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell1);
		
		PdfPCell cell2 = null;
		
		if(value instanceof String) {
			cell2 = new PdfPCell(new Paragraph(value.toString()));
		} else {
			cell2 = new PdfPCell(new Paragraph(value.toString()));
		}
		
		cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell2);
	}
	
	private void generateFooter(ReportFooter footer) throws DocumentException {
		mDocument.add(new Paragraph(footer.getDescription()));
	}

	
}
