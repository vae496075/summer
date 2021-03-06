package com.bcdbook.summer.wechat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bcdbook.summer.common.backmsg.BackMsg;
import com.bcdbook.summer.common.context.ContextParameter;
import com.bcdbook.summer.common.util.JsonUtil;
import com.bcdbook.summer.common.util.SHA1;
import com.bcdbook.summer.common.util.StringUtils;
import com.bcdbook.summer.wechat.pojo.Wechat;
import com.bcdbook.summer.wechat.pojo.message.resp.NewsMessage;
import com.bcdbook.summer.wechat.pojo.message.resp.Article;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class WechatUtil {

	private static Logger logger = Logger.getLogger(WechatUtil.class);
	/**
	 * 
	 * @Discription 处理getter请求,传入一个连接地址,返回相应格式的字符串值
	 * @author lason
	 * @created 2016年5月24日 下午3:12:15
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String wechatGet(String url) {
		//验证参数合法性
		if(StringUtils.isNull(url))
			return null;
		
		//定义返回值
		String backValue = null;
		
		// 创建地址对象
		try {
			URL urlGet = new URL(url);
			//开启连接
			HttpURLConnection http = (HttpURLConnection) urlGet.openConnection();
			
			//连接参数的相关配置
			http.setRequestMethod("GET"); //定义请求方式
			http.setRequestProperty("Content-Type","application/x-www-form-urlencoded");//设置连接特性
			http.setDoOutput(true);//是否可以输出
			http.setDoInput(true);//是否可以写入
			System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒
			System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒
			http.connect();//执行连接
			
			//创建读取流,从流中读取字节
			InputStream is = http.getInputStream();
			//防止相应不及时的问题
			int size = 0;
			while (size == 0) {
				//获取文件字节的大小
				size = is.available();
			}
			byte[] jsonBytes = new byte[size];//根据字节流的大小,创建一个字节数组
			is.read(jsonBytes);//执行读取操作
			backValue = new String(jsonBytes, "UTF-8");//把读到的字节,转成字符串(编码是UTF-8)
		} catch (MalformedURLException e) {
			logger.info("WechatUtil GET 请求 MalformedURLException");
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("WechatUtil GET 请求 IOException");
			e.printStackTrace();
		}
//		System.out.println("backValue:"+backValue);
		return backValue;
	}

	/**
	 * 
	 * @Discription 微信post请求的处理
	 * @author lason
	 * @created 2016年5月24日 下午3:33:02
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String wechatPost(String url, String postValue) {
		
		if(StringUtils.isNull(url))
			return null;
		
		//定义返回值
		String backValue = null;

		//当url不为空的时候执行
		try {
			//根据传入的url,创建发送请求的URL对象
			URL urlPost = new URL(url);
			HttpURLConnection http = (HttpURLConnection) urlPost.openConnection();//开启连接
			
			//连接参数的相关配置
			http.setRequestMethod("POST");//定义传输方式
			http.setRequestProperty("Content-Type","application/x-www-form-urlencoded");//定义传输的格式
			http.setDoOutput(true);//允许读取
			http.setDoInput(true);//允许写入
			System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒
			System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒
			http.connect();//执行连接
			
			OutputStream os = http.getOutputStream();//获取写入流
			os.write(postValue.getBytes("UTF-8"));//传入参数
			os.flush();//刷新流
			os.close();//关闭写入流
			
			InputStream is = http.getInputStream();//获取返回数据流
			//防止相应不及时的问题
			int size = 0;
			while (size == 0) {
				// 获取文件字节的大小
				size = is.available();
			}
			byte[] jsonBytes = new byte[size];
			is.read(jsonBytes);//把数据传入的字节数组
			backValue = new String(jsonBytes, "UTF-8");
		} catch (MalformedURLException e) {
			logger.info("WechatUtil POST 请求 MalformedURLException");
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("WechatUtil POST 请求 IOException");
			e.printStackTrace();
		}
		return backValue;
	}

	
	/**
	 * 
	    * @Discription 通过调用微信端的接口,获取accessToken的方法
	    * @author lason       
	    * @created 2016年6月1日 下午1:33:42      
	    * @return     
	    * @see com.bcdbook.summer.wechatold.service.ConnectService#makeAccessToken()
	 */
	public static String getNewAccessTokenFromWechatServer() {
		//定义请求的url
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
				+ "&appid="
				+ Wechat.APP_ID
				+ "&secret=" 
				+ Wechat.APP_SECRET;
		//定义用来返回的accessToken,
		String accessToken = null;
		
		//执行请求,获取accessToken的返回值
		String tokenBack = wechatGet(url);
		if(tokenBack!=null){
			JSONObject tokenJson = JSON.parseObject(tokenBack);
			accessToken = tokenJson.getString("access_token");
		}
		return accessToken;
	}
	
	/**
	 * @Description: 验证连接是否OK的方法
	 * 根据传入的验证值和用来生成验证值的不定量参数,进行校验,并返回校验结构
	 * @param @param equalValue
	 * @param @param checkValues
	 * @param @return   
	 * @return boolean  
	 * @throws
	 * @author lason
	 * @date 2016年9月19日
	 */
	public static boolean safeUuid(String equalValue,String... checkValues){
		//验证参数的合法性
		if(StringUtils.isNull(equalValue)
				||checkValues==null
				||checkValues.length==0)
			return false;
		
		//获取拼接后的字符串
		String pieceString = StringUtils.sortString(checkValues);
		if(StringUtils.isNull(pieceString))
			return false;
		
		//对排序后的字符串进行sha1加密
		String usSignature = SHA1.encode(pieceString);
		
		return usSignature.equals(equalValue);
	}
	
	/**
	 * @Description: 获取json配置文件中的栏目
	 * @param @return   
	 * @return String  
	 * @throws
	 * @author lason
	 * @date 2016年9月19日
	 */
	public static String getMenu() {
		String menu = null;
		//获取项目的跟路径
		String basePash = ContextParameter.getRealPath();
		if(basePash==null)
			return null;
		//拼接栏目配置文件的目标路径
		String filePath = basePash+"WEB-INF/classes/wechat.json";

		//把获取到的文件解析成json
		JsonUtil jsonUtil = new JsonUtil();
		String paramString = jsonUtil.readJson(filePath);
		JSONObject paramJson = JSON.parseObject(paramString);

		//把解析好的json转换成字符串
		menu = paramJson.getString("menu");

		return menu;
	}
	
	/**
	    * @Discription 根据微信的openid获取用户详细信息的方法
	    * @author lason       
	    * @created 2016年9月22日 下午10:18:22     
	    * @param accessToken
	    * @param openId
	    * @return
	 */
	public static String getUserInfo(String accessToken,String openId){
		//验证参数的合法性
		if(StringUtils.isNull(accessToken)
				||StringUtils.isNull(openId))
			return BackMsg.error("accessToken or openId is null");
		
		//拼接请求参数
		String url = "https://api.weixin.qq.com/cgi-bin/user/info"
				+ "?access_token="+accessToken
				+ "&openid="+openId
				+ "&lang=zh_CN";
		
		//返回获取到的用户数据
		return wechatGet(url);
	}
	/**
	 * @Description: 对象转xml的方法
	 * @param @param entity
	 * @param @return   
	 * @return String  
	 * @throws
	 * @author lason
	 * @date 2016年9月22日
	 */
	public static <T> String packageXML(T entity){
		xstream.alias("xml", entity.getClass());
		return xstream.toXML(entity);
	}

	/**
	 * @Description: news对象转xml的方法
	 * @param @param newsMessage
	 * @param @return   
	 * @return String  
	 * @throws
	 * @author lason
	 * @date 2016年9月22日
	 */
	public static String packageNewsXML(NewsMessage newsMessage) {
		xstream.alias("xml", newsMessage.getClass());
		xstream.alias("item", new Article().getClass());
		return xstream.toXML(newsMessage);
	}
	
	/**
	 * 扩展xstream，使其支持CDATA块
	 * 
	 */
	private static XStream xstream = new XStream(new XppDriver() {
		@Override
		public HierarchicalStreamWriter createWriter(Writer out) {
			return new PrettyPrintWriter(out) {
				// 对所有xml节点的转换都增加CDATA标记
				boolean cdata = true;
				@Override
				protected void writeText(QuickWriter writer, String text) {
					if (cdata) {
						writer.write("<![CDATA[");
						writer.write(text);
						writer.write("]]>");
////						writer.write("<![CDATA[");
//						writer.write(text);
////						writer.write("]]>");
					} else {
						writer.write(text);
					}
				}
			};
		}
	});
	
	/**
	 * 
	    * @Discription 对url转码的工具方法
	    * @author lason       
	    * @created 2016年5月26日 上午10:35:08     
	    * @param str
	    * @return
	 */
    public static String urlEnodeUTF8(String str){
        String result = str;
        try {
            result = URLEncoder.encode(str,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    

	/**
	 * @Description: 封装获取code的连接地址的方法
	 * @param @param redirectUri 授权后,重定向的回调连接地址
	 * @param @param scope  应用授权作用域(snsapi_base /snsapi_userinfo)
	 * @param @param state  重定向后会带上的参数(开发者自定义)
	 * @param @return   
	 * @return String  
	 * @throws
	 * @author lason
	 * @date 2016年9月26日
	 */
	public static String packageGetCodeUrl(String redirectUri,String scope,String state){
		//验证参数的合法性
		if(StringUtils.isNull(redirectUri)
				||StringUtils.isNull(scope))
			return null;
		
		//定义请求地址
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize"
				+ "?appid=APP_ID"
				+ "&redirect_uri=REDIRECT_URI"
				+ "&response_type=code"
				+ "&scope=SCOPE"
				+ "&state=STATE"
				+ "#wechat_redirect";
		
		//转译并替换相关的参数
		url = url.replace("APP_ID", WechatUtil.urlEnodeUTF8(Wechat.APP_ID));
		url = url.replace("REDIRECT_URI", WechatUtil.urlEnodeUTF8(redirectUri));
		url = url.replace("SCOPE", WechatUtil.urlEnodeUTF8(scope));
		url = url.replace("STATE", WechatUtil.urlEnodeUTF8(state));
		
		//返回封装后的值
		return url;
	}
	
	/**
	 * @Description: 通过code换取网页端使用的accessToken的方法
	 * @param @param code
	 * @param @return   
	 * @return String  
	 * @throws
	 * @author lason
	 * @date 2016年9月26日
	 */
	public static String getWebAccessTokenAndOpenId(String code){
		//验证参数的合法性
		if(StringUtils.isNull(code))
			return null;
		
		//拼接连接地址
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token"
				+ "?appid="+Wechat.APP_ID
				+ "&secret="+Wechat.APP_SECRET
				+ "&code="+code
				+ "&grant_type=authorization_code";
		
		String backMsg = WechatUtil.wechatGet(url);
		
		return backMsg;
	}
	
	
	public static void main(String[] args) {
//		System.out.println(DateUtil.getTime());
//		getSecondDateTime();
//		System.out.println(getNewAccessTokenFromWechatServer());
	}
}
