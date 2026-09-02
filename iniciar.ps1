$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaExecutable = Join-Path $projectRoot '.runtime\jdk\jdk-21.0.12.1+1\bin\java.exe'
$applicationJar = Join-Path $projectRoot 'target\telemedicina-demo-1.0.0.jar'
$standardLog = Join-Path $projectRoot '.runtime\server.out.log'
$errorLog = Join-Path $projectRoot '.runtime\server.err.log'

if (-not (Test-Path -LiteralPath $javaExecutable)) {
    throw 'Runtime Java portátil não encontrado em .runtime.'
}
if (-not (Test-Path -LiteralPath $applicationJar)) {
    throw 'Aplicação ainda não foi empacotada. Consulte o README.md.'
}

$existingServer = Get-NetTCPConnection -LocalAddress '127.0.0.1' -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($existingServer) {
    Write-Host 'Fernanda Azevedo já está disponível em http://localhost:8080/' -ForegroundColor Green
    exit 0
}

$quotedJar = '"' + $applicationJar + '"'
$process = Start-Process -FilePath $javaExecutable -ArgumentList @('-jar', $quotedJar, '--server.address=127.0.0.1', '--server.port=8080') -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput $standardLog -RedirectStandardError $errorLog -PassThru
Write-Host "Servidor iniciando (processo $($process.Id)). Aguarde alguns segundos e acesse http://localhost:8080/" -ForegroundColor Green
