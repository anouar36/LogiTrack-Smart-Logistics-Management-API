pipeline {
    agent any

    tools {
        maven 'Maven_Auto'
    }

    stages {
        stage('1. Checkout Code (جلب الكود)') {
            steps {
                // هادي كيجيب الكود من Git لي عرفتيه الفوق
                checkout scm
            }
        }

        stage('2. Build & Test (بناء واختبار)') {
            steps {
                // هادي هي الكوماندا السحرية:
                // clean = نقي
                // verify = دوز التيستات (JUnit) وصاوب التقرير (JaCoCo)
                sh 'mvn clean verify'
            }
        }

        // ...
        stage('3. Analyse SonarQube (تحليل الجودة)') {
            steps {
                //   هنا فين تبدل: 'sonar-token'   <---   'sonar-key-jdid'
                withCredentials([string(credentialsId: 'sonar-key-jdid', variable: 'SONAR_LOGIN_TOKEN')]) {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=logitrack-api \
                        -Dsonar.host.url=http://sonarqube_ci:9000 \
                        -Dsonar.login=\${SONAR_LOGIN_TOKEN}
                    """
                }
            }
        }
        // ...

        stage('4. Quality Gate Check (فحص البوابة)') {
            steps {
                // كنتسناو SonarQube يجاوبنا واش الكود نقي ولا لا
                timeout(time: 1, unit: 'HOURS') {
                    // abortPipeline: true = حبس كلشي إيلا الكود ناقص
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        // هادشي كيدار فـ اللخر ديما (سوا نجح الـ build ولا فشل)
        always {
            // هادا من الـ "Livrables": أرشفة تقارير JUnit
            junit 'target/surefire-reports/*.xml'

            // هادا باش يبان داك الكرافيك ديال JaCoCo فـ Jenkins
            jacoco(execPattern: 'target/jacoco.exec')
        }
    }
}