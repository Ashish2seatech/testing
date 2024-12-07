pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'asisgolu95/demo-app'
        DOCKER_REGISTRY = 'https://hub.docker.com/'
        REGISTRY_CREDENTIAL = 'dockerhub_iD'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout([$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[url: 'https://github.com/Ashish2seatech/testing.git']]])
                }
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
                    docker.withRegistry('$DOCKER_REGISTRY', '$REGISTRY_CREDENTIAL') {
                        sh 'docker push $DOCKER_IMAGE'
                    }
                }
            }
        }
        stage('Deploy Docker Container') {
            steps {
                script {
                    // Stop and remove any existing container
                    sh '''
                    docker stop demo-app || true
                    docker rm demo-app || true
                    docker run -d --name demo-app -p 3000:3000 $DOCKER_IMAGE
                    '''
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
            mail to: 'asis.golu@gmail.com',
                 subject: "Jenkins Pipeline Failure",
                 body: "The Jenkins pipeline has failed. Please check the logs."
        }
    }
}
