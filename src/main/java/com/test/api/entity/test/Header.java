package com.test.api.entity.test;

import java.util.List;

public class Header {
	private String name;
	private List<Element> elementList;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Element> getElementList() {
		return elementList;
	}
	public void setElementList(List<Element> elementList) {
		this.elementList = elementList;
	}
	public Header(String name, List<Element> elementList) {
		super();
		this.name = name;
		this.elementList = elementList;
	}
	@Override
	public String toString() {
		return "Header [name=" + name + ", elementList=" + elementList + "]";
	}
	public Header() {
		super();
	}

	
}
