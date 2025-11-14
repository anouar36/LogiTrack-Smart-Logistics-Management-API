# Jenkins Setup Guide for LogiTrack

## 1. Jenkins Initial Setup

**Jenkins Password:** `fed4d073e6fe497ca0fd45af80036939`

1. Go to: http://localhost:8080
2. Enter the password above
3. Install suggested plugins
4. Create admin user (recommended: admin/admin)

## 2. Install Required Plugins

Go to **Manage Jenkins** → **Manage Plugins** → **Available Plugins** and install:
- SonarQube Scanner
- Pipeline: Stage View
- Blue Ocean (optional but recommended)
- Docker Pipeline (if you need Docker support)

## 3. Configure Maven

Go to **Manage Jenkins** → **Global Tool Configuration**:
- Add Maven installation
- Name: `M3`
- Version: `3.9.4` (or latest)
- Install automatically: ✅

## 4. Configure SonarQube Server

Go to **Manage Jenkins** → **Configure System** → **SonarQube servers**:
- Add SonarQube server
- Name: `SonarQube`
- Server URL: `http://sonarqube_ci:9000`
- Server authentication token: (Create credential with your token)

## 5. Create SonarQube Token Credential

Go to **Manage Jenkins** → **Manage Credentials** → **System** → **Global credentials**:
- Click **Add Credentials**
- Kind: `Secret text`
- Secret: `sqa_a4443853409628289a40ada2deac36b3a584d1d8`
- ID: `sonar-global-token`
- Description: `SonarQube Global Analysis Token`

## 6. Create Pipeline Job

1. Click **New Item**
2. Enter name: `LogiTrack-Pipeline`
3. Choose **Pipeline**
4. In Pipeline section:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: (your repo URL)
   - Branch: `*/main` (or your default branch)
   - Script Path: `Jenkinsfile`

## 7. SonarQube Project Setup

1. Go to: http://localhost:9000
2. Login: `admin` / `admin`
3. Create project manually:
   - Project key: `logitrack-api`
   - Display name: `LogiTrack API`
4. Generate token if not already done
5. Choose analysis method: `With Jenkins`

## 8. Network Test

Test connectivity between Jenkins and SonarQube:
```bash
# From inside Jenkins container
docker exec jenkins-ci curl -I http://sonarqube-ci:9000
```

Expected response: `HTTP/1.1 200 OK`

## 9. Manual Test Run

To test the SonarQube analysis manually:
```bash
# From your project directory
docker exec jenkins-ci /bin/bash -c "cd /var/jenkins_home && mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar -Dsonar.projectKey=logitrack-api -Dsonar.host.url=http://sonarqube-ci:9000 -Dsonar.token=sqa_a4443853409628289a40ada2deac36b3a584d1d8"
```

## 10. Fixed Command

The original failing command:
```bash
mvn sonar:sonar -Dsonar.projectKey=logitrack-api -Dsonar.host.url=http://sonarqube_ci:9000 -Dsonar.login=sqa_a4443853409628289a40ada2deac36b3a584d1d8
```

Should be replaced with:
```bash
mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar -Dsonar.projectKey=logitrack-api -Dsonar.host.url=http://sonarqube-ci:9000 -Dsonar.token=sqa_a4443853409628289a40ada2deac36b3a584d1d8
```

Key changes:
1. Use full plugin coordinates: `org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar`
2. Changed hostname from `sonarqube_ci` to `sonarqube-ci` (no underscores)
3. Changed `-Dsonar.login=` to `-Dsonar.token=`

## 11. Troubleshooting

If SonarQube shows errors:
1. Check if project exists in SonarQube UI
2. Verify token is correct
3. Check network connectivity
4. Review SonarQube logs: `docker logs sonarqube_ci`
5. Review Jenkins logs: `docker logs jenkins_ci`
