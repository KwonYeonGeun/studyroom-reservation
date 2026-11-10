-- 관리자 계정은 웹 회원가입에서 만들 수 없습니다.
-- 필요할 때 H2 콘솔에서 아래 SQL을 직접 실행하여 관리자 계정을 추가합니다.
INSERT INTO SR_USERS(USER_ID, PASSWORD, NAME, PHONE, ROLE)
VALUES('admin', '1234', '관리자', '010-0000-0000', 'ADMIN');
