package com.conlan.compound.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.conlan.compound.service.Web3Service;

public class ReportLiquidationServlet extends HttpServlet {
	private static final long serialVersionUID = -6486835234713150146L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Web3Service.GetLogs();
	}
}
