pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'asisgolu95/demo-app'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/master']],
                        userRemoteConfigs: [[url: 'https://github.com/Ashish2seatech/testing.git']]
                    ]
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh 'sudo docker build -t ${DOCKER_IMAGE} .'
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker_hub_new', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'sudo docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}'
                        sh 'sudo docker push ${DOCKER_IMAGE}'
                    }
                }
            }
        }

        stage('Deploy Docker Container') {
            steps {
                script {
                    sh '''
                        docker stop demo-app || true
                        docker rm demo-app || true
                        docker run -d --name demo-app -p 3000:3000 ${DOCKER_IMAGE}
                    '''
                }
            }
        }
    }

    post {
        always {
            // Clean workspace after pipeline execution
            cleanWs()
        }
    }
}
