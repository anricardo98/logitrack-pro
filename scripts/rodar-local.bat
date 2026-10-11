@echo off
REM Sobe backend (8080) e frontend (5173) em duas janelas.
REM Pre-requisito: PostgreSQL rodando com o banco "logitrack" (instalar-ambiente.bat cria).
set RAIZ=%~dp0..
start "LogiTrack API" cmd /k "cd /d %RAIZ%\backend && mvn spring-boot:run"
start "LogiTrack Web" cmd /k "cd /d %RAIZ%\frontend && npm install && npm run dev"
echo.
echo Aguarde ~1 minuto e abra http://localhost:5173  (admin@logitrack.com / admin123)
