package com.test.api.entity;

import java.util.List;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* Class description
*/
public class JobExt {
	String nameSpace;
	List<Job> joblist;
	public String getNameSpace() {
		return nameSpace;
	}
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}
	public List<Job> getJoblist() {
		return joblist;
	}
	public void setJoblist(List<Job> joblist) {
		this.joblist = joblist;
	}
	public JobExt(String nameSpace, List<Job> joblist) {
		super();
		this.nameSpace = nameSpace;
		this.joblist = joblist;
	}
	public JobExt() {
		super();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobExt [nameSpace=" + nameSpace + ", joblist=" + joblist + "]";
	}
	

}
