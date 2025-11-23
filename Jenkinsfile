    pipeline {
    agent any // خدم فـ أي بلاصة

    tools {
        // استعمل Maven لي عرفناه فـ Jenkins
        maven 'M3'
    }

    stages {
        // المرحلة 1: جلب آخر نسخة من الكود
        stage('1. Checkout Code (جلب الكود)') {
            steps {
                checkout scm
            }
        }

        // المرحلة 2: البناء والاختبار (أهم مرحلة)
        stage('2. Build & Test (بناء واختبار)') {
            steps {
                // هادي كتشغل JUnit (مع H2) وكتولد تقرير JaCoCo
                sh 'mvn clean verify'
            }
        }        // المرحلة 3: إرسال التقرير لـ SonarQube
        stage('3. Analyse SonarQube (تحليل الجودة)') {
            steps {
                // كنجبدو الساروت (Token) لي سميناه 'sonar-global-token'
                withCredentials([string(credentialsId: 'sonar-global-token', variable: 'SONAR_LOGIN_TOKEN')]) {

                    // كنخدمو الكوماندا مع الساروت والعنوان الصحيح
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

        // المرحلة 4: فحص بوابة الجودة
        stage('4. Quality Gate Check (فحص البوابة)') {
            steps {
                // كنتسناو الجواب ديال SonarQube (ناجح أو فاشل)
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            // أرشفة تقارير JUnit (باش تبان فـ Jenkins)
            junit 'target/surefire-reports/*.xml'

            // أرشفة تقارير JaCoCo (باش يبان الكرافيك)
            jacoco(execPattern: 'target/jacoco.exec')
        }
    }
}