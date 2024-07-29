package com.test.api.entity;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* Class description
*/
public class Job {

	String jobName;
	String jobClass;
	String jobType;
	String cron;
	String shardingTotalCount;
	String shardingItemParameters;
	String jobParameter;
	String failover;
	String description;
	String misfire;
	String jobProperties;
	String nameSpace;
	String env;
	
	public Job(String jobName, String jobParameter, String nameSpace) {
		super();
		this.jobName = jobName;
		this.jobParameter = jobParameter;
		this.nameSpace = nameSpace;
	}
	public Job(String jobName, String jobClass, String jobType, String cron, String shardingTotalCount,
			String shardingItemParameters, String jobParameter, String failover, String description, String misfire,
			String jobProperties) {
		super();
		this.jobName = jobName;
		this.jobClass = jobClass;
		this.jobType = jobType;
		this.cron = cron;
		this.shardingTotalCount = shardingTotalCount;
		this.shardingItemParameters = shardingItemParameters;
		this.jobParameter = jobParameter;
		this.failover = failover;
		this.description = description;
		this.misfire = misfire;
		this.jobProperties = jobProperties;
	}
	public Job() {
		super();
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobClass() {
		return jobClass;
	}
	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	public String getShardingTotalCount() {
		return shardingTotalCount;
	}
	public void setShardingTotalCount(String shardingTotalCount) {
		this.shardingTotalCount = shardingTotalCount;
	}
	public String getShardingItemParameters() {
		return shardingItemParameters;
	}
	public void setShardingItemParameters(String shardingItemParameters) {
		this.shardingItemParameters = shardingItemParameters;
	}
	public String getJobParameter() {
		return jobParameter;
	}
	public void setJobParameter(String jobParameter) {
		this.jobParameter = jobParameter;
	}
	public String getFailover() {
		return failover;
	}
	public void setFailover(String failover) {
		this.failover = failover;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMisfire() {
		return misfire;
	}
	public void setMisfire(String misfire) {
		this.misfire = misfire;
	}
	public String getJobProperties() {
		return jobProperties;
	}
	public void setJobProperties(String jobProperties) {
		this.jobProperties = jobProperties;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Job [jobName=" + jobName + ", jobClass=" + jobClass + ", jobType=" + jobType + ", cron=" + cron
				+ ", shardingTotalCount=" + shardingTotalCount + ", shardingItemParameters=" + shardingItemParameters
				+ ", jobParameter=" + jobParameter + ", failover=" + failover + ", description=" + description
				+ ", misfire=" + misfire + ", jobProperties=" + jobProperties + ", nameSpace=" + nameSpace + ", env="
				+ env + "]";
	}
	/**
	 * @return the nameSpace
	 */
	public String getNameSpace() {
		return nameSpace;
	}
	/**
	 * @param nameSpace the nameSpace to set
	 */
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}
	/**
	 * @param jobName
	 * @param jobClass
	 * @param jobType
	 * @param cron
	 * @param shardingTotalCount
	 * @param shardingItemParameters
	 * @param jobParameter
	 * @param failover
	 * @param description
	 * @param misfire
	 * @param jobProperties
	 * @param nameSpace
	 */
	public Job(String jobName, String jobClass, String jobType, String cron, String shardingTotalCount,
			String shardingItemParameters, String jobParameter, String failover, String description, String misfire,
			String jobProperties, String nameSpace) {
		super();
		this.jobName = jobName;
		this.jobClass = jobClass;
		this.jobType = jobType;
		this.cron = cron;
		this.shardingTotalCount = shardingTotalCount;
		this.shardingItemParameters = shardingItemParameters;
		this.jobParameter = jobParameter;
		this.failover = failover;
		this.description = description;
		this.misfire = misfire;
		this.jobProperties = jobProperties;
		this.nameSpace = nameSpace;
	}
	/**
	 * @return the env
	 */
	public String getEnv() {
		return env;
	}
	/**
	 * @param env the env to set
	 */
	public void setEnv(String env) {
		this.env = env;
	}
	/**
	 * @param jobName
	 * @param jobClass
	 * @param jobType
	 * @param cron
	 * @param shardingTotalCount
	 * @param shardingItemParameters
	 * @param jobParameter
	 * @param failover
	 * @param description
	 * @param misfire
	 * @param jobProperties
	 * @param nameSpace
	 * @param env
	 */
	public Job(String jobName, String jobClass, String jobType, String cron, String shardingTotalCount,
			String shardingItemParameters, String jobParameter, String failover, String description, String misfire,
			String jobProperties, String nameSpace, String env) {
		super();
		this.jobName = jobName;
		this.jobClass = jobClass;
		this.jobType = jobType;
		this.cron = cron;
		this.shardingTotalCount = shardingTotalCount;
		this.shardingItemParameters = shardingItemParameters;
		this.jobParameter = jobParameter;
		this.failover = failover;
		this.description = description;
		this.misfire = misfire;
		this.jobProperties = jobProperties;
		this.nameSpace = nameSpace;
		this.env = env;
	}
	
}
