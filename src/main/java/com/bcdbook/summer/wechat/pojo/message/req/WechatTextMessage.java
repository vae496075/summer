package com.bcdbook.summer.wechat.pojo.message.req;

/**
 * @Description: 用于接收的文本消息
 * @author lason
 * @date 2016年9月19日
 */
public class WechatTextMessage extends WechatMessage {
	private String Content;//文本消息内容

	//空参构造
	public WechatTextMessage() {
		super();
	}
	//全参构造
	public WechatTextMessage(String content) {
		super();
		Content = content;
	}

	//getter and setter
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	
	@Override
	public String toString() {
		return "WechatTextMessage ["
				+ (Content != null ? "Content=" + Content : "") + "]";
	}
}
