package cn.yangguobao.minutes.activity;

import java.io.Serializable;

public class OrderInfo implements Serializable{
	
	private String orderId;
	
	private String toUserId;

	private String toUserNickname;

	private String toUserTel;
	
	private String toUserGender;
	
	private String toUserToScore;
	
	private String fromUserId;

	private String fromUserNickname;

	private String fromUserTel;
	
	private String fromUserGender;
	
	private String fromUserFromScore;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public String getToUserNickname() {
		return toUserNickname;
	}

	public void setToUserNickname(String toUserNickname) {
		this.toUserNickname = toUserNickname;
	}

	public String getToUserTel() {
		return toUserTel;
	}

	public void setToUserTel(String toUserTel) {
		this.toUserTel = toUserTel;
	}

	public String getToUserGender() {
		return toUserGender;
	}

	public void setToUserGender(String toUserGender) {
		this.toUserGender = toUserGender;
	}

	public String getToUserToScore() {
		return toUserToScore;
	}

	public void setToUserToScore(String toUserToScore) {
		this.toUserToScore = toUserToScore;
	}





	public String getfromUserId() {
		return fromUserId;
	}

	public void setfromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getfromUserNickname() {
		return fromUserNickname;
	}

	public void setfromUserNickname(String fromUserNickname) {
		this.fromUserNickname = fromUserNickname;
	}

	public String getfromUserTel() {
		return fromUserTel;
	}

	public void setfromUserTel(String fromUserTel) {
		this.fromUserTel = fromUserTel;
	}

	public String getfromUserGender() {
		return fromUserGender;
	}

	public void setfromUserGender(String fromUserGender) {
		this.fromUserGender = fromUserGender;
	}

	public String getfromUserfromScore() {
		return fromUserFromScore;
	}

	public void setfromUserfromScore(String fromUserfromScore) {
		this.fromUserFromScore = fromUserFromScore;
	}
}

