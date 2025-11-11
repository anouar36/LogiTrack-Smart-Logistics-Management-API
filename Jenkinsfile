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

        stage('3. Analyse SonarQube (تحليل الجودة)') {
            steps {
                // هادي هي لي غتصيفط التقارير لـ SonarQube
                // (السمية 'SonarQube_CI' هي لي درنا فـ 'Configure System')
                withSonarQubeEnv('SonarQube_CI') {
                    // خاصك تبدل 'logitrack-api' بالـ Project Key لي درتي فـ SonarQube
                    sh 'mvn sonar:sonar -Dsonar.projectKey=logitrack-api'
                }
            }
        }

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