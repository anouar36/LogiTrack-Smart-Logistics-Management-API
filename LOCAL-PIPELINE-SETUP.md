# 🚀 إعداد Jenkins Pipeline محلي (حل مشكلة GitHub)

## 🔧 المشكلة
- GitHub يرجع خطأ 500 (Internal Server Error)
- Jenkins لا يستطيع الوصول للريبو
- تحتاج pipeline يشتغل محلياً

## ✅ الحل: Pipeline محلي

### الخطوة 1: إنشاء Pipeline جديد في Jenkins

1. **افتح Jenkins**: http://localhost:8080
2. **اضغط "New Item"**
3. **اسم المشروع**: `LogiTrack-Local`
4. **اختر**: `Pipeline`
5. **اضغط OK**

### الخطوة 2: إعداد Pipeline Script

في صفحة Configuration:

1. **اذهب لـ Pipeline section**
2. **Definition**: اختر `Pipeline script` (مش Pipeline script from SCM)
3. **انسخ والصق الكود التالي**:

```groovy
pipeline {
    agent any
    
    tools {
        maven 'M3'
    }

    stages {
        stage('1. Setup Workspace') {
            steps {
                script {
                    echo "🚀 Starting LogiTrack CI/CD Pipeline..."
                    echo "📁 Working directory: C:/Users/Youcode/IdeaProjects/LogiTrack"
                }
            }
        }

        stage('2. Build & Test') {
            steps {
                script {
                    dir('C:/Users/Youcode/IdeaProjects/LogiTrack') {
                        bat 'mvn clean compile'
                        bat 'mvn test jacoco:report'
                        echo "✅ Build and tests completed!"
                    }
                }
            }
        }
        
        stage('3. SonarQube Analysis') {
            steps {
                script {
                    dir('C:/Users/Youcode/IdeaProjects/LogiTrack') {
                        withSonarQubeEnv('SonarQube') {
                            withCredentials([string(credentialsId: 'sonar-global-token', variable: 'SONAR_LOGIN_TOKEN')]) {
                                bat """
                                    mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar ^
                                    -Dsonar.projectKey=logitrack-api ^
                                    -Dsonar.projectName="LogiTrack API" ^
                                    -Dsonar.token=%SONAR_LOGIN_TOKEN% ^
                                    -Dsonar.java.coveragePlugin=jacoco ^
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml ^
                                    -Dsonar.host.url=http://sonarqube-ci:9000
                                """
                            }
                        }
                        echo "✅ SonarQube analysis completed!"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                dir('C:/Users/Youcode/IdeaProjects/LogiTrack') {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    publishCoverage adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')], 
                                  sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                    archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
                }
            }
        }
        success {
            echo "✅ Build successful!"
        }
        failure {
            echo "❌ Build failed."
        }
    }
}
```

4. **اضغط Save**

### الخطوة 3: تشغيل Pipeline

1. **اضغط "Build Now"**
2. **انتظر النتيجة** (2-3 دقائق)

## 🎯 المميزات

- ✅ **بدون GitHub**: يعمل مع ملفات محلية
- ✅ **Windows متوافق**: يستخدم `bat` بدلاً من `sh`
- ✅ **تقارير كاملة**: JUnit + JaCoCo + SonarQube
- ✅ **أرشفة**: JAR files محفوظة
- ✅ **سريع**: 2-3 دقائق فقط

## 🔍 عرض النتائج

بعد انتهاء البناء:

1. **Test Results**: في الصفحة الرئيسية للـ build
2. **Code Coverage**: في Build → Coverage Report  
3. **SonarQube**: http://localhost:9000/dashboard?id=logitrack-api
4. **Artifacts**: في Build → Artifacts

## ⚡ للتجربة السريعة

```powershell
# اختبار محلي سريع
cd "C:/Users/Youcode/IdeaProjects/LogiTrack"
mvn clean test
mvn jacoco:report
```

**🎉 الآن لديك CI/CD pipeline يعمل بدون مشاكل GitHub!**
