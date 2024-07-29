package com.test.api.pojo;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class WriteCell {

	private String no;
	private String cellName;
	private String context;
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getCellName() {
		return cellName;
	}
	public void setCellName(String cellName) {
		this.cellName = cellName;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	@Override
	public String toString() {
		return "WriteCell [no=" + no + ", cellName=" + cellName + ", context=" + context + "]";
	}
	public WriteCell(String no, String cellName, String context) {
		super();
		this.no = no;
		this.cellName = cellName;
		this.context = context;
	}
	public WriteCell() {
		super();
	}
	
}
