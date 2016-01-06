package com.nubisZemi;

public interface NubisAsyncResponse {
	void processFinish(String output, int responseCode, String responseString, NubisDelayedAnswer delayedAnswer, int deleteId);
}
