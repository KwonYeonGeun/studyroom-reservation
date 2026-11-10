package com.wplab.studyroom.controller;

import java.io.IOException;

import com.wplab.studyroom.command.Command;
import com.wplab.studyroom.command.CommandFactory;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("*.do")
public class FrontControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public FrontControllerServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getServletPath();
		Command command = CommandFactory.getCommand(path);

		try {
			String viewName = command.execute(request, response);
			if (viewName == null) {
				return;
			}
			if (viewName.startsWith("redirect:")) {
				response.sendRedirect(request.getContextPath() + "/" + viewName.substring("redirect:".length()));
				return;
			}
			RequestDispatcher view = request.getRequestDispatcher(viewName);
			view.forward(request, response);
		} catch (Exception e) {
			request.setAttribute("errorMessage", e.getMessage() == null ? "처리 중 오류가 발생했습니다." : e.getMessage());
			RequestDispatcher view = request.getRequestDispatcher("/WEB-INF/views/error.jsp");
			view.forward(request, response);
		}
	}
}
