package com.ibm.xsp.bluemix.util;

import javax.xml.bind.DatatypeConverter;

import com.ibm.commons.Platform;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.xsp.bluemix.util.context.BluemixContextManager;

/**
 * This utility class stores important Bluemix credentials
 * and provides a helper method to retrieve them from Bluemix
 * 
 * @author Brian Gleeson - brian.gleeson@ie.ibm.com
 */
public class BluemixContextUtil {

	private String username;
	private String password;
	private String baseUrl;
	private String host;
	private String apiKey;
	private String serviceName;

	private String hardcodedUsername;
	private String hardcodedPassword;
	private String hardcodedBaseUrl;
	private String hardcodedApiKey;
	
	public BluemixContextUtil(String serviceName) {
		this(serviceName, null, null, null);
	}

	public BluemixContextUtil(String serviceName, String apiKey, String baseUrl) {
		setServiceName(serviceName);
		setHardcodedBaseUrl(baseUrl);
		setHardcodedApiKey(apiKey);
		processVCAPServices();
	}
	
	public BluemixContextUtil(String serviceName, String username, String password, String baseUrl) {
		setServiceName(serviceName);
		setHardcodedBaseUrl(baseUrl);
		setHardcodedUsername(username);
		setHardcodedPassword(password);
		processVCAPServices();
	}
	
	/**
	 * Get the <b>VCAP_SERVICES</b> environment variable and return it
	 * as a JSONObject.
	 *
	 * @return the VCAP_SERVICES as Json
	 */
	protected JsonJavaObject getVcapServices() {
		JsonJavaObject vcapJson = null;
		JsonJavaFactory factory = JsonJavaFactory.instanceEx;
		String sysEnv = BluemixContextManager.getInstance().getVCAP_SERVICES();
		try {
			vcapJson = (JsonJavaObject)JsonParser.fromJson(factory, sysEnv);
		} catch (JsonException e) {
			Platform.getInstance().log(e);
		}
		return vcapJson;
	}
	
	/**
	 * If it exists, process the VCAP_SERVICES environment variable in order to get the
	 * username, password and baseURL
	 */
	public void processVCAPServices() {
		if (!BluemixContextManager.getInstance().isRunningOnBluemix())
			return;
		
		JsonJavaObject sysEnv = getVcapServices();
		if (sysEnv == null)
			return;

		if (sysEnv.containsKey(serviceName)) {
			JsonJavaArray services = sysEnv.getAsArray(serviceName);
			JsonJavaObject service = (JsonJavaObject) services.get(0);
			JsonJavaObject credentials = (JsonJavaObject) service.get("credentials");
			setBaseUrl((String) credentials.get("url"));

			String username = (String) credentials.get("username");
			String password = (String) credentials.get("password");
			if(StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
				setUsername(username);
				setPassword(password);
			}
			String host = (String) credentials.get("host");
			if(StringUtil.isNotEmpty(host)) {
				setHost(host);
			}
			
			String apiKey = (String) credentials.get("apikey");
			if(StringUtil.isNotEmpty(apiKey)) {
				setApiKey(apiKey);
			}
		}
	}
	
	/**
	 * Build the Authorization header used in REST requests
	 * @return basic authorization header value
	 */
	public String getAuthorizationHeader(){
		String auth   = getUsername() + ":" + getPassword();
		String header = "Basic " + DatatypeConverter.printBase64Binary(auth.getBytes());
		return header;
	}

	/*
	 * Getter and setter methods
	 */
	public void setUsername(String user) {
		this.username = user;
	}

	public String getUsername() {
		if (BluemixContextManager.getInstance().isRunningOnBluemix()) {
			return username;
		} else {
			// Hardcoded credential for local testing
			return hardcodedUsername;
		}
	}

	public void setPassword(String pword) {
		this.password = pword;
	}

	public String getPassword() {
		if (BluemixContextManager.getInstance().isRunningOnBluemix()) {
			return password;
		} else {
			// Hardcoded credential for local testing
			return hardcodedPassword;
		}
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		if (BluemixContextManager.getInstance().isRunningOnBluemix()) {
			return baseUrl;
		} else {
			// Hardcoded credential for local testing
			return hardcodedBaseUrl;
		}
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setHardcodedUsername(String hardcodedUsername) {
		this.hardcodedUsername = hardcodedUsername;
	}

	public String getHardcodedUsername() {
		return hardcodedUsername;
	}

	public void setHardcodedPassword(String hardcodedPassword) {
		this.hardcodedPassword = hardcodedPassword;
	}

	public String getHardcodedPassword() {
		return hardcodedPassword;
	}

	public void setHardcodedBaseUrl(String hardcodedBaseUrl) {
		this.hardcodedBaseUrl = hardcodedBaseUrl;
	}

	public String getHardcodedBaseUrl() {
		return hardcodedBaseUrl;
	}

	public void setHost(String host) {
		this.host = "https://" + host;
	}

	public String getHost() {
		return host;
	}

	public void setHardcodedApiKey(String hardcodedApiKey) {
		this.hardcodedApiKey = hardcodedApiKey;
	}

	public String getHardcodedApiKey() {
		return hardcodedApiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiKey() {
		if (BluemixContextManager.getInstance().isRunningOnBluemix()) {
			return apiKey;
		} else {
			// Hardcoded credential for local testing
			return getHardcodedApiKey();
		}
	}
}
