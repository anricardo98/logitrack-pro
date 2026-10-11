# =====================================================================
# LogiTrack Pro - instalacao do ambiente de desenvolvimento (Windows)
# Rode pelo arquivo instalar-ambiente.bat (pede permissao de administrador).
# Instala: JDK 21, Maven, Node.js LTS, Git, PostgreSQL 16 e VS Code.
# Docker Desktop e opcional (pergunta no final).
# =====================================================================
$ErrorActionPreference = 'Continue'

function Instalar($id, $nome, $extra = @()) {
    Write-Host "`n>>> $nome ($id)" -ForegroundColor Cyan
    $ja = winget list --id $id -e --accept-source-agreements 2>$null | Select-String $id
    if ($ja) { Write-Host "    ja instalado, pulando." -ForegroundColor DarkGray; return }
    winget install --id $id -e --silent --accept-package-agreements --accept-source-agreements @extra
}

Instalar 'EclipseAdoptium.Temurin.21.JDK' 'Java JDK 21 (Temurin)'
Instalar 'OpenJS.NodeJS.LTS'              'Node.js LTS'
Instalar 'Git.Git'                         'Git'
Instalar 'Microsoft.VisualStudioCode'      'Visual Studio Code'
Instalar 'PostgreSQL.PostgreSQL.16'        'PostgreSQL 16' @('--override', '--mode unattended --unattendedmodeui none --superpassword postgres --serverport 5432')

# ---- JAVA_HOME ----
$jdk = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -Filter 'jdk-21*' -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jdk) {
    [Environment]::SetEnvironmentVariable('JAVA_HOME', $jdk.FullName, 'User')
    $env:JAVA_HOME = $jdk.FullName
    Write-Host "`nJAVA_HOME = $($jdk.FullName)" -ForegroundColor Green
}

# ---- Maven (nao existe pacote oficial no winget: baixa o zip da Apache) ----
$mvnVersao = '3.9.11'
$tools = Join-Path $env:USERPROFILE 'tools'
$mvnDir = Join-Path $tools "apache-maven-$mvnVersao"
if (-not (Test-Path $mvnDir)) {
    Write-Host "`n>>> Maven $mvnVersao" -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $tools | Out-Null
    $zip = Join-Path $env:TEMP "maven.zip"
    Invoke-WebRequest "https://archive.apache.org/dist/maven/maven-3/$mvnVersao/binaries/apache-maven-$mvnVersao-bin.zip" -OutFile $zip
    Expand-Archive $zip -DestinationPath $tools -Force
    Remove-Item $zip
}
[Environment]::SetEnvironmentVariable('MAVEN_HOME', $mvnDir, 'User')

# ---- PATH do usuario: Maven, Java e psql ----
$path = [Environment]::GetEnvironmentVariable('Path', 'User')
foreach ($p in @("$mvnDir\bin", "$($jdk.FullName)\bin", 'C:\Program Files\PostgreSQL\16\bin')) {
    if ($p -and (Test-Path $p) -and ($path -notlike "*$p*")) { $path = "$path;$p" }
}
[Environment]::SetEnvironmentVariable('Path', $path, 'User')

# ---- Banco de dados do projeto ----
$psql = 'C:\Program Files\PostgreSQL\16\bin\psql.exe'
if (Test-Path $psql) {
    Write-Host "`n>>> Criando banco 'logitrack'" -ForegroundColor Cyan
    $env:PGPASSWORD = 'postgres'
    $existe = & $psql -U postgres -h localhost -tAc "SELECT 1 FROM pg_database WHERE datname='logitrack'"
    if ($existe -ne '1') { & $psql -U postgres -h localhost -c 'CREATE DATABASE logitrack;' }
    else { Write-Host '    banco ja existe.' -ForegroundColor DarkGray }
}

$docker = Read-Host "`nInstalar tambem o Docker Desktop (opcional, ~600 MB)? [s/N]"
if ($docker -match '^[sS]') { Instalar 'Docker.DockerDesktop' 'Docker Desktop' }

Write-Host "`n==============================================================" -ForegroundColor Green
Write-Host " Pronto! FECHE este terminal e abra um novo para o PATH valer." -ForegroundColor Green
Write-Host " Teste com:  java -version   mvn -v   node -v   psql --version" -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Green
