package handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import html.Pages;

public class TerminateWithError {
	public static void terminateWithError(String message, String code, HttpServletResponse response, String retryLink, int httpError) throws IOException {
		String fileContents = Pages.checkoutError;           
      	fileContents = fileContents.replace("{Result}", message);
    	fileContents = fileContents.replace("{Result_Code}", code);
    	fileContents = fileContents.replace("{Retry_Link}", retryLink);
    	response.getWriter().write(fileContents);
        response.addHeader("Content-Type", "text/html");
        response.setStatus(httpError);
        return;
	}
	
}
