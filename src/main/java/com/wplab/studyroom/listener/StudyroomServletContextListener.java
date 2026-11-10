package com.wplab.studyroom.listener;

import com.wplab.studyroom.service.StudyroomDAO;
import com.wplab.studyroom.service.impl.StudyroomDAObyDBCP;
import com.wplab.studyroom.service.impl.StudyroomServiceImpl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class StudyroomServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println(">>> Studyroom ServletContext initialized...");

		ServletContext context = sce.getServletContext();
		String dbcpResourceName = context.getInitParameter("dbcp_resource_name");

		StudyroomDAO studyroomDao = new StudyroomDAObyDBCP(dbcpResourceName);
		context.setAttribute("studyroom_dao", studyroomDao);
		context.setAttribute("studyroom_service", new StudyroomServiceImpl(studyroomDao));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println(">>> Studyroom ServletContext destroyed...");
	}
}
