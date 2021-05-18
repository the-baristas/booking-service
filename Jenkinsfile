pipeline {
    agent any
    environment {
        COMMIT_HASH = "${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
    }
    stages {
        stage('Clean and Test target') {
            steps {
                sh 'mvn clean test'
            }
        }
        stage('Test and Package') {
            steps {
                sh 'mvn package'
            }
        }
        stage('Docker Build') {
            steps {
                echo 'Deploying....'
                // sh "aws ecr ........."
                sh "docker build --tag MicroServiceName:$COMMIT_HASH ."
                // sh "docker tag MicroServiceName:$COMMIT_HASH $AWS_ID/ECR Repo/MicroServiceName:$COMMIT_HASH"
                // sh "docker push $AWS_ID/ECR Repo/MicroServiceName:$COMMIT_HASH"
            }
        }
        stage('Code Analysis: Sonarqube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar -Dsonar.login=fe2fd4de999e222d92ab830601a6d0e663cc1cbe'
                }
            }
        }
        // stage('Await Quality Gateway') {
        //     steps {
        //         waitForQualityGate abortPipeline: true
        //     }
        // }
    }
    post {
        always {
            sh 'mvn clean'
            sh 'docker image prune'
        }
    }
}