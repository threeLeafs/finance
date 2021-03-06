package com.waben.stock.datalayer.publisher.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.waben.stock.datalayer.publisher.entity.enumconverter.CapitalFlowTypeConverter;
import com.waben.stock.interfaces.enums.CapitalFlowType;

/**
 * @author Created by yuyidi on 2017/11/10.
 * @desc 资金流水
 */
@Entity
@Table(name = "capital_flow")
public class CapitalFlow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	/**
	 * 流水号
	 */
	@Column(name = "flow_no")
	private String flowNo;
	/**
	 * 金额
	 */
	@Column(name = "amount")
	private BigDecimal amount;
	/**
	 * 流水类型
	 */
	@Column(name = "type")
	@Convert(converter = CapitalFlowTypeConverter.class)
	private CapitalFlowType type;
	/**
	 * 备注
	 */
	@Column(name = "remark")
	private String remark;
	/**
	 * 产生时间
	 */
	@Column(name = "occurrence_time")
	private Date occurrenceTime;

	@Column(name = "publisher_id")
	private Long publisherId;
	/**
	 * 发布人ID
	 */
	@JoinColumn(name = "publisher_id",insertable = false,updatable = false)
	@ManyToOne
	private Publisher publisher;
	/**
	 * 发布人序列号
	 */
	@Column(name = "publisher_serial_code")
	private String publisherSerialCode;
	/**
	 * 流水扩展列表
	 */
	@JsonManagedReference
	@OneToMany(mappedBy = "flow")
	private Set<CapitalFlowExtend> extendList = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public CapitalFlowType getType() {
		return type;
	}

	public void setType(CapitalFlowType type) {
		this.type = type;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getOccurrenceTime() {
		return occurrenceTime;
	}

	public void setOccurrenceTime(Date occurrenceTime) {
		this.occurrenceTime = occurrenceTime;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public String getPublisherSerialCode() {
		return publisherSerialCode;
	}

	public void setPublisherSerialCode(String publisherSerialCode) {
		this.publisherSerialCode = publisherSerialCode;
	}

	public Set<CapitalFlowExtend> getExtendList() {
		return extendList;
	}

	public void setExtendList(Set<CapitalFlowExtend> extendList) {
		this.extendList = extendList;
	}

	public String getCapitalFlowType(){
		String capitalFlowType = null;
		if(type != null){
			capitalFlowType = type.getType();
		}
		return capitalFlowType;
	}
	
	public String getFlowNo() {
		return flowNo;
	}

	public void setFlowNo(String flowNo) {
		this.flowNo = flowNo;
	}

 	public String getPublisherPhone() {
		return publisher.getPhone();
	}

	public Long getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(Long publisherId) {
		this.publisherId = publisherId;
	}
}
