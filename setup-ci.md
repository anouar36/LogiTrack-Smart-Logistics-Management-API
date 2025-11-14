# CI/CD Setup Guide

## 1. Start the services

```bash
# Stop existing containers if any
docker-compose down

# Start all services
docker-compose up -d

# Check if all services are running
docker-compose ps
```

## 2. Wait for services to be ready

- **SonarQube**: http://localhost:9000 (admin/admin)
- **Jenkins**: http://localhost:8080
- **Application**: http://localhost:8082
- **PgAdmin**: http://localhost:8081

## 3. Configure SonarQube

1. Go to http://localhost:9000
2. Login with admin/admin
3. Change the password when prompted
4. Go to Administration > Security > Users
5. Create a token or use existing token: `sqa_a4443853409628289a40ada2deac36b3a584d1d8`
6. Create a new project with key: `logitrack-api`

## 4. Configure Jenkins

1. Go to http://localhost:8080
2. Get the initial admin password:
   ```bash
   docker exec jenkins_ci cat /var/jenkins_home/secrets/initialAdminPassword
   ```
3. Install suggested plugins
4. Create admin user
5. Install additional plugins:
   - SonarQube Scanner
   - Pipeline
   - Git

6. Configure SonarQube integration:
   - Go to Manage Jenkins > Configure System
   - Add SonarQube server:
     - Name: SonarQube
     - Server URL: http://sonarqube_ci:9000
   - Go to Manage Jenkins > Credentials
   - Add Secret Text credential:
     - ID: `sonar-global-token`
     - Secret: `sqa_a4443853409628289a40ada2deac36b3a584d1d8`

7. Configure Maven:
   - Go to Manage Jenkins > Global Tool Configuration
   - Add Maven installation:
     - Name: M3
     - Install automatically: Yes

## 5. Create Jenkins Pipeline

1. Create New Item > Pipeline
2. Name: LogiTrack-Pipeline
3. Pipeline Definition: Pipeline script from SCM
4. SCM: Git
5. Repository URL: (your git repository URL)
6. Script Path: Jenkinsfile

## 6. Test the pipeline

Run the pipeline and check the logs. The SonarQube analysis should work now.

## Troubleshooting

If you still get connection errors:

1. Check if all containers are on the same network:
   ```bash
   docker network ls
   docker network inspect logitrack_logistics-net
   ```

2. Test connectivity from Jenkins to SonarQube:
   ```bash
   docker exec jenkins_ci curl -I http://sonarqube_ci:9000
   ```

3. Check SonarQube logs:
   ```bash
   docker logs sonarqube_ci
   ```

4. Check Jenkins logs:
   ```bash
   docker logs jenkins_ci
   ```
