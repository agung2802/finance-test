package com.test.api.entity.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Element {

	private String name;
	private String value;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Element(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	public Element() {
		super();
	}
	@Override
	public String toString() {
		return "Element [name=" + name + ", value=" + value + "]";
	}
	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("q");
		list.add("a");
		list.add("b");
		Iterator<String> iterator = list.iterator();
		while(iterator.hasNext()) {
			String next = iterator.next();
			if(next.equalsIgnoreCase("a")) {
				System.out.println("======");
				iterator.remove();
				
			}
			System.out.println("iterator.next()"+next);
		}
		list.forEach(x->System.out.println(x));

	}
	
}
