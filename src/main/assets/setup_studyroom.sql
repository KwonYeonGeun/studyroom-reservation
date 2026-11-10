-- H2 콘솔에서 한 번에 실행할 때 사용하는 파일입니다.
-- 프로젝트 위치가 변경되면 아래 경로를 변경하거나, 두 SQL 파일을 직접 순서대로 실행하면 됩니다.
RUNSCRIPT FROM 'C:/Users/Jaehee/Desktop/univ/WEB/web-workspace/term-studyroom/src/main/assets/create_studyroom_tables.sql' CHARSET 'UTF-8';
RUNSCRIPT FROM 'C:/Users/Jaehee/Desktop/univ/WEB/web-workspace/term-studyroom/src/main/assets/insert_studyroom_seed.sql' CHARSET 'UTF-8';
