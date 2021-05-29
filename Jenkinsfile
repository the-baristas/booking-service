pipeline {
    agent any
    environment {
        COMMIT_HASH = "${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
    }
    stages {
        stage('Clean and test target') {
            steps {
                sh 'mvn clean test'
            }
        }
        stage('Test and Package') {
            steps {
                sh 'mvn package'
            }
        }
        // stage('Code Analysis: SonarQube') {
        //     steps {
        //         withSonarQubeEnv('SonarQube') {
        //             sh 'mvn sonar:sonar'
        //         }
        //     }
        // }
        // stage('Quality gate') {
        //     steps {
        //         waitForQualityGate abortPipeline: true
        //     }
        // }
        stage('Docker Build') {
            steps {
                echo 'Deploying....'
                sh "aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 135316859264.dkr.ecr.us-east-2.amazonaws.com"
                sh "docker build -t booking-service:$COMMIT_HASH ."
                sh "docker tag booking-service:$COMMIT_HASH 135316859264.dkr.ecr.us-east-2.amazonaws.com/booking-service:$COMMIT_HASH"
                sh "docker push 135316859264.dkr.ecr.us-east-2.amazonaws.com/booking-service:$COMMIT_HASH"
            }
        }
        stage('CloudFormation Deploy') {
            steps {
                echo 'Fetching CloudFormation template..'
                sh "aws s3 cp s3://cloudformation-us-east-2-135316859264/booking-service/cloudformation.template ./"
                echo 'Deploying CloudFormation..'
                sh "aws cloudformation deploy --stack-name booking-service --template-file ./cloudformation.template --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM --region us-east-2"
            }
        }
    }
    post {
        always {
            sh 'mvn clean'
            sh 'docker system prune -af'
        }
    }
}