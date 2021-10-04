pipeline {
    agent any
    environment {
        COMMIT_HASH = "${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
        ECR_REGISTRY_URI = "135316859264.dkr.ecr.us-east-2.amazonaws.com"
        S3_URI = "s3://cloudformation-us-east-2-135316859264/booking-service-stack/cloudformation.template"
        SERVICE_NAME = "booking-service"
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
                sh "aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin ${ECR_REGISTRY_URI}"
                sh "docker build -t ${SERVICE_NAME}:${COMMIT_HASH} ."
                sh "docker tag ${SERVICE_NAME}:${COMMIT_HASH} ${ECR_REGISTRY_URI}/${SERVICE_NAME}:${COMMIT_HASH}"
                sh "docker push ${ECR_REGISTRY_URI}/${SERVICE_NAME}:${COMMIT_HASH}"
            }
        }
        stage('CloudFormation Deploy') {
            steps {
                echo 'Fetching CloudFormation template..'
                sh "aws s3 cp ${S3_URI} ./"
                echo 'Deploying CloudFormation..'
                sh "aws cloudformation deploy --stack-name ${SERVICE_NAME}-stack --template-file ./cloudformation.template  --parameter-overrides EcrImageUri=${ECR_REGISTRY_URI}/${SERVICE_NAME}:${COMMIT_HASH} --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM --region us-east-2"

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