package org.miosec.coolweather.util;

public interface HttpCallbackListener {
	void onFinish(String response);
	void onError(Exception e);
}
