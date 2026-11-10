package com.wplab.studyroom.command;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;

import com.wplab.studyroom.common.DateUtil;
import com.wplab.studyroom.common.StudyroomException;
import com.wplab.studyroom.domain.ReservationView;
import com.wplab.studyroom.domain.Resource;
import com.wplab.studyroom.domain.User;
import com.wplab.studyroom.service.StudyroomService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class CommandFactory {
	public static Command getCommand(String path) {
		if ("/main.do".equals(path)) {
			return new MainCommand();
		}
		if ("/signupForm.do".equals(path)) {
			return new SignupFormCommand();
		}
		if ("/signup.do".equals(path)) {
			return new SignupCommand();
		}
		if ("/loginForm.do".equals(path)) {
			return new LoginFormCommand();
		}
		if ("/login.do".equals(path)) {
			return new LoginCommand();
		}
		if ("/logout.do".equals(path)) {
			return new LogoutCommand();
		}
		if ("/resourceList.do".equals(path)) {
			return new ResourceListCommand();
		}
		if ("/reservationForm.do".equals(path)) {
			return new ReservationFormCommand();
		}
		if ("/reservationConfirm.do".equals(path)) {
			return new ReservationConfirmCommand();
		}
		if ("/paymentApprove.do".equals(path)) {
			return new PaymentApproveCommand();
		}
		if ("/myPage.do".equals(path)) {
			return new MyPageCommand();
		}
		if ("/reservationDetail.do".equals(path)) {
			return new ReservationDetailCommand();
		}
		if ("/cancelRequest.do".equals(path)) {
			return new CancelRequestCommand();
		}
		if ("/adminMain.do".equals(path)) {
			return new AdminMainCommand();
		}
		if ("/adminResources.do".equals(path)) {
			return new AdminResourcesCommand();
		}
		if ("/adminReservations.do".equals(path)) {
			return new AdminReservationsCommand();
		}
		if ("/cancelApprove.do".equals(path)) {
			return new CancelApproveCommand();
		}
		if ("/cancelReject.do".equals(path)) {
			return new CancelRejectCommand();
		}
		if ("/adminStats.do".equals(path)) {
			return new AdminStatsCommand();
		}
		return new NotFoundCommand();
	}
}

class CommandSupport {
	private static final int OPEN_HOUR = 8;
	private static final int CLOSE_HOUR = 23;
	private static final int LAST_START_HOUR = 22;

	protected StudyroomService service(HttpServletRequest request) {
		return (StudyroomService)request.getServletContext().getAttribute("studyroom_service");
	}

	protected User currentUser(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		return (User)session.getAttribute("user");
	}

	protected String requireLogin(HttpServletRequest request) {
		User user = currentUser(request);
		if (user == null) {
			return "redirect:loginForm.do";
		}
		return null;
	}

	protected String requireAdmin(HttpServletRequest request) {
		User user = currentUser(request);
		if (user == null) {
			return "redirect:loginForm.do";
		}
		if (!user.isAdmin()) {
			request.setAttribute("errorMessage", "관리자 권한이 필요합니다.");
			return "/WEB-INF/views/error.jsp";
		}
		return null;
	}

	protected String error(HttpServletRequest request, String message) {
		request.setAttribute("errorMessage", message);
		return "/WEB-INF/views/error.jsp";
	}

	protected long longParam(HttpServletRequest request, String name) throws StudyroomException {
		try {
			return Long.parseLong(request.getParameter(name));
		} catch (Exception e) {
			throw new StudyroomException("요청 값이 올바르지 않습니다: " + name);
		}
	}

	protected String param(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		return value == null ? "" : value.trim();
	}

	protected String selectedDate(HttpServletRequest request) {
		String date = param(request, "date");
		return date.length() == 0 ? DateUtil.today() : date;
	}

	protected int selectedHour(HttpServletRequest request, String date, boolean futureOnly) {
		int hour = DateUtil.parseHour(request.getParameter("startHour"), defaultStartHour(date, futureOnly));
		int minHour = minStartHour(date, futureOnly);
		if (hour < minHour) {
			hour = minHour;
		}
		if (hour > LAST_START_HOUR) {
			hour = LAST_START_HOUR;
		}
		return hour;
	}

	protected int selectedDuration(HttpServletRequest request, int hour) {
		int duration = 1;
		try {
			duration = Integer.parseInt(request.getParameter("durationHour"));
		} catch (Exception e) {
			duration = 1;
		}
		if (duration < 1) {
			duration = 1;
		}
		int maxDuration = CLOSE_HOUR - hour;
		if (duration > maxDuration) {
			duration = maxDuration;
		}
		return duration;
	}

	protected void setTimeAttributes(HttpServletRequest request) throws StudyroomException {
		setTimeAttributes(request, false);
	}

	protected void setTimeAttributes(HttpServletRequest request, boolean futureOnly) throws StudyroomException {
		String date = selectedDate(request);
		boolean hasAvailableTime = hasAvailableTime(date, futureOnly);
		int hour = selectedHour(request, date, futureOnly);
		int duration = selectedDuration(request, hour);
		int endHour = hour + duration;
		Timestamp startTime = DateUtil.toTimestamp(date, hour);
		Timestamp endTime = DateUtil.toTimestamp(date, endHour);
		request.setAttribute("selectedDate", date);
		request.setAttribute("selectedHour", Integer.valueOf(hour));
		request.setAttribute("selectedDuration", Integer.valueOf(duration));
		request.setAttribute("minStartHour", Integer.valueOf(minStartHour(date, futureOnly)));
		request.setAttribute("lastStartHour", Integer.valueOf(LAST_START_HOUR));
		request.setAttribute("closeHour", Integer.valueOf(CLOSE_HOUR));
		request.setAttribute("hasAvailableTime", Boolean.valueOf(hasAvailableTime));
		request.setAttribute("maxDuration", Integer.valueOf(CLOSE_HOUR - hour));
		request.setAttribute("endHour", Integer.valueOf(endHour));
		request.setAttribute("startTime", startTime);
		request.setAttribute("endTime", endTime);
	}

	protected void requireFutureReservable(HttpServletRequest request) throws StudyroomException {
		Boolean hasAvailableTime = (Boolean)request.getAttribute("hasAvailableTime");
		Timestamp startTime = (Timestamp)request.getAttribute("startTime");
		String requestedHour = param(request, "startHour");
		String requestedDuration = param(request, "durationHour");
		Integer selectedHour = (Integer)request.getAttribute("selectedHour");
		Integer selectedDuration = (Integer)request.getAttribute("selectedDuration");
		if (requestedHour.length() > 0
				&& DateUtil.parseHour(requestedHour, -1) != selectedHour.intValue()) {
			throw new StudyroomException("현재 시각 이후의 예약 가능한 시간만 선택할 수 있습니다.");
		}
		if (requestedDuration.length() > 0) {
			try {
				if (Integer.parseInt(requestedDuration) != selectedDuration.intValue()) {
					throw new StudyroomException("이용 시간은 영업 종료 시간 안에서만 선택할 수 있습니다.");
				}
			} catch (NumberFormatException e) {
				throw new StudyroomException("이용 시간은 영업 종료 시간 안에서만 선택할 수 있습니다.");
			}
		}
		if (hasAvailableTime == null || !hasAvailableTime.booleanValue() || !startTime.after(nowTimestamp())) {
			throw new StudyroomException("현재 시각 이후의 예약 가능한 시간만 선택할 수 있습니다.");
		}
	}

	private boolean hasAvailableTime(String date, boolean futureOnly) {
		return minStartHour(date, futureOnly) <= LAST_START_HOUR;
	}

	private int minStartHour(String date, boolean futureOnly) {
		if (!futureOnly) {
			return OPEN_HOUR;
		}
		String today = DateUtil.today();
		if (date.compareTo(today) < 0) {
			return CLOSE_HOUR;
		}
		if (date.compareTo(today) > 0) {
			return OPEN_HOUR;
		}
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY) + 1;
		return hour < OPEN_HOUR ? OPEN_HOUR : hour;
	}

	private int defaultStartHour(String date, boolean futureOnly) {
		if (futureOnly || !DateUtil.today().equals(date)) {
			return 9;
		}
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < OPEN_HOUR) {
			return OPEN_HOUR;
		}
		if (hour > LAST_START_HOUR) {
			return LAST_START_HOUR;
		}
		return hour;
	}

	private Timestamp nowTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}
}

class MainCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		return "/WEB-INF/views/main.jsp";
	}
}

class SignupFormCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		return "/WEB-INF/views/signup.jsp";
	}
}

class SignupCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		User user = new User();
		user.setUserId(param(request, "userId"));
		user.setPassword(param(request, "password"));
		user.setName(param(request, "name"));
		user.setPhone(param(request, "phone"));

		try {
			service(request).registerUser(user);
		} catch (StudyroomException e) {
			request.setAttribute("errorMessage", e.getMessage());
			return "/WEB-INF/views/signup.jsp";
		}

		request.setAttribute("message", "회원가입이 완료되었습니다.");
		request.setAttribute("redirectUrl", "loginForm.do");
		return "/WEB-INF/views/result.jsp";
	}
}

class LoginFormCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		return "/WEB-INF/views/login.jsp";
	}
}

class LoginCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String userId = param(request, "userId");
		String password = param(request, "password");

		try {
			User user = service(request).authenticateUser(userId, password);
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			if (user.isAdmin()) {
				return "redirect:adminMain.do";
			}
			return "redirect:main.do";
		} catch (StudyroomException e) {
			request.setAttribute("errorMessage", e.getMessage());
			return "/WEB-INF/views/login.jsp";
		}
	}
}

class LogoutCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return "redirect:main.do";
	}
}

class ResourceListCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		try {
			setTimeAttributes(request, true);
			Boolean hasAvailableTime = (Boolean)request.getAttribute("hasAvailableTime");
			String type = param(request, "type");
			Timestamp startTime = (Timestamp)request.getAttribute("startTime");
			Timestamp endTime = (Timestamp)request.getAttribute("endTime");
			request.setAttribute("selectedType", type);
			if (hasAvailableTime != null && hasAvailableTime.booleanValue()) {
				request.setAttribute("resources", service(request).getResources(type, startTime, endTime));
			}
			return "/WEB-INF/views/resource_list.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class ReservationFormCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String loginCheck = requireLogin(request);
		if (loginCheck != null) {
			return loginCheck;
		}

		try {
			setTimeAttributes(request, true);
			requireFutureReservable(request);
			long resourceId = longParam(request, "resourceId");
			request.setAttribute("resource", service(request).getResource(resourceId));
			return "/WEB-INF/views/reservation_form.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class ReservationConfirmCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String loginCheck = requireLogin(request);
		if (loginCheck != null) {
			return loginCheck;
		}

		try {
			setTimeAttributes(request, true);
			requireFutureReservable(request);
			long resourceId = longParam(request, "resourceId");
			Timestamp startTime = (Timestamp)request.getAttribute("startTime");
			Timestamp endTime = (Timestamp)request.getAttribute("endTime");
			Resource resource = service(request).getResource(resourceId);
			if (!service(request).isAvailable(resourceId, startTime, endTime)) {
				throw new StudyroomException("선택한 시간에는 예약할 수 없습니다.");
			}
			Integer duration = (Integer)request.getAttribute("selectedDuration");
			request.setAttribute("resource", resource);
			request.setAttribute("amount", Integer.valueOf(resource.getPrice() * duration.intValue()));
			request.setAttribute("method", param(request, "method"));
			return "/WEB-INF/views/reservation_confirm.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class PaymentApproveCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String loginCheck = requireLogin(request);
		if (loginCheck != null) {
			return loginCheck;
		}

		try {
			User user = currentUser(request);
			setTimeAttributes(request, true);
			requireFutureReservable(request);
			long resourceId = longParam(request, "resourceId");
			Timestamp startTime = (Timestamp)request.getAttribute("startTime");
			Timestamp endTime = (Timestamp)request.getAttribute("endTime");
			long reservationId = service(request).approvePayment(user.getUserId(), resourceId, startTime, endTime,
					param(request, "method"));
			request.setAttribute("message", "예약과 결제가 완료되었습니다. 예약번호: " + reservationId);
			request.setAttribute("redirectUrl", "myPage.do");
			return "/WEB-INF/views/result.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class MyPageCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String loginCheck = requireLogin(request);
		if (loginCheck != null) {
			return loginCheck;
		}

		try {
			User user = currentUser(request);
			setTimeAttributes(request);
			Timestamp startTime = (Timestamp)request.getAttribute("startTime");
			Timestamp endTime = (Timestamp)request.getAttribute("endTime");
			request.setAttribute("resources", service(request).getResources(null, startTime, endTime));
			request.setAttribute("reservations", service(request).getMyReservations(user.getUserId()));
			return "/WEB-INF/views/my_page.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class ReservationDetailCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String loginCheck = requireLogin(request);
		if (loginCheck != null) {
			return loginCheck;
		}

		try {
			User user = currentUser(request);
			ReservationView reservation = service(request).getReservation(longParam(request, "reservationId"));
			if (!user.isAdmin() && !user.getUserId().equals(reservation.getUserId())) {
				return error(request, "본인 예약만 조회할 수 있습니다.");
			}
			request.setAttribute("reservation", reservation);
			return "/WEB-INF/views/reservation_detail.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class CancelRequestCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String loginCheck = requireLogin(request);
		if (loginCheck != null) {
			return loginCheck;
		}

		try {
			User user = currentUser(request);
			long reservationId = longParam(request, "reservationId");
			service(request).requestCancel(user.getUserId(), reservationId, param(request, "reason"));
			request.setAttribute("message", "취소 요청이 등록되었습니다.");
			request.setAttribute("redirectUrl", "myPage.do");
			return "/WEB-INF/views/result.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class AdminMainCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String adminCheck = requireAdmin(request);
		if (adminCheck != null) {
			return adminCheck;
		}

		try {
			request.setAttribute("userCount", Integer.valueOf(service(request).countUsers()));
			request.setAttribute("reservationCount", Integer.valueOf(service(request).countReservedReservations()));
			request.setAttribute("cancelCount", Integer.valueOf(service(request).countRequestedCancels()));
			request.setAttribute("totalRevenue", Integer.valueOf(service(request).getApprovedRevenue()));
			return "/WEB-INF/views/admin_main.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class AdminResourcesCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String adminCheck = requireAdmin(request);
		if (adminCheck != null) {
			return adminCheck;
		}

		try {
			setTimeAttributes(request);
			String type = param(request, "type");
			Timestamp startTime = (Timestamp)request.getAttribute("startTime");
			Timestamp endTime = (Timestamp)request.getAttribute("endTime");
			request.setAttribute("selectedType", type);
			request.setAttribute("resources", service(request).getResources(type, startTime, endTime));
			return "/WEB-INF/views/admin_resources.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class AdminReservationsCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String adminCheck = requireAdmin(request);
		if (adminCheck != null) {
			return adminCheck;
		}

		try {
			String userKeyword = param(request, "userKeyword");
			String status = param(request, "status");
			String type = param(request, "type");
			String date = param(request, "date");
			request.setAttribute("userKeyword", userKeyword);
			request.setAttribute("status", status);
			request.setAttribute("selectedType", type);
			request.setAttribute("selectedDate", date);
			request.setAttribute("reservations", service(request).getAdminReservations(userKeyword, status, type, date));
			request.setAttribute("cancelRequests", service(request).getCancelRequests("REQUESTED"));
			return "/WEB-INF/views/admin_reservations.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class CancelApproveCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String adminCheck = requireAdmin(request);
		if (adminCheck != null) {
			return adminCheck;
		}

		try {
			service(request).approveCancel(longParam(request, "requestId"));
			return "redirect:adminReservations.do";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class CancelRejectCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String adminCheck = requireAdmin(request);
		if (adminCheck != null) {
			return adminCheck;
		}

		try {
			service(request).rejectCancel(longParam(request, "requestId"));
			return "redirect:adminReservations.do";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class AdminStatsCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String adminCheck = requireAdmin(request);
		if (adminCheck != null) {
			return adminCheck;
		}

		try {
			String mode = param(request, "mode");
			if (!"MONTH".equals(mode) && !"RESOURCE".equals(mode)) {
				mode = "DAY";
			}
			request.setAttribute("mode", mode);
			request.setAttribute("stats", service(request).getRevenueStats(mode));
			return "/WEB-INF/views/admin_stats.jsp";
		} catch (StudyroomException e) {
			return error(request, e.getMessage());
		}
	}
}

class NotFoundCommand extends CommandSupport implements Command {
	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return error(request, "요청한 기능을 찾을 수 없습니다.");
	}
}
