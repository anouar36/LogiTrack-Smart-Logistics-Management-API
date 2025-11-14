# CI/CD Setup Script for Windows
# Run this in PowerShell as Administrator

Write-Host "Starting LogiTrack CI/CD Setup..." -ForegroundColor Green

# Check if Docker is running
if (!(Get-Process "Docker Desktop" -ErrorAction SilentlyContinue)) {
    Write-Host "Please start Docker Desktop first!" -ForegroundColor Red
    exit 1
}

# Stop existing containers
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose down

# Start all services
Write-Host "Starting all services..." -ForegroundColor Yellow
docker-compose up -d

# Wait for services to start
Write-Host "Waiting for services to start (60 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Check service status
Write-Host "Checking service status..." -ForegroundColor Yellow
docker-compose ps

Write-Host "`n=== Service URLs ===" -ForegroundColor Green
Write-Host "SonarQube: http://localhost:9000 (admin/admin)" -ForegroundColor Cyan
Write-Host "Jenkins: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Application: http://localhost:8082" -ForegroundColor Cyan
Write-Host "PgAdmin: http://localhost:8081" -ForegroundColor Cyan

# Get Jenkins initial password
Write-Host "`n=== Jenkins Initial Password ===" -ForegroundColor Green
try {
    $jenkinsPassword = docker exec jenkins_ci cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
    if ($jenkinsPassword) {
        Write-Host "Jenkins Initial Admin Password: $jenkinsPassword" -ForegroundColor Yellow
    } else {
        Write-Host "Jenkins password not ready yet. Try again in a few minutes with:" -ForegroundColor Yellow
        Write-Host "docker exec jenkins_ci cat /var/jenkins_home/secrets/initialAdminPassword" -ForegroundColor White
    }
} catch {
    Write-Host "Jenkins not ready yet. Try again in a few minutes." -ForegroundColor Yellow
}

Write-Host "`nSetup complete! Follow the setup-ci.md guide for configuration." -ForegroundColor Green
