pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'asisgolu95/demo-app'
        DOCKER_REGISTRY = 'https://hub.docker.com/r/asisgolu95/demo-app'
        DOCKER_IMAGE    = 'demo-app'
        
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    sh 'docker build -t $DOCKER_IMAGE .'
                }
            }
        }
        stage('Push to Docker Hub') {
            steps {
                script {
                    
                    sh 'docker push $DOCKER_IMAGE'
                }
            }
        }
        stage('Deploy Docker Container') {
            steps {
                script {
                    sh 'docker run -d --name demo-app -p 3000:3000 --replicas 2 $DOCKER_IMAGE'
                }
            }
        }
    }

    post {
        success {
            mail to: 'asis.golu@gmail.com',
                 subject: "Jenkins Pipeline Success",
                 body: "The Jenkins pipeline has completed successfully."
        }
        failure {
            mail to: 'asisgolu@gmail.com',
                 subject: "Jenkins Pipeline Failure",
                 body: "The Jenkins pipeline has failed. Please check the logs."
        }
    }
}

