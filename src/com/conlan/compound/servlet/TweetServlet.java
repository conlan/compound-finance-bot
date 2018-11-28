package com.conlan.compound.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TweetServlet extends HttpServlet {
	public static final Logger log = Logger.getLogger(TweetServlet.class.getName());
	
	private static final long serialVersionUID = -3581362016207800654L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String statusContent = req.getParameter("status");
		
		Twitter twitter = TwitterFactory.getSingleton();
		
		try {
			Status status = twitter.updateStatus(statusContent);
			
			log.info(status.getText());
		} catch (Throwable t) {
			log.warning(t.toString());
		}
	}
}
