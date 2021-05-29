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
        stage('Code Analysis: SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        // stage('Quality gate') {
        //     steps {
        //         waitForQualityGate abortPipeline: true
        //     }
        // }
        stage('Docker Build') {
            steps {
                echo 'Deploying....'
                aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 135316859264.dkr.ecr.us-east-2.amazonaws.com
                sh "docker build -t booking-service:$COMMIT_HASH ."
                // sh "docker tag booking-service:$COMMIT_HASH $AWS_ID/ECR Repo/booking-service:$COMMIT_HASH"
                // sh "docker push $AWS_ID/ECR Repo/booking-service:$COMMIT_HASH"
                sh "docker tag booking-service:$COMMIT_HASH $AWS_ID.dkr.ecr.us-east-2.amazonaws.com/booking-service:$COMMIT_HASH"
                sh "docker push 135316859264.dkr.ecr.us-east-2.amazonaws.com/booking-service:$COMMIT_HASH"
            }
        }
        // stage('CloudFormation Deploy') {
        //     steps {
        //         echo 'Fetching CloudFormation template..'
        //         sh "touch ECS.yml"
        //         sh "rm ECS.yml"
        //         sh "wget https://raw.githubusercontent.com/Java-Feb-CRAM/cloud-formation/main/ECS.yml"
        //         echo 'Deploying CloudFormation..'
        //         sh "aws cloudformation deploy --stack-name UtopiaFlightPlaneMS --template-file ./ECS.yml --parameter-overrides ApplicationName=FlightPlaneMS ECRepositoryUri=038778514259.dkr.ecr.us-east-1.amazonaws.com/utopia-flight-plane:$COMMIT_HASH ExecutionRoleArn=arn:aws:iam::038778514259:role/ecsTaskExecutionRole TargetGroupArn=arn:aws:elasticloadbalancing:us-east-1:038778514259:targetgroup/FlightPlaneTG/89c35ebca3b8e8fe --role-arn arn:aws:iam::038778514259:role/CloudFormationECS --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM --region us-east-1"
        //     }
        // }
    }
    post {
        always {
            sh 'mvn clean'
            sh 'docker system prune -af'
        }
    }
}