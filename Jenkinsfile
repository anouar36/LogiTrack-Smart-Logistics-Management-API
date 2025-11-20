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
        
        stage('3. SonarQube Analysis FAST') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sonar-global-token', variable: 'SONAR_LOGIN_TOKEN')]) {
                        sh """
                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                            -Dsonar.projectKey=logitrack-api \
                            -Dsonar.projectName="LogiTrack API" \
                            -Dsonar.token=\${SONAR_LOGIN_TOKEN} \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.scanner.skip=false
                        """
                    }
                }
                
                // Fast result without waiting for Quality Gate
                echo "✅ SonarQube analysis sent successfully!"
                echo "🔍 Check results at: http://localhost:9000/dashboard?id=logitrack-api"
            }
        }
    }

    post {
        always {
            // Archive JUnit reports (corrected)
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            
            // Archive JaCoCo reports
            jacoco(execPattern: 'target/jacoco.exec')
            
            echo "🚀 Pipeline completed! Check SonarQube dashboard for results."
        }
        
        success {
            echo "✅ Build successful! All tests passed."
        }
        
        failure {
            echo "❌ Build failed. Check the logs above."
        }
    }
}