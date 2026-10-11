@echo off
REM Executa o instalador PowerShell como administrador
powershell -NoProfile -Command "Start-Process powershell -Verb RunAs -ArgumentList '-NoProfile -ExecutionPolicy Bypass -NoExit -File \"%~dp0instalar-ambiente.ps1\"'"
