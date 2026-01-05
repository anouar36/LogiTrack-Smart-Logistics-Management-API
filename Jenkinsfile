pipeline {
    agent any

    tools {
        maven 'M3'
    }

    stages {
        stage('1. Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('2. Build & Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('3. SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-global-token', variable: 'SONAR_LOGIN_TOKEN')]) {
                    sh """
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                        -Dsonar.projectKey=logitrack-api \
                        -Dsonar.projectName="LogiTrack API" \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.token=\${SONAR_LOGIN_TOKEN} \
                        -Dsonar.java.coveragePlugin=jacoco \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }

        stage('4. Quality Gate Check') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            jacoco(execPattern: 'target/jacoco.exec')
        }
    }
}
