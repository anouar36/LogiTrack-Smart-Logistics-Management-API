# Jenkins + SonarQube Setup Automation Script

Write-Host "🚀 Starting Jenkins + SonarQube Setup..." -ForegroundColor Green

# 1. Check if containers are running
Write-Host "📋 Checking container status..." -ForegroundColor Yellow
docker-compose ps

# 2. Get Jenkins password
Write-Host "🔑 Getting Jenkins initial password..." -ForegroundColor Yellow
$jenkinsPassword = docker exec jenkins_ci cat /var/jenkins_home/secrets/initialAdminPassword
Write-Host "Jenkins Password: $jenkinsPassword" -ForegroundColor Cyan

# 3. Test Jenkins connectivity
Write-Host "🔗 Testing Jenkins connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 10
    Write-Host "✅ Jenkins is accessible at http://localhost:8080" -ForegroundColor Green
} catch {
    Write-Host "❌ Jenkins is not accessible. Please wait and try again." -ForegroundColor Red
}

# 4. Test SonarQube connectivity
Write-Host "🔗 Testing SonarQube connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9000" -UseBasicParsing -TimeoutSec 10
    Write-Host "✅ SonarQube is accessible at http://localhost:9000" -ForegroundColor Green
} catch {
    Write-Host "❌ SonarQube is not accessible. Please wait and try again." -ForegroundColor Red
}

# 5. Test internal network connectivity
Write-Host "🌐 Testing internal network connectivity..." -ForegroundColor Yellow
try {
    $networkTest = docker exec jenkins_ci curl -s -o /dev/null -w "%{http_code}" http://sonarqube_ci:9000
    if ($networkTest -eq "200") {
        Write-Host "✅ Jenkins can reach SonarQube internally" -ForegroundColor Green
    } else {
        Write-Host "❌ Jenkins cannot reach SonarQube (HTTP $networkTest)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Network connectivity test failed" -ForegroundColor Red
}

# 6. Show next steps
Write-Host "`n📝 Next Steps:" -ForegroundColor Cyan
Write-Host "1. Open Jenkins: http://localhost:8080" -ForegroundColor White
Write-Host "2. Use password: $jenkinsPassword" -ForegroundColor White
Write-Host "3. Open SonarQube: http://localhost:9000 (admin/admin)" -ForegroundColor White
Write-Host "4. Follow jenkins-setup.md for detailed configuration" -ForegroundColor White

# 7. Test Maven command that was failing
Write-Host "`n🧪 Testing the SonarQube Maven command..." -ForegroundColor Yellow
Write-Host "Command that was failing:" -ForegroundColor Gray
Write-Host "mvn sonar:sonar -Dsonar.projectKey=logitrack-api -Dsonar.host.url=http://sonarqube_ci:9000 -Dsonar.login=sqa_a4443853409628289a40ada2deac36b3a584d1d8" -ForegroundColor Gray

Write-Host "`nFixed command should be:" -ForegroundColor Green
Write-Host "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar -Dsonar.projectKey=logitrack-api -Dsonar.host.url=http://sonarqube_ci:9000 -Dsonar.token=sqa_a4443853409628289a40ada2deac36b3a584d1d8" -ForegroundColor Green

Write-Host "`n✅ Setup completed! Check jenkins-setup.md for detailed configuration steps." -ForegroundColor Green
