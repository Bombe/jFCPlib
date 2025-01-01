pipeline {
    agent any

    options {
        timestamps()
    }

    stages {
        stage('Build (Java 8)') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './mvnw clean compile test-compile'
            }
        }
        stage('Test (Java 8)') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './mvnw verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    recordCoverage(tools: [[parser: 'JACOCO', pattern: '**/jacoco.xml']])
                }
            }
        }
        stage('Binary (Java 8)') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './mvnw -DskipTests=true package'
                archiveArtifacts artifacts: 'target/jFCPlib-*.jar', fingerprint: true
            }
        }
        stage('Compatibility (Java 17)') {
            tools {
                jdk 'OpenJDK 17'
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh './mvnw clean test'
                }
            }
        }
        stage('Compatibility (Java 21)') {
            tools {
                jdk 'OpenJDK 21'
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh './mvnw clean test'
                }
            }
        }
    }
}

// vi: ts=4 sw=4 et si
